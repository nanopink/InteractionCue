package com.interactioncue;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public enum InteractionCueTooltipPreset
{
	CUSTOM("Custom", ruinedHeirSettings()),
	FULL("Full", fullSettings()),
	RUINED_HEIR("Ruined Heir's", ruinedHeirSettings());

	static final InteractionCueTooltipPreset DEFAULT = RUINED_HEIR;

	private final String displayName;
	private final Map<String, Object> settings;

	InteractionCueTooltipPreset(String displayName, Map<String, Object> settings)
	{
		this.displayName = displayName;
		this.settings = settings;
	}

	Map<String, Object> getSettings()
	{
		return settings;
	}

	private static Map<String, Object> fullSettings()
	{
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.X_OFFSET_KEY, 22);
		values.put(InteractionCueConfig.Y_OFFSET_KEY, 0);
		values.put(InteractionCueConfig.ICON_SIZE_KEY, 16);
		values.put(InteractionCueConfig.TOOLTIP_ORIGIN_KEY,
			InteractionCueTooltipOrigin.CENTER_LEFT);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_TOP_KEY, 2);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_RIGHT_KEY, 8);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_BOTTOM_KEY, 2);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_LEFT_KEY, 8);
		values.put(InteractionCueConfig.LABEL_FORMAT_KEY,
			"{#FFF}{action} {source_icon} {#FF9040}{source}"
				+ "{target:{#FFF} -> {#00ffff}@1}");
		values.put(InteractionCueConfig.BACKGROUND_KEY, true);
		values.put(InteractionCueConfig.BACKGROUND_COLOR_KEY,
			new Color(0x4F, 0x48, 0x36, 0xAA));
		values.put(InteractionCueConfig.BACKGROUND_CORNER_KEY, 2);
		values.put(InteractionCueConfig.TOOLTIP_BORDER_KEY, true);
		values.put(InteractionCueConfig.TOOLTIP_BORDER_COLOR_KEY,
			new Color(0x73, 0x6B, 0x5A, 0xFF));
		values.put(InteractionCueConfig.TOOLTIP_BORDER_WIDTH_KEY, 0.8);
		return Collections.unmodifiableMap(values);
	}

	private static Map<String, Object> ruinedHeirSettings()
	{
		Map<String, Object> values = new LinkedHashMap<>(fullSettings());
		values.put(InteractionCueConfig.X_OFFSET_KEY, 4);
		values.put(InteractionCueConfig.Y_OFFSET_KEY, 32);
		values.put(InteractionCueConfig.ICON_SIZE_KEY, 24);
		values.put(InteractionCueConfig.TOOLTIP_ORIGIN_KEY,
			InteractionCueTooltipOrigin.CENTER_LEFT);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_TOP_KEY, 0);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_RIGHT_KEY, 0);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_BOTTOM_KEY, 0);
		values.put(InteractionCueConfig.BACKGROUND_PADDING_LEFT_KEY, 0);
		values.put(InteractionCueConfig.LABEL_FORMAT_KEY, "{source_icon}");
		values.put(InteractionCueConfig.BACKGROUND_KEY, false);
		values.put(InteractionCueConfig.TOOLTIP_BORDER_KEY, false);
		return Collections.unmodifiableMap(values);
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
