package com.interactioncue;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuAction;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Interaction Cue",
	description = "Shows High Alchemy, Low Alchemy, alch, all selected spell, Use item, cursor indicator, label, and pending inventory action cues for your next click",
	tags = {"interaction cue", "interaction", "interactions", "cue", "action cue", "click cue", "click indicator", "pending action", "high alchemy", "low alchemy", "high alch", "low alch", "alch", "alchemy", "spell", "spellbook", "cast", "use item", "item use", "cursor", "next click", "inventory", "pending"}
)
public class InteractionCuePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InteractionCueCursorOverlay cursorOverlay;

	@Inject
	private InteractionCueItemOverlay itemOverlay;

	private final Map<Integer, BufferedImage> itemImages = new HashMap<>();
	private final Map<Integer, BufferedImage> spellImages = new HashMap<>();
	private int pendingSlot = -1;
	private int pendingAnimation;
	private int pendingGraphic;
	private Actor pendingInteracting;
	private int pendingObservedAnimation;
	private int pendingObservedGraphic;
	private Actor pendingObservedInteracting;
	private boolean pendingObservedAction;
	private InteractionCue pendingCue = InteractionCue.NONE;

	@Override
	protected void startUp()
	{
		overlayManager.add(cursorOverlay);
		overlayManager.add(itemOverlay);
		log.debug("Interaction Cue started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(cursorOverlay);
		overlayManager.remove(itemOverlay);
		clearPending();
		itemImages.clear();
		spellImages.clear();
		log.debug("Interaction Cue stopped");
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		InteractionCue cue = getCurrentCue();
		if (!cue.isActive() || !isInventoryPendingAction(event))
		{
			return;
		}

		pendingSlot = event.getParam0();
		pendingAnimation = getLocalAnimation();
		pendingGraphic = getLocalGraphic();
		pendingInteracting = getLocalInteracting();
		pendingObservedAnimation = -1;
		pendingObservedGraphic = -1;
		pendingObservedInteracting = null;
		pendingObservedAction = false;
		pendingCue = cue;
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() == client.getLocalPlayer())
		{
			observePendingAction();
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if (event.getActor() == client.getLocalPlayer())
		{
			observePendingAction();
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() == client.getLocalPlayer())
		{
			observePendingAction();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (pendingSlot < 0 || event.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		clearPending();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (pendingSlot < 0)
		{
			return;
		}
		if (!pendingObservedAction)
		{
			observePendingAction();
			if (!pendingObservedAction)
			{
				clearPending();
				return;
			}
		}

		if (!isObservedActionActive())
		{
			clearPending();
		}
	}

	InteractionCue getCurrentCue()
	{
		if (!client.isWidgetSelected())
		{
			return InteractionCue.NONE;
		}

		Widget widget = client.getSelectedWidget();
		if (widget == null)
		{
			return InteractionCue.NONE;
		}

		if (widget.getItemId() > 0)
		{
			if (!isSelectedInventoryItemValid(widget))
			{
				return InteractionCue.NONE;
			}

			String itemName = getItemName(widget.getItemId());
			if (itemName.isEmpty())
			{
				return InteractionCue.NONE;
			}

			return new InteractionCue(true, "Use", itemName, getMenuTarget(), getItemImage(widget.getItemId()), new Color(70, 145, 230));
		}

		if (WidgetUtil.componentToInterface(widget.getId()) != InterfaceID.MAGIC_SPELLBOOK)
		{
			return InteractionCue.NONE;
		}

		return new InteractionCue(true, getActionName(widget), getWidgetName(widget), getMenuTarget(), getSpellImage(widget), new Color(255, 144, 64));
	}

	InteractionCue getPendingCue()
	{
		return pendingCue;
	}

	boolean isPendingSlot(int slot)
	{
		return pendingSlot == slot;
	}

	private boolean isInventoryPendingAction(MenuOptionClicked event)
	{
		if (event.getWidgetId() != InterfaceID.Inventory.ITEMS)
		{
			return false;
		}

		MenuAction action = event.getMenuAction();
		return action == MenuAction.WIDGET_TARGET_ON_WIDGET || action == MenuAction.WIDGET_USE_ON_ITEM || action == MenuAction.ITEM_USE_ON_ITEM;
	}

	private boolean isSelectedInventoryItemValid(Widget widget)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return false;
		}

		Item[] items = inventory.getItems();
		int index = widget.getIndex();
		if (index >= 0 && index < items.length)
		{
			Item item = items[index];
			return item != null && item.getId() == widget.getItemId() && item.getQuantity() > 0;
		}

		for (Item item : items)
		{
			if (item != null && item.getId() == widget.getItemId() && item.getQuantity() > 0)
			{
				return true;
			}
		}

		return false;
	}

	BufferedImage getItemImage(int itemId)
	{
		return itemImages.computeIfAbsent(itemId, itemManager::getImage);
	}

	private BufferedImage getSpellImage(Widget widget)
	{
		int spriteId = getSpriteId(widget);
		if (spriteId <= 0)
		{
			return null;
		}

		return spellImages.computeIfAbsent(spriteId, id -> spriteManager.getSprite(id, 0));
	}

	private int getSpriteId(Widget widget)
	{
		if (widget.getSpriteId() > 0)
		{
			return widget.getSpriteId();
		}

		Widget[] children = widget.getChildren();
		if (children == null)
		{
			return -1;
		}

		for (Widget child : children)
		{
			if (child == null)
			{
				continue;
			}

			int spriteId = getSpriteId(child);
			if (spriteId > 0)
			{
				return spriteId;
			}
		}

		return -1;
	}

	private String getItemName(int itemId)
	{
		return clean(client.getItemDefinition(itemId).getName(), "");
	}

	private String getActionName(Widget widget)
	{
		return clean(widget.getTargetVerb(), "Cast");
	}

	private String getWidgetName(Widget widget)
	{
		String name = clean(widget.getName(), "");
		if (!name.isEmpty())
		{
			return name;
		}

		return clean(widget.getText(), "Spell");
	}

	private String getMenuTarget()
	{
		MenuEntry[] entries = client.getMenuEntries();
		if (entries.length == 0)
		{
			return "";
		}

		String target = clean(entries[entries.length - 1].getTarget(), "");
		int arrow = target.lastIndexOf("->");
		return arrow >= 0 ? target.substring(arrow + 2).trim() : target;
	}

	private String clean(String value, String fallback)
	{
		if (value == null)
		{
			return fallback;
		}

		String cleaned = Text.removeTags(value).trim();
		return cleaned.isEmpty() || "null".equalsIgnoreCase(cleaned) ? fallback : cleaned;
	}

	private void clearPending()
	{
		pendingSlot = -1;
		pendingAnimation = -1;
		pendingGraphic = -1;
		pendingInteracting = null;
		pendingObservedAnimation = -1;
		pendingObservedGraphic = -1;
		pendingObservedInteracting = null;
		pendingObservedAction = false;
		pendingCue = InteractionCue.NONE;
	}

	private void observePendingAction()
	{
		if (pendingSlot < 0 || pendingObservedAction || !hasNewLocalActionSignal())
		{
			return;
		}

		int animation = getLocalAnimation();
		int graphic = getLocalGraphic();
		Actor interacting = getLocalInteracting();
		pendingObservedAnimation = animation != -1 && animation != pendingAnimation ? animation : -1;
		pendingObservedGraphic = graphic != -1 && graphic != pendingGraphic ? graphic : -1;
		pendingObservedInteracting = pendingObservedAnimation == -1 && pendingObservedGraphic == -1 && interacting != null && interacting != pendingInteracting ? interacting : null;
		pendingObservedAction = pendingObservedAnimation != -1 || pendingObservedGraphic != -1 || pendingObservedInteracting != null;
	}

	private boolean hasNewLocalActionSignal()
	{
		int animation = getLocalAnimation();
		int graphic = getLocalGraphic();
		Actor interacting = getLocalInteracting();
		return animation != -1 && animation != pendingAnimation || graphic != -1 && graphic != pendingGraphic || interacting != null && interacting != pendingInteracting;
	}

	private boolean isObservedActionActive()
	{
		return pendingObservedAnimation != -1 && getLocalAnimation() == pendingObservedAnimation
			|| pendingObservedGraphic != -1 && getLocalGraphic() == pendingObservedGraphic
			|| pendingObservedInteracting != null && getLocalInteracting() == pendingObservedInteracting;
	}

	private int getLocalAnimation()
	{
		return client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getAnimation();
	}

	private int getLocalGraphic()
	{
		return client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getGraphic();
	}

	private Actor getLocalInteracting()
	{
		return client.getLocalPlayer() == null ? null : client.getLocalPlayer().getInteracting();
	}

	@Provides
	InteractionCueConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InteractionCueConfig.class);
	}
}
