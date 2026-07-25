package com.interactioncue;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCueCursorOverlayTest
{
	@Test
	public void keepsEscapedColonInsideConditionalText()
	{
		Assert.assertEquals(
			Arrays.asList("target", "Target\\: @1", "No target"),
			InteractionCueCursorOverlay.splitTopLevel("target:Target\\: @1:No target")
		);
	}

	@Test
	public void splitsInvertedConditionalWithoutElse()
	{
		Assert.assertEquals(
			Arrays.asList("!target", "No target"),
			InteractionCueCursorOverlay.splitTopLevel("!target:No target")
		);
	}

	@Test
	public void ignoresColonsInsideNestedTags()
	{
		Assert.assertEquals(
			Arrays.asList("target", "{#FFF} -> {#00ffff}@1"),
			InteractionCueCursorOverlay.splitTopLevel("target:{#FFF} -> {#00ffff}@1")
		);
	}

	@Test
	public void supportsOriginalTooltipTokenAliases()
	{
		InteractionCueConfig config = new InteractionCueConfig()
		{
			@Override
			public String labelFormat()
			{
				return "{tool}{action_icon}{icon}{tool:@1}";
			}
		};
		InteractionCueCursorOverlay overlay = new InteractionCueCursorOverlay(null, null, config);
		InteractionCue cue = new InteractionCue(
			true,
			"Use",
			"Knife",
			"Logs",
			null,
			Color.BLUE
		);

		List<InteractionCueLabelPart> parts = overlay.parseLabel(cue);

		Assert.assertEquals(4, parts.size());
		Assert.assertEquals("Knife", parts.get(0).getText());
		Assert.assertTrue(parts.get(1).isImage());
		Assert.assertEquals("USE", parts.get(1).getText());
		Assert.assertTrue(parts.get(2).isImage());
		Assert.assertEquals("USE", parts.get(2).getText());
		Assert.assertEquals("Knife", parts.get(3).getText());
	}

	@Test
	public void parsesOriginalTooltipFormat()
	{
		InteractionCueConfig config = new InteractionCueConfig()
		{
			@Override
			public String labelFormat()
			{
				return "{#FFF}{action} {action_icon} {#FF9040}{tool}"
					+ "{target:{#FFF} -> {#00ffff}@1}";
			}
		};
		InteractionCueCursorOverlay overlay = new InteractionCueCursorOverlay(null, null, config);
		InteractionCue cue = new InteractionCue(
			true,
			"Use",
			"Ranarr weed",
			"Potion",
			null,
			Color.BLUE
		);

		List<InteractionCueLabelPart> parts = overlay.parseLabel(cue);
		StringBuilder rendered = new StringBuilder();
		for (InteractionCueLabelPart part : parts)
		{
			if (part.isImage())
			{
				rendered.append('[').append(part.getText()).append(']');
			}
			else
			{
				rendered.append(part.getText());
			}
		}

		Assert.assertEquals("Use [USE] Ranarr weed -> Potion", rendered.toString());
	}

	@Test
	public void defaultSourceIconUsesSourceFallback()
	{
		InteractionCueCursorOverlay overlay = new InteractionCueCursorOverlay(
			null,
			null,
			new InteractionCueConfig()
			{
			}
		);
		InteractionCue cue = new InteractionCue(
			true,
			"Use",
			"Ranarr weed",
			"Potion",
			null,
			Color.BLUE
		);

		List<InteractionCueLabelPart> parts = overlay.parseLabel(cue);
		StringBuilder rendered = new StringBuilder();
		for (InteractionCueLabelPart part : parts)
		{
			if (part.isImage())
			{
				rendered.append('[').append(part.getText()).append(']');
			}
			else
			{
				rendered.append(part.getText());
			}
		}

		Assert.assertEquals("[RAN]", rendered.toString());
	}
}
