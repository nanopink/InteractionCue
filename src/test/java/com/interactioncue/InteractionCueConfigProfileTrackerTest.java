package com.interactioncue;

import org.junit.Assert;
import org.junit.Test;

public class InteractionCueConfigProfileTrackerTest
{
	@Test
	public void suppressesConfigEventsUntilProfileChangedCompletes()
	{
		InteractionCueConfigProfileTracker tracker =
			new InteractionCueConfigProfileTracker();
		tracker.initialize(1);

		Assert.assertFalse(tracker.onConfigChanged(1));
		Assert.assertTrue(tracker.onConfigChanged(2));
		Assert.assertTrue(tracker.onConfigChanged(2));

		tracker.beginProfileChanged(2);
		Assert.assertTrue(tracker.onConfigChanged(2));
		tracker.finishProfileChanged(2);
		Assert.assertFalse(tracker.onConfigChanged(2));
	}

	@Test
	public void olderProfileCompletionCannotReleaseNewerSwitch()
	{
		InteractionCueConfigProfileTracker tracker =
			new InteractionCueConfigProfileTracker();
		tracker.initialize(1);

		tracker.beginProfileChanged(2);
		Assert.assertTrue(tracker.onConfigChanged(3));
		tracker.finishProfileChanged(2);
		Assert.assertTrue(tracker.onConfigChanged(3));

		tracker.finishProfileChanged(3);
		Assert.assertFalse(tracker.onConfigChanged(3));
	}
}
