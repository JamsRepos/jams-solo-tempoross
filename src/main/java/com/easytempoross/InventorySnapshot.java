package com.easytempoross;

import lombok.Value;

@Value
public class InventorySnapshot
{
	int rawFish;
	int cookedFish;
	int crystalFish;
	int emptyBuckets;
	int waterBuckets;
	int emptySlots;
	boolean hasHarpoon;
	boolean hasHammer;
	boolean hasImcando;
	boolean hasRope;
	boolean spiritAnglerComplete;

	public int getTotalFish()
	{
		return rawFish + cookedFish + crystalFish;
	}

	public int getDumpableFish()
	{
		return cookedFish + crystalFish;
	}

	public int getBuckets()
	{
		return emptyBuckets + waterBuckets;
	}

	public boolean hasHammerTool()
	{
		return hasHammer || hasImcando;
	}

	public boolean hasRopeOrOutfit()
	{
		return hasRope || spiritAnglerComplete;
	}

	public boolean toolsReady()
	{
		return hasHarpoon && hasHammerTool() && hasRopeOrOutfit() && getBuckets() >= RotationConstants.BUCKETS_NEEDED;
	}
}
