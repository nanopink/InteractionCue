package com.interactioncue;

import org.junit.Assert;
import org.junit.Test;

public class InteractionCueSpotAnimationSnapshotTest
{
	@Test
	public void ignoresBaselineProgressAndRemoval()
	{
		InteractionCueSpotAnimationSnapshot snapshot =
			InteractionCueSpotAnimationSnapshot.capture(animation(100, 50, 1));

		Assert.assertFalse(snapshot.hasNewAnimation(animation(100, 50, 1)));
		Assert.assertFalse(snapshot.hasNewAnimation());
	}

	@Test
	public void detectsNewAndRestartedSpotAnimations()
	{
		InteractionCueSpotAnimationSnapshot snapshot =
			InteractionCueSpotAnimationSnapshot.capture(animation(100, 50, 1));

		Assert.assertTrue(snapshot.hasNewAnimation(animation(101, 51, 2)));
		Assert.assertTrue(snapshot.hasNewAnimation(animation(100, 52, 1)));
		Assert.assertTrue(snapshot.hasNewAnimation(animation(100, 50, 2)));
	}

	@Test
	public void detectsAdditionalIdenticalSpotAnimation()
	{
		InteractionCueSpotAnimationSnapshot.SpotAnimationKey animation =
			animation(100, 50, 1);
		InteractionCueSpotAnimationSnapshot snapshot =
			InteractionCueSpotAnimationSnapshot.capture(animation);

		Assert.assertTrue(snapshot.hasNewAnimation(animation, animation));
	}

	@Test
	public void currentSnapshotBecomesTheRollingBaseline()
	{
		InteractionCueSpotAnimationSnapshot.SpotAnimationKey first =
			animation(100, 50, 1);
		InteractionCueSpotAnimationSnapshot.SpotAnimationKey second =
			animation(101, 51, 2);
		InteractionCueSpotAnimationSnapshot.SpotAnimationKey third =
			animation(102, 52, 3);
		InteractionCueSpotAnimationSnapshot baseline =
			InteractionCueSpotAnimationSnapshot.capture(first);
		InteractionCueSpotAnimationSnapshot current =
			InteractionCueSpotAnimationSnapshot.capture(first, second);
		InteractionCueSpotAnimationSnapshot next =
			InteractionCueSpotAnimationSnapshot.capture(first, second, third);

		Assert.assertTrue(baseline.hasNewAnimation(current));
		Assert.assertFalse(current.hasNewAnimation(current));
		Assert.assertTrue(current.hasNewAnimation(next));
		Assert.assertFalse(next.hasNewAnimation(next));
	}

	@Test
	public void removalIsNotAReplacementAnimation()
	{
		InteractionCueSpotAnimationSnapshot.SpotAnimationKey first =
			animation(100, 50, 1);
		InteractionCueSpotAnimationSnapshot.SpotAnimationKey second =
			animation(101, 51, 2);
		InteractionCueSpotAnimationSnapshot baseline =
			InteractionCueSpotAnimationSnapshot.capture(first, second);
		InteractionCueSpotAnimationSnapshot current =
			InteractionCueSpotAnimationSnapshot.capture(first);

		Assert.assertFalse(baseline.hasNewAnimation(current));
	}

	private static InteractionCueSpotAnimationSnapshot.SpotAnimationKey animation(
		int id,
		int startCycle,
		long nodeHash)
	{
		return new InteractionCueSpotAnimationSnapshot.SpotAnimationKey(
			id,
			startCycle,
			nodeHash
		);
	}
}
