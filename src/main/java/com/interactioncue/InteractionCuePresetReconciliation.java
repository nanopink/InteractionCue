package com.interactioncue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

final class InteractionCuePresetReconciliation
{
	enum Action
	{
		NONE,
		APPLY_SELECTED,
		SELECT_CUSTOM
	}

	private InteractionCuePresetReconciliation()
	{
	}

	static Action tooltip(
		InteractionCueTooltipPreset selected,
		InteractionCueTooltipPreset applied,
		Map<String, Object> currentSettings,
		String appliedSettings)
	{
		return decide(
			selected,
			InteractionCueTooltipPreset.CUSTOM,
			applied,
			currentSettings,
			appliedSettings,
			InteractionCueTooltipPreset.values(),
			InteractionCueTooltipPreset::getSettings
		);
	}

	static Action marker(
		InteractionCueMarkerPreset selected,
		InteractionCueMarkerPreset applied,
		Map<String, Object> currentSettings,
		String appliedSettings)
	{
		return decide(
			selected,
			InteractionCueMarkerPreset.CUSTOM,
			applied,
			currentSettings,
			appliedSettings,
			InteractionCueMarkerPreset.values(),
			InteractionCueMarkerPreset::getSettings
		);
	}

	static String fingerprint(Map<String, Object> settings)
	{
		List<String> keys = new ArrayList<>(settings.keySet());
		Collections.sort(keys);
		StringBuilder value = new StringBuilder();
		for (String key : keys)
		{
			append(value, key);
			append(value, stableValue(settings.get(key)));
		}
		return value.toString();
	}

	private static <T extends Enum<T>> Action decide(
		T selected,
		T custom,
		T applied,
		Map<String, Object> currentSettings,
		String appliedSettings,
		T[] presets,
		Function<T, Map<String, Object>> settings)
	{
		if (selected == null
			|| selected == custom
			|| Objects.equals(settings.apply(selected), currentSettings))
		{
			return Action.NONE;
		}

		if (applied != null)
		{
			if (selected == applied)
			{
				return Action.SELECT_CUSTOM;
			}

			boolean priorSettingsUnchanged = appliedSettings == null
				? applied != custom
					&& Objects.equals(settings.apply(applied), currentSettings)
				: Objects.equals(
					appliedSettings,
					fingerprint(currentSettings)
				);
			return priorSettingsUnchanged
				? Action.APPLY_SELECTED
				: Action.SELECT_CUSTOM;
		}

		for (T preset : presets)
		{
			if (preset != custom
				&& Objects.equals(settings.apply(preset), currentSettings))
			{
				return Action.APPLY_SELECTED;
			}
		}
		return Action.SELECT_CUSTOM;
	}

	private static String stableValue(Object value)
	{
		if (value == null)
		{
			return "null";
		}
		if (value instanceof Color)
		{
			return "color:" + ((Color) value).getRGB();
		}
		if (value instanceof Enum)
		{
			Enum<?> enumValue = (Enum<?>) value;
			return enumValue.getDeclaringClass().getName()
				+ ":"
				+ enumValue.name();
		}
		return value.getClass().getName() + ":" + value;
	}

	private static void append(StringBuilder target, String value)
	{
		target.append(value.length()).append(':').append(value);
	}
}
