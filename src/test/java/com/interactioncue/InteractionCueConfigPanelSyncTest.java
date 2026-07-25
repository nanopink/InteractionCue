package com.interactioncue;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.runelite.client.ui.components.ColorJButton;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCueConfigPanelSyncTest
{
	@Test
	public void synchronizesEverySupportedControlTypeInItsOwnSection()
	{
		JPanel root = configPanel("Interaction Cue");
		JPanel tooltip = section(root, "Tooltip");
		JPanel marker = section(root, "Marker");

		JComboBox<InteractionCueTooltipPreset> tooltipPreset =
			new JComboBox<>(InteractionCueTooltipPreset.values());
		tooltipPreset.setSelectedItem(InteractionCueTooltipPreset.FULL);
		row(tooltip, "Preset", tooltipPreset);

		JSpinner tooltipX = intSpinner(22);
		row(tooltip, "X offset", tooltipX);
		JComboBox<InteractionCueTooltipOrigin> tooltipOrigin =
			new JComboBox<>(InteractionCueTooltipOrigin.values());
		tooltipOrigin.setSelectedItem(InteractionCueTooltipOrigin.CENTER_LEFT);
		row(tooltip, "Origin", tooltipOrigin);
		JTextField format = new JTextField("old");
		row(tooltip, "Format", format);
		JCheckBox tooltipBackground = new JCheckBox();
		tooltipBackground.setSelected(true);
		row(tooltip, "Background", tooltipBackground);
		ColorJButton backgroundColor =
			new ColorJButton("#000000FF", Color.BLACK);
		row(tooltip, "Background color", backgroundColor);
		JSpinner borderWidth = doubleSpinner(0.5);
		row(tooltip, "Border width", borderWidth);

		JComboBox<InteractionCueMarkerPreset> markerPreset =
			new JComboBox<>(InteractionCueMarkerPreset.values());
		markerPreset.setSelectedItem(InteractionCueMarkerPreset.CUSTOM);
		row(marker, "Preset", markerPreset);
		JSpinner markerX = intSpinner(7);
		row(marker, "X offset", markerX);
		JCheckBox markerBackground = new JCheckBox();
		row(marker, "Background", markerBackground);

		Color updatedColor = new Color(12, 34, 56, 78);
		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.TOOLTIP_PRESET_KEY, "RUINED_HEIR");
		values.put(InteractionCueConfig.X_OFFSET_KEY, -4);
		values.put(InteractionCueConfig.TOOLTIP_ORIGIN_KEY,
			InteractionCueTooltipOrigin.CENTER);
		values.put(InteractionCueConfig.LABEL_FORMAT_KEY, "{source_icon}");
		values.put(InteractionCueConfig.BACKGROUND_KEY, false);
		values.put(InteractionCueConfig.BACKGROUND_COLOR_KEY, updatedColor);
		values.put(InteractionCueConfig.TOOLTIP_BORDER_WIDTH_KEY, 0.8);
		values.put(InteractionCueConfig.MARKER_PRESET_KEY, "RUINED_HEIR");
		values.put(InteractionCueConfig.PENDING_MARKER_X_OFFSET_KEY, 0);
		values.put(InteractionCueConfig.PENDING_MARKER_BACKGROUND_KEY, true);

		Assert.assertEquals(
			10,
			InteractionCueConfigPanelSync.synchronize(root, values));
		Assert.assertSame(
			InteractionCueTooltipPreset.RUINED_HEIR,
			tooltipPreset.getSelectedItem());
		Assert.assertEquals(-4, tooltipX.getValue());
		Assert.assertSame(
			InteractionCueTooltipOrigin.CENTER,
			tooltipOrigin.getSelectedItem());
		Assert.assertEquals("{source_icon}", format.getText());
		Assert.assertFalse(tooltipBackground.isSelected());
		Assert.assertEquals(updatedColor, backgroundColor.getColor());
		Assert.assertEquals(0.8, (Double) borderWidth.getValue(), 0.0);
		Assert.assertSame(
			InteractionCueMarkerPreset.RUINED_HEIR,
			markerPreset.getSelectedItem());
		Assert.assertEquals(0, markerX.getValue());
		Assert.assertTrue(markerBackground.isSelected());
	}

	@Test
	public void duplicateRowNamesRemainScopedToTheirSections()
	{
		JPanel root = configPanel("Interaction Cue");
		JPanel tooltip = section(root, "Tooltip");
		JPanel marker = section(root, "Marker");
		JSpinner tooltipX = intSpinner(22);
		JSpinner markerX = intSpinner(7);
		row(tooltip, "X offset", tooltipX);
		row(marker, "X offset", markerX);

		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.X_OFFSET_KEY, -4);

		Assert.assertEquals(
			1,
			InteractionCueConfigPanelSync.synchronize(root, values));
		Assert.assertEquals(-4, tooltipX.getValue());
		Assert.assertEquals(7, markerX.getValue());
	}

	@Test
	public void ignoresLookalikeRowsOutsideInteractionCuePanel()
	{
		JPanel root = configPanel("Another plugin");
		JPanel tooltip = section(root, "Tooltip");
		section(root, "Marker");
		JSpinner xOffset = intSpinner(22);
		row(tooltip, "X offset", xOffset);

		Map<String, Object> values = new LinkedHashMap<>();
		values.put(InteractionCueConfig.X_OFFSET_KEY, -4);

		Assert.assertEquals(
			0,
			InteractionCueConfigPanelSync.synchronize(root, values));
		Assert.assertEquals(22, xOffset.getValue());
	}

	@Test
	public void everyPresetControlledKeyHasARegisteredField()
	{
		assertRegistered(InteractionCueTooltipPreset.FULL.getSettings());
		assertRegistered(InteractionCueTooltipPreset.RUINED_HEIR.getSettings());
		assertRegistered(InteractionCueMarkerPreset.RUINED_HEIR.getSettings());
		assertRegistered(InteractionCueMarkerPreset.LEGACY.getSettings());
		Assert.assertTrue(InteractionCueConfigPanelSync.hasRegisteredField(
			InteractionCueConfig.TOOLTIP_PRESET_KEY));
		Assert.assertTrue(InteractionCueConfigPanelSync.hasRegisteredField(
			InteractionCueConfig.MARKER_PRESET_KEY));
		Assert.assertFalse(InteractionCueConfigPanelSync.hasRegisteredField("unknown"));
	}

	private static void assertRegistered(Map<String, Object> values)
	{
		for (String key : values.keySet())
		{
			Assert.assertTrue(
				key,
				InteractionCueConfigPanelSync.hasRegisteredField(key));
		}
	}

	private static JPanel configPanel(String title)
	{
		JPanel root = new JPanel();
		JPanel header = new JPanel();
		header.add(new JLabel(title));
		root.add(header);
		return root;
	}

	private static JPanel section(JPanel root, String name)
	{
		JPanel section = new JPanel();
		JPanel header = new JPanel();
		header.add(new JLabel(name));
		section.add(header);
		root.add(section);
		return section;
	}

	private static void row(
		JPanel section,
		String name,
		java.awt.Component control)
	{
		JPanel row = new JPanel();
		row.add(new JLabel(name));
		row.add(control);
		section.add(row);
	}

	private static JSpinner intSpinner(int value)
	{
		return new JSpinner(new SpinnerNumberModel(value, -1000, 1000, 1));
	}

	private static JSpinner doubleSpinner(double value)
	{
		return new JSpinner(new SpinnerNumberModel(value, 0.0, 1000.0, 0.1));
	}
}
