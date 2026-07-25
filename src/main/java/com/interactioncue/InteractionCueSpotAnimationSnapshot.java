package com.interactioncue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.runelite.api.Actor;
import net.runelite.api.ActorSpotAnim;

final class InteractionCueSpotAnimationSnapshot
{
	private final Map<SpotAnimationKey, Integer> animations;

	private InteractionCueSpotAnimationSnapshot(Map<SpotAnimationKey, Integer> animations)
	{
		this.animations = animations;
	}

	static InteractionCueSpotAnimationSnapshot capture(Actor actor)
	{
		return capture(actor == null ? null : actor.getSpotAnims());
	}

	static InteractionCueSpotAnimationSnapshot capture(Iterable<ActorSpotAnim> spotAnimations)
	{
		if (spotAnimations == null)
		{
			return new InteractionCueSpotAnimationSnapshot(Collections.emptyMap());
		}

		Map<SpotAnimationKey, Integer> animations = new HashMap<>();
		for (ActorSpotAnim animation : spotAnimations)
		{
			if (animation != null)
			{
				add(animations, key(animation));
			}
		}
		return new InteractionCueSpotAnimationSnapshot(animations);
	}

	static InteractionCueSpotAnimationSnapshot capture(SpotAnimationKey... animations)
	{
		Map<SpotAnimationKey, Integer> counts = new HashMap<>();
		for (SpotAnimationKey animation : animations)
		{
			if (animation != null)
			{
				add(counts, animation);
			}
		}
		return new InteractionCueSpotAnimationSnapshot(counts);
	}

	boolean hasNewAnimation(Actor actor)
	{
		return actor != null && hasNewAnimation(actor.getSpotAnims());
	}

	boolean hasNewAnimation(Iterable<ActorSpotAnim> current)
	{
		if (current == null)
		{
			return false;
		}

		Map<SpotAnimationKey, Integer> observed = new HashMap<>();
		for (ActorSpotAnim animation : current)
		{
			if (animation == null)
			{
				continue;
			}

			SpotAnimationKey key = key(animation);
			int count = observed.getOrDefault(key, 0) + 1;
			observed.put(key, count);
			if (count > animations.getOrDefault(key, 0))
			{
				return true;
			}
		}
		return false;
	}

	boolean hasNewAnimation(InteractionCueSpotAnimationSnapshot current)
	{
		if (current == null)
		{
			return false;
		}

		for (Map.Entry<SpotAnimationKey, Integer> animation
			: current.animations.entrySet())
		{
			if (animation.getValue()
				> animations.getOrDefault(animation.getKey(), 0))
			{
				return true;
			}
		}
		return false;
	}

	boolean hasNewAnimation(SpotAnimationKey... current)
	{
		Map<SpotAnimationKey, Integer> observed = new HashMap<>();
		for (SpotAnimationKey animation : current)
		{
			if (animation == null)
			{
				continue;
			}

			int count = observed.getOrDefault(animation, 0) + 1;
			observed.put(animation, count);
			if (count > animations.getOrDefault(animation, 0))
			{
				return true;
			}
		}
		return false;
	}

	private static SpotAnimationKey key(ActorSpotAnim animation)
	{
		return new SpotAnimationKey(
			animation.getId(),
			animation.getStartCycle(),
			animation.getHash()
		);
	}

	private static void add(
		Map<SpotAnimationKey, Integer> animations,
		SpotAnimationKey animation)
	{
		animations.put(animation, animations.getOrDefault(animation, 0) + 1);
	}

	static final class SpotAnimationKey
	{
		private final int id;
		private final int startCycle;
		private final long nodeHash;

		SpotAnimationKey(int id, int startCycle, long nodeHash)
		{
			this.id = id;
			this.startCycle = startCycle;
			this.nodeHash = nodeHash;
		}

		@Override
		public boolean equals(Object other)
		{
			if (this == other)
			{
				return true;
			}
			if (!(other instanceof SpotAnimationKey))
			{
				return false;
			}

			SpotAnimationKey that = (SpotAnimationKey) other;
			return id == that.id
				&& startCycle == that.startCycle
				&& nodeHash == that.nodeHash;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(id, startCycle, nodeHash);
		}
	}
}
