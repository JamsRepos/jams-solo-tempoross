package com.easytempoross;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;

@Singleton
public class InventoryChecker
{
	private static final int INVENTORY_SIZE = 28;

	private final Client client;

	@Inject
	InventoryChecker(Client client)
	{
		this.client = client;
	}

	public InventorySnapshot scan()
	{
		int raw = 0;
		int cooked = 0;
		int crystal = 0;
		int emptyBuckets = 0;
		int waterBuckets = 0;
		int empty = 0;
		boolean harpoon = false;
		boolean hammer = false;
		boolean imcando = false;
		boolean rope = false;

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			empty = INVENTORY_SIZE;
		}
		else
		{
			Item[] items = inventory.getItems();
			for (int i = 0; i < INVENTORY_SIZE; i++)
			{
				Item item = i < items.length ? items[i] : null;
				if (item == null || item.getId() < 0)
				{
					empty++;
					continue;
				}
				int id = item.getId();
				int qty = Math.max(1, item.getQuantity());
				if (id == ItemID.TEMPOROSS_RAW_HARPOONFISH)
				{
					raw += qty;
				}
				else if (id == ItemID.TEMPOROSS_HARPOONFISH)
				{
					cooked += qty;
				}
				else if (id == ItemID.TEMPOROSS_CRYSTALLISED_HARPOONFISH)
				{
					crystal += qty;
				}
				else if (id == ItemID.BUCKET_EMPTY)
				{
					emptyBuckets += qty;
				}
				else if (id == ItemID.BUCKET_WATER)
				{
					waterBuckets += qty;
				}
				else if (id == ItemID.ROPE)
				{
					rope = true;
				}
				else if (id == ItemID.HAMMER)
				{
					hammer = true;
				}
				else if (id == ItemID.IMCANDO_HAMMER || id == ItemID.IMCANDO_HAMMER_OFFHAND)
				{
					imcando = true;
				}
				else if (TemporossIds.isHarpoon(id))
				{
					harpoon = true;
				}
			}
		}

		boolean hat = false;
		boolean top = false;
		boolean legs = false;
		boolean boots = false;
		ItemContainer worn = client.getItemContainer(InventoryID.WORN);
		if (worn != null)
		{
			if (isSlot(worn, EquipmentInventorySlot.WEAPON, TemporossIds::isHarpoon))
			{
				harpoon = true;
			}
			if (isSlotId(worn, EquipmentInventorySlot.WEAPON, ItemID.IMCANDO_HAMMER)
				|| isSlotId(worn, EquipmentInventorySlot.SHIELD, ItemID.IMCANDO_HAMMER_OFFHAND)
				|| isSlotId(worn, EquipmentInventorySlot.SHIELD, ItemID.IMCANDO_HAMMER))
			{
				imcando = true;
			}
			hat = isSlotId(worn, EquipmentInventorySlot.HEAD, ItemID.SPIRIT_ANGLER_HAT);
			top = isSlotId(worn, EquipmentInventorySlot.BODY, ItemID.SPIRIT_ANGLER_TOP);
			legs = isSlotId(worn, EquipmentInventorySlot.LEGS, ItemID.SPIRIT_ANGLER_LEGS);
			boots = isSlotId(worn, EquipmentInventorySlot.BOOTS, ItemID.SPIRIT_ANGLER_BOOTS);
		}

		return new InventorySnapshot(
			raw,
			cooked,
			crystal,
			emptyBuckets,
			waterBuckets,
			empty,
			harpoon,
			hammer,
			imcando,
			rope,
			hat && top && legs && boots);
	}

	private static boolean isSlotId(ItemContainer worn, EquipmentInventorySlot slot, int id)
	{
		Item item = worn.getItem(slot.getSlotIdx());
		return item != null && item.getId() == id;
	}

	private interface IdMatch
	{
		boolean test(int id);
	}

	private static boolean isSlot(ItemContainer worn, EquipmentInventorySlot slot, IdMatch match)
	{
		Item item = worn.getItem(slot.getSlotIdx());
		return item != null && match.test(item.getId());
	}
}
