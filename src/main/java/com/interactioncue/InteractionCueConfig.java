package com.interactioncue;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("interaction-cue-v2")
public interface InteractionCueConfig extends Config
{
	@ConfigSection(
		name = "Tooltip",
		description = "Controls whether the cursor cue is shown, where it appears, its icon size, and the text/icon format used while an item or spell is selected.",
		position = 0
	)
	String tooltipSection = "tooltip";

	@ConfigSection(
		name = "Tooltip Background",
		description = "Styles the filled area behind the cursor cue. Disable it for a bare icon/label, or use alpha on the color for a softer background.",
		position = 1
	)
	String tooltipBackgroundSection = "tooltipBackground";

	@ConfigSection(
		name = "Tooltip Border",
		description = "Styles the outline around the cursor cue, including fractional border widths and transparent colors.",
		position = 2
	)
	String tooltipBorderSection = "tooltipBorder";

	@ConfigSection(
		name = "Marker",
		description = "Controls the badge shown on an inventory slot after you click it with a selected item or spell, until the next tick resolves the action state.",
		position = 3
	)
	String markerSection = "marker";

	@ConfigSection(
		name = "Marker Border",
		description = "Styles the outline around the inventory marker badge.",
		position = 4
	)
	String markerBorderSection = "markerBorder";

	@ConfigSection(
		name = "Marker Icon",
		description = "Controls the selected item or spell icon drawn inside the marker badge.",
		position = 5
	)
	String markerIconSection = "markerIcon";

	@ConfigSection(
		name = "Marker Background",
		description = "Styles the filled area behind the marker badge icon.",
		position = 6
	)
	String markerBackgroundSection = "markerBackground";

	@ConfigItem(
		position = 0,
		keyName = "cursorIndicator",
		name = "Toggle",
		description = "Show the selected spell and Use item tooltip near the cursor",
		section = tooltipSection
	)
	default boolean cursorIndicator()
	{
		return true;
	}

	@Range(
		min = -100,
		max = 100
	)
	@ConfigItem(
		position = 1,
		keyName = "xOffset",
		name = "X offset",
		description = "Horizontal cursor icon offset",
		section = tooltipSection
	)
	default int xOffset()
	{
		return 7;
	}

	@Range(
		min = -100,
		max = 100
	)
	@ConfigItem(
		position = 2,
		keyName = "yOffset",
		name = "Y offset",
		description = "Vertical cursor icon offset",
		section = tooltipSection
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
		keyName = "iconSize",
		name = "Icon size",
		description = "Cursor indicator icon size",
		section = tooltipSection
	)
	default int iconSize()
	{
		return 32;
	}

	@ConfigItem(
		position = 4,
		keyName = "tooltipOrigin",
		name = "Origin",
		description = "Cursor tooltip origin before viewport fitting",
		section = tooltipSection
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
		position = 5,
		keyName = "backgroundPaddingTop",
		name = "Padding top",
		description = "Top padding around the tooltip",
		section = tooltipSection
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
		position = 6,
		keyName = "backgroundPaddingRight",
		name = "Padding right",
		description = "Right padding around the tooltip",
		section = tooltipSection
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
		position = 7,
		keyName = "backgroundPaddingBottom",
		name = "Padding bottom",
		description = "Bottom padding around the tooltip",
		section = tooltipSection
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
		position = 8,
		keyName = "backgroundPaddingLeft",
		name = "Padding left",
		description = "Left padding around the tooltip",
		section = tooltipSection
	)
	default int backgroundPaddingLeft()
	{
		return 0;
	}

	@ConfigItem(
		position = 9,
		keyName = "labelFormat",
		name = "Format",
		description = "Supports {action}, {source}, {target}, {source_icon}, and color tags such as {#FFF}",
		section = tooltipSection
	)
	default String labelFormat()
	{
		return "{source_icon}";
	}

	@ConfigItem(
		position = 0,
		keyName = "background",
		name = "Enabled",
		description = "Show a background behind the cursor tooltip",
		section = tooltipBackgroundSection
	)
	default boolean background()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 1,
		keyName = "backgroundColor",
		name = "Color",
		description = "Tooltip background color",
		section = tooltipBackgroundSection
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
		position = 2,
		keyName = "backgroundCorner",
		name = "Corner",
		description = "Corner size of the tooltip background",
		section = tooltipBackgroundSection
	)
	default int backgroundCorner()
	{
		return 2;
	}

	@ConfigItem(
		position = 0,
		keyName = "tooltipBorder",
		name = "Enabled",
		description = "Draw a border around the cursor tooltip",
		section = tooltipBorderSection
	)
	default boolean tooltipBorder()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 1,
		keyName = "tooltipBorderColor",
		name = "Color",
		description = "Tooltip border color",
		section = tooltipBorderSection
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
		position = 2,
		keyName = "tooltipBorderWidth",
		name = "Width",
		description = "Tooltip border width",
		section = tooltipBorderSection
	)
	default double tooltipBorderWidth()
	{
		return 0.8;
	}

	@ConfigItem(
		position = 0,
		keyName = "pendingMarker",
		name = "Enabled",
		description = "Mark inventory slots with pending selected spell or item-use actions",
		section = markerSection
	)
	default boolean pendingMarker()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "pendingMarkerOrigin",
		name = "Origin",
		description = "Inventory item point used to place the marker badge",
		section = markerSection
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
		keyName = "pendingMarkerXOffset",
		name = "X offset",
		description = "Horizontal marker badge offset",
		section = markerSection
	)
	default int pendingMarkerXOffset()
	{
		return 0;
	}

	@Range(
		min = -128,
		max = 128
	)
	@ConfigItem(
		position = 3,
		keyName = "pendingMarkerYOffset",
		name = "Y offset",
		description = "Vertical marker badge offset",
		section = markerSection
	)
	default int pendingMarkerYOffset()
	{
		return 0;
	}

	@Range(
		min = 0,
		max = 32
	)
	@ConfigItem(
		position = 4,
		keyName = "pendingMarkerCorner",
		name = "Corner",
		description = "Corner size for marker border and background",
		section = markerSection
	)
	default int pendingMarkerCorner()
	{
		return 2;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 5,
		keyName = "pendingMarkerPaddingTop",
		name = "Padding top",
		description = "Top padding around the marker badge",
		section = markerSection
	)
	default int pendingMarkerPaddingTop()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 6,
		keyName = "pendingMarkerPaddingRight",
		name = "Padding right",
		description = "Right padding around the marker badge",
		section = markerSection
	)
	default int pendingMarkerPaddingRight()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 7,
		keyName = "pendingMarkerPaddingBottom",
		name = "Padding bottom",
		description = "Bottom padding around the marker badge",
		section = markerSection
	)
	default int pendingMarkerPaddingBottom()
	{
		return 3;
	}

	@Range(
		min = 0,
		max = 128
	)
	@ConfigItem(
		position = 8,
		keyName = "pendingMarkerPaddingLeft",
		name = "Padding left",
		description = "Left padding around the marker badge",
		section = markerSection
	)
	default int pendingMarkerPaddingLeft()
	{
		return 3;
	}

	@ConfigItem(
		position = 0,
		keyName = "pendingMarkerBorder",
		name = "Enabled",
		description = "Draw a border around pending action markers",
		section = markerBorderSection
	)
	default boolean pendingMarkerBorder()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 1,
		keyName = "pendingMarkerColor",
		name = "Color",
		description = "Color used for the marker border",
		section = markerBorderSection
	)
	default Color pendingMarkerColor()
	{
		return new Color(0xFF, 0xFF, 0x0C, 0xFF);
	}

	@Range(
		min = 1,
		max = 16
	)
	@ConfigItem(
		position = 2,
		keyName = "pendingMarkerBorderWidth",
		name = "Width",
		description = "Border width for pending action markers",
		section = markerBorderSection
	)
	default double pendingMarkerBorderWidth()
	{
		return 0.5;
	}

	@ConfigItem(
		position = 0,
		keyName = "pendingMarkerIcon",
		name = "Enabled",
		description = "Show the pending action icon",
		section = markerIconSection
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
		position = 1,
		keyName = "pendingMarkerIconSize",
		name = "Size",
		description = "Icon size for pending action markers",
		section = markerIconSection
	)
	default int pendingMarkerIconSize()
	{
		return 25;
	}

	@ConfigItem(
		position = 0,
		keyName = "pendingMarkerBackground",
		name = "Enabled",
		description = "Show a background behind the marker",
		section = markerBackgroundSection
	)
	default boolean pendingMarkerBackground()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 1,
		keyName = "pendingMarkerBackgroundColor",
		name = "Color",
		description = "Color used for the marker background",
		section = markerBackgroundSection
	)
	default Color pendingMarkerBackgroundColor()
	{
		return new Color(0x4F, 0x48, 0x36, 0xAA);
	}
}
