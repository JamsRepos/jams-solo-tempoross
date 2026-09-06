package com.easytempoross;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecoveryPolicyTest
{
	@Test
	public void waveIncomingTethers()
	{
		GameSnapshot snap = inGame()
			.waveIncoming(true)
			.tethered(false)
			.busyCooking(true)
			.rawFish(8)
			.build();
		assertEquals(RecoveryKind.WAVE_TETHER, RecoveryPolicy.decide(snap));
	}

	@Test
	public void brokenTotemRepairsBeforeTether()
	{
		GameSnapshot snap = inGame()
			.waveIncoming(true)
			.totemBroken(true)
			.build();
		assertEquals(RecoveryKind.WAVE_REPAIR, RecoveryPolicy.decide(snap));
	}

	@Test
	public void missingHammerDoesNotRecover()
	{
		GameSnapshot snap = inGame()
			.hasHammer(false)
			.build();
		assertEquals(RecoveryKind.NONE, RecoveryPolicy.decide(snap));
	}

	@Test
	public void intensityDumpDoesNotWaitForNineteen()
	{
		GameSnapshot snap = inGame()
			.intensity(92)
			.cookedFish(11)
			.rawFish(0)
			.onIsland(true)
			.build();
		assertEquals(RecoveryKind.INTENSITY_DUMP, RecoveryPolicy.decide(snap));
	}

	@Test
	public void intensityCooksRawFirst()
	{
		GameSnapshot snap = inGame()
			.intensity(92)
			.rawFish(6)
			.cookedFish(4)
			.build();
		assertEquals(RecoveryKind.INTENSITY_COOK, RecoveryPolicy.decide(snap));
	}

	@Test
	public void energyZeroIsHappyPathSpirit()
	{
		GameSnapshot snap = inGame()
			.energy(0)
			.rawFish(7)
			.spiritPoolUp(true)
			.build();
		assertEquals(RecoveryKind.NONE, RecoveryPolicy.decide(snap));
	}

	@Test
	public void lostBucketsGoesToCrate()
	{
		GameSnapshot snap = inGame()
			.waterBuckets(2)
			.emptyBuckets(0)
			.build();
		assertEquals(RecoveryKind.LOST_BUCKETS, RecoveryPolicy.decide(snap));
	}

	@Test
	public void fireOnPlayerDoesNotInterruptFishing()
	{
		GameSnapshot snap = inGame()
			.fireOnPlayerOrBlocking(true)
			.nearbyFires(3)
			.waterBuckets(4)
			.rawFish(5)
			.busyFishing(true)
			.build();
		assertEquals(RecoveryKind.NONE, RecoveryPolicy.decide(snap));
	}

	@Test
	public void dumpedAllNineteenGoesToHappySpirit()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.cookedFish(0)
			.energy(0)
			.spiritPoolUp(true)
			.build();
		assertEquals(RecoveryKind.NONE, RecoveryPolicy.decide(snap));
	}

	@Test
	public void washedToDockIsHappyPathNotRecovery()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.inMinigame(false)
			.atUnkah(true)
			.hasHammer(true)
			.hasRopeOrOutfit(true)
			.hasHarpoon(true)
			.requireHarpoon(true)
			.waterBuckets(4)
			.energy(-1)
			.intensity(-1)
			.build();
		assertEquals(RecoveryKind.NONE, RecoveryPolicy.decide(snap));
	}

	@Test
	public void lostHarpoonRecoversWhenRequired()
	{
		GameSnapshot snap = inGame()
			.hasHarpoon(false)
			.requireHarpoon(true)
			.build();
		assertEquals(RecoveryKind.LOST_HARPOON, RecoveryPolicy.decide(snap));
	}

	@Test
	public void barehandedSkipsHarpoonRecovery()
	{
		GameSnapshot snap = inGame()
			.hasHarpoon(false)
			.requireHarpoon(false)
			.build();
		assertEquals(RecoveryKind.NONE, RecoveryPolicy.decide(snap));
	}

	private static GameSnapshot.GameSnapshotBuilder inGame()
	{
		return GameSnapshot.builder()
			.inMinigame(true)
			.onIsland(true)
			.hasHammer(true)
			.hasRopeOrOutfit(true)
			.hasHarpoon(true)
			.requireHarpoon(true)
			.waterBuckets(4)
			.emptyBuckets(0)
			.energy(100)
			.intensity(20)
			.hudVisible(true);
	}
}
