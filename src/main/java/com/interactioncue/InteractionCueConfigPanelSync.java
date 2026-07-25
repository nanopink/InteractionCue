package com.interactioncue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.util.ColorUtil;

final class InteractionCueConfigPanelSync
{
	private static final String PLUGIN_TITLE = "Interaction Cue";
	private static final String TOOLTIP_SECTION = "Tooltip";
	private static final String MARKER_SECTION = "Marker";
	private static final String[] REQUIRED_SECTIONS = {
		TOOLTIP_SECTION,
		MARKER_SECTION
	};
	private static final Map<String, ConfigField> FIELDS = createFields();

	private InteractionCueConfigPanelSync()
	{
	}

	static void refreshOpenPanel(Map<String, Object> values)
	{
		if (values == null || values.isEmpty())
		{
			return;
		}

		Map<String, Object> pendingValues = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : values.entrySet())
		{
			if (FIELDS.containsKey(entry.getKey()))
			{
				pendingValues.put(entry.getKey(), entry.getValue());
			}
		}
		if (pendingValues.isEmpty())
		{
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			for (Window window : Window.getWindows())
			{
				synchronize(window, pendingValues);
			}
		});
	}

	static int synchronize(Component root, Map<String, Object> values)
	{
		if (root == null || values == null || values.isEmpty())
		{
			return 0;
		}

		Container configPanel = findConfigPanel(root);
		if (configPanel == null)
		{
			return 0;
		}

		Map<String, Container> sectionPanels = new HashMap<>();
		int synchronizedFields = 0;
		for (Map.Entry<String, Object> entry : values.entrySet())
		{
			ConfigField field = FIELDS.get(entry.getKey());
			if (field == null)
			{
				continue;
			}

			Container sectionPanel = sectionPanels.get(field.section);
			if (sectionPanel == null)
			{
				sectionPanel = findSectionPanel(configPanel, field.section);
				if (sectionPanel == null)
				{
					continue;
				}
				sectionPanels.put(field.section, sectionPanel);
			}

			Container row = findRow(sectionPanel, field.name);
			if (row != null && updateControl(row, entry.getValue(), field))
			{
				synchronizedFields++;
			}
		}
		return synchronizedFields;
	}

	static boolean hasRegisteredField(String key)
	{
		return FIELDS.containsKey(key);
	}

	private static Container findConfigPanel(Component component)
	{
		if (component instanceof JLabel
			&& PLUGIN_TITLE.equals(((JLabel) component).getText()))
		{
			for (Container ancestor = component.getParent();
				ancestor != null;
				ancestor = ancestor.getParent())
			{
				if (containsAllSectionLabels(ancestor))
				{
					return ancestor;
				}
			}
		}

		if (!(component instanceof Container))
		{
			return null;
		}
		for (Component child : ((Container) component).getComponents())
		{
			Container configPanel = findConfigPanel(child);
			if (configPanel != null)
			{
				return configPanel;
			}
		}
		return null;
	}

	private static boolean containsAllSectionLabels(Container container)
	{
		for (String section : REQUIRED_SECTIONS)
		{
			if (findLabel(container, section) == null)
			{
				return false;
			}
		}
		return true;
	}

	private static Container findSectionPanel(Container configPanel, String sectionName)
	{
		JLabel sectionLabel = findLabel(configPanel, sectionName);
		if (sectionLabel == null || sectionLabel.getParent() == null)
		{
			return null;
		}
		return sectionLabel.getParent().getParent();
	}

	private static JLabel findLabel(Component component, String text)
	{
		if (component instanceof JLabel && text.equals(((JLabel) component).getText()))
		{
			return (JLabel) component;
		}
		if (!(component instanceof Container))
		{
			return null;
		}
		for (Component child : ((Container) component).getComponents())
		{
			JLabel label = findLabel(child, text);
			if (label != null)
			{
				return label;
			}
		}
		return null;
	}

	private static Container findRow(Component component, String name)
	{
		if (!(component instanceof Container))
		{
			return null;
		}

		Container container = (Container) component;
		for (Component child : container.getComponents())
		{
			if (child instanceof JLabel && name.equals(((JLabel) child).getText()))
			{
				return container;
			}
		}
		for (Component child : container.getComponents())
		{
			Container row = findRow(child, name);
			if (row != null)
			{
				return row;
			}
		}
		return null;
	}

	private static boolean updateControl(Container row, Object value, ConfigField field)
	{
		if (value instanceof Boolean)
		{
			JCheckBox checkBox = (JCheckBox) findDescendant(
				row,
				child -> child instanceof JCheckBox);
			if (checkBox == null)
			{
				return false;
			}
			checkBox.setSelected((Boolean) value);
			return true;
		}

		if (value instanceof Number)
		{
			JSpinner spinner = (JSpinner) findDescendant(
				row,
				child -> child instanceof JSpinner);
			if (spinner == null)
			{
				return false;
			}
			Object spinnerValue = spinnerValue((Number) value, spinner.getValue());
			if (!Objects.equals(spinner.getValue(), spinnerValue))
			{
				spinner.setValue(spinnerValue);
			}
			return true;
		}

		if (value instanceof Color)
		{
			ColorJButton colorButton = (ColorJButton) findDescendant(
				row,
				child -> child instanceof ColorJButton);
			if (colorButton == null)
			{
				return false;
			}
			Color color = (Color) value;
			colorButton.setColor(color);
			String colorHex = field.alpha
				? ColorUtil.colorToAlphaHexCode(color)
				: ColorUtil.colorToHexCode(color);
			colorButton.setText("#" + colorHex.toUpperCase(Locale.ROOT));
			return true;
		}

		JComboBox<?> comboBox = (JComboBox<?>) findDescendant(
			row,
			child -> child instanceof JComboBox);
		if (comboBox != null)
		{
			Object item = findComboBoxItem(comboBox, value);
			if (item == null)
			{
				return false;
			}
			if (!Objects.equals(comboBox.getSelectedItem(), item))
			{
				comboBox.setSelectedItem(item);
			}
			return true;
		}

		if (value instanceof String)
		{
			JTextComponent text = (JTextComponent) findDescendant(
				row,
				child -> child instanceof JTextComponent);
			if (text != null)
			{
				text.setText((String) value);
				return true;
			}
		}
		return false;
	}

	private static Object spinnerValue(Number value, Object currentValue)
	{
		if (currentValue instanceof Integer)
		{
			return value.intValue();
		}
		if (currentValue instanceof Long)
		{
			return value.longValue();
		}
		if (currentValue instanceof Float)
		{
			return value.floatValue();
		}
		return value.doubleValue();
	}

	private static Object findComboBoxItem(JComboBox<?> comboBox, Object value)
	{
		String expected = value instanceof Enum
			? ((Enum<?>) value).name()
			: String.valueOf(value);
		for (int index = 0; index < comboBox.getItemCount(); index++)
		{
			Object item = comboBox.getItemAt(index);
			if (Objects.equals(item, value)
				|| item instanceof Enum && expected.equals(((Enum<?>) item).name()))
			{
				return item;
			}
		}
		return null;
	}

	private static Component findDescendant(
		Component component,
		Predicate<Component> predicate)
	{
		if (predicate.test(component))
		{
			return component;
		}
		if (!(component instanceof Container))
		{
			return null;
		}
		for (Component child : ((Container) component).getComponents())
		{
			Component match = findDescendant(child, predicate);
			if (match != null)
			{
				return match;
			}
		}
		return null;
	}

	private static Map<String, ConfigField> createFields()
	{
		Map<String, ConfigField> fields = new HashMap<>();

		field(fields, TOOLTIP_SECTION, "Preset", InteractionCueConfig.TOOLTIP_PRESET_KEY);
		field(fields, TOOLTIP_SECTION, "X offset", InteractionCueConfig.X_OFFSET_KEY);
		field(fields, TOOLTIP_SECTION, "Y offset", InteractionCueConfig.Y_OFFSET_KEY);
		field(fields, TOOLTIP_SECTION, "Icon size", InteractionCueConfig.ICON_SIZE_KEY);
		field(fields, TOOLTIP_SECTION, "Origin", InteractionCueConfig.TOOLTIP_ORIGIN_KEY);
		field(fields, TOOLTIP_SECTION, "Padding top",
			InteractionCueConfig.BACKGROUND_PADDING_TOP_KEY);
		field(fields, TOOLTIP_SECTION, "Padding right",
			InteractionCueConfig.BACKGROUND_PADDING_RIGHT_KEY);
		field(fields, TOOLTIP_SECTION, "Padding bottom",
			InteractionCueConfig.BACKGROUND_PADDING_BOTTOM_KEY);
		field(fields, TOOLTIP_SECTION, "Padding left",
			InteractionCueConfig.BACKGROUND_PADDING_LEFT_KEY);
		field(fields, TOOLTIP_SECTION, "Format", InteractionCueConfig.LABEL_FORMAT_KEY);
		field(fields, TOOLTIP_SECTION, "Background", InteractionCueConfig.BACKGROUND_KEY);
		alphaField(fields, TOOLTIP_SECTION, "Background color",
			InteractionCueConfig.BACKGROUND_COLOR_KEY);
		field(fields, TOOLTIP_SECTION, "Background corner",
			InteractionCueConfig.BACKGROUND_CORNER_KEY);
		field(fields, TOOLTIP_SECTION, "Border", InteractionCueConfig.TOOLTIP_BORDER_KEY);
		alphaField(fields, TOOLTIP_SECTION, "Border color",
			InteractionCueConfig.TOOLTIP_BORDER_COLOR_KEY);
		field(fields, TOOLTIP_SECTION, "Border width",
			InteractionCueConfig.TOOLTIP_BORDER_WIDTH_KEY);

		field(fields, MARKER_SECTION, "Preset", InteractionCueConfig.MARKER_PRESET_KEY);
		field(fields, MARKER_SECTION, "Origin",
			InteractionCueConfig.PENDING_MARKER_ORIGIN_KEY);
		field(fields, MARKER_SECTION, "X offset",
			InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY);
		field(fields, MARKER_SECTION, "Y offset",
			InteractionCueConfig.PENDING_MARKER_Y_OFFSET_KEY);
		field(fields, MARKER_SECTION, "Icon",
			InteractionCueConfig.PENDING_MARKER_ICON_KEY);
		field(fields, MARKER_SECTION, "Icon size",
			InteractionCueConfig.PENDING_MARKER_ICON_SIZE_KEY);
		field(fields, MARKER_SECTION, "Background",
			InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY);
		alphaField(fields, MARKER_SECTION, "Background color",
			InteractionCueConfig.PENDING_MARKER_BACKGROUND_COLOR_KEY);
		field(fields, MARKER_SECTION, "Corner",
			InteractionCueConfig.PENDING_MARKER_CORNER_KEY);
		field(fields, MARKER_SECTION, "Border",
			InteractionCueConfig.PENDING_MARKER_BORDER_KEY);
		alphaField(fields, MARKER_SECTION, "Border color",
			InteractionCueConfig.PENDING_MARKER_COLOR_KEY);
		field(fields, MARKER_SECTION, "Border width",
			InteractionCueConfig.PENDING_MARKER_BORDER_WIDTH_KEY);
		field(fields, MARKER_SECTION, "Padding top",
			InteractionCueConfig.PENDING_MARKER_PADDING_TOP_KEY);
		field(fields, MARKER_SECTION, "Padding right",
			InteractionCueConfig.PENDING_MARKER_PADDING_RIGHT_KEY);
		field(fields, MARKER_SECTION, "Padding bottom",
			InteractionCueConfig.PENDING_MARKER_PADDING_BOTTOM_KEY);
		field(fields, MARKER_SECTION, "Padding left",
			InteractionCueConfig.PENDING_MARKER_PADDING_LEFT_KEY);

		return Collections.unmodifiableMap(fields);
	}

	private static void field(
		Map<String, ConfigField> fields,
		String section,
		String name,
		String key)
	{
		fields.put(key, new ConfigField(section, name, false));
	}

	private static void alphaField(
		Map<String, ConfigField> fields,
		String section,
		String name,
		String key)
	{
		fields.put(key, new ConfigField(section, name, true));
	}

	private static final class ConfigField
	{
		private final String section;
		private final String name;
		private final boolean alpha;

		private ConfigField(String section, String name, boolean alpha)
		{
			this.section = section;
			this.name = name;
			this.alpha = alpha;
		}
	}
}
