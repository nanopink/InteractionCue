package com.interactioncue;

import java.util.Arrays;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

final class InteractionCueInventorySnapshot
{
	private final int[] itemIds;
	private final int[] quantities;

	private InteractionCueInventorySnapshot(int[] itemIds, int[] quantities)
	{
		this.itemIds = itemIds;
		this.quantities = quantities;
	}

	static InteractionCueInventorySnapshot capture(ItemContainer inventory)
	{
		return capture(inventory == null ? null : inventory.getItems());
	}

	static InteractionCueInventorySnapshot capture(Item[] items)
	{
		int length = items == null ? 0 : items.length;
		while (length > 0 && isEmpty(items[length - 1]))
		{
			length--;
		}

		int[] itemIds = new int[length];
		int[] quantities = new int[length];
		for (int slot = 0; slot < length; slot++)
		{
			Item item = items[slot];
			if (isEmpty(item))
			{
				itemIds[slot] = -1;
				continue;
			}

			itemIds[slot] = item.getId();
			quantities[slot] = item.getQuantity();
		}

		return new InteractionCueInventorySnapshot(itemIds, quantities);
	}

	boolean matches(InteractionCueInventorySnapshot other)
	{
		return other != null
			&& Arrays.equals(itemIds, other.itemIds)
			&& Arrays.equals(quantities, other.quantities);
	}

	boolean containsAt(int slot, int itemId, int quantity)
	{
		return slot >= 0
			&& slot < itemIds.length
			&& itemIds[slot] == itemId
			&& quantities[slot] == quantity;
	}

	int findFirstSlot(int itemId, int excludedSlot)
	{
		for (int slot = 0; slot < itemIds.length; slot++)
		{
			if (slot != excludedSlot && itemIds[slot] == itemId && quantities[slot] > 0)
			{
				return slot;
			}
		}
		return -1;
	}

	int getItemIdAt(int slot)
	{
		return slot >= 0 && slot < itemIds.length ? itemIds[slot] : -1;
	}

	int getQuantityAt(int slot)
	{
		return slot >= 0 && slot < quantities.length ? quantities[slot] : 0;
	}

	long getTotalQuantity(int itemId)
	{
		if (itemId <= 0)
		{
			return 0;
		}

		long total = 0;
		for (int slot = 0; slot < itemIds.length; slot++)
		{
			if (itemIds[slot] == itemId)
			{
				total += quantities[slot];
			}
		}
		return total;
	}

	int countReducedSlots(
		InteractionCueInventorySnapshot current,
		int itemId)
	{
		if (current == null || itemId <= 0)
		{
			return 0;
		}

		int changedSlots = 0;
		for (int slot = 0; slot < itemIds.length; slot++)
		{
			if (itemIds[slot] == itemId
				&& (current.getItemIdAt(slot) != itemId
				|| current.getQuantityAt(slot) < quantities[slot]))
			{
				changedSlots++;
			}
		}
		return changedSlots;
	}

	private static boolean isEmpty(Item item)
	{
		return item == null || item.getId() <= 0 || item.getQuantity() <= 0;
	}
}
