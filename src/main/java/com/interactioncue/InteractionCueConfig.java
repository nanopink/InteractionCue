package com.interactioncue;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(InteractionCueConfig.GROUP)
public interface InteractionCueConfig extends Config
{
	String GROUP = "interaction-cue-v2";

	String TOOLTIP_PRESET_KEY = "tooltipPreset";
	String TOOLTIP_APPLIED_PRESET_KEY = "tooltipAppliedPreset";
	String TOOLTIP_APPLIED_SETTINGS_KEY = "tooltipAppliedSettings";
	String CURSOR_INDICATOR_KEY = "cursorIndicator";
	String X_OFFSET_KEY = "xOffset";
	String Y_OFFSET_KEY = "yOffset";
	String ICON_SIZE_KEY = "iconSize";
	String TOOLTIP_ORIGIN_KEY = "tooltipOrigin";
	String BACKGROUND_PADDING_TOP_KEY = "backgroundPaddingTop";
	String BACKGROUND_PADDING_RIGHT_KEY = "backgroundPaddingRight";
	String BACKGROUND_PADDING_BOTTOM_KEY = "backgroundPaddingBottom";
	String BACKGROUND_PADDING_LEFT_KEY = "backgroundPaddingLeft";
	String LABEL_FORMAT_KEY = "labelFormat";
	String BACKGROUND_KEY = "background";
	String BACKGROUND_COLOR_KEY = "backgroundColor";
	String BACKGROUND_CORNER_KEY = "backgroundCorner";
	String TOOLTIP_BORDER_KEY = "tooltipBorder";
	String TOOLTIP_BORDER_COLOR_KEY = "tooltipBorderColor";
	String TOOLTIP_BORDER_WIDTH_KEY = "tooltipBorderWidth";

	String MARKER_PRESET_KEY = "markerPreset";
	String MARKER_APPLIED_PRESET_KEY = "markerAppliedPreset";
	String MARKER_APPLIED_SETTINGS_KEY = "markerAppliedSettings";
	String PENDING_MARKER_KEY = "pendingMarker";
	String PENDING_MARKER_ORIGIN_KEY = "pendingMarkerOrigin";
	String PENDING_MARKER_X_OFFSET_KEY = "pendingMarkerXOffset";
	String PENDING_MARKER_Y_OFFSET_KEY = "pendingMarkerYOffset";
	String PENDING_MARKER_CORNER_KEY = "pendingMarkerCorner";
	String PENDING_MARKER_PADDING_TOP_KEY = "pendingMarkerPaddingTop";
	String PENDING_MARKER_PADDING_RIGHT_KEY = "pendingMarkerPaddingRight";
	String PENDING_MARKER_PADDING_BOTTOM_KEY = "pendingMarkerPaddingBottom";
	String PENDING_MARKER_PADDING_LEFT_KEY = "pendingMarkerPaddingLeft";
	String PENDING_MARKER_BORDER_KEY = "pendingMarkerBorder";
	String PENDING_MARKER_COLOR_KEY = "pendingMarkerColor";
	String PENDING_MARKER_BORDER_WIDTH_KEY = "pendingMarkerBorderWidth";
	String PENDING_MARKER_ICON_KEY = "pendingMarkerIcon";
	String PENDING_MARKER_ICON_SIZE_KEY = "pendingMarkerIconSize";
	String PENDING_MARKER_BACKGROUND_KEY = "pendingMarkerBackground";
	String PENDING_MARKER_BACKGROUND_COLOR_KEY = "pendingMarkerBackgroundColor";

	@ConfigSection(
		name = "Tooltip",
		description = "Controls the cue shown near the cursor while an item or spell is selected.",
		position = 0
	)
	String TOOLTIP_SECTION = "tooltip";

	@ConfigSection(
		name = "Marker",
		description = "Controls the badge shown on an inventory slot while an interaction is awaiting a response.",
		position = 1
	)
	String MARKER_SECTION = "marker";

	@ConfigItem(
		position = -1,
		keyName = CURSOR_INDICATOR_KEY,
		name = "Enabled",
		description = "Show the selected spell and Use item tooltip near the cursor",
		section = TOOLTIP_SECTION
	)
	default boolean cursorIndicator()
	{
		return true;
	}

	@ConfigItem(
		position = 0,
		keyName = TOOLTIP_PRESET_KEY,
		name = "Preset",
		description = "Copies the tooltip appearance values below once. Editing a copied value changes this section to Custom. "
			+ "When both change while disabled, edited values are preserved.",
		section = TOOLTIP_SECTION
	)
	default InteractionCueTooltipPreset tooltipPreset()
	{
		return InteractionCueTooltipPreset.DEFAULT;
	}

	@Range(
		min = -100,
		max = 100
	)
	@ConfigItem(
		position = 1,
		keyName = X_OFFSET_KEY,
		name = "X offset",
		description = "Horizontal cursor icon offset",
		section = TOOLTIP_SECTION
	)
	default int xOffset()
	{
		return 4;
	}

	@Range(
		min = -100,
		max = 100
	)
	@ConfigItem(
		position = 2,
		keyName = Y_OFFSET_KEY,
		name = "Y offset",
		description = "Vertical cursor icon offset",
		section = TOOLTIP_SECTION
	)
	default int yOffset()
	{
		return 32;
	}

	@Range(
		min = 4,
		max = 512
	)
	@ConfigItem(
		position = 3,
		keyName = ICON_SIZE_KEY,
		name = "Icon size",
		description = "Cursor indicator icon size",
		section = TOOLTIP_SECTION
	)
	default int iconSize()
	{
		return 24;
	}

	@ConfigItem(
		position = 4,
		keyName = TOOLTIP_ORIGIN_KEY,
		name = "Origin",
		description = "Cursor tooltip origin before viewport fitting",
		section = TOOLTIP_SECTION
	)
	default InteractionCueTooltipOrigin tooltipOrigin()
	{
		return InteractionCueTooltipOrigin.CENTER_LEFT;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 12,
		keyName = BACKGROUND_PADDING_TOP_KEY,
		name = "Padding top",
		description = "Top padding around the tooltip",
		section = TOOLTIP_SECTION
	)
	default int backgroundPaddingTop()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 13,
		keyName = BACKGROUND_PADDING_RIGHT_KEY,
		name = "Padding right",
		description = "Right padding around the tooltip",
		section = TOOLTIP_SECTION
	)
	default int backgroundPaddingRight()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 14,
		keyName = BACKGROUND_PADDING_BOTTOM_KEY,
		name = "Padding bottom",
		description = "Bottom padding around the tooltip",
		section = TOOLTIP_SECTION
	)
	default int backgroundPaddingBottom()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 15,
		keyName = BACKGROUND_PADDING_LEFT_KEY,
		name = "Padding left",
		description = "Left padding around the tooltip",
		section = TOOLTIP_SECTION
	)
	default int backgroundPaddingLeft()
	{
		return 0;
	}

	@ConfigItem(
		position = 5,
		keyName = LABEL_FORMAT_KEY,
		name = "Format",
		description = "Supports {action}, {source}, {tool}, {target}, {source_icon}, {action_icon}, {icon}, and color tags such as {#FFF}",
		section = TOOLTIP_SECTION
	)
	default String labelFormat()
	{
		return "{source_icon}";
	}

	@ConfigItem(
		position = 6,
		keyName = BACKGROUND_KEY,
		name = "Background",
		description = "Show a background behind the cursor tooltip",
		section = TOOLTIP_SECTION
	)
	default boolean background()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 7,
		keyName = BACKGROUND_COLOR_KEY,
		name = "Background color",
		description = "Tooltip background color",
		section = TOOLTIP_SECTION
	)
	default Color backgroundColor()
	{
		return new Color(0x4F, 0x48, 0x36, 0xAA);
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 8,
		keyName = BACKGROUND_CORNER_KEY,
		name = "Background corner",
		description = "Corner size of the tooltip background",
		section = TOOLTIP_SECTION
	)
	default int backgroundCorner()
	{
		return 2;
	}

	@ConfigItem(
		position = 9,
		keyName = TOOLTIP_BORDER_KEY,
		name = "Border",
		description = "Draw a border around the cursor tooltip",
		section = TOOLTIP_SECTION
	)
	default boolean tooltipBorder()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 10,
		keyName = TOOLTIP_BORDER_COLOR_KEY,
		name = "Border color",
		description = "Tooltip border color",
		section = TOOLTIP_SECTION
	)
	default Color tooltipBorderColor()
	{
		return new Color(0x73, 0x6B, 0x5A, 0xFF);
	}

	@Range(
		min = 0,
		max = 16
	)
	@ConfigItem(
		position = 11,
		keyName = TOOLTIP_BORDER_WIDTH_KEY,
		name = "Border width",
		description = "Tooltip border width",
		section = TOOLTIP_SECTION
	)
	default double tooltipBorderWidth()
	{
		return 0.8;
	}

	@ConfigItem(
		position = -1,
		keyName = PENDING_MARKER_KEY,
		name = "Enabled",
		description = "Mark inventory slots while selected spell or item-use actions await a response",
		section = MARKER_SECTION
	)
	default boolean pendingMarker()
	{
		return true;
	}

	@ConfigItem(
		position = 0,
		keyName = MARKER_PRESET_KEY,
		name = "Preset",
		description = "Copies the marker appearance values below once. Editing a copied value changes this section to Custom. "
			+ "When both change while disabled, edited values are preserved.",
		section = MARKER_SECTION
	)
	default InteractionCueMarkerPreset markerPreset()
	{
		return InteractionCueMarkerPreset.DEFAULT;
	}

	@ConfigItem(
		position = 1,
		keyName = PENDING_MARKER_ORIGIN_KEY,
		name = "Origin",
		description = "Inventory item point used to place the marker badge",
		section = MARKER_SECTION
	)
	default InteractionCueMarkerOrigin pendingMarkerOrigin()
	{
		return InteractionCueMarkerOrigin.CENTER;
	}

	@Range(
		min = -128,
		max = 128
	)
	@ConfigItem(
		position = 2,
		keyName = PENDING_MARKER_X_OFFSET_KEY,
		name = "X offset",
		description = "Horizontal marker badge offset",
		section = MARKER_SECTION
	)
	default int pendingMarkerXOffset()
	{
		return 7;
	}

	@Range(
		min = -128,
		max = 128
	)
	@ConfigItem(
		position = 3,
		keyName = PENDING_MARKER_Y_OFFSET_KEY,
		name = "Y offset",
		description = "Vertical marker badge offset",
		section = MARKER_SECTION
	)
	default int pendingMarkerYOffset()
	{
		return 5;
	}

	@ConfigItem(
		position = 4,
		keyName = PENDING_MARKER_ICON_KEY,
		name = "Icon",
		description = "Show the pending action icon",
		section = MARKER_SECTION
	)
	default boolean pendingMarkerIcon()
	{
		return true;
	}

	@Range(
		min = 4,
		max = 128
	)
	@ConfigItem(
		position = 5,
		keyName = PENDING_MARKER_ICON_SIZE_KEY,
		name = "Icon size",
		description = "Icon size for pending action markers",
		section = MARKER_SECTION
	)
	default int pendingMarkerIconSize()
	{
		return 26;
	}

	@ConfigItem(
		position = 6,
		keyName = PENDING_MARKER_BACKGROUND_KEY,
		name = "Background",
		description = "Show a background behind the marker",
		section = MARKER_SECTION
	)
	default boolean pendingMarkerBackground()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 7,
		keyName = PENDING_MARKER_BACKGROUND_COLOR_KEY,
		name = "Background color",
		description = "Color used for the marker background",
		section = MARKER_SECTION
	)
	default Color pendingMarkerBackgroundColor()
	{
		return new Color(0x4F, 0x48, 0x36, 0xAA);
	}

	@Range(
		min = 0,
		max = 32
	)
	@ConfigItem(
		position = 8,
		keyName = PENDING_MARKER_CORNER_KEY,
		name = "Corner",
		description = "Corner size for marker border and background",
		section = MARKER_SECTION
	)
	default int pendingMarkerCorner()
	{
		return 0;
	}

	@ConfigItem(
		position = 9,
		keyName = PENDING_MARKER_BORDER_KEY,
		name = "Border",
		description = "Draw a border around pending action markers",
		section = MARKER_SECTION
	)
	default boolean pendingMarkerBorder()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 10,
		keyName = PENDING_MARKER_COLOR_KEY,
		name = "Border color",
		description = "Color used for the marker border",
		section = MARKER_SECTION
	)
	default Color pendingMarkerColor()
	{
		return new Color(0xFF, 0xFF, 0x0C, 0xFF);
	}

	@Range(
		min = 0,
		max = 16
	)
	@ConfigItem(
		position = 11,
		keyName = PENDING_MARKER_BORDER_WIDTH_KEY,
		name = "Border width",
		description = "Border width for pending action markers",
		section = MARKER_SECTION
	)
	default double pendingMarkerBorderWidth()
	{
		return 0.5;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 12,
		keyName = PENDING_MARKER_PADDING_TOP_KEY,
		name = "Padding top",
		description = "Top padding around the marker badge",
		section = MARKER_SECTION
	)
	default int pendingMarkerPaddingTop()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 13,
		keyName = PENDING_MARKER_PADDING_RIGHT_KEY,
		name = "Padding right",
		description = "Right padding around the marker badge",
		section = MARKER_SECTION
	)
	default int pendingMarkerPaddingRight()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 14,
		keyName = PENDING_MARKER_PADDING_BOTTOM_KEY,
		name = "Padding bottom",
		description = "Bottom padding around the marker badge",
		section = MARKER_SECTION
	)
	default int pendingMarkerPaddingBottom()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 15,
		keyName = PENDING_MARKER_PADDING_LEFT_KEY,
		name = "Padding left",
		description = "Left padding around the marker badge",
		section = MARKER_SECTION
	)
	default int pendingMarkerPaddingLeft()
	{
		return 0;
	}
}
