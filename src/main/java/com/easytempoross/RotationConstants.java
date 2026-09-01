package com.easytempoross;

import net.runelite.api.coords.WorldPoint;

/**
 * Solo rotation counts. Easy to tweak after a live game.
 */
final class RotationConstants
{
	static final int FIRST_COOK_AT = 8;
	static final int INVENTORY_TARGET = 19;
	static final int FIRST_DUMP_KEEP = 3;
	static final int FIRST_DEPOSIT_COUNT = INVENTORY_TARGET - FIRST_DUMP_KEEP;
	static final int BUCKETS_NEEDED = 4;
	static final int INTENSITY_EMERGENCY = 90;
	static final int ENERGY_SPIRIT = 0;
	static final int ENERGY_FULL = 94;
	static final int ENERGY_DUMP16_DONE = 85;
	static final int AT_TARGET_TILES = 1;
	static final int TEMPOROSS_REGION = 12076;
	static final int COVE_CENTER_X = 3047;
	static final int FISH_NEAR_SHRINE = 20;
	static final int FIRE_ISLAND_TILES = 24;
	static final int COOK_WORTH_TRIP = 8;
	static final int LANDMARK_NEAR_TILES = 12;
	static final int NORTH_SHORE_MIN_Y = 2850;
	static final WorldPoint NORTH_SHRINE = new WorldPoint(3041, 2873, 0);
	static final WorldPoint NORTH_SPIRIT = new WorldPoint(3047, 2856, 0);
	static final WorldPoint NORTH_DOCK = new WorldPoint(3048, 2858, 0);
	/** West boat crate deposit tile — a fire often spawns here while loading. */
	static final WorldPoint DEPOSIT_STAND = new WorldPoint(3037, 2850, 0);
	/** One tile south-east; still in range to keep loading the crate. */
	static final WorldPoint DEPOSIT_SAFE = new WorldPoint(3038, 2849, 0);
	/** A fire this close to the loading spot is close enough to burn you. */
	static final int DEPOSIT_FIRE_TILES = 1;
	/** Chime on each of the last few deposits so you can look away while loading. */
	static final int DEPOSIT_CHIME_FROM = 3;

	private RotationConstants()
	{
	}

	static boolean isNear(WorldPoint point, WorldPoint landmark)
	{
		return point != null && landmark != null && point.distanceTo(landmark) <= LANDMARK_NEAR_TILES;
	}
}
