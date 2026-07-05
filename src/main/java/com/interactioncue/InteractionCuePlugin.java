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
import net.runelite.api.events.PostClientTick;
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
	description = "Marks inventory slots clicked with selected spell or item-use actions, and shows selected spell and Use item cues near the cursor",
	tags = {"interaction cue", "interaction", "interactions", "cue", "action cue", "click cue", "click indicator", "pending action", "high alchemy", "low alchemy", "high alch", "low alch", "alch", "alchemy", "spell", "spellbook", "cast", "use item", "item use", "cursor", "next click", "inventory", "pending"}
)
public class InteractionCuePlugin extends Plugin
{
	private static final int SIGNAL_WAIT_TICKS = 2;

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

	private final Map<Long, BufferedImage> itemImages = new HashMap<>();
	private final Map<Integer, BufferedImage> spellImages = new HashMap<>();
	private int pendingSlot = -1;
	private int pendingItemId = -1;
	private int pendingQuantity;
	private int pendingSourceWidgetId;
	private int pendingSourceIndex;
	private int pendingSourceItemId;
	private int pendingAnimation;
	private int pendingAnimationFrame;
	private boolean pendingAnimationCleared;
	private int pendingGraphic;
	private int pendingGraphicFrame;
	private boolean pendingGraphicCleared;
	private Actor pendingInteracting;
	private int pendingObservedAnimation;
	private int pendingObservedGraphic;
	private Actor pendingObservedInteracting;
	private int pendingAwaitingActionTicks;
	private int pendingInactiveTicks;
	private boolean pendingObservedAction;
	private boolean pendingMarkerVisible;
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

		if (isSelectedInventorySlot(event.getParam0()))
		{
			clearPending();
			return;
		}

		pendingSlot = event.getParam0();
		Widget selected = client.getSelectedWidget();
		Item item = getInventoryItem(pendingSlot);
		pendingItemId = item == null ? -1 : item.getId();
		pendingQuantity = item == null ? 0 : item.getQuantity();
		pendingSourceWidgetId = selected == null ? -1 : selected.getId();
		pendingSourceIndex = selected == null ? -1 : selected.getIndex();
		pendingSourceItemId = selected == null ? -1 : selected.getItemId();
		pendingAnimation = getLocalAnimation();
		pendingAnimationFrame = getLocalAnimationFrame();
		pendingAnimationCleared = pendingAnimation == -1;
		pendingGraphic = getLocalGraphic();
		pendingGraphicFrame = getLocalGraphicFrame();
		pendingGraphicCleared = pendingGraphic == -1;
		pendingInteracting = getLocalInteracting();
		pendingObservedAnimation = -1;
		pendingObservedGraphic = -1;
		pendingObservedInteracting = null;
		pendingAwaitingActionTicks = 0;
		pendingInactiveTicks = 0;
		pendingObservedAction = false;
		pendingMarkerVisible = isInitialActionStillActive();
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
	public void onPostClientTick(PostClientTick event)
	{
		observePendingAction();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (pendingSlot < 0 || event.getContainerId() != InventoryID.INVENTORY.getId())
		{
			return;
		}

		if (!isPendingItemStillInSlot(event.getItemContainer()))
		{
			clearPending();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (pendingSlot < 0)
		{
			return;
		}

		observePendingAction();
		if (!pendingObservedAction)
		{
			if (isPendingSourceSelected() || isInitialActionStillActive())
			{
				pendingAwaitingActionTicks = 0;
				return;
			}

			pendingAwaitingActionTicks++;
			if (pendingAwaitingActionTicks > SIGNAL_WAIT_TICKS)
			{
				clearPending();
			}
			return;
		}

		if (isObservedActionActive())
		{
			pendingInactiveTicks = 0;
			return;
		}

		pendingInactiveTicks++;
		if (pendingInactiveTicks > 1)
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

			int quantity = Math.max(1, widget.getItemQuantity());
			return new InteractionCue(true, "Use", itemName, getMenuTarget(), getItemImage(widget.getItemId(), quantity), new Color(70, 145, 230));
		}

		if (WidgetUtil.componentToInterface(widget.getId()) != InterfaceID.MAGIC_SPELLBOOK)
		{
			return InteractionCue.NONE;
		}

		return new InteractionCue(true, getActionName(widget), getWidgetName(widget), getMenuTarget(), getSpellImage(widget), new Color(255, 144, 64));
	}

	InteractionCue getPendingCue()
	{
		return pendingMarkerVisible ? pendingCue : InteractionCue.NONE;
	}

	boolean isPendingSlot(int slot)
	{
		return pendingMarkerVisible && pendingSlot == slot;
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

	private boolean isSelectedInventorySlot(int slot)
	{
		Widget widget = client.getSelectedWidget();
		return widget != null && widget.getId() == InterfaceID.Inventory.ITEMS && widget.getIndex() == slot;
	}

	private Item getInventoryItem(int slot)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory == null)
		{
			return null;
		}

		Item[] items = inventory.getItems();
		return slot >= 0 && slot < items.length ? items[slot] : null;
	}

	private boolean isPendingItemStillInSlot(ItemContainer inventory)
	{
		if (inventory == null)
		{
			return false;
		}

		Item[] items = inventory.getItems();
		if (pendingSlot < 0 || pendingSlot >= items.length)
		{
			return false;
		}

		Item item = items[pendingSlot];
		if (pendingItemId <= 0)
		{
			return item == null || item.getQuantity() <= 0;
		}

		return item != null && item.getId() == pendingItemId && item.getQuantity() == pendingQuantity;
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

	BufferedImage getItemImage(int itemId, int quantity)
	{
		long key = ((long) itemId << 32) | (quantity & 0xFFFFFFFFL);
		return itemImages.computeIfAbsent(key, ignored -> itemManager.getImage(itemId, quantity, false));
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
		pendingItemId = -1;
		pendingQuantity = 0;
		pendingSourceWidgetId = -1;
		pendingSourceIndex = -1;
		pendingSourceItemId = -1;
		pendingAnimation = -1;
		pendingAnimationFrame = -1;
		pendingAnimationCleared = true;
		pendingGraphic = -1;
		pendingGraphicFrame = -1;
		pendingGraphicCleared = true;
		pendingInteracting = null;
		pendingObservedAnimation = -1;
		pendingObservedGraphic = -1;
		pendingObservedInteracting = null;
		pendingAwaitingActionTicks = 0;
		pendingInactiveTicks = 0;
		pendingObservedAction = false;
		pendingMarkerVisible = false;
		pendingCue = InteractionCue.NONE;
	}

	private boolean isPendingSourceSelected()
	{
		if (!client.isWidgetSelected())
		{
			return false;
		}

		Widget selected = client.getSelectedWidget();
		return selected != null
			&& selected.getId() == pendingSourceWidgetId
			&& selected.getIndex() == pendingSourceIndex
			&& selected.getItemId() == pendingSourceItemId;
	}

	private boolean isInitialActionStillActive()
	{
		return pendingAnimation != -1 && !pendingAnimationCleared && getLocalAnimation() == pendingAnimation
			|| pendingGraphic != -1 && !pendingGraphicCleared && getLocalGraphic() == pendingGraphic
			|| pendingInteracting != null && getLocalInteracting() == pendingInteracting;
	}

	private void observePendingAction()
	{
		if (pendingSlot < 0 || pendingObservedAction)
		{
			return;
		}

		int animation = getLocalAnimation();
		int animationFrame = getLocalAnimationFrame();
		int graphic = getLocalGraphic();
		int graphicFrame = getLocalGraphicFrame();
		Actor interacting = getLocalInteracting();
		if (animation == -1 && pendingAnimation != -1)
		{
			pendingAnimationCleared = true;
		}
		if (graphic == -1 && pendingGraphic != -1)
		{
			pendingGraphicCleared = true;
		}

		pendingObservedAnimation = hasNewAnimationSignal(animation, animationFrame) ? animation : -1;
		pendingObservedGraphic = hasNewGraphicSignal(graphic, graphicFrame) ? graphic : -1;
		pendingObservedInteracting = pendingObservedAnimation == -1 && pendingObservedGraphic == -1 && interacting != null && interacting != pendingInteracting ? interacting : null;
		pendingObservedAction = pendingObservedAnimation != -1 || pendingObservedGraphic != -1 || pendingObservedInteracting != null;
		pendingMarkerVisible = pendingMarkerVisible || pendingObservedAction;
	}

	private boolean hasNewAnimationSignal(int animation, int frame)
	{
		return animation != -1 && (animation != pendingAnimation || pendingAnimationCleared || frame >= 0 && frame < pendingAnimationFrame);
	}

	private boolean hasNewGraphicSignal(int graphic, int frame)
	{
		return graphic != -1 && (graphic != pendingGraphic || pendingGraphicCleared || frame >= 0 && frame < pendingGraphicFrame);
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

	private int getLocalAnimationFrame()
	{
		return client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getAnimationFrame();
	}

	private int getLocalGraphic()
	{
		return client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getGraphic();
	}

	private int getLocalGraphicFrame()
	{
		return client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getSpotAnimFrame();
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
