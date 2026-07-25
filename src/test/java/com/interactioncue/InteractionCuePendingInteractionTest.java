package com.interactioncue;

import java.awt.Color;
import net.runelite.api.Item;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCuePendingInteractionTest
{
	private static final int SOURCE_ID = ItemID.RANARR_WEED;
	private static final int TARGET_ID = ItemID.RANARRVIAL;
	private static final int PRODUCT_ID = ItemID.GUAMVIAL;
	private static final int SOURCE_SLOT = 0;
	private static final int TARGET_PARENT_ID = InterfaceID.Inventory.ITEMS;
	private static final InteractionCue CUE =
		new InteractionCue(true, "Use", "Herb", "Potion", null, Color.BLUE);

	@Test
	public void selectsFirstTargetWhenLaterDuplicateWasClicked()
	{
		InteractionCuePendingInteraction pending = pending(
			4,
			snapshot(
				item(SOURCE_ID, 1),
				null,
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			)
		);

		Assert.assertSame(CUE, pending.getCue());
		Assert.assertTrue(pending.isValid());
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			2,
			TARGET_ID,
			1
		));
		Assert.assertFalse(pending.isTarget(
			TARGET_PARENT_ID,
			4,
			TARGET_ID,
			1
		));
	}

	@Test
	public void advancesToNextTargetAfterFirstTransforms()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1),
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
	}

	@Test
	public void makeOneStopsAfterFirstTargetTransforms()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			3,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1),
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			1
		);

		Assert.assertTrue(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
		Assert.assertFalse(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
	}

	@Test
	public void finiteQueueStopsOnSelectedCompletionCount()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			5,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 3),
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			2
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 2),
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));

		Assert.assertTrue(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			null,
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
		Assert.assertFalse(pending.isTarget(
			TARGET_PARENT_ID,
			5,
			TARGET_ID,
			1
		));
	}

	@Test
	public void sourceFirstUpdateDoesNotSpendQueuedInteraction()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			3,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1),
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			1
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			null,
			item(TARGET_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			1,
			TARGET_ID,
			1
		));
		Assert.assertTrue(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void sourceOnlyCompletionSettlesOnNextTick()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			1,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			1
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			1,
			TARGET_ID,
			1
		));

		Assert.assertTrue(pending.advanceTick());
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void settledSourceCompletionIsNotCountedAgainByLateTargetUpdate()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			1,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 1),
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			2
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.advanceTick());

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			2,
			TARGET_ID,
			1
		));
	}

	@Test
	public void quantitySignalAfterSourceUpdateStillBoundsTheQueue()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1),
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			),
			-1,
			-1
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			null,
			item(TARGET_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.hasStarted());
		pending.updateProductionQuantity(1);

		Assert.assertTrue(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void targetFirstUpdateIsNotCountedAgainBySourceUpdate()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			3,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			2
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 2),
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
		Assert.assertTrue(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1),
			null,
			item(PRODUCT_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void collapsedTargetUpdatesConsumeTheirFullQueueCount()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			5,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 3),
				item(TARGET_ID, 1),
				item(TARGET_ID, 1),
				null,
				null,
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			2
		);

		Assert.assertTrue(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			item(PRODUCT_ID, 1),
			null,
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void skipsMultipleTargetsChangedInOneSnapshot()
	{
		InteractionCuePendingInteraction pending = pending(
			4,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 1),
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			)
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 2),
			item(PRODUCT_ID, 1),
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			4,
			TARGET_ID,
			1
		));
	}

	@Test
	public void sourceExhaustionEndsBoundedQueueImmediately()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			3,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			),
			-1,
			-1,
			5
		);

		Assert.assertTrue(pending.observeInventory(snapshot(
			item(ItemID.GUAM_LEAF, 1),
			item(TARGET_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.hasStarted());
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void finalTargetFirstUpdateCompletesWhenSourceFollows()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			)
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));

		Assert.assertTrue(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void priorCompletionStillEndsWhenFinalSourceRunsOut()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			)
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void separateSourceAndTargetUpdatesAdvanceAfterCompletedStep()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1),
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			),
			-1,
			-1
		);

		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			1,
			TARGET_ID,
			1
		));
		Assert.assertFalse(pending.observeInventory(snapshot(
			null,
			item(TARGET_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			1,
			TARGET_ID,
			1
		));

		Assert.assertFalse(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1),
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isValid());
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
	}

	@Test
	public void stackQuantityChangeStaysAnchored()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			snapshot(
				item(SOURCE_ID, 2),
				null,
				null,
				item(TARGET_ID, 5)
			)
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 2),
			null,
			null,
			item(TARGET_ID, 4)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			4
		));
		Assert.assertFalse(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			5
		));
	}

	@Test
	public void stackedIngredientReductionCountsAsOneQueuedAction()
	{
		InteractionCuePendingInteraction pending = pendingWithQuantity(
			1,
			SOURCE_ID,
			0,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 30)
			),
			-1,
			-1,
			2
		);

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(TARGET_ID, 15)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			1,
			TARGET_ID,
			15
		));
		Assert.assertTrue(pending.observeInventory(snapshot(
			null,
			item(PRODUCT_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void observingSameSnapshotIsIdempotent()
	{
		InteractionCueInventorySnapshot inventory = snapshot(
			item(SOURCE_ID, 1),
			item(TARGET_ID, 1)
		);
		InteractionCuePendingInteraction pending = pending(1, inventory);

		Assert.assertFalse(pending.observeInventory(inventory));
		Assert.assertFalse(pending.observeInventory(inventory));
		Assert.assertFalse(pending.hasStarted());
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			1,
			TARGET_ID,
			1
		));
	}

	@Test
	public void unrelatedChangeRollsSnapshotWithoutAdvancing()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			snapshot(
				item(SOURCE_ID, 1),
				item(ItemID.COINS, 1),
				null,
				item(TARGET_ID, 1)
			)
		);
		InteractionCueInventorySnapshot changed = snapshot(
			item(SOURCE_ID, 1),
			item(ItemID.LOGS, 1),
			null,
			item(TARGET_ID, 1)
		);

		Assert.assertFalse(pending.observeInventory(changed));
		Assert.assertFalse(pending.observeInventory(changed));
		Assert.assertFalse(pending.hasStarted());
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
	}

	@Test
	public void targetExhaustionCompletesInteraction()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		Assert.assertTrue(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1)
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void nullInventoryCompletesInteraction()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		Assert.assertTrue(pending.observeInventory(
			(InteractionCueInventorySnapshot) null
		));
	}

	@Test
	public void sameSourceAndTargetExcludesSourceAndCompletesConservatively()
	{
		InteractionCueInventorySnapshot inventory = snapshot(
			item(TARGET_ID, 1),
			null,
			item(TARGET_ID, 1),
			item(TARGET_ID, 1)
		);
		InteractionCuePendingInteraction pending = pending(
			3,
			TARGET_ID,
			SOURCE_SLOT,
			inventory,
			-1,
			-1
		);

		Assert.assertFalse(pending.isTarget(
			TARGET_PARENT_ID,
			SOURCE_SLOT,
			TARGET_ID,
			1
		));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			2,
			TARGET_ID,
			1
		));
		Assert.assertTrue(pending.observeInventory(snapshot(
			item(TARGET_ID, 1),
			null,
			item(PRODUCT_ID, 1),
			item(TARGET_ID, 1)
		)));
	}

	@Test
	public void spellAnchorsClickedDuplicateAndCompletesAfterOneChange()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			-1,
			-1,
			snapshot(
				item(TARGET_ID, 1),
				null,
				null,
				item(TARGET_ID, 1)
			),
			-1,
			-1
		);

		Assert.assertFalse(pending.isTarget(
			TARGET_PARENT_ID,
			0,
			TARGET_ID,
			1
		));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
		Assert.assertTrue(pending.observeInventory(snapshot(
			item(TARGET_ID, 1),
			null,
			null,
			item(PRODUCT_ID, 1)
		)));
	}

	@Test
	public void expiresAfterFiveFullyQuietTicks()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		Assert.assertFalse(pending.advanceTick());
		for (int tick = 1; tick < InteractionCuePendingInteraction.TIMEOUT_TICKS; tick++)
		{
			Assert.assertFalse(pending.advanceTick());
		}
		Assert.assertTrue(pending.advanceTick());
	}

	@Test
	public void inventoryActivityAtThreeTicksResetsTimeout()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 1),
				null,
				item(TARGET_ID, 1)
			)
		);

		Assert.assertFalse(pending.advanceTick());
		Assert.assertFalse(pending.advanceTick());
		Assert.assertFalse(pending.advanceTick());
		Assert.assertFalse(pending.advanceTick());

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			null,
			item(TARGET_ID, 1)
		)));
		Assert.assertFalse(pending.advanceTick());
		for (int tick = 1; tick < InteractionCuePendingInteraction.TIMEOUT_TICKS; tick++)
		{
			Assert.assertFalse(pending.advanceTick());
		}
		Assert.assertTrue(pending.advanceTick());
	}

	@Test
	public void animationActivityKeepsMarkerAndResetsTimeout()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			SOURCE_ID,
			SOURCE_SLOT,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			),
			100,
			4
		);

		Assert.assertFalse(pending.observeAnimation(100, 5));
		Assert.assertTrue(pending.observeAnimation(101, 0));
		Assert.assertTrue(pending.hasStarted());
		Assert.assertTrue(pending.isValid());
		Assert.assertFalse(pending.advanceTick());

		Assert.assertTrue(pending.observeAnimation(101, 5));
		Assert.assertTrue(pending.observeAnimation(101, 0));
		Assert.assertTrue(pending.isValid());
		Assert.assertFalse(pending.advanceTick());
	}

	@Test
	public void sameAnimationRestartAfterClearKeepsMarker()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			SOURCE_ID,
			SOURCE_SLOT,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			),
			100,
			4
		);

		Assert.assertFalse(pending.observeAnimation(-1, -1));
		Assert.assertTrue(pending.observeAnimation(100, 0));
		Assert.assertTrue(pending.hasStarted());
		Assert.assertTrue(pending.isValid());
	}

	@Test
	public void sameAnimationFrameRollbackKeepsMarker()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			SOURCE_ID,
			SOURCE_SLOT,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			),
			100,
			0
		);

		Assert.assertFalse(pending.observeAnimation(100, 1));
		Assert.assertFalse(pending.observeAnimation(100, 5));
		Assert.assertTrue(pending.observeAnimation(100, 0));
		Assert.assertTrue(pending.observeAnimation(100, 1));
		Assert.assertTrue(pending.hasStarted());
		Assert.assertTrue(pending.isValid());
	}

	@Test
	public void spotAnimationActivityKeepsMarkerAndRollsBaseline()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);
		InteractionCueSpotAnimationSnapshot first =
			InteractionCueSpotAnimationSnapshot.capture(
				spotAnimation(100, 50, 1)
			);
		InteractionCueSpotAnimationSnapshot second =
			InteractionCueSpotAnimationSnapshot.capture(
				spotAnimation(100, 50, 1),
				spotAnimation(101, 51, 2)
			);

		Assert.assertTrue(pending.observeSpotAnimations(first));
		Assert.assertTrue(pending.isValid());
		Assert.assertFalse(pending.observeSpotAnimations(first));
		Assert.assertTrue(pending.observeSpotAnimations(second));
		Assert.assertTrue(pending.hasStarted());
		Assert.assertTrue(pending.isValid());
		Assert.assertFalse(pending.observeSpotAnimations(second));
	}

	@Test
	public void pausedInteractionDoesNotExpire()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		pending.setPaused(true);
		Assert.assertTrue(pending.isPaused());
		for (int tick = 0; tick < InteractionCuePendingInteraction.TIMEOUT_TICKS * 2; tick++)
		{
			Assert.assertFalse(pending.advanceTick());
		}

		pending.setPaused(false);
		Assert.assertFalse(pending.isPaused());
		Assert.assertFalse(pending.advanceTick());
	}

	@Test
	public void closingProductionDialogWithoutChoiceCancelsImmediately()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		pending.openProductionDialog();

		Assert.assertTrue(pending.isPaused());
		Assert.assertTrue(pending.closeProductionDialog(true));
		Assert.assertFalse(pending.isPaused());
	}

	@Test
	public void choosingQuantityWithoutProductStillCancels()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		pending.openProductionDialog();
		pending.updateProductionQuantity(5);

		Assert.assertTrue(pending.closeProductionDialog(true));
		Assert.assertFalse(pending.isPaused());
	}

	@Test
	public void submittedProductionChoiceSurvivesDialogClose()
	{
		InteractionCuePendingInteraction pending = pending(
			2,
			snapshot(
				item(SOURCE_ID, 2),
				item(TARGET_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		pending.openProductionDialog();
		pending.markProductionControl();

		Assert.assertFalse(pending.closeProductionDialog(true));
		Assert.assertFalse(pending.isPaused());
		Assert.assertFalse(pending.advanceTick());
		Assert.assertTrue(pending.isValid());
		Assert.assertFalse(pending.observeInventory(snapshot(
			item(SOURCE_ID, 1),
			item(PRODUCT_ID, 1),
			item(TARGET_ID, 1)
		)));
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			2,
			TARGET_ID,
			1
		));
	}

	@Test
	public void productionReloadKeepsAcceptedChoiceWithoutSecondControl()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		pending.openProductionDialog();
		pending.markProductionControl();
		Assert.assertFalse(pending.closeProductionDialog(false));

		pending.openProductionDialog();
		Assert.assertFalse(pending.closeProductionDialog(true));
		Assert.assertFalse(pending.isPaused());
	}

	@Test
	public void completedBatchStepSurvivesLateAndDuplicateDialogCloses()
	{
		InteractionCuePendingInteraction pending = pending(
			3,
			SOURCE_ID,
			4,
			snapshot(
				item(TARGET_ID, 1),
				item(SOURCE_ID, 1),
				null,
				item(TARGET_ID, 1),
				item(SOURCE_ID, 1)
			),
			-1,
			-1
		);

		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			0,
			TARGET_ID,
			1
		));
		pending.openProductionDialog();
		pending.markProductionControl(2);
		Assert.assertFalse(pending.closeProductionDialog(false));
		pending.openProductionDialog();

		Assert.assertFalse(pending.observeInventory(snapshot(
			item(PRODUCT_ID, 1),
			item(SOURCE_ID, 1),
			null,
			item(TARGET_ID, 1),
			null
		)));
		Assert.assertTrue(pending.hasStarted());
		Assert.assertTrue(pending.isValid());
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
		Assert.assertFalse(pending.closeProductionDialog(true));
		Assert.assertFalse(pending.closeProductionDialog(true));
		Assert.assertTrue(pending.isValid());
		Assert.assertTrue(pending.isTarget(
			TARGET_PARENT_ID,
			3,
			TARGET_ID,
			1
		));
		Assert.assertTrue(pending.observeInventory(snapshot(
			item(PRODUCT_ID, 1),
			null,
			null,
			item(PRODUCT_ID, 1),
			null
		)));
		Assert.assertFalse(pending.isValid());
	}

	@Test
	public void reloadedProductionDialogRemainsPaused()
	{
		InteractionCuePendingInteraction pending = pending(
			1,
			snapshot(
				item(SOURCE_ID, 1),
				item(TARGET_ID, 1)
			)
		);

		pending.openProductionDialog();

		Assert.assertFalse(pending.closeProductionDialog(false));
		Assert.assertTrue(pending.isPaused());
		Assert.assertTrue(pending.closeProductionDialog(true));
	}

	private static InteractionCuePendingInteraction pending(
		int clickedTargetSlot,
		InteractionCueInventorySnapshot inventory)
	{
		return pending(
			clickedTargetSlot,
			SOURCE_ID,
			SOURCE_SLOT,
			inventory,
			-1,
			-1
		);
	}

	private static InteractionCuePendingInteraction pending(
		int clickedTargetSlot,
		int sourceItemId,
		int sourceSlot,
		InteractionCueInventorySnapshot inventory,
		int animation,
		int animationFrame)
	{
		return pendingWithQuantity(
			clickedTargetSlot,
			sourceItemId,
			sourceSlot,
			inventory,
			animation,
			animationFrame,
			InteractionCuePendingInteraction.UNBOUNDED_ACTIONS
		);
	}

	private static InteractionCuePendingInteraction pendingWithQuantity(
		int clickedTargetSlot,
		int sourceItemId,
		int sourceSlot,
		InteractionCueInventorySnapshot inventory,
		int animation,
		int animationFrame,
		int queuedActions)
	{
		InteractionCuePendingInteraction pending =
			new InteractionCuePendingInteraction(
			CUE,
			TARGET_PARENT_ID,
			clickedTargetSlot,
			TARGET_ID,
			sourceItemId,
			sourceSlot,
			inventory,
			animation,
			animationFrame,
			InteractionCueSpotAnimationSnapshot.capture()
		);
		pending.updateProductionQuantity(queuedActions);
		return pending;
	}

	private static InteractionCueInventorySnapshot snapshot(Item... items)
	{
		return InteractionCueInventorySnapshot.capture(items);
	}

	private static Item item(int id, int quantity)
	{
		return new Item(id, quantity);
	}

	private static InteractionCueSpotAnimationSnapshot.SpotAnimationKey spotAnimation(
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
