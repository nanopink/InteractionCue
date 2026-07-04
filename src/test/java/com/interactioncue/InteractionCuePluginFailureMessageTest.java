package com.interactioncue;

import org.junit.Assert;
import org.junit.Test;

public class InteractionCuePluginFailureMessageTest
{
	@Test
	public void recognizesNothingInterestingHappens()
	{
		Assert.assertTrue(InteractionCuePlugin.isActionFailureMessage("Nothing interesting happens."));
	}

	@Test
	public void ignoresSuccessfulActionText()
	{
		Assert.assertFalse(InteractionCuePlugin.isActionFailureMessage("You cast High Level Alchemy."));
	}
}
