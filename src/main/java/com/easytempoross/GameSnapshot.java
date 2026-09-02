package com.easytempoross;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class GameSnapshot
{
	boolean inMinigame;
	boolean atUnkah;
	boolean onShip;
	boolean onIsland;
	boolean waveIncoming;
	boolean tethered;
	boolean totemBroken;
	boolean hasHammer;
	boolean hasRopeOrOutfit;
	boolean hasHarpoon;
	int emptyBuckets;
	int waterBuckets;
	int rawFish;
	int cookedFish;
	int crystalFish;
	int emptySlots;
	int energy;
	int intensity;
	int essence;
	int points;
	boolean spiritPoolUp;
	boolean spiritPoolAttackable;
	boolean spiritPoolDone;
	boolean fireOnPlayerOrBlocking;
	int nearbyFires;
	boolean dump16Done;
	boolean firstCookDone;
	boolean douseDone;
	boolean depositingKeep3;
	boolean depositingAll;
	int depositKeep3StopAt;
	int depositAllStopAt;
	boolean needsSpirit;
	int spiritCycles;
	boolean victory;
	boolean doubleSpotUp;
	boolean busyFishing;
	boolean busyCooking;
	boolean hudVisible;

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

	public static GameSnapshot empty()
	{
		return GameSnapshot.builder()
			.energy(-1)
			.intensity(-1)
			.essence(-1)
			.points(-1)
			.build();
	}
}
