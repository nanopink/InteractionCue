package com.interactioncue;

import net.runelite.api.Item;
import net.runelite.api.gameval.ItemID;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCueInventorySnapshotTest
{
	@Test
	public void ignoresUnusedTrailingSlots()
	{
		InteractionCueInventorySnapshot compact = snapshot(item(ItemID.RANARR_WEED, 1));
		InteractionCueInventorySnapshot padded = snapshot(
			item(ItemID.RANARR_WEED, 1),
			null,
			null
		);

		Assert.assertTrue(compact.matches(padded));
	}

	@Test
	public void keepsSlotAndQuantityChangesDistinct()
	{
		InteractionCueInventorySnapshot original = snapshot(
			item(ItemID.RANARR_WEED, 1),
			item(ItemID.RANARRVIAL, 2)
		);

		Assert.assertFalse(original.matches(snapshot(
			item(ItemID.RANARRVIAL, 2),
			item(ItemID.RANARR_WEED, 1)
		)));
		Assert.assertFalse(original.matches(snapshot(
			item(ItemID.RANARR_WEED, 1),
			item(ItemID.RANARRVIAL, 1)
		)));
	}

	@Test
	public void totalsDuplicateStacksByItemId()
	{
		InteractionCueInventorySnapshot inventory = snapshot(
			item(ItemID.RANARR_WEED, 3),
			item(ItemID.RANARRVIAL, 1),
			item(ItemID.RANARR_WEED, 7)
		);

		Assert.assertEquals(10L, inventory.getTotalQuantity(ItemID.RANARR_WEED));
		Assert.assertEquals(1L, inventory.getTotalQuantity(ItemID.RANARRVIAL));
		Assert.assertEquals(0L, inventory.getTotalQuantity(ItemID.COINS));
	}

	@Test
	public void findsFirstMatchingSlotInInventoryOrder()
	{
		InteractionCueInventorySnapshot inventory = snapshot(
			item(ItemID.RANARR_WEED, 1),
			null,
			item(ItemID.RANARRVIAL, 1),
			item(ItemID.COINS, 1),
			item(ItemID.RANARRVIAL, 1)
		);

		Assert.assertEquals(
			2,
			inventory.findFirstSlot(ItemID.RANARRVIAL, -1)
		);
	}

	@Test
	public void excludesReservedSourceSlotFromSearch()
	{
		InteractionCueInventorySnapshot inventory = snapshot(
			item(ItemID.RANARRVIAL, 1),
			null,
			item(ItemID.RANARRVIAL, 1)
		);

		Assert.assertEquals(
			2,
			inventory.findFirstSlot(ItemID.RANARRVIAL, 0)
		);
		Assert.assertEquals(
			-1,
			inventory.findFirstSlot(ItemID.RANARR_WEED, 0)
		);
	}

	@Test
	public void readsMissingSlotsAsEmpty()
	{
		InteractionCueInventorySnapshot inventory = snapshot(
			item(ItemID.RANARR_WEED, 3),
			item(ItemID.RANARRVIAL, 2)
		);

		Assert.assertEquals(ItemID.RANARR_WEED, inventory.getItemIdAt(0));
		Assert.assertEquals(3, inventory.getQuantityAt(0));
		Assert.assertEquals(-1, inventory.getItemIdAt(-1));
		Assert.assertEquals(0, inventory.getQuantityAt(-1));
		Assert.assertEquals(-1, inventory.getItemIdAt(2));
		Assert.assertEquals(0, inventory.getQuantityAt(2));
	}

	private static InteractionCueInventorySnapshot snapshot(Item... items)
	{
		return InteractionCueInventorySnapshot.capture(items);
	}

	private static Item item(int id, int quantity)
	{
		return new Item(id, quantity);
	}
}
