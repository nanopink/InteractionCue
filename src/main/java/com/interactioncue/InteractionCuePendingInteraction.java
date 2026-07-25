package com.interactioncue;

import net.runelite.api.Actor;
import net.runelite.api.ItemContainer;

final class InteractionCuePendingInteraction
{
	static final int TIMEOUT_TICKS = 5;
	static final int UNKNOWN_ACTIONS = 0;
	static final int UNBOUNDED_ACTIONS = -1;

	private final InteractionCue cue;
	private final int targetParentId;
	private final int clickedTargetSlot;
	private final int targetItemId;
	private final int sourceItemId;
	private final int sourceSlot;
	private final int animationBefore;
	private final int animationFrameBefore;
	private InteractionCueInventorySnapshot inventory;
	private InteractionCueSpotAnimationSnapshot spotAnimations;
	private long targetQuantityLowWaterMark;
	private long sourceQuantityLowWaterMark;
	private long targetActionsObserved;
	private long sourceActionsObserved;
	private long queuedActionsAccounted;
	private int targetSlot = -1;
	private int targetQuantity;
	private boolean baselineAnimationCleared;
	private int lastAnimation;
	private int lastAnimationFrame;
	private int observedAnimation = -1;
	private int quietTicks;
	private boolean activitySinceLastTick = true;
	private boolean paused;
	private boolean started;
	private boolean targetCompletionObserved;
	private boolean productionControlActivated;
	private long remainingActions = UNBOUNDED_ACTIONS;

	InteractionCuePendingInteraction(
		InteractionCue cue,
		int targetParentId,
		int clickedTargetSlot,
		int targetItemId,
		int sourceItemId,
		int sourceSlot,
		InteractionCueInventorySnapshot inventory,
		int animationBefore,
		int animationFrameBefore,
		InteractionCueSpotAnimationSnapshot spotAnimations)
	{
		this.cue = cue;
		this.targetParentId = targetParentId;
		this.clickedTargetSlot = clickedTargetSlot;
		this.targetItemId = targetItemId;
		this.sourceItemId = sourceItemId;
		this.sourceSlot = sourceSlot;
		this.inventory = inventory;
		this.animationBefore = animationBefore;
		this.animationFrameBefore = animationFrameBefore;
		this.spotAnimations = spotAnimations;
		targetQuantityLowWaterMark = inventory == null
			? 0
			: inventory.getTotalQuantity(targetItemId);
		sourceQuantityLowWaterMark = inventory == null
			? 0
			: inventory.getTotalQuantity(sourceItemId);
		baselineAnimationCleared = animationBefore == -1;
		lastAnimation = animationBefore;
		lastAnimationFrame = animationFrameBefore;
		retarget(inventory);
	}

	InteractionCue getCue()
	{
		return cue;
	}

	boolean isValid()
	{
		return targetSlot >= 0;
	}

	boolean isTarget(int parentId, int slot, int itemId, int quantity)
	{
		return parentId == targetParentId
			&& slot == targetSlot
			&& itemId == targetItemId
			&& quantity == targetQuantity;
	}

	boolean observeInventory(ItemContainer current)
	{
		return observeInventory(
			current == null ? null : InteractionCueInventorySnapshot.capture(current)
		);
	}

	boolean observeInventory(InteractionCueInventorySnapshot current)
	{
		if (current == null)
		{
			return true;
		}

		if (inventory.matches(current))
		{
			return false;
		}

		long previousTargetQuantity = inventory.getTotalQuantity(targetItemId);
		long currentTargetQuantity = current.getTotalQuantity(targetItemId);
		long previousSourceQuantity = inventory.getTotalQuantity(sourceItemId);
		long currentSourceQuantity = current.getTotalQuantity(sourceItemId);
		long completedTargetActions = completedActions(
			inventory,
			current,
			targetItemId,
			targetQuantityLowWaterMark
		);
		long completedSourceActions = sourceItemId > 0
			&& sourceItemId != targetItemId
			? completedActions(
				inventory,
				current,
				sourceItemId,
				sourceQuantityLowWaterMark
			)
			: 0;
		targetQuantityLowWaterMark = Math.min(
			targetQuantityLowWaterMark,
			currentTargetQuantity
		);
		sourceQuantityLowWaterMark = Math.min(
			sourceQuantityLowWaterMark,
			currentSourceQuantity
		);
		targetActionsObserved += completedTargetActions;
		sourceActionsObserved += completedSourceActions;
		boolean targetChanged =
			previousTargetQuantity != currentTargetQuantity
			|| !current.containsAt(targetSlot, targetItemId, targetQuantity);
		boolean sourceChanged = sourceItemId > 0
			&& previousSourceQuantity != currentSourceQuantity;
		inventory = current;
		if (!targetChanged && !sourceChanged)
		{
			return false;
		}

		started = true;
		recordActivity();
		targetCompletionObserved |= completedTargetActions > 0;
		if (consumeQueuedActions(targetActionsObserved))
		{
			return true;
		}
		if (sourceItemId <= 0)
		{
			return complete();
		}
		if (sourceItemId == targetItemId)
		{
			return complete();
		}
		if (currentSourceQuantity <= 0)
		{
			return complete();
		}

		return !retarget(current);
	}

	boolean observeAnimation(int animation, int animationFrame)
	{
		if (animation == -1)
		{
			baselineAnimationCleared = true;
			lastAnimation = -1;
			lastAnimationFrame = -1;
			observedAnimation = -1;
			return false;
		}

		boolean restarted = animation == animationBefore
			&& animationFrame >= 0
			&& (animationFrameBefore >= 0 && animationFrame < animationFrameBefore
			|| lastAnimation == animation && lastAnimationFrame >= 0
			&& animationFrame < lastAnimationFrame);
		boolean newAction = animation != animationBefore
			|| baselineAnimationCleared
			|| restarted;
		if (newAction)
		{
			observedAnimation = animation;
			started = true;
		}

		boolean active = newAction || observedAnimation == animation;
		if (active)
		{
			recordActivity();
		}

		lastAnimation = animation;
		lastAnimationFrame = animationFrame;
		return active;
	}

	boolean observeSpotAnimations(Actor actor)
	{
		return observeSpotAnimations(
			InteractionCueSpotAnimationSnapshot.capture(actor)
		);
	}

	boolean observeSpotAnimations(InteractionCueSpotAnimationSnapshot current)
	{
		boolean newAnimation = spotAnimations.hasNewAnimation(current);
		spotAnimations = current;
		if (newAnimation)
		{
			started = true;
			recordActivity();
		}
		return newAnimation;
	}

	void setPaused(boolean paused)
	{
		if (paused)
		{
			openProductionDialog();
			return;
		}

		this.paused = false;
		recordActivity();
	}

	void openProductionDialog()
	{
		paused = true;
	}

	void markProductionControl()
	{
		markProductionControl(UNKNOWN_ACTIONS);
	}

	void markProductionControl(int queuedActions)
	{
		productionControlActivated = true;
		updateProductionQuantity(queuedActions);
		recordActivity();
	}

	void updateProductionQuantity(int queuedActions)
	{
		if (!targetCompletionObserved
			&& queuedActionsAccounted == 0
			&& (queuedActions > 0 || queuedActions == UNBOUNDED_ACTIONS))
		{
			remainingActions = queuedActions;
		}
	}

	boolean closeProductionDialog(boolean unload)
	{
		if (!unload)
		{
			paused = true;
			return false;
		}
		if (!paused)
		{
			return false;
		}

		boolean cancelled = !productionControlActivated && !started;
		paused = false;
		if (!cancelled)
		{
			recordActivity();
		}
		return cancelled;
	}

	boolean isPaused()
	{
		return paused;
	}

	boolean hasStarted()
	{
		return started;
	}

	boolean advanceTick()
	{
		if (paused)
		{
			return false;
		}
		if (consumeQueuedActions(Math.max(
			targetActionsObserved,
			sourceActionsObserved
		)))
		{
			return true;
		}
		if (activitySinceLastTick)
		{
			activitySinceLastTick = false;
			quietTicks = 0;
			return false;
		}

		quietTicks++;
		return quietTicks >= TIMEOUT_TICKS;
	}

	private boolean retarget(InteractionCueInventorySnapshot current)
	{
		if (current == null)
		{
			targetSlot = -1;
			targetQuantity = 0;
			return false;
		}

		if (sourceItemId <= 0)
		{
			targetSlot = current.containsAt(
				clickedTargetSlot,
				targetItemId,
				current.getQuantityAt(clickedTargetSlot))
				? clickedTargetSlot
				: -1;
		}
		else
		{
			int excludedSlot = sourceItemId == targetItemId
				&& current.getItemIdAt(sourceSlot) == sourceItemId
				? sourceSlot
				: -1;
			targetSlot = current.findFirstSlot(targetItemId, excludedSlot);
		}

		targetQuantity = current.getQuantityAt(targetSlot);
		return targetSlot >= 0 && targetQuantity > 0;
	}

	private static long completedActions(
		InteractionCueInventorySnapshot previous,
		InteractionCueInventorySnapshot current,
		int itemId,
		long quantityLowWaterMark)
	{
		long newlyConsumedUnits = Math.max(
			0L,
			quantityLowWaterMark - current.getTotalQuantity(itemId)
		);
		if (newlyConsumedUnits <= 0)
		{
			return 0;
		}

		return Math.max(
			1L,
			Math.min(
				newlyConsumedUnits,
				previous.countReducedSlots(current, itemId)
			)
		);
	}

	private boolean consumeQueuedActions(long observedActions)
	{
		long completedActions = observedActions - queuedActionsAccounted;
		if (remainingActions <= 0 || completedActions <= 0)
		{
			return false;
		}

		queuedActionsAccounted = observedActions;
		remainingActions -= completedActions;
		if (remainingActions > 0)
		{
			return false;
		}

		return complete();
	}

	private boolean complete()
	{
		targetSlot = -1;
		targetQuantity = 0;
		return true;
	}

	private void recordActivity()
	{
		quietTicks = 0;
		activitySinceLastTick = true;
	}
}
