package com.interactioncue;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
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
	private static final Pattern PRODUCTION_QUANTITY_PATTERN =
		Pattern.compile(
			"^make(?:\\s+|-)(\\d+|x|all)$",
			Pattern.CASE_INSENSITIVE
		);

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

	@Inject
	private InteractionCueConfig config;

	@Inject
	private ConfigManager configManager;

	private final Map<Integer, BufferedImage> spellImages = new HashMap<>();
	private final AtomicLong tooltipCustomSelectionGeneration = new AtomicLong();
	private final AtomicLong markerCustomSelectionGeneration = new AtomicLong();
	private final InteractionCueConfigProfileTracker configProfileTracker =
		new InteractionCueConfigProfileTracker();
	private InteractionCuePendingInteraction pendingInteraction;
	private volatile boolean applyingPreset;

	@Override
	protected void startUp()
	{
		InteractionCueConfigMigration.migrate(configManager);
		configProfileTracker.initialize(currentConfigProfileId());
		reconcilePresetSelections();
		overlayManager.add(cursorOverlay);
		overlayManager.add(itemOverlay);
		log.debug("Interaction Cue started");
	}

	@Override
	protected void shutDown()
	{
		cancelPendingCustomSelection(tooltipCustomSelectionGeneration);
		cancelPendingCustomSelection(markerCustomSelectionGeneration);
		overlayManager.remove(cursorOverlay);
		overlayManager.remove(itemOverlay);
		clearPending();
		spellImages.clear();
		log.debug("Interaction Cue stopped");
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.isConsumed())
		{
			return;
		}

		MenuEntry entry = event.getMenuEntry();
		if (pendingInteraction != null)
		{
			if (isCancellationMenuAction(entry))
			{
				clearPending();
				return;
			}
			if (isProductionMenuAction(entry))
			{
				Widget productionWidget = entry.getWidget();
				if (isSkillmultiQuantityControl(productionWidget))
				{
					return;
				}

				pendingInteraction.markProductionControl(
					productionQuantity(entry)
				);
				return;
			}
		}

		InteractionCue cue = getCurrentCue();
		if (!cue.isActive())
		{
			clearPending();
			return;
		}

		clearPending();
		if (!isInventoryPendingAction(entry))
		{
			return;
		}

		Widget target = entry.getWidget();
		int targetSlot = entry.getParam0();
		if (isSelectedInventorySlot(targetSlot))
		{
			return;
		}

		ItemContainer inventory = getInventory();
		Item targetItem = getInventoryItem(inventory, targetSlot);
		if (!InteractionCueClickTarget.matches(
			targetSlot,
			target.getIndex(),
			target.getItemId(),
			targetItem))
		{
			return;
		}

		Widget selected = client.getSelectedWidget();
		int sourceItemId = selected == null ? -1 : selected.getItemId();
		int sourceSlot = isSelectedInventoryWidget(selected)
			? selected.getIndex()
			: -1;
		pendingInteraction = new InteractionCuePendingInteraction(
			cue.withTarget(getMenuTarget(entry.getTarget())),
			target.getParentId(),
			targetSlot,
			targetItem.getId(),
			sourceItemId,
			sourceSlot,
			InteractionCueInventorySnapshot.capture(inventory),
			getLocalAnimation(),
			getLocalAnimationFrame(),
			InteractionCueSpotAnimationSnapshot.capture(client.getLocalPlayer())
		);
		if (!pendingInteraction.isValid())
		{
			clearPending();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (pendingInteraction != null
			&& event.getActor() != null
			&& event.getActor() == client.getLocalPlayer())
		{
			pendingInteraction.observeAnimation(
				event.getActor().getAnimation(),
				event.getActor().getAnimationFrame()
			);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (pendingInteraction != null && isActionFailureMessage(event))
		{
			clearPending();
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		Actor actor = event.getActor();
		if (pendingInteraction != null
			&& actor != null
			&& actor == client.getLocalPlayer())
		{
			pendingInteraction.observeSpotAnimations(actor);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			clearPending();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (pendingInteraction == null || event.getContainerId() != InventoryID.INV)
		{
			return;
		}

		if (pendingInteraction.observeInventory(event.getItemContainer()))
		{
			clearPending();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (pendingInteraction == null)
		{
			return;
		}

		if (pendingInteraction.observeInventory(getInventory()))
		{
			clearPending();
			return;
		}

		pendingInteraction.observeAnimation(
			getLocalAnimation(),
			getLocalAnimationFrame()
		);
		pendingInteraction.observeSpotAnimations(client.getLocalPlayer());
		if (pendingInteraction.advanceTick())
		{
			clearPending();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (pendingInteraction != null
			&& isProductionDialog(event.getGroupId()))
		{
			pendingInteraction.openProductionDialog();
		}
	}

	@Subscribe
	public void onVarClientIntChanged(VarClientIntChanged event)
	{
		if (pendingInteraction != null
			&& event.getIndex() == VarClientID.SKILLMULTI_QUANTITY
			&& skillmultiQuantitiesVisible())
		{
			pendingInteraction.updateProductionQuantity(
				client.getVarcIntValue(VarClientID.SKILLMULTI_QUANTITY)
			);
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (pendingInteraction != null
			&& isProductionDialog(event.getGroupId()))
		{
			if (event.getGroupId() == InterfaceID.SKILLMULTI
				&& wasSkillmultiProductSubmitted())
			{
				pendingInteraction.markProductionControl(
					skillmultiQuantity()
				);
			}
			if (isEscapeCancellation(
				event.isUnload(),
				client.isKeyPressed(KeyCode.KC_ESCAPE)
			) || pendingInteraction.closeProductionDialog(event.isUnload()))
			{
				clearPending();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		boolean profileSwitchInProgress =
			configProfileTracker.onConfigChanged(currentConfigProfileId());

		if (!InteractionCueConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		if (profileSwitchInProgress)
		{
			return;
		}

		if (InteractionCueConfig.TOOLTIP_PRESET_KEY.equals(event.getKey()))
		{
			cancelPendingCustomSelection(tooltipCustomSelectionGeneration);
			InteractionCueTooltipPreset preset =
				tooltipPresetFromValue(event.getNewValue());
			if (!applyingPreset && preset != null)
			{
				if (preset != InteractionCueTooltipPreset.CUSTOM)
				{
					applySectionPreset(preset.getSettings());
				}
				recordTooltipPreset(preset);
			}
			return;
		}

		if (InteractionCueConfig.MARKER_PRESET_KEY.equals(event.getKey()))
		{
			cancelPendingCustomSelection(markerCustomSelectionGeneration);
			InteractionCueMarkerPreset preset =
				markerPresetFromValue(event.getNewValue());
			if (!applyingPreset && preset != null)
			{
				if (preset != InteractionCueMarkerPreset.CUSTOM)
				{
					applySectionPreset(preset.getSettings());
				}
				recordMarkerPreset(preset);
			}
			return;
		}

		InteractionCueTooltipPreset tooltipPreset = config.tooltipPreset();
		if (shouldSwitchTooltipToCustom(
			applyingPreset,
			event.getKey(),
			tooltipPreset))
		{
			queueTooltipCustomSelection();
		}
		else if (shouldRecordTooltipCustomSettings(
			applyingPreset,
			event.getKey(),
			tooltipPreset))
		{
			recordTooltipPreset(tooltipPreset);
		}

		InteractionCueMarkerPreset markerPreset = config.markerPreset();
		if (shouldSwitchMarkerToCustom(
			applyingPreset,
			event.getKey(),
			markerPreset))
		{
			queueMarkerCustomSelection();
		}
		else if (shouldRecordMarkerCustomSettings(
			applyingPreset,
			event.getKey(),
			markerPreset))
		{
			recordMarkerPreset(markerPreset);
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		cancelPendingCustomSelection(tooltipCustomSelectionGeneration);
		cancelPendingCustomSelection(markerCustomSelectionGeneration);
		long profileId = currentConfigProfileId();
		configProfileTracker.beginProfileChanged(profileId);
		boolean previouslyApplyingPreset = applyingPreset;
		applyingPreset = true;
		try
		{
			InteractionCueConfigMigration.migrate(configManager);
		}
		finally
		{
			applyingPreset = previouslyApplyingPreset;
			configProfileTracker.finishProfileChanged(profileId);
		}
		SwingUtilities.invokeLater(() ->
		{
			if (profileId == currentConfigProfileId())
			{
				reconcilePresetSelections();
			}
		});
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
			Item selectedItem = getSelectedInventoryItem(widget);
			if (selectedItem == null)
			{
				return InteractionCue.NONE;
			}

			String itemName = getItemName(widget.getItemId());
			if (itemName.isEmpty())
			{
				return InteractionCue.NONE;
			}

			return new InteractionCue(
				true,
				"Use",
				itemName,
				getMenuTarget(),
				getItemImage(selectedItem.getId(), selectedItem.getQuantity()),
				new Color(70, 145, 230)
			);
		}

		if (!isSpellSelection(widget.getId()))
		{
			return InteractionCue.NONE;
		}

		return new InteractionCue(true, getActionName(widget), getWidgetName(widget), getMenuTarget(), getSpellImage(widget), new Color(255, 144, 64));
	}

	InteractionCue getPendingCue()
	{
		return pendingInteraction == null ? InteractionCue.NONE : pendingInteraction.getCue();
	}

	boolean isPendingSlot(Widget widget, int itemId, int quantity)
	{
		return pendingInteraction != null
			&& widget != null
			&& pendingInteraction.isTarget(
				widget.getParentId(),
				widget.getIndex(),
				itemId,
				quantity
			);
	}

	private boolean isInventoryPendingAction(MenuEntry entry)
	{
		if (entry == null)
		{
			return false;
		}

		Widget target = entry.getWidget();
		return target != null
			&& InteractionCueClickTarget.isInventoryAction(
				entry.getType(),
				target.getParentId()
			);
	}

	private boolean isProductionMenuAction(MenuEntry entry)
	{
		if (entry == null || !isProductionActionType(entry.getType()))
		{
			return false;
		}

		Widget widget = entry.getWidget();
		if (widget == null)
		{
			return false;
		}

		return isProductionDialog(WidgetUtil.componentToInterface(widget.getId()))
			|| isProductionDialog(
				WidgetUtil.componentToInterface(widget.getParentId())
			);
	}

	static boolean isProductionActionType(MenuAction action)
	{
		return action == MenuAction.CC_OP
			|| action == MenuAction.WIDGET_CONTINUE;
	}

	static boolean isProductionDialog(int interfaceId)
	{
		return interfaceId == InterfaceID.SKILLMULTI
			|| interfaceId == InterfaceID.GRAPHICAL_MULTI
			|| interfaceId == InterfaceID.CHATMENU;
	}

	private int productionQuantity(MenuEntry entry)
	{
		Widget widget = entry == null ? null : entry.getWidget();
		if (!isSkillmultiProduct(widget))
		{
			return productionQuantityFromText(
				widget == null ? null : widget.getText(),
				entry == null ? null : entry.getOption(),
				entry == null ? null : entry.getTarget()
			);
		}

		if (!skillmultiQuantitiesVisible())
		{
			return 1;
		}

		return skillmultiQuantity();
	}

	private int skillmultiQuantity()
	{
		int quantity = client.getVarcIntValue(
			VarClientID.SKILLMULTI_QUANTITY
		);
		return quantity > 0
			? quantity
			: InteractionCuePendingInteraction.UNKNOWN_ACTIONS;
	}

	private boolean wasSkillmultiProductSubmitted()
	{
		for (int widgetId = InterfaceID.Skillmulti.A;
			widgetId <= InterfaceID.Skillmulti.R;
			widgetId++)
		{
			Widget product = client.getWidget(widgetId);
			if (product == null)
			{
				continue;
			}

			Widget[] dynamicChildren = product.getDynamicChildren();
			if (isSubmittedSkillmultiProduct(
				dynamicChildren == null ? 0 : dynamicChildren.length,
				product.getOnOpListener(),
				product.getOnKeyListener()
			))
			{
				return true;
			}
		}
		return false;
	}

	static boolean isSubmittedSkillmultiProduct(
		int dynamicChildCount,
		Object[] onOpListener,
		Object[] onKeyListener)
	{
		return dynamicChildCount > 0
			&& onOpListener == null
			&& onKeyListener == null;
	}

	private boolean skillmultiQuantitiesVisible()
	{
		Widget quantityControls = client.getWidget(
			InterfaceID.Skillmulti.QUANTITIES
		);
		return quantityControls != null && !quantityControls.isHidden();
	}

	static int productionQuantityFromText(String... values)
	{
		if (values == null)
		{
			return InteractionCuePendingInteraction.UNKNOWN_ACTIONS;
		}

		for (String value : values)
		{
			String cleaned = clean(value, "")
				.replace('\u00A0', ' ')
				.replaceAll("\\s+", " ")
				.trim();
			Matcher matcher = PRODUCTION_QUANTITY_PATTERN.matcher(cleaned);
			if (!matcher.matches())
			{
				continue;
			}

			String quantity = matcher.group(1);
			if ("all".equalsIgnoreCase(quantity))
			{
				return InteractionCuePendingInteraction.UNBOUNDED_ACTIONS;
			}
			if ("x".equalsIgnoreCase(quantity))
			{
				return InteractionCuePendingInteraction.UNKNOWN_ACTIONS;
			}
			try
			{
				int parsed = Integer.parseInt(quantity);
				return parsed > 0
					? parsed
					: InteractionCuePendingInteraction.UNKNOWN_ACTIONS;
			}
			catch (NumberFormatException ignored)
			{
				return InteractionCuePendingInteraction.UNKNOWN_ACTIONS;
			}
		}
		return InteractionCuePendingInteraction.UNKNOWN_ACTIONS;
	}

	static boolean isSkillmultiQuantityControl(Widget widget)
	{
		return widget != null
			&& (isSkillmultiQuantityControl(widget.getId())
			|| isSkillmultiQuantityControl(widget.getParentId()));
	}

	static boolean isSkillmultiQuantityControl(int widgetId)
	{
		return widgetId == InterfaceID.Skillmulti._1
			|| widgetId == InterfaceID.Skillmulti._5
			|| widgetId == InterfaceID.Skillmulti._10
			|| widgetId == InterfaceID.Skillmulti.OTHER
			|| widgetId == InterfaceID.Skillmulti.X
			|| widgetId == InterfaceID.Skillmulti.ALL;
	}

	static boolean isSkillmultiProduct(Widget widget)
	{
		return widget != null
			&& (isSkillmultiProduct(widget.getId())
			|| isSkillmultiProduct(widget.getParentId()));
	}

	static boolean isSkillmultiProduct(int widgetId)
	{
		return widgetId >= InterfaceID.Skillmulti.A
			&& widgetId <= InterfaceID.Skillmulti.R;
	}

	private boolean isCancellationMenuAction(MenuEntry entry)
	{
		return entry != null
			&& isCancellationMenuAction(entry.getType(), entry.getOption());
	}

	static boolean isCancellationMenuAction(MenuAction action, String option)
	{
		if (action == MenuAction.CANCEL || action == MenuAction.WIDGET_CLOSE)
		{
			return true;
		}

		String cleaned = clean(option, "").toLowerCase(Locale.ROOT);
		return cleaned.equals("cancel")
			|| cleaned.equals("close")
			|| cleaned.equals("never mind")
			|| cleaned.equals("nevermind");
	}

	static boolean isEscapeCancellation(boolean unload, boolean escapePressed)
	{
		return unload && escapePressed;
	}

	private boolean isSelectedInventorySlot(int slot)
	{
		Widget widget = client.getSelectedWidget();
		return isSelectedInventoryWidget(widget) && widget.getIndex() == slot;
	}

	private boolean isSelectedInventoryWidget(Widget widget)
	{
		return widget != null && widget.getId() == InterfaceID.Inventory.ITEMS;
	}

	private ItemContainer getInventory()
	{
		return client.getItemContainer(InventoryID.INV);
	}

	private Item getInventoryItem(ItemContainer inventory, int slot)
	{
		if (inventory == null)
		{
			return null;
		}
		Item[] items = inventory.getItems();
		return items != null && slot >= 0 && slot < items.length ? items[slot] : null;
	}

	private Item getSelectedInventoryItem(Widget widget)
	{
		ItemContainer inventory = getInventory();
		if (inventory == null)
		{
			return null;
		}

		Item[] items = inventory.getItems();
		return InteractionCueInventorySelection.find(
			items,
			widget.getIndex(),
			widget.getItemId()
		);
	}

	BufferedImage getItemImage(int itemId, int quantity)
	{
		return itemManager.getImage(itemId, quantity, false);
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
		MenuEntry[] entries = client.getMenu().getMenuEntries();
		if (entries.length == 0)
		{
			return "";
		}

		return getMenuTarget(entries[entries.length - 1].getTarget());
	}

	static String getMenuTarget(String value)
	{
		String target = clean(value, "");
		int arrow = target.lastIndexOf("->");
		return arrow >= 0 ? target.substring(arrow + 2).trim() : target;
	}

	static boolean isSpellSelection(int widgetId)
	{
		return WidgetUtil.componentToInterface(widgetId) == InterfaceID.MAGIC_SPELLBOOK;
	}

	private boolean isActionFailureMessage(ChatMessage event)
	{
		return (event.getType() == ChatMessageType.GAMEMESSAGE || event.getType() == ChatMessageType.SPAM)
			&& isActionFailureMessage(event.getMessage());
	}

	static boolean isActionFailureMessage(String message)
	{
		String cleaned = clean(message, "").toLowerCase(Locale.ROOT);
		if (cleaned.endsWith("."))
		{
			cleaned = cleaned.substring(0, cleaned.length() - 1);
		}

		return cleaned.equals("nothing interesting happens")
			|| cleaned.startsWith("you can't use ")
			|| cleaned.startsWith("you cannot use ")
			|| cleaned.startsWith("you can't cast ")
			|| cleaned.startsWith("you cannot cast ")
			|| cleaned.startsWith("you don't have ")
			|| cleaned.startsWith("you do not have ")
			|| cleaned.startsWith("you haven't got ")
			|| cleaned.startsWith("you need ");
	}

	private void reconcilePresetSelections()
	{
		reconcileTooltipPreset();
		reconcileMarkerPreset();
	}

	private void reconcileTooltipPreset()
	{
		InteractionCueTooltipPreset selected = config.tooltipPreset();
		InteractionCueTooltipPreset applied = tooltipPresetFromValue(
			configManager.getConfiguration(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.TOOLTIP_APPLIED_PRESET_KEY
			)
		);
		Map<String, Object> tooltipSettings = currentTooltipSettings();
		InteractionCuePresetReconciliation.Action action =
			InteractionCuePresetReconciliation.tooltip(
				selected,
				applied,
				tooltipSettings,
				configManager.getConfiguration(
					InteractionCueConfig.GROUP,
					InteractionCueConfig.TOOLTIP_APPLIED_SETTINGS_KEY
				)
			);
		switch (action)
		{
			case APPLY_SELECTED:
				applySectionPreset(selected.getSettings());
				break;
			case SELECT_CUSTOM:
				selected = InteractionCueTooltipPreset.CUSTOM;
				setPresetSelection(
					InteractionCueConfig.TOOLTIP_PRESET_KEY,
					selected
				);
				break;
			default:
				break;
		}
		recordTooltipPreset(selected);
	}

	private void reconcileMarkerPreset()
	{
		InteractionCueMarkerPreset selected = config.markerPreset();
		InteractionCueMarkerPreset applied = markerPresetFromValue(
			configManager.getConfiguration(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_APPLIED_PRESET_KEY
			)
		);
		Map<String, Object> markerSettings = currentMarkerSettings();
		InteractionCuePresetReconciliation.Action action =
			InteractionCuePresetReconciliation.marker(
				selected,
				applied,
				markerSettings,
				configManager.getConfiguration(
					InteractionCueConfig.GROUP,
					InteractionCueConfig.MARKER_APPLIED_SETTINGS_KEY
				)
			);
		switch (action)
		{
			case APPLY_SELECTED:
				applySectionPreset(selected.getSettings());
				break;
			case SELECT_CUSTOM:
				selected = InteractionCueMarkerPreset.CUSTOM;
				setPresetSelection(
					InteractionCueConfig.MARKER_PRESET_KEY,
					selected
				);
				break;
			default:
				break;
		}
		recordMarkerPreset(selected);
	}

	private Map<String, Object> currentTooltipSettings()
	{
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.X_OFFSET_KEY, config.xOffset());
		values.put(InteractionCueConfig.Y_OFFSET_KEY, config.yOffset());
		values.put(InteractionCueConfig.ICON_SIZE_KEY, config.iconSize());
		values.put(
			InteractionCueConfig.TOOLTIP_ORIGIN_KEY,
			config.tooltipOrigin()
		);
		values.put(
			InteractionCueConfig.BACKGROUND_PADDING_TOP_KEY,
			config.backgroundPaddingTop()
		);
		values.put(
			InteractionCueConfig.BACKGROUND_PADDING_RIGHT_KEY,
			config.backgroundPaddingRight()
		);
		values.put(
			InteractionCueConfig.BACKGROUND_PADDING_BOTTOM_KEY,
			config.backgroundPaddingBottom()
		);
		values.put(
			InteractionCueConfig.BACKGROUND_PADDING_LEFT_KEY,
			config.backgroundPaddingLeft()
		);
		values.put(InteractionCueConfig.LABEL_FORMAT_KEY, config.labelFormat());
		values.put(InteractionCueConfig.BACKGROUND_KEY, config.background());
		values.put(
			InteractionCueConfig.BACKGROUND_COLOR_KEY,
			config.backgroundColor()
		);
		values.put(
			InteractionCueConfig.BACKGROUND_CORNER_KEY,
			config.backgroundCorner()
		);
		values.put(
			InteractionCueConfig.TOOLTIP_BORDER_KEY,
			config.tooltipBorder()
		);
		values.put(
			InteractionCueConfig.TOOLTIP_BORDER_COLOR_KEY,
			config.tooltipBorderColor()
		);
		values.put(
			InteractionCueConfig.TOOLTIP_BORDER_WIDTH_KEY,
			config.tooltipBorderWidth()
		);
		return values;
	}

	private Map<String, Object> currentMarkerSettings()
	{
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(
			InteractionCueConfig.PENDING_MARKER_ORIGIN_KEY,
			config.pendingMarkerOrigin()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
			config.pendingMarkerXOffset()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_Y_OFFSET_KEY,
			config.pendingMarkerYOffset()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_CORNER_KEY,
			config.pendingMarkerCorner()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_PADDING_TOP_KEY,
			config.pendingMarkerPaddingTop()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_PADDING_RIGHT_KEY,
			config.pendingMarkerPaddingRight()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_PADDING_BOTTOM_KEY,
			config.pendingMarkerPaddingBottom()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_PADDING_LEFT_KEY,
			config.pendingMarkerPaddingLeft()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_BORDER_KEY,
			config.pendingMarkerBorder()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_COLOR_KEY,
			config.pendingMarkerColor()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_BORDER_WIDTH_KEY,
			config.pendingMarkerBorderWidth()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_ICON_KEY,
			config.pendingMarkerIcon()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY,
			config.pendingMarkerIconSize()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY,
			config.pendingMarkerBackground()
		);
		values.put(
			InteractionCueConfig.PENDING_MARKER_BACKGROUND_COLOR_KEY,
			config.pendingMarkerBackgroundColor()
		);
		return values;
	}

	private void applySectionPreset(Map<String, Object> settings)
	{
		boolean previouslyApplyingPreset = applyingPreset;
		applyingPreset = true;
		try
		{
			settings.forEach((key, value) ->
				configManager.setConfiguration(
					InteractionCueConfig.GROUP,
					key,
					value
				)
			);
			InteractionCueConfigPanelSync.refreshOpenPanel(settings);
		}
		finally
		{
			applyingPreset = previouslyApplyingPreset;
		}
	}

	private void setPresetSelection(String key, Object preset)
	{
		boolean previouslyApplyingPreset = applyingPreset;
		applyingPreset = true;
		try
		{
			configManager.setConfiguration(
				InteractionCueConfig.GROUP,
				key,
				preset
			);
			InteractionCueConfigPanelSync.refreshOpenPanel(
				Collections.singletonMap(key, preset)
			);
		}
		finally
		{
			applyingPreset = previouslyApplyingPreset;
		}
	}

	private void recordTooltipPreset(InteractionCueTooltipPreset preset)
	{
		recordAppliedPreset(
			InteractionCueConfig.TOOLTIP_APPLIED_PRESET_KEY,
			InteractionCueConfig.TOOLTIP_APPLIED_SETTINGS_KEY,
			preset,
			currentTooltipSettings()
		);
	}

	private void recordMarkerPreset(InteractionCueMarkerPreset preset)
	{
		recordAppliedPreset(
			InteractionCueConfig.MARKER_APPLIED_PRESET_KEY,
			InteractionCueConfig.MARKER_APPLIED_SETTINGS_KEY,
			preset,
			currentMarkerSettings()
		);
	}

	private void recordAppliedPreset(
		String presetKey,
		String settingsKey,
		Enum<?> preset,
		Map<String, Object> settings)
	{
		if (preset != null)
		{
			configManager.setConfiguration(
				InteractionCueConfig.GROUP,
				presetKey,
				preset.name()
			);
			configManager.setConfiguration(
				InteractionCueConfig.GROUP,
				settingsKey,
				InteractionCuePresetReconciliation.fingerprint(settings)
			);
		}
	}

	private void queueTooltipCustomSelection()
	{
		queueCustomSelection(
			InteractionCueConfig.TOOLTIP_PRESET_KEY,
			InteractionCueTooltipPreset.CUSTOM,
			tooltipCustomSelectionGeneration,
			true
		);
	}

	private void queueMarkerCustomSelection()
	{
		queueCustomSelection(
			InteractionCueConfig.MARKER_PRESET_KEY,
			InteractionCueMarkerPreset.CUSTOM,
			markerCustomSelectionGeneration,
			false
		);
	}

	private void queueCustomSelection(
		String key,
		Object customPreset,
		AtomicLong generationCounter,
		boolean tooltip)
	{
		long generation;
		long profileId;
		synchronized (generationCounter)
		{
			generation = generationCounter.incrementAndGet();
			profileId = currentConfigProfileId();
		}

		SwingUtilities.invokeLater(() ->
		{
			synchronized (generationCounter)
			{
				if (generation != generationCounter.get()
					|| profileId != currentConfigProfileId()
					|| applyingPreset
					|| !isNamedPresetSelected(tooltip))
				{
					return;
				}

				configManager.setConfiguration(
					InteractionCueConfig.GROUP,
					key,
					customPreset
				);
				InteractionCueConfigPanelSync.refreshOpenPanel(
					Collections.singletonMap(key, customPreset)
				);
			}
		});
	}

	private boolean isNamedPresetSelected(boolean tooltip)
	{
		if (tooltip)
		{
			InteractionCueTooltipPreset preset = config.tooltipPreset();
			return preset != null
				&& preset != InteractionCueTooltipPreset.CUSTOM;
		}

		InteractionCueMarkerPreset preset = config.markerPreset();
		return preset != null && preset != InteractionCueMarkerPreset.CUSTOM;
	}

	private long currentConfigProfileId()
	{
		ConfigProfile profile = configManager.getProfile();
		return profile == null ? Long.MIN_VALUE : profile.getId();
	}

	private static void cancelPendingCustomSelection(AtomicLong generation)
	{
		synchronized (generation)
		{
			generation.incrementAndGet();
		}
	}

	static boolean shouldSwitchTooltipToCustom(
		boolean applyingPreset,
		String key,
		InteractionCueTooltipPreset selectedPreset)
	{
		return !applyingPreset
			&& InteractionCueTooltipPreset.FULL.getSettings().containsKey(key)
			&& selectedPreset != null
			&& selectedPreset != InteractionCueTooltipPreset.CUSTOM;
	}

	static boolean shouldSwitchMarkerToCustom(
		boolean applyingPreset,
		String key,
		InteractionCueMarkerPreset selectedPreset)
	{
		return !applyingPreset
			&& InteractionCueMarkerPreset.RUINED_HEIR.getSettings().containsKey(key)
			&& selectedPreset != null
			&& selectedPreset != InteractionCueMarkerPreset.CUSTOM;
	}

	static boolean shouldRecordTooltipCustomSettings(
		boolean applyingPreset,
		String key,
		InteractionCueTooltipPreset selectedPreset)
	{
		return !applyingPreset
			&& InteractionCueTooltipPreset.FULL.getSettings().containsKey(key)
			&& selectedPreset == InteractionCueTooltipPreset.CUSTOM;
	}

	static boolean shouldRecordMarkerCustomSettings(
		boolean applyingPreset,
		String key,
		InteractionCueMarkerPreset selectedPreset)
	{
		return !applyingPreset
			&& InteractionCueMarkerPreset.RUINED_HEIR.getSettings()
				.containsKey(key)
			&& selectedPreset == InteractionCueMarkerPreset.CUSTOM;
	}

	static InteractionCueTooltipPreset tooltipPresetFromValue(String value)
	{
		return presetFromValue(value, InteractionCueTooltipPreset.values());
	}

	static InteractionCueMarkerPreset markerPresetFromValue(String value)
	{
		if ("DEFAULT".equals(value) || "Default".equals(value))
		{
			return InteractionCueMarkerPreset.LEGACY;
		}
		return presetFromValue(value, InteractionCueMarkerPreset.values());
	}

	private static <T extends Enum<T>> T presetFromValue(
		String value,
		T[] presets)
	{
		if (value == null)
		{
			return null;
		}

		for (T preset : presets)
		{
			if (preset.name().equals(value) || preset.toString().equals(value))
			{
				return preset;
			}
		}
		return null;
	}

	private static String clean(String value, String fallback)
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
		pendingInteraction = null;
	}

	private int getLocalAnimation()
	{
		return client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getAnimation();
	}

	private int getLocalAnimationFrame()
	{
		return client.getLocalPlayer() == null ? -1 : client.getLocalPlayer().getAnimationFrame();
	}

	@Provides
	InteractionCueConfig provideConfig(ConfigManager configManager)
	{
		InteractionCueConfigMigration.migrate(configManager);
		return configManager.getConfig(InteractionCueConfig.class);
	}
}
