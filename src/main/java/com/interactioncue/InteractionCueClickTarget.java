package com.interactioncue;

import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.gameval.InterfaceID;

final class InteractionCueClickTarget
{
	private InteractionCueClickTarget()
	{
	}

	static boolean isInventoryAction(MenuAction action, int parentId)
	{
		return action == MenuAction.WIDGET_TARGET_ON_WIDGET
			&& parentId == InterfaceID.Inventory.ITEMS;
	}

	static boolean matches(
		int menuSlot,
		int widgetSlot,
		int widgetItemId,
		Item inventoryItem)
	{
		if (menuSlot < 0 || widgetSlot != menuSlot)
		{
			return false;
		}

		if (inventoryItem == null
			|| inventoryItem.getId() <= 0
			|| inventoryItem.getQuantity() <= 0)
		{
			return false;
		}

		return widgetItemId <= 0 || widgetItemId == inventoryItem.getId();
	}
}
