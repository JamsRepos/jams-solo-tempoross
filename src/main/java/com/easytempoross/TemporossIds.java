package com.easytempoross;

import java.util.HashSet;
import java.util.Set;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.TileObject;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;

/**
 * Tempoross IDs. Gameval first; a few objects still only exist on the
 * deprecated {@link net.runelite.api.ObjectID} names.
 */
final class TemporossIds
{
	static final int[] FISHING_SPOTS = {
		NpcID.TEMPOROSS_HARPOONFISH_FISHINGSPOT_NORTH,
		NpcID.TEMPOROSS_HARPOONFISH_FISHINGSPOT_SOUTH,
		NpcID.TEMPOROSS_HARPOONFISH_FISHINGSPOT_SPECIAL
	};

	static final int DOUBLE_SPOT = NpcID.TEMPOROSS_HARPOONFISH_FISHINGSPOT_SPECIAL;
	static final int SPIRIT_POOL = NpcID.TEMPOROSS_P2_FISHINGSPOT;

	static final int[] AMMO_CRATES = {
		NpcID.TEMPOROSS_NPC_CRATE_AMMUNITION_1,
		NpcID.TEMPOROSS_NPC_CRATE_AMMUNITION_2,
		NpcID.TEMPOROSS_NPC_CRATE_AMMUNITION_3,
		NpcID.TEMPOROSS_NPC_CRATE_AMMUNITION_4
	};

	static final int[] FIRES = {
		NpcID.TEMPOROSS_FIRE_HITBOX,
		NpcID.TEMPOROSS_FIRE_TARGET_NPC
	};

	static final int[] HOSTS = {
		NpcID.TEMPOROSS_INSTANCE_HOST_E,
		NpcID.TEMPOROSS_INSTANCE_HOST_W,
		NpcID.TEMPOROSS_INSTANCE_HOST_N,
		NpcID.TEMPOROSS_INSTANCE_HOST_S,
		NpcID.TEMPOROSS_INSTANCE_HOST_E_VICTORY,
		NpcID.TEMPOROSS_INSTANCE_HOST_W_VICTORY,
		NpcID.TEMPOROSS_INSTANCE_HOST_N_VICTORY,
		NpcID.TEMPOROSS_INSTANCE_HOST_S_VICTORY
	};

	static final int[] VICTORY_HOSTS = {
		NpcID.TEMPOROSS_INSTANCE_HOST_E_VICTORY,
		NpcID.TEMPOROSS_INSTANCE_HOST_W_VICTORY,
		NpcID.TEMPOROSS_INSTANCE_HOST_N_VICTORY,
		NpcID.TEMPOROSS_INSTANCE_HOST_S_VICTORY
	};

	// Both island shrines share this ID; north vs south is the tile, not the ID.
	static final int SHRINE = net.runelite.api.ObjectID.SHRINE_41236;
	static final int SHIP_PUMP = net.runelite.api.ObjectID.WATER_PUMP_41000;
	static final int DOCK_PUMP = net.runelite.api.ObjectID.WATER_PUMP_41004;
	static final int SOLO_LADDER = net.runelite.api.ObjectID.ROPE_LADDER_41305;
	static final int BUCKET_CRATE = net.runelite.api.ObjectID.BUCKETS;
	static final int ROPE_CRATE = net.runelite.api.ObjectID.ROPES;
	static final int HAMMER_CRATE = net.runelite.api.ObjectID.HAMMERS_40964;
	static final int HAMMER_CRATE_ALT = net.runelite.api.ObjectID.HAMMERS;
	static final int HARPOON_CRATE = net.runelite.api.ObjectID.HARPOONS;
	static final int DAMAGED_TOTEM = net.runelite.api.ObjectID.DAMAGED_TOTEM_POLE;
	static final int DAMAGED_TOTEM_2 = net.runelite.api.ObjectID.DAMAGED_TOTEM_POLE_41011;
	static final int DAMAGED_MAST = net.runelite.api.ObjectID.DAMAGED_MAST_40996;
	static final int DAMAGED_MAST_2 = net.runelite.api.ObjectID.DAMAGED_MAST_40997;
	static final int TOTEM_A = net.runelite.api.NullObjectID.NULL_41354;
	static final int TOTEM_B = net.runelite.api.NullObjectID.NULL_41355;
	static final int MAST_A = net.runelite.api.NullObjectID.NULL_41352;
	static final int MAST_B = net.runelite.api.NullObjectID.NULL_41353;
	static final int CLOUD = net.runelite.api.NullObjectID.NULL_41006;

	static final int[] HARPOONS = {
		ItemID.HARPOON,
		ItemID.HUNTING_BARBED_HARPOON,
		ItemID.DRAGON_HARPOON,
		ItemID.INFERNAL_HARPOON,
		ItemID.INFERNAL_HARPOON_EMPTY,
		ItemID.CRYSTAL_HARPOON,
		ItemID.CRYSTAL_HARPOON_INACTIVE,
		ItemID.TRAILBLAZER_HARPOON,
		ItemID.TRAILBLAZER_HARPOON_EMPTY,
		ItemID.TRAILBLAZER_HARPOON_NO_INFERNAL,
		ItemID.TRAILBLAZER_RELOADED_HARPOON,
		ItemID.TRAILBLAZER_RELOADED_HARPOON_EMPTY,
		ItemID.TRAILBLAZER_RELOADED_HARPOON_NO_INFERNAL
	};

	private static final Set<Integer> HARPOON_SET = toSet(HARPOONS);
	private static final Set<Integer> SPOT_SET = toSet(FISHING_SPOTS);
	private static final Set<Integer> AMMO_SET = toSet(AMMO_CRATES);
	private static final Set<Integer> FIRE_SET = toSet(FIRES);
	private static final Set<Integer> HOST_SET = toSet(HOSTS);
	private static final Set<Integer> VICTORY_SET = toSet(VICTORY_HOSTS);

	private TemporossIds()
	{
	}

	static boolean isHarpoon(int id)
	{
		return HARPOON_SET.contains(id);
	}

	static boolean isFishingSpot(int id)
	{
		return SPOT_SET.contains(id);
	}

	static boolean isHarpoonSpot(NPC npc)
	{
		if (npc == null)
		{
			return false;
		}
		int id = npc.getId();
		if (isSpiritPool(id) || !isFishingSpot(id))
		{
			return false;
		}
		return hasAction(npc, "Harpoon");
	}

	static boolean isSouthFishingSpot(int id)
	{
		return id == NpcID.TEMPOROSS_HARPOONFISH_FISHINGSPOT_SOUTH;
	}

	static boolean isDoubleSpot(int id)
	{
		return id == DOUBLE_SPOT;
	}

	static boolean isAmmoCrate(int id)
	{
		return AMMO_SET.contains(id);
	}

	static boolean isAmmoCrateNpc(NPC npc)
	{
		if (npc == null)
		{
			return false;
		}
		if (isAmmoCrate(npc.getId()) || hasAction(npc, "Check-ammo"))
		{
			return true;
		}
		NPCComposition comp = npc.getTransformedComposition();
		if (comp == null)
		{
			comp = npc.getComposition();
		}
		if (comp == null || comp.getName() == null)
		{
			return false;
		}
		return comp.getName().toLowerCase().contains("ammunition");
	}

	static boolean isFireNpc(int id)
	{
		return FIRE_SET.contains(id);
	}

	static boolean isFireTargetNpc(int id)
	{
		return id == NpcID.TEMPOROSS_FIRE_TARGET_NPC;
	}

	static boolean isActiveFireNpc(int id)
	{
		return id == NpcID.TEMPOROSS_FIRE_HITBOX;
	}

	static boolean isHost(int id)
	{
		return HOST_SET.contains(id);
	}

	static boolean isVictoryHost(int id)
	{
		return VICTORY_SET.contains(id);
	}

	static boolean isWestHost(int id)
	{
		return id == NpcID.TEMPOROSS_INSTANCE_HOST_W
			|| id == NpcID.TEMPOROSS_INSTANCE_HOST_W_VICTORY
			|| id == NpcID.TEMPOROSS_INSTANCE_HOST_N
			|| id == NpcID.TEMPOROSS_INSTANCE_HOST_N_VICTORY;
	}

	static boolean isSpiritPool(int id)
	{
		return id == SPIRIT_POOL;
	}

	static boolean isSpiritPoolAttackable(NPC npc)
	{
		return npc != null && isSpiritPool(npc.getId()) && hasAction(npc, "Harpoon");
	}

	static boolean isShrine(int id)
	{
		return id == SHRINE;
	}

	static boolean isPump(int id)
	{
		return id == SHIP_PUMP || id == DOCK_PUMP;
	}

	static boolean isSoloLadder(int id)
	{
		return id == SOLO_LADDER;
	}

	static boolean isBucketCrate(int id)
	{
		return id == BUCKET_CRATE;
	}

	static boolean isRopeCrate(int id)
	{
		return id == ROPE_CRATE;
	}

	static boolean isHammerCrate(int id)
	{
		return id == HAMMER_CRATE || id == HAMMER_CRATE_ALT;
	}

	static boolean isHarpoonCrate(int id)
	{
		return id == HARPOON_CRATE;
	}

	static boolean isDamagedTotem(int id)
	{
		return id == DAMAGED_TOTEM || id == DAMAGED_TOTEM_2;
	}

	static boolean isDamagedMast(int id)
	{
		return id == DAMAGED_MAST || id == DAMAGED_MAST_2;
	}

	static boolean isTotem(int id)
	{
		return id == TOTEM_A || id == TOTEM_B || isDamagedTotem(id);
	}

	static boolean isMast(int id)
	{
		return id == MAST_A || id == MAST_B || isDamagedMast(id);
	}

	static boolean isCloud(int id)
	{
		return id == CLOUD;
	}

	static boolean isTrackedObject(TileObject object)
	{
		if (object == null)
		{
			return false;
		}
		int id = object.getId();
		return isShrine(id) || isPump(id) || isSoloLadder(id) || isBucketCrate(id)
			|| isRopeCrate(id) || isHammerCrate(id) || isHarpoonCrate(id)
			|| isTotem(id) || isMast(id) || isCloud(id);
	}

	static boolean isTrackedNpc(NPC npc)
	{
		if (npc == null)
		{
			return false;
		}
		int id = npc.getId();
		if (isFishingSpot(id) || isAmmoCrateNpc(npc) || isFireNpc(id) || isHost(id)
			|| isSpiritPool(id))
		{
			return true;
		}
		return hasAction(npc, "Douse") || hasAction(npc, "Check-ammo") || hasAction(npc, "Leave")
			|| hasAction(npc, "Forfeit") || hasAction(npc, "Harpoon");
	}

	static boolean hasAction(NPC npc, String action)
	{
		if (npc == null || action == null)
		{
			return false;
		}
		NPCComposition comp = npc.getTransformedComposition();
		if (comp == null)
		{
			comp = npc.getComposition();
		}
		if (comp == null || comp.getActions() == null)
		{
			return false;
		}
		for (String a : comp.getActions())
		{
			if (action.equals(a))
			{
				return true;
			}
		}
		return false;
	}

	private static Set<Integer> toSet(int[] ids)
	{
		Set<Integer> set = new HashSet<>();
		for (int id : ids)
		{
			set.add(id);
		}
		return set;
	}
}
