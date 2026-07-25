package com.interactioncue;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCuePresetTest
{
	@Test
	public void fullTooltipPresetRetainsFormerDefaults()
	{
		Map<String, Object> values = InteractionCueTooltipPreset.FULL.getSettings();

		Assert.assertEquals(15, values.size());
		Assert.assertEquals(22, values.get(InteractionCueConfig.X_OFFSET_KEY));
		Assert.assertEquals(0, values.get(InteractionCueConfig.Y_OFFSET_KEY));
		Assert.assertEquals(16, values.get(InteractionCueConfig.ICON_SIZE_KEY));
		Assert.assertSame(
			InteractionCueTooltipOrigin.CENTER_LEFT,
			values.get(InteractionCueConfig.TOOLTIP_ORIGIN_KEY));
		Assert.assertEquals(
			"{#FFF}{action} {source_icon} {#FF9040}{source}"
				+ "{target:{#FFF} -> {#00ffff}@1}",
			values.get(InteractionCueConfig.LABEL_FORMAT_KEY));
		Assert.assertEquals(true, values.get(InteractionCueConfig.BACKGROUND_KEY));
		Assert.assertEquals(
			new Color(0x4F, 0x48, 0x36, 0xAA),
			values.get(InteractionCueConfig.BACKGROUND_COLOR_KEY));
		Assert.assertEquals(true, values.get(InteractionCueConfig.TOOLTIP_BORDER_KEY));
		Assert.assertEquals(
			new Color(0x73, 0x6B, 0x5A, 0xFF),
			values.get(InteractionCueConfig.TOOLTIP_BORDER_COLOR_KEY));
		Assert.assertEquals(0.8,
			((Number) values.get(InteractionCueConfig.TOOLTIP_BORDER_WIDTH_KEY))
				.doubleValue(),
			0.0);
	}

	@Test
	public void ruinedHeirTooltipPresetHasRequestedBareIconLayout()
	{
		Map<String, Object> values =
			InteractionCueTooltipPreset.RUINED_HEIR.getSettings();

		Assert.assertEquals(
			InteractionCueTooltipPreset.FULL.getSettings().keySet(),
			values.keySet());
		Assert.assertEquals(4, values.get(InteractionCueConfig.X_OFFSET_KEY));
		Assert.assertEquals(32, values.get(InteractionCueConfig.Y_OFFSET_KEY));
		Assert.assertEquals(24, values.get(InteractionCueConfig.ICON_SIZE_KEY));
		Assert.assertSame(
			InteractionCueTooltipOrigin.CENTER_LEFT,
			values.get(InteractionCueConfig.TOOLTIP_ORIGIN_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.BACKGROUND_PADDING_TOP_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.BACKGROUND_PADDING_RIGHT_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.BACKGROUND_PADDING_BOTTOM_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.BACKGROUND_PADDING_LEFT_KEY));
		Assert.assertEquals(
			"{source_icon}",
			values.get(InteractionCueConfig.LABEL_FORMAT_KEY));
		Assert.assertEquals(false, values.get(InteractionCueConfig.BACKGROUND_KEY));
		Assert.assertEquals(false, values.get(InteractionCueConfig.TOOLTIP_BORDER_KEY));
	}

	@Test
	public void legacyMarkerPresetRetainsFormerAppearance()
	{
		Map<String, Object> values = InteractionCueMarkerPreset.LEGACY.getSettings();

		Assert.assertEquals(15, values.size());
		Assert.assertSame(
			InteractionCueMarkerOrigin.CENTER,
			values.get(InteractionCueConfig.PENDING_MARKER_ORIGIN_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.PENDING_MARKER_Y_OFFSET_KEY));
		Assert.assertEquals(2,
			values.get(InteractionCueConfig.PENDING_MARKER_CORNER_KEY));
		Assert.assertEquals(3,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_TOP_KEY));
		Assert.assertEquals(3,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_RIGHT_KEY));
		Assert.assertEquals(3,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_BOTTOM_KEY));
		Assert.assertEquals(3,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_LEFT_KEY));
		Assert.assertEquals(true,
			values.get(InteractionCueConfig.PENDING_MARKER_BORDER_KEY));
		Assert.assertEquals(
			new Color(0xFF, 0xFF, 0x0C, 0xFF),
			values.get(InteractionCueConfig.PENDING_MARKER_COLOR_KEY));
		Assert.assertEquals(25,
			values.get(InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY));
		Assert.assertEquals(true,
			values.get(InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY));
	}

	@Test
	public void ruinedHeirMarkerPresetHasRequestedMinimalAppearance()
	{
		Map<String, Object> values =
			InteractionCueMarkerPreset.RUINED_HEIR.getSettings();

		Assert.assertEquals(
			InteractionCueMarkerPreset.LEGACY.getSettings().keySet(),
			values.keySet());
		Assert.assertSame(
			InteractionCueMarkerOrigin.CENTER,
			values.get(InteractionCueConfig.PENDING_MARKER_ORIGIN_KEY));
		Assert.assertEquals(7,
			values.get(InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY));
		Assert.assertEquals(5,
			values.get(InteractionCueConfig.PENDING_MARKER_Y_OFFSET_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.PENDING_MARKER_CORNER_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_TOP_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_RIGHT_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_BOTTOM_KEY));
		Assert.assertEquals(0,
			values.get(InteractionCueConfig.PENDING_MARKER_PADDING_LEFT_KEY));
		Assert.assertEquals(false,
			values.get(InteractionCueConfig.PENDING_MARKER_BORDER_KEY));
		Assert.assertEquals(true,
			values.get(InteractionCueConfig.PENDING_MARKER_ICON_KEY));
		Assert.assertEquals(26,
			values.get(InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY));
		Assert.assertEquals(false,
			values.get(InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY));
	}

	@Test
	public void presetMapsAreImmutableAndHaveStableDisplayNames()
	{
		assertImmutable(InteractionCueTooltipPreset.FULL.getSettings());
		assertImmutable(InteractionCueTooltipPreset.RUINED_HEIR.getSettings());
		assertImmutable(InteractionCueMarkerPreset.RUINED_HEIR.getSettings());
		assertImmutable(InteractionCueMarkerPreset.LEGACY.getSettings());

		Assert.assertEquals("Custom", InteractionCueTooltipPreset.CUSTOM.toString());
		Assert.assertEquals("Full", InteractionCueTooltipPreset.FULL.toString());
		Assert.assertEquals(
			"Ruined Heir's",
			InteractionCueTooltipPreset.RUINED_HEIR.toString());
		Assert.assertEquals(
			"Ruined Heir's",
			InteractionCueMarkerPreset.RUINED_HEIR.toString());
		Assert.assertEquals("Legacy", InteractionCueMarkerPreset.LEGACY.toString());
	}

	@Test
	public void presetValuesAcceptStoredNamesAndDisplayNames()
	{
		Assert.assertSame(
			InteractionCueTooltipPreset.RUINED_HEIR,
			InteractionCuePlugin.tooltipPresetFromValue("RUINED_HEIR")
		);
		Assert.assertSame(
			InteractionCueTooltipPreset.RUINED_HEIR,
			InteractionCuePlugin.tooltipPresetFromValue("Ruined Heir's")
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.RUINED_HEIR,
			InteractionCuePlugin.markerPresetFromValue("RUINED_HEIR")
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.RUINED_HEIR,
			InteractionCuePlugin.markerPresetFromValue("Ruined Heir's")
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			InteractionCuePlugin.markerPresetFromValue("LEGACY")
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			InteractionCuePlugin.markerPresetFromValue("DEFAULT")
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			InteractionCuePlugin.markerPresetFromValue("Default")
		);
		Assert.assertNull(InteractionCuePlugin.tooltipPresetFromValue("Unknown"));
	}

	@Test
	public void disabledPresetSelectionAppliesWhenPriorSettingsAreUnchanged()
	{
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.APPLY_SELECTED,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				InteractionCueTooltipPreset.FULL,
				InteractionCueTooltipPreset.FULL.getSettings(),
				InteractionCuePresetReconciliation.fingerprint(
					InteractionCueTooltipPreset.FULL.getSettings()
				)
			)
		);
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.APPLY_SELECTED,
			InteractionCuePresetReconciliation.marker(
				InteractionCueMarkerPreset.RUINED_HEIR,
				InteractionCueMarkerPreset.LEGACY,
				InteractionCueMarkerPreset.LEGACY.getSettings(),
				InteractionCuePresetReconciliation.fingerprint(
					InteractionCueMarkerPreset.LEGACY.getSettings()
				)
			)
		);
	}

	@Test
	public void disabledAppearanceEditSelectsCustom()
	{
		Map<String, Object> edited = new LinkedHashMap<>(
			InteractionCueTooltipPreset.FULL.getSettings()
		);
		edited.put(InteractionCueConfig.X_OFFSET_KEY, 23);

		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.SELECT_CUSTOM,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.FULL,
				InteractionCueTooltipPreset.FULL,
				edited,
				InteractionCuePresetReconciliation.fingerprint(
					InteractionCueTooltipPreset.FULL.getSettings()
				)
			)
		);
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.SELECT_CUSTOM,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				InteractionCueTooltipPreset.FULL,
				edited,
				InteractionCuePresetReconciliation.fingerprint(
					InteractionCueTooltipPreset.FULL.getSettings()
				)
			)
		);
	}

	@Test
	public void disabledEditAfterCustomPresetSelectionIsPreserved()
	{
		Map<String, Object> custom = new LinkedHashMap<>(
			InteractionCueTooltipPreset.FULL.getSettings()
		);
		custom.put(InteractionCueConfig.X_OFFSET_KEY, 23);
		String appliedSettings =
			InteractionCuePresetReconciliation.fingerprint(custom);

		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.APPLY_SELECTED,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				InteractionCueTooltipPreset.CUSTOM,
				custom,
				appliedSettings
			)
		);

		Map<String, Object> edited = new LinkedHashMap<>(custom);
		edited.put(InteractionCueConfig.Y_OFFSET_KEY, 12);
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.SELECT_CUSTOM,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				InteractionCueTooltipPreset.CUSTOM,
				edited,
				appliedSettings
			)
		);
	}

	@Test
	public void missingPresetHistoryUsesRecognizedSettingsConservatively()
	{
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.APPLY_SELECTED,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				null,
				InteractionCueTooltipPreset.FULL.getSettings(),
				null
			)
		);

		Map<String, Object> custom = new LinkedHashMap<>(
			InteractionCueTooltipPreset.FULL.getSettings()
		);
		custom.put(InteractionCueConfig.X_OFFSET_KEY, 23);
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.SELECT_CUSTOM,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.FULL,
				null,
				custom,
				null
			)
		);
	}

	@Test
	public void legacyAppliedPresetHistoryUsesSettingsConservatively()
	{
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.APPLY_SELECTED,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				InteractionCueTooltipPreset.FULL,
				InteractionCueTooltipPreset.FULL.getSettings(),
				null
			)
		);

		Map<String, Object> edited = new LinkedHashMap<>(
			InteractionCueTooltipPreset.FULL.getSettings()
		);
		edited.put(InteractionCueConfig.X_OFFSET_KEY, 23);
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.SELECT_CUSTOM,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				InteractionCueTooltipPreset.FULL,
				edited,
				null
			)
		);
		Assert.assertSame(
			InteractionCuePresetReconciliation.Action.SELECT_CUSTOM,
			InteractionCuePresetReconciliation.tooltip(
				InteractionCueTooltipPreset.RUINED_HEIR,
				InteractionCueTooltipPreset.CUSTOM,
				InteractionCueTooltipPreset.FULL.getSettings(),
				null
			)
		);
	}

	@Test
	public void settingsFingerprintIsOrderIndependentAndTypeSensitive()
	{
		Map<String, Object> first = new LinkedHashMap<>();
		first.put("color", new Color(1, 2, 3, 4));
		first.put("preset", InteractionCueTooltipPreset.FULL);
		first.put("value", 1);

		Map<String, Object> reordered = new LinkedHashMap<>();
		reordered.put("value", 1);
		reordered.put("preset", InteractionCueTooltipPreset.FULL);
		reordered.put("color", new Color(1, 2, 3, 4));

		Assert.assertEquals(
			InteractionCuePresetReconciliation.fingerprint(first),
			InteractionCuePresetReconciliation.fingerprint(reordered)
		);

		reordered.put("value", "1");
		Assert.assertNotEquals(
			InteractionCuePresetReconciliation.fingerprint(first),
			InteractionCuePresetReconciliation.fingerprint(reordered)
		);
	}

	@Test
	public void controlledEditsSwitchOnlyTheirOwnSectionToCustom()
	{
		Assert.assertTrue(InteractionCuePlugin.shouldSwitchTooltipToCustom(
			false,
			InteractionCueConfig.X_OFFSET_KEY,
			InteractionCueTooltipPreset.RUINED_HEIR
		));
		Assert.assertFalse(InteractionCuePlugin.shouldSwitchTooltipToCustom(
			false,
			InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
			InteractionCueTooltipPreset.RUINED_HEIR
		));
		Assert.assertFalse(InteractionCuePlugin.shouldSwitchTooltipToCustom(
			false,
			InteractionCueConfig.CURSOR_INDICATOR_KEY,
			InteractionCueTooltipPreset.RUINED_HEIR
		));
		Assert.assertTrue(InteractionCuePlugin.shouldSwitchMarkerToCustom(
			false,
			InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
			InteractionCueMarkerPreset.RUINED_HEIR
		));
		Assert.assertTrue(InteractionCuePlugin.shouldSwitchMarkerToCustom(
			false,
			InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY,
			InteractionCueMarkerPreset.LEGACY
		));
		Assert.assertFalse(InteractionCuePlugin.shouldSwitchMarkerToCustom(
			false,
			InteractionCueConfig.X_OFFSET_KEY,
			InteractionCueMarkerPreset.RUINED_HEIR
		));
		Assert.assertFalse(InteractionCuePlugin.shouldSwitchMarkerToCustom(
			true,
			InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
			InteractionCueMarkerPreset.LEGACY
		));
		Assert.assertTrue(
			InteractionCuePlugin.shouldRecordTooltipCustomSettings(
				false,
				InteractionCueConfig.X_OFFSET_KEY,
				InteractionCueTooltipPreset.CUSTOM
			)
		);
		Assert.assertFalse(
			InteractionCuePlugin.shouldRecordTooltipCustomSettings(
				false,
				InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
				InteractionCueTooltipPreset.CUSTOM
			)
		);
		Assert.assertTrue(
			InteractionCuePlugin.shouldRecordMarkerCustomSettings(
				false,
				InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
				InteractionCueMarkerPreset.CUSTOM
			)
		);
		Assert.assertFalse(
			InteractionCuePlugin.shouldRecordMarkerCustomSettings(
				true,
				InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
				InteractionCueMarkerPreset.CUSTOM
			)
		);
	}

	private static void assertImmutable(Map<String, Object> values)
	{
		try
		{
			values.put("unexpected", true);
			Assert.fail("Expected an immutable preset map");
		}
		catch (UnsupportedOperationException expected)
		{
			// Expected.
		}
	}
}
