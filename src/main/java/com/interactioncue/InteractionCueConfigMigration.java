package com.interactioncue;

import java.util.Map;
import net.runelite.client.config.ConfigManager;

final class InteractionCueConfigMigration
{
	static final String LEGACY_GROUP = "interaction-cue";
	static final String MIGRATION_KEY = "legacyConfigMigration";
	static final String MIGRATION_VERSION = "1";
	static final String PRESET_WORKFLOW_MIGRATION_KEY = "presetWorkflowMigration";
	static final String PRESET_WORKFLOW_MIGRATION_VERSION = "3";
	private static final String TOOLTIP_GEOMETRY_MIGRATION_VERSION = "1";
	private static final String LEGACY_MARKER_MIGRATION_VERSION = "2";

	interface Store
	{
		String get(String group, String key);

		Iterable<String> keys(String prefix);

		void set(String group, String key, Object value);
	}

	private InteractionCueConfigMigration()
	{
	}

	static void migrate(ConfigManager configManager)
	{
		migrate(new Store()
		{
			@Override
			public String get(String group, String key)
			{
				return configManager.getConfiguration(group, key);
			}

			@Override
			public Iterable<String> keys(String prefix)
			{
				return configManager.getConfigurationKeys(prefix);
			}

			@Override
			public void set(String group, String key, Object value)
			{
				configManager.setConfiguration(group, key, value);
			}
		});
	}

	static void migrate(Store store)
	{
		migrateLegacyGroup(store);
		migratePresetWorkflow(store);
	}

	private static void migrateLegacyGroup(Store store)
	{
		if (MIGRATION_VERSION.equals(store.get(InteractionCueConfig.GROUP, MIGRATION_KEY)))
		{
			return;
		}

		String prefix = LEGACY_GROUP + ".";
		boolean foundLegacyConfig = false;
		for (String wholeKey : store.keys(prefix))
		{
			String key = wholeKey.substring(prefix.length());
			String value = store.get(LEGACY_GROUP, key);
			if (value == null)
			{
				continue;
			}

			foundLegacyConfig = true;
			if (store.get(InteractionCueConfig.GROUP, key) == null)
			{
				store.set(InteractionCueConfig.GROUP, key, value);
			}
		}

		if (foundLegacyConfig)
		{
			store.set(InteractionCueConfig.GROUP, MIGRATION_KEY, MIGRATION_VERSION);
		}
	}

	private static void migratePresetWorkflow(Store store)
	{
		String version = store.get(
			InteractionCueConfig.GROUP,
			PRESET_WORKFLOW_MIGRATION_KEY
		);
		if (PRESET_WORKFLOW_MIGRATION_VERSION.equals(version))
		{
			return;
		}
		if (TOOLTIP_GEOMETRY_MIGRATION_VERSION.equals(version))
		{
			migrateRuinedHeirGeometry(store);
			migrateLegacyMarkerPreset(store);
		}
		else if (LEGACY_MARKER_MIGRATION_VERSION.equals(version))
		{
			migrateLegacyMarkerPreset(store);
		}
		else if (hasPrePresetConfiguration(store))
		{
			seedMissingSettings(
				store,
				tooltipSeedSettings(store)
			);
			seedMissingSettings(
				store,
				InteractionCueMarkerPreset.LEGACY.getSettings()
			);
			setIfMissing(
				store,
				InteractionCueConfig.TOOLTIP_PRESET_KEY,
				InteractionCueTooltipPreset.FULL
			);
			selectLegacyMarkerPreset(store);
		}

		store.set(
			InteractionCueConfig.GROUP,
			PRESET_WORKFLOW_MIGRATION_KEY,
			PRESET_WORKFLOW_MIGRATION_VERSION
		);
	}

	private static void migrateLegacyMarkerPreset(Store store)
	{
		seedMissingSettings(
			store,
			InteractionCueMarkerPreset.LEGACY.getSettings()
		);
		selectLegacyMarkerPreset(store);
	}

	private static void selectLegacyMarkerPreset(Store store)
	{
		String selected = store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.MARKER_PRESET_KEY
		);
		if (selected == null
			|| "DEFAULT".equals(selected)
			|| "Default".equals(selected))
		{
			store.set(
				InteractionCueConfig.GROUP,
				InteractionCueConfig.MARKER_PRESET_KEY,
				InteractionCueMarkerPreset.LEGACY
			);
		}
	}

	private static void migrateRuinedHeirGeometry(Store store)
	{
		String selected = store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY
		);
		if (!InteractionCueTooltipPreset.RUINED_HEIR.name().equals(selected)
			&& !InteractionCueTooltipPreset.RUINED_HEIR.toString().equals(selected))
		{
			return;
		}

		String xOffset = store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.X_OFFSET_KEY
		);
		String yOffset = store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.Y_OFFSET_KEY
		);
		String origin = store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_ORIGIN_KEY
		);
		if (!matchesOldOrMissing(xOffset, "-4")
			|| !matchesOldOrMissing(yOffset, "33")
			|| !matchesOldOrMissing(origin, InteractionCueTooltipOrigin.CENTER.name()))
		{
			return;
		}

		store.set(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.X_OFFSET_KEY,
			4
		);
		store.set(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.Y_OFFSET_KEY,
			32
		);
		store.set(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_ORIGIN_KEY,
			InteractionCueTooltipOrigin.CENTER_LEFT
		);
	}

	private static boolean matchesOldOrMissing(String value, String oldValue)
	{
		return value == null || oldValue.equals(value);
	}

	private static boolean hasPrePresetConfiguration(Store store)
	{
		String prefix = InteractionCueConfig.GROUP + ".";
		for (String wholeKey : store.keys(prefix))
		{
			String key = wholeKey.substring(prefix.length());
			if (!isMigrationKey(key))
			{
				return true;
			}
		}

		return false;
	}

	private static boolean isMigrationKey(String key)
	{
		return MIGRATION_KEY.equals(key)
			|| PRESET_WORKFLOW_MIGRATION_KEY.equals(key);
	}

	private static Map<String, Object> tooltipSeedSettings(Store store)
	{
		String selected = store.get(
			InteractionCueConfig.GROUP,
			InteractionCueConfig.TOOLTIP_PRESET_KEY
		);
		if (InteractionCueTooltipPreset.RUINED_HEIR.name().equals(selected)
			|| InteractionCueTooltipPreset.RUINED_HEIR.toString().equals(selected))
		{
			return InteractionCueTooltipPreset.RUINED_HEIR.getSettings();
		}
		return InteractionCueTooltipPreset.FULL.getSettings();
	}

	private static void seedMissingSettings(
		Store store,
		Map<String, Object> settings)
	{
		for (Map.Entry<String, Object> setting : settings.entrySet())
		{
			setIfMissing(store, setting.getKey(), setting.getValue());
		}
	}

	private static void setIfMissing(Store store, String key, Object value)
	{
		if (store.get(InteractionCueConfig.GROUP, key) == null)
		{
			store.set(InteractionCueConfig.GROUP, key, value);
		}
	}
}
