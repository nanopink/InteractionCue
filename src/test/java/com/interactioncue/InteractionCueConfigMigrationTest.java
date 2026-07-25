package com.interactioncue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCueConfigMigrationTest
{
	@Test
	public void copiesLegacyValuesWithoutRemovingSourceAndSeedsPresetSettings()
	{
		FakeStore store = new FakeStore();
		store.put(InteractionCueConfigMigration.LEGACY_GROUP, "labelFormat", "{tool}");
		store.put(InteractionCueConfigMigration.LEGACY_GROUP, "emptyValue", "");

		InteractionCueConfigMigration.migrate(store);

		Assert.assertEquals("{tool}", store.get(InteractionCueConfig.GROUP, "labelFormat"));
		Assert.assertEquals("", store.get(InteractionCueConfig.GROUP, "emptyValue"));
		Assert.assertEquals(
			"{tool}",
			store.get(InteractionCueConfigMigration.LEGACY_GROUP, "labelFormat")
		);
		Assert.assertEquals(
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY,
			store.writes.get(store.writes.size() - 1)
		);
		Assert.assertSame(
			InteractionCueTooltipPreset.FULL,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.TOOLTIP_PRESET_KEY
			)
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY
			)
		);
	}

	@Test
	public void keepsCurrentValuesAndRunsOnlyOnce()
	{
		FakeStore store = new FakeStore();
		store.put(InteractionCueConfigMigration.LEGACY_GROUP, "labelFormat", "{tool}");
		store.put(InteractionCueConfig.GROUP, "labelFormat", "{source}");

		InteractionCueConfigMigration.migrate(store);
		int writesAfterFirstRun = store.writes.size();
		store.put(InteractionCueConfigMigration.LEGACY_GROUP, "labelFormat", "{action}");
		InteractionCueConfigMigration.migrate(store);

		Assert.assertEquals("{source}", store.get(InteractionCueConfig.GROUP, "labelFormat"));
		Assert.assertEquals(writesAfterFirstRun, store.writes.size());
	}

	@Test
	public void freshProfileOnlyRecordsPresetWorkflowVersion()
	{
		FakeStore store = new FakeStore();

		InteractionCueConfigMigration.migrate(store);

		Assert.assertEquals(1, store.writes.size());
		Assert.assertEquals(
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY,
			store.writes.get(0)
		);
		Assert.assertNull(store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfigMigration.MIGRATION_KEY
		));
		Assert.assertNull(store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY
		));
		Assert.assertNull(store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.MARKER_PRESET_KEY
		));
		for (String key : InteractionCueTooltipPreset.FULL.getSettings().keySet())
		{
			Assert.assertNull(store.get(InteractionCueConfig.GROUP, key));
		}
		for (String key : InteractionCueMarkerPreset.RUINED_HEIR.getSettings().keySet())
		{
			Assert.assertNull(store.get(InteractionCueConfig.GROUP, key));
		}
	}

	@Test
	public void seedsMissingPresetValuesForPartialExistingConfiguration()
	{
		FakeStore store = new FakeStore();
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.CURSOR_INDICATOR_KEY,
			"false"
		);
		store.put(InteractionCueConfig.GROUP, InteractionCueConfig.X_OFFSET_KEY, "91");
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.PENDING_MARKER_BORDER_KEY,
			"false"
		);

		InteractionCueConfigMigration.migrate(store);

		Assert.assertEquals(
			"false",
			store.get(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.CURSOR_INDICATOR_KEY
			)
		);
		Assert.assertEquals(
			"91",
			store.get(InteractionCueConfig.GROUP, InteractionCueConfig.X_OFFSET_KEY)
		);
		Assert.assertEquals(
			"false",
			store.get(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.PENDING_MARKER_BORDER_KEY
			)
		);
		assertSeededSettings(
			store,
			InteractionCueTooltipPreset.FULL.getSettings(),
			InteractionCueConfig.X_OFFSET_KEY
		);
		assertSeededSettings(
			store,
			InteractionCueMarkerPreset.LEGACY.getSettings(),
			InteractionCueConfig.PENDING_MARKER_BORDER_KEY
		);
		Assert.assertSame(
			InteractionCueTooltipPreset.FULL,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.TOOLTIP_PRESET_KEY
			)
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY
			)
		);
	}

	@Test
	public void convertsVersionTwoDefaultMarkerToLegacy()
	{
		FakeStore store = new FakeStore();
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY,
			"2"
		);
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.MARKER_PRESET_KEY,
			"DEFAULT"
		);
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY,
			"41"
		);

		InteractionCueConfigMigration.migrate(store);

		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY
			)
		);
		Assert.assertEquals(
			"41",
			store.get(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY
			)
		);
		assertSeededSettings(
			store,
			InteractionCueMarkerPreset.LEGACY.getSettings(),
			InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY
		);
		Assert.assertEquals(
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_VERSION,
			store.get(
				InteractionCueConfig.GROUP,
				InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY
			)
		);
	}

	@Test
	public void versionTwoCustomMarkerKeepsOverridesAndFormerDefaults()
	{
		FakeStore store = new FakeStore();
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY,
			"2"
		);
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.MARKER_PRESET_KEY,
			InteractionCueMarkerPreset.CUSTOM
		);
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY,
			"37"
		);

		InteractionCueConfigMigration.migrate(store);

		Assert.assertSame(
			InteractionCueMarkerPreset.CUSTOM,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY
			)
		);
		Assert.assertEquals(
			"37",
			store.get(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY
			)
		);
		assertSeededSettings(
			store,
			InteractionCueMarkerPreset.LEGACY.getSettings(),
			InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY
		);
	}

	@Test
	public void versionTwoImplicitDefaultMarkerBecomesLegacy()
	{
		FakeStore store = new FakeStore();
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY,
			"2"
		);

		InteractionCueConfigMigration.migrate(store);

		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY
			)
		);
		assertSeededSettings(
			store,
			InteractionCueMarkerPreset.LEGACY.getSettings(),
			""
		);
	}

	@Test
	public void preservesExistingPresetSelectionsDuringMigration()
	{
		FakeStore store = new FakeStore();
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.CURSOR_INDICATOR_KEY,
			"true"
		);
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY,
			InteractionCueTooltipPreset.CUSTOM
		);
		store.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.MARKER_PRESET_KEY,
			InteractionCueMarkerPreset.CUSTOM
		);

		InteractionCueConfigMigration.migrate(store);

		Assert.assertSame(
			InteractionCueTooltipPreset.CUSTOM,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.TOOLTIP_PRESET_KEY
			)
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.CUSTOM,
			store.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY
			)
		);
	}

	@Test
	public void reconstructsSettingsWhenOnlyNamedPresetWasStored()
	{
		FakeStore full = new FakeStore();
		full.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY,
			InteractionCueTooltipPreset.FULL
		);
		InteractionCueConfigMigration.migrate(full);
		assertSeededSettings(
			full,
			InteractionCueTooltipPreset.FULL.getSettings(),
			""
		);

		FakeStore ruined = new FakeStore();
		ruined.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY,
			InteractionCueTooltipPreset.RUINED_HEIR
		);
		InteractionCueConfigMigration.migrate(ruined);
		assertSeededSettings(
			ruined,
			InteractionCueTooltipPreset.RUINED_HEIR.getSettings(),
			""
		);
	}

	@Test
	public void updatesPreviousRuinedHeirGeometryWithoutOverwritingCustomValues()
	{
		FakeStore oldRuined = new FakeStore();
		oldRuined.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY,
			"1"
		);
		oldRuined.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY,
			InteractionCueTooltipPreset.RUINED_HEIR
		);
		oldRuined.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.X_OFFSET_KEY,
			"-4"
		);
		oldRuined.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.Y_OFFSET_KEY,
			"33"
		);
		oldRuined.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_ORIGIN_KEY,
			InteractionCueTooltipOrigin.CENTER
		);

		InteractionCueConfigMigration.migrate(oldRuined);

		Assert.assertEquals(
			4,
			oldRuined.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.X_OFFSET_KEY
			)
		);
		Assert.assertEquals(
			32,
			oldRuined.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.Y_OFFSET_KEY
			)
		);
		Assert.assertSame(
			InteractionCueTooltipOrigin.CENTER_LEFT,
			oldRuined.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.TOOLTIP_ORIGIN_KEY
			)
		);
		Assert.assertSame(
			InteractionCueMarkerPreset.LEGACY,
			oldRuined.value(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY
			)
		);

		FakeStore custom = new FakeStore();
		custom.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfigMigration.PRESET_WORKFLOW_MIGRATION_KEY,
			"1"
		);
		custom.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY,
			InteractionCueTooltipPreset.RUINED_HEIR
		);
		custom.put(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.X_OFFSET_KEY,
			"9"
		);

		InteractionCueConfigMigration.migrate(custom);

		Assert.assertEquals(
			"9",
			custom.get(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.X_OFFSET_KEY
			)
		);
	}

	private static void assertSeededSettings(
		FakeStore store,
		Map<String, Object> settings,
		String overriddenKey)
	{
		for (Map.Entry<String, Object> setting : settings.entrySet())
		{
			if (!overriddenKey.equals(setting.getKey()))
			{
				Assert.assertEquals(
					setting.getValue(),
					store.value(InteractionCueConfig.GROUP, setting.getKey())
				);
			}
		}
	}

	private static final class FakeStore implements InteractionCueConfigMigration.Store
	{
		private final Map<String, Object> values = new LinkedHashMap<>();
		private final List<String> writes = new ArrayList<>();

		@Override
		public String get(String group, String key)
		{
			Object value = value(group, key);
			if (value == null)
			{
				return null;
			}
			if (value instanceof Enum)
			{
				return ((Enum<?>) value).name();
			}
			return String.valueOf(value);
		}

		@Override
		public Iterable<String> keys(String prefix)
		{
			return values.keySet().stream()
				.filter(key -> key.startsWith(prefix))
				.collect(Collectors.toList());
		}

		@Override
		public void set(String group, String key, Object value)
		{
			values.put(group + "." + key, value);
			writes.add(key);
		}

		private void put(String group, String key, Object value)
		{
			values.put(group + "." + key, value);
		}

		private Object value(String group, String key)
		{
			return values.get(group + "." + key);
		}
	}
}
