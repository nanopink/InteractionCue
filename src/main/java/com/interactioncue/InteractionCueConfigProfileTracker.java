package com.interactioncue;

final class InteractionCueConfigProfileTracker
{
	private long observedProfileId = Long.MIN_VALUE;
	private boolean switchInProgress;

	synchronized void initialize(long profileId)
	{
		observedProfileId = profileId;
		switchInProgress = false;
	}

	synchronized boolean onConfigChanged(long profileId)
	{
		if (observedProfileId != profileId)
		{
			observedProfileId = profileId;
			switchInProgress = true;
		}
		return switchInProgress;
	}

	synchronized void beginProfileChanged(long profileId)
	{
		observedProfileId = profileId;
		switchInProgress = true;
	}

	synchronized void finishProfileChanged(long profileId)
	{
		if (observedProfileId == profileId)
		{
			switchInProgress = false;
		}
	}
}
