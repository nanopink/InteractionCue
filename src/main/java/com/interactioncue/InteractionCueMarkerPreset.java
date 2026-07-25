package com.interactioncue;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public enum InteractionCueMarkerPreset
{
	CUSTOM("Custom", ruinedHeirSettings()),
	RUINED_HEIR("Ruined Heir's", ruinedHeirSettings()),
	LEGACY("Legacy", legacySettings());

	static final InteractionCueMarkerPreset DEFAULT = RUINED_HEIR;

	private final String displayName;
	private final Map<String, Object> settings;

	InteractionCueMarkerPreset(String displayName, Map<String, Object> settings)
	{
		this.displayName = displayName;
		this.settings = settings;
	}

	Map<String, Object> getSettings()
	{
		return settings;
	}

	private static Map<String, Object> legacySettings()
	{
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.PENDING_MARKER_ORIGIN_KEY,
			InteractionCueMarkerOrigin.CENTER);
		values.put(InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY, 0);
		values.put(InteractionCueConfig.PENDING_MARKER_Y_OFFSET_KEY, 0);
		values.put(InteractionCueConfig.PENDING_MARKER_CORNER_KEY, 2);
		values.put(InteractionCueConfig.PENDING_MARKER_ICON_KEY, true);
		values.put(InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY, 25);
		values.put(InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY, true);
		values.put(InteractionCueConfig.PENDING_MARKER_BACKGROUND_COLOR_KEY,
			new Color(0x4F, 0x48, 0x36, 0xAA));
		values.put(InteractionCueConfig.PENDING_MARKER_BORDER_KEY, true);
		values.put(InteractionCueConfig.PENDING_MARKER_COLOR_KEY,
			new Color(0xFF, 0xFF, 0x0C, 0xFF));
		values.put(InteractionCueConfig.PENDING_MARKER_BORDER_WIDTH_KEY, 0.5);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_TOP_KEY, 3);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_RIGHT_KEY, 3);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_BOTTOM_KEY, 3);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_LEFT_KEY, 3);
		return Collections.unmodifiableMap(values);
	}

	private static Map<String, Object> ruinedHeirSettings()
	{
		Map<String, Object> values = new LinkedHashMap<>(legacySettings());
		values.put(InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY, 7);
		values.put(InteractionCueConfig.PENDING_MARKER_Y_OFFSET_KEY, 5);
		values.put(InteractionCueConfig.PENDING_MARKER_CORNER_KEY, 0);
		values.put(InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY, 26);
		values.put(InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY, false);
		values.put(InteractionCueConfig.PENDING_MARKER_BORDER_KEY, false);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_TOP_KEY, 0);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_RIGHT_KEY, 0);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_BOTTOM_KEY, 0);
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_LEFT_KEY, 0);
		return Collections.unmodifiableMap(values);
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
