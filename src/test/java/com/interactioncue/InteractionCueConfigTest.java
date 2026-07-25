package com.interactioncue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCueConfigTest
{
	private final InteractionCueConfig config = new InteractionCueConfig()
	{
	};

	@Test
	public void freshConfigurationUsesRuinedHeirPresets()
	{
		Assert.assertSame(InteractionCueTooltipPreset.RUINED_HEIR, config.tooltipPreset());
		Assert.assertSame(InteractionCueMarkerPreset.RUINED_HEIR, config.markerPreset());
		Assert.assertTrue(config.cursorIndicator());
		Assert.assertTrue(config.pendingMarker());

		Assert.assertEquals(4, config.xOffset());
		Assert.assertEquals(32, config.yOffset());
		Assert.assertEquals(24, config.iconSize());
		Assert.assertSame(
			InteractionCueTooltipOrigin.CENTER_LEFT,
			config.tooltipOrigin()
		);
		Assert.assertEquals("{source_icon}", config.labelFormat());
		Assert.assertFalse(config.background());
		Assert.assertFalse(config.tooltipBorder());

		Assert.assertSame(
			InteractionCueMarkerOrigin.CENTER,
			config.pendingMarkerOrigin());
		Assert.assertEquals(7, config.pendingMarkerXOffset());
		Assert.assertEquals(5, config.pendingMarkerYOffset());
		Assert.assertEquals(0, config.pendingMarkerCorner());
		Assert.assertEquals(0, config.pendingMarkerPaddingTop());
		Assert.assertEquals(0, config.pendingMarkerPaddingRight());
		Assert.assertEquals(0, config.pendingMarkerPaddingBottom());
		Assert.assertEquals(0, config.pendingMarkerPaddingLeft());
		Assert.assertFalse(config.pendingMarkerBorder());
		Assert.assertEquals(26, config.pendingMarkerIconSize());
		Assert.assertFalse(config.pendingMarkerBackground());
	}

	@Test
	public void freshValuesExactlyMatchSelectedPresetMaps()
	{
		Assert.assertEquals(
			tooltipValues(config),
			InteractionCueTooltipPreset.RUINED_HEIR.getSettings());
		Assert.assertEquals(
			markerValues(config),
			InteractionCueMarkerPreset.RUINED_HEIR.getSettings());
	}

	@Test
	public void enableTogglesRemainIndependentFromAppearancePresets()
	{
		Assert.assertFalse(InteractionCueTooltipPreset.FULL.getSettings()
			.containsKey(InteractionCueConfig.CURSOR_INDICATOR_KEY));
		Assert.assertFalse(InteractionCueMarkerPreset.RUINED_HEIR.getSettings()
			.containsKey(InteractionCueConfig.PENDING_MARKER_KEY));
	}

	private static Map<String, Object> tooltipValues(InteractionCueConfig source)
	{
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.X_OFFSET_KEY, source.xOffset());
		values.put(InteractionCueConfig.Y_OFFSET_KEY, source.yOffset());
		values.put(InteractionCueConfig.ICON_SIZE_KEY, source.iconSize());
		values.put(InteractionCueConfig.TOOLTIP_ORIGIN_KEY, source.tooltipOrigin());
		values.put(InteractionCueConfig.BACKGROUND_PADDING_TOP_KEY,
			source.backgroundPaddingTop());
		values.put(InteractionCueConfig.BACKGROUND_PADDING_RIGHT_KEY,
			source.backgroundPaddingRight());
		values.put(InteractionCueConfig.BACKGROUND_PADDING_BOTTOM_KEY,
			source.backgroundPaddingBottom());
		values.put(InteractionCueConfig.BACKGROUND_PADDING_LEFT_KEY,
			source.backgroundPaddingLeft());
		values.put(InteractionCueConfig.LABEL_FORMAT_KEY, source.labelFormat());
		values.put(InteractionCueConfig.BACKGROUND_KEY, source.background());
		values.put(InteractionCueConfig.BACKGROUND_COLOR_KEY, source.backgroundColor());
		values.put(InteractionCueConfig.BACKGROUND_CORNER_KEY, source.backgroundCorner());
		values.put(InteractionCueConfig.TOOLTIP_BORDER_KEY, source.tooltipBorder());
		values.put(InteractionCueConfig.TOOLTIP_BORDER_COLOR_KEY,
			source.tooltipBorderColor());
		values.put(InteractionCueConfig.TOOLTIP_BORDER_WIDTH_KEY,
			source.tooltipBorderWidth());
		return values;
	}

	private static Map<String, Object> markerValues(InteractionCueConfig source)
	{
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.PENDING_MARKER_ORIGIN_KEY,
			source.pendingMarkerOrigin());
		values.put(InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
			source.pendingMarkerXOffset());
		values.put(InteractionCueConfig.PENDING_MARKER_Y_OFFSET_KEY,
			source.pendingMarkerYOffset());
		values.put(InteractionCueConfig.PENDING_MARKER_CORNER_KEY,
			source.pendingMarkerCorner());
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_TOP_KEY,
			source.pendingMarkerPaddingTop());
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_RIGHT_KEY,
			source.pendingMarkerPaddingRight());
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_BOTTOM_KEY,
			source.pendingMarkerPaddingBottom());
		values.put(InteractionCueConfig.PENDING_MARKER_PADDING_LEFT_KEY,
			source.pendingMarkerPaddingLeft());
		values.put(InteractionCueConfig.PENDING_MARKER_BORDER_KEY,
			source.pendingMarkerBorder());
		values.put(InteractionCueConfig.PENDING_MARKER_COLOR_KEY,
			source.pendingMarkerColor());
		values.put(InteractionCueConfig.PENDING_MARKER_BORDER_WIDTH_KEY,
			source.pendingMarkerBorderWidth());
		values.put(InteractionCueConfig.PENDING_MARKER_ICON_KEY,
			source.pendingMarkerIcon());
		values.put(InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY,
			source.pendingMarkerIconSize());
		values.put(InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY,
			source.pendingMarkerBackground());
		values.put(InteractionCueConfig.PENDING_MARKER_BACKGROUND_COLOR_KEY,
			source.pendingMarkerBackgroundColor());
		return values;
	}
}
