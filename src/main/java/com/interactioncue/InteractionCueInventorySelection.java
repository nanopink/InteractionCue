package com.interactioncue;

import net.runelite.api.Item;

final class InteractionCueInventorySelection
{
	private InteractionCueInventorySelection()
	{
	}

	static Item find(Item[] items, int slot, int itemId)
	{
		if (items == null || slot < 0 || slot >= items.length)
		{
			return null;
		}

		Item item = items[slot];
		return item != null
			&& item.getId() == itemId
			&& item.getQuantity() > 0
			? item
			: null;
	}
}
