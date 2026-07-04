package com.interactioncue;

import java.util.Arrays;
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
}
