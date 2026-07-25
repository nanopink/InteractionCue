package com.interactioncue;

import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import org.junit.Assert;
import org.junit.Test;

public class InteractionCuePluginBehaviorTest
{
	@Test
	public void usesActualClickedTarget()
	{
		Assert.assertEquals(
			"Ranarr potion (unf)",
			InteractionCuePlugin.getMenuTarget(
				"<col=ff9040>Ranarr weed</col> -> <col=ff9040>Ranarr potion (unf)</col>"
			)
		);
	}

	@Test
	public void qualifiesOnlyModernInventoryTargetActions()
	{
		Assert.assertTrue(InteractionCueClickTarget.isInventoryAction(
			MenuAction.WIDGET_TARGET_ON_WIDGET,
			InterfaceID.Inventory.ITEMS
		));
		Assert.assertFalse(InteractionCueClickTarget.isInventoryAction(
			MenuAction.CANCEL,
			InterfaceID.Inventory.ITEMS
		));
		Assert.assertFalse(InteractionCueClickTarget.isInventoryAction(
			MenuAction.WIDGET_TARGET_ON_WIDGET,
			InterfaceID.MagicSpellbook.HIGH_ALCHEMY
		));
	}

	@Test
	public void validatesCanonicalTargetSlotAndItem()
	{
		Item target = new Item(ItemID.RANARRVIAL, 1);

		Assert.assertTrue(InteractionCueClickTarget.matches(
			3,
			3,
			ItemID.RANARRVIAL,
			target
		));
		Assert.assertTrue(InteractionCueClickTarget.matches(3, 3, -1, target));
		Assert.assertFalse(InteractionCueClickTarget.matches(
			-1,
			3,
			ItemID.RANARRVIAL,
			target
		));
		Assert.assertFalse(InteractionCueClickTarget.matches(
			2,
			3,
			ItemID.RANARRVIAL,
			target
		));
		Assert.assertFalse(InteractionCueClickTarget.matches(
			3,
			3,
			ItemID.GUAMVIAL,
			target
		));
		Assert.assertFalse(InteractionCueClickTarget.matches(
			3,
			3,
			ItemID.RANARRVIAL,
			null
		));
		Assert.assertFalse(InteractionCueClickTarget.matches(
			3,
			3,
			ItemID.RANARRVIAL,
			new Item(ItemID.RANARRVIAL, 0)
		));
	}

	@Test
	public void selectedInventoryItemMustRemainInExactSlot()
	{
		Item source = new Item(ItemID.RANARR_WEED, 1);
		Item[] moved = {new Item(ItemID.RANARRVIAL, 1), source};

		Assert.assertNull(InteractionCueInventorySelection.find(
			moved,
			0,
			ItemID.RANARR_WEED
		));
		Assert.assertSame(source, InteractionCueInventorySelection.find(
			moved,
			1,
			ItemID.RANARR_WEED
		));
		Assert.assertNull(InteractionCueInventorySelection.find(
			new Item[]{new Item(ItemID.RANARRVIAL, 1)},
			0,
			ItemID.RANARR_WEED
		));
	}

	@Test
	public void recognizesMagicSpellbookComponents()
	{
		Assert.assertTrue(InteractionCuePlugin.isSpellSelection(
			InterfaceID.MagicSpellbook.HIGH_ALCHEMY
		));
		Assert.assertFalse(InteractionCuePlugin.isSpellSelection(
			InterfaceID.Inventory.ITEMS
		));
	}

	@Test
	public void recognizesProductionDialogs()
	{
		Assert.assertTrue(InteractionCuePlugin.isProductionDialog(
			InterfaceID.SKILLMULTI
		));
		Assert.assertTrue(InteractionCuePlugin.isProductionDialog(
			InterfaceID.GRAPHICAL_MULTI
		));
		Assert.assertTrue(InteractionCuePlugin.isProductionDialog(
			InterfaceID.CHATMENU
		));
		Assert.assertFalse(InteractionCuePlugin.isProductionDialog(
			InterfaceID.INVENTORY
		));
	}

	@Test
	public void distinguishesSkillmultiQuantityAndProductControls()
	{
		Assert.assertTrue(InteractionCuePlugin.isProductionActionType(
			MenuAction.CC_OP
		));
		Assert.assertTrue(InteractionCuePlugin.isProductionActionType(
			MenuAction.WIDGET_CONTINUE
		));
		Assert.assertFalse(InteractionCuePlugin.isProductionActionType(
			MenuAction.WALK
		));

		Assert.assertTrue(InteractionCuePlugin.isSkillmultiQuantityControl(
			InterfaceID.Skillmulti._1
		));
		Assert.assertTrue(InteractionCuePlugin.isSkillmultiQuantityControl(
			InterfaceID.Skillmulti._5
		));
		Assert.assertTrue(InteractionCuePlugin.isSkillmultiQuantityControl(
			InterfaceID.Skillmulti._10
		));
		Assert.assertTrue(InteractionCuePlugin.isSkillmultiQuantityControl(
			InterfaceID.Skillmulti.OTHER
		));
		Assert.assertTrue(InteractionCuePlugin.isSkillmultiQuantityControl(
			InterfaceID.Skillmulti.X
		));
		Assert.assertTrue(InteractionCuePlugin.isSkillmultiQuantityControl(
			InterfaceID.Skillmulti.ALL
		));
		Assert.assertFalse(InteractionCuePlugin.isSkillmultiQuantityControl(
			InterfaceID.Skillmulti.A
		));

		Assert.assertTrue(InteractionCuePlugin.isSkillmultiProduct(
			InterfaceID.Skillmulti.A
		));
		Assert.assertTrue(InteractionCuePlugin.isSkillmultiProduct(
			InterfaceID.Skillmulti.R
		));
		Assert.assertFalse(InteractionCuePlugin.isSkillmultiProduct(
			InterfaceID.Skillmulti.ALL
		));
	}

	@Test
	public void recognizesSubmittedSkillmultiProductState()
	{
		Assert.assertTrue(InteractionCuePlugin.isSubmittedSkillmultiProduct(
			1,
			null,
			null
		));
		Assert.assertFalse(InteractionCuePlugin.isSubmittedSkillmultiProduct(
			0,
			null,
			null
		));
		Assert.assertFalse(InteractionCuePlugin.isSubmittedSkillmultiProduct(
			1,
			new Object[0],
			null
		));
		Assert.assertFalse(InteractionCuePlugin.isSubmittedSkillmultiProduct(
			1,
			null,
			new Object[0]
		));
	}

	@Test
	public void parsesOnlyExplicitLegacyProductionQuantities()
	{
		Assert.assertEquals(
			1,
			InteractionCuePlugin.productionQuantityFromText(
				"<col=ffffff>Make 1</col>"
			)
		);
		Assert.assertEquals(
			7,
			InteractionCuePlugin.productionQuantityFromText("Make-7")
		);
		Assert.assertEquals(
			InteractionCuePendingInteraction.UNBOUNDED_ACTIONS,
			InteractionCuePlugin.productionQuantityFromText("Make All")
		);
		Assert.assertEquals(
			InteractionCuePendingInteraction.UNKNOWN_ACTIONS,
			InteractionCuePlugin.productionQuantityFromText("Make X")
		);
		Assert.assertEquals(
			InteractionCuePendingInteraction.UNKNOWN_ACTIONS,
			InteractionCuePlugin.productionQuantityFromText(
				"Make 4-dose potion"
			)
		);
	}

	@Test
	public void recognizesExplicitProductionCancellation()
	{
		Assert.assertTrue(InteractionCuePlugin.isCancellationMenuAction(
			MenuAction.CANCEL,
			""
		));
		Assert.assertTrue(InteractionCuePlugin.isCancellationMenuAction(
			MenuAction.CC_OP,
			"<col=ffffff>Cancel</col>"
		));
		Assert.assertTrue(InteractionCuePlugin.isCancellationMenuAction(
			MenuAction.CC_OP,
			"Never mind"
		));
		Assert.assertTrue(InteractionCuePlugin.isCancellationMenuAction(
			MenuAction.WIDGET_CLOSE,
			""
		));
		Assert.assertFalse(InteractionCuePlugin.isCancellationMenuAction(
			MenuAction.CC_OP,
			"Make All"
		));
	}

	@Test
	public void recognizesEscapeOnlyForFinalDialogClose()
	{
		Assert.assertTrue(InteractionCuePlugin.isEscapeCancellation(true, true));
		Assert.assertFalse(InteractionCuePlugin.isEscapeCancellation(false, true));
		Assert.assertFalse(InteractionCuePlugin.isEscapeCancellation(true, false));
	}

	@Test
	public void recognizesActionFailureMessages()
	{
		Assert.assertTrue(InteractionCuePlugin.isActionFailureMessage(
			"<col=000000>Nothing interesting happens.</col>"
		));
		Assert.assertTrue(InteractionCuePlugin.isActionFailureMessage(
			"You don't have enough inventory space."
		));
		Assert.assertTrue(InteractionCuePlugin.isActionFailureMessage(
			"You need a higher Herblore level."
		));
	}

	@Test
	public void ignoresUnrelatedMessages()
	{
		Assert.assertFalse(InteractionCuePlugin.isActionFailureMessage(null));
		Assert.assertFalse(InteractionCuePlugin.isActionFailureMessage("Welcome to RuneScape."));
	}
}
