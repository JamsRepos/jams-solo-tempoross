package com.easytempoross;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HappyPathPolicyTest
{
	@Test
	public void dockSoloStartWhenReady()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.atUnkah(true)
			.hasHammer(true)
			.hasRopeOrOutfit(true)
			.hasHarpoon(true)
			.waterBuckets(4)
			.energy(-1)
			.intensity(-1)
			.build();
		assertEquals(HappyKind.SOLO_START, HappyPathPolicy.decide(snap));
	}

	@Test
	public void dockSoloStartWithoutHammer()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.atUnkah(true)
			.hasHammer(false)
			.hasRopeOrOutfit(true)
			.hasHarpoon(true)
			.waterBuckets(4)
			.energy(-1)
			.intensity(-1)
			.build();
		assertEquals(HappyKind.SOLO_START, HappyPathPolicy.decide(snap));
	}

	@Test
	public void infernalCookedCountsTowardFirstEight()
	{
		GameSnapshot snap = inGame()
			.rawFish(0)
			.cookedFish(5)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void infernalEightCookedSkipsCookStep()
	{
		GameSnapshot snap = inGame()
			.rawFish(0)
			.cookedFish(8)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void fishUntilEightBeforeFirstCook()
	{
		GameSnapshot snap = inGame()
			.rawFish(5)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void cookAtEight()
	{
		GameSnapshot snap = inGame()
			.rawFish(8)
			.build();
		assertEquals(HappyKind.COOK, HappyPathPolicy.decide(snap));
	}

	@Test
	public void doubleSpotBeatsCookAtEightInPhase1()
	{
		GameSnapshot snap = inGame()
			.rawFish(8)
			.doubleSpotUp(true)
			.build();
		assertEquals(HappyKind.FISH_DOUBLE, HappyPathPolicy.decide(snap));
	}

	@Test
	public void fiveRawIsNotWorthTheShrineTrip()
	{
		GameSnapshot snap = inGame()
			.rawFish(5)
			.dump16Done(true)
			.douseDone(true)
			.energy(40)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void doubleSpotBeatsFiresOnIsland()
	{
		GameSnapshot snap = inGame()
			.rawFish(8)
			.cookedFish(3)
			.doubleSpotUp(true)
			.dump16Done(true)
			.douseDone(true)
			.nearbyFires(3)
			.energy(40)
			.build();
		assertEquals(HappyKind.FISH_DOUBLE, HappyPathPolicy.decide(snap));
	}

	@Test
	public void doubleSpotInterruptsCookAfterDump16()
	{
		GameSnapshot snap = inGame()
			.rawFish(8)
			.cookedFish(3)
			.doubleSpotUp(true)
			.dump16Done(true)
			.douseDone(true)
			.energy(40)
			.build();
		assertEquals(HappyKind.FISH_DOUBLE, HappyPathPolicy.decide(snap));
	}

	@Test
	public void capsFishingAtNineteenWithExtraInventorySpace()
	{
		GameSnapshot snap = inGame()
			.firstCookDone(true)
			.rawFish(15)
			.emptySlots(23)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));

		GameSnapshot atCap = inGame()
			.firstCookDone(true)
			.rawFish(19)
			.emptySlots(23)
			.build();
		assertEquals(HappyKind.COOK, HappyPathPolicy.decide(atCap));
	}

	@Test
	public void depositSixteenOnlyFromFirstBatch()
	{
		GameSnapshot snap = inGame()
			.cookedFish(19)
			.depositingKeep3(true)
			.depositKeep3StopAt(3)
			.build();
		assertEquals(HappyKind.DEPOSIT_KEEP3, HappyPathPolicy.decide(snap));

		GameSnapshot done = inGame()
			.cookedFish(3)
			.depositingKeep3(false)
			.depositKeep3StopAt(3)
			.dump16Done(true)
			.douseDone(false)
			.nearbyFires(2)
			.build();
		assertEquals(HappyKind.DOUSE, HappyPathPolicy.decide(done));
	}

	@Test
	public void midDepositKeepsDepositEvenWithRawFish()
	{
		GameSnapshot keep3 = inGame()
			.cookedFish(15)
			.rawFish(4)
			.depositingKeep3(true)
			.depositKeep3StopAt(3)
			.build();
		assertEquals(HappyKind.DEPOSIT_KEEP3, HappyPathPolicy.decide(keep3));

		GameSnapshot all = inGame()
			.cookedFish(15)
			.rawFish(4)
			.dump16Done(true)
			.douseDone(true)
			.depositingAll(true)
			.depositAllStopAt(0)
			.energy(40)
			.build();
		assertEquals(HappyKind.DEPOSIT, HappyPathPolicy.decide(all));
	}

	@Test
	public void depositNineteenOnlyWhenOverFished()
	{
		GameSnapshot snap = inGame()
			.cookedFish(22)
			.dump16Done(true)
			.douseDone(true)
			.depositingAll(true)
			.depositAllStopAt(3)
			.build();
		assertEquals(HappyKind.DEPOSIT, HappyPathPolicy.decide(snap));

		GameSnapshot capped = inGame()
			.cookedFish(3)
			.dump16Done(true)
			.douseDone(true)
			.depositingAll(false)
			.depositAllStopAt(3)
			.needsSpirit(true)
			.spiritPoolUp(true)
			.spiritPoolAttackable(true)
			.build();
		assertEquals(HappyKind.SPIRIT, HappyPathPolicy.decide(capped));
	}

	@Test
	public void dumpSixteenKeepThree()
	{
		GameSnapshot snap = inGame()
			.cookedFish(19)
			.energy(100)
			.dump16Done(false)
			.build();
		assertEquals(HappyKind.DEPOSIT_KEEP3, HappyPathPolicy.decide(snap));
	}

	@Test
	public void staysOnDepositAfterOneFish()
	{
		GameSnapshot snap = inGame()
			.cookedFish(18)
			.energy(99)
			.dump16Done(false)
			.depositingKeep3(true)
			.build();
		assertEquals(HappyKind.DEPOSIT_KEEP3, HappyPathPolicy.decide(snap));
	}

	@Test
	public void eightCookedDoesNotDepositYet()
	{
		GameSnapshot snap = inGame()
			.cookedFish(8)
			.energy(100)
			.dump16Done(false)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void fireDuringFishDoesNotDouse()
	{
		GameSnapshot snap = inGame()
			.rawFish(5)
			.nearbyFires(4)
			.fireOnPlayerOrBlocking(true)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void energyZeroWhileLoadingDoesNotSpirit()
	{
		GameSnapshot snap = inGame()
			.energy(0)
			.rawFish(7)
			.spiritPoolUp(true)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void spiritPoolUpWhileLoadingDoesNotSpirit()
	{
		GameSnapshot snap = inGame()
			.rawFish(5)
			.spiritPoolUp(true)
			.energy(20)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void emptyBucketsMidRoundDoNotFill()
	{
		GameSnapshot snap = inGame()
			.emptyBuckets(4)
			.waterBuckets(0)
			.cookedFish(3)
			.dump16Done(true)
			.douseDone(true)
			.energy(40)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void spiritPoolWithoutHarpoonMovesOn()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.douseDone(true)
			.energy(40)
			.needsSpirit(true)
			.spiritPoolUp(true)
			.spiritPoolAttackable(false)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void spiritDoneAfterHarpoonGoneGoesToFishEvenIfEnergyIsLow()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.douseDone(true)
			.energy(40)
			.needsSpirit(true)
			.spiritPoolDone(true)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void attackableSpiritPoolKeepsHarpooning()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.douseDone(true)
			.energy(40)
			.needsSpirit(true)
			.spiritPoolUp(true)
			.spiritPoolAttackable(true)
			.build();
		assertEquals(HappyKind.SPIRIT, HappyPathPolicy.decide(snap));
	}

	@Test
	public void fillBucketsOnlyAtShipStart()
	{
		GameSnapshot snap = inGame()
			.onShip(true)
			.onIsland(false)
			.emptyBuckets(4)
			.waterBuckets(0)
			.energy(100)
			.dump16Done(false)
			.build();
		assertEquals(HappyKind.FILL_SHIP, HappyPathPolicy.decide(snap));
	}

	@Test
	public void victoryFillsAtNorthDockThenLeaves()
	{
		GameSnapshot snap = inGame()
			.victory(true)
			.emptyBuckets(4)
			.waterBuckets(0)
			.energy(0)
			.build();
		assertEquals(HappyKind.REFILL_DOCK, HappyPathPolicy.decide(snap));
	}

	@Test
	public void victoryLeavesOnceBucketsAreFull()
	{
		GameSnapshot snap = inGame()
			.victory(true)
			.emptyBuckets(0)
			.waterBuckets(4)
			.energy(0)
			.build();
		assertEquals(HappyKind.LEAVE_GAME, HappyPathPolicy.decide(snap));
	}

	@Test
	public void dockRefillsAfterTheGame()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.atUnkah(true)
			.hasHammer(true)
			.hasRopeOrOutfit(true)
			.hasHarpoon(true)
			.emptyBuckets(4)
			.energy(-1)
			.intensity(-1)
			.build();
		assertEquals(HappyKind.REFILL_DOCK, HappyPathPolicy.decide(snap));
	}

	@Test
	public void fillBucketsEvenIfEnergyIsUnknown()
	{
		GameSnapshot snap = inGame()
			.onShip(true)
			.onIsland(false)
			.emptyBuckets(4)
			.waterBuckets(0)
			.energy(-1)
			.dump16Done(false)
			.build();
		assertEquals(HappyKind.FILL_SHIP, HappyPathPolicy.decide(snap));
	}

	@Test
	public void afterDumpAllGoesToSpiritEvenAtFullEnergy()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.cookedFish(0)
			.energy(100)
			.needsSpirit(true)
			.build();
		assertEquals(HappyKind.SPIRIT, HappyPathPolicy.decide(snap));
	}

	@Test
	public void afterSpiritGoesBackToFishing()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.douseDone(true)
			.cookedFish(0)
			.energy(100)
			.needsSpirit(false)
			.spiritPoolUp(false)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void eightRawAfterFirstCookKeepsFishing()
	{
		GameSnapshot snap = inGame()
			.rawFish(8)
			.cookedFish(3)
			.dump16Done(true)
			.douseDone(true)
			.firstCookDone(true)
			.energy(40)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void cooksOnlyWhenInventoryIsFull()
	{
		GameSnapshot snap = inGame()
			.rawFish(10)
			.cookedFish(9)
			.emptySlots(0)
			.dump16Done(true)
			.douseDone(true)
			.firstCookDone(true)
			.energy(40)
			.build();
		assertEquals(HappyKind.COOK, HappyPathPolicy.decide(snap));
	}

	@Test
	public void startOfGameWithEmptyInventoryFishesNotSpirit()
	{
		GameSnapshot snap = inGame()
			.rawFish(0)
			.cookedFish(0)
			.dump16Done(false)
			.energy(100)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void mixedLeftoverKeepsFishingUntilWorthCooking()
	{
		GameSnapshot snap = inGame()
			.rawFish(5)
			.cookedFish(6)
			.dump16Done(true)
			.douseDone(true)
			.energy(40)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void afterDump16DousesNearbyFiresOnce()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.cookedFish(3)
			.nearbyFires(3)
			.waterBuckets(4)
			.energy(40)
			.build();
		assertEquals(HappyKind.DOUSE, HappyPathPolicy.decide(snap));
	}

	@Test
	public void afterFirstDouseNeverDousesAgain()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.douseDone(true)
			.cookedFish(3)
			.nearbyFires(4)
			.waterBuckets(4)
			.energy(40)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
	}

	@Test
	public void afterDepositAllGoesToSpiritNotDouse()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.cookedFish(0)
			.nearbyFires(4)
			.energy(8)
			.build();
		assertEquals(HappyKind.SPIRIT, HappyPathPolicy.decide(snap));
	}

	@Test
	public void peekFishToEightGoesToCook()
	{
		GameSnapshot snap = inGame()
			.rawFish(5)
			.busyFishing(true)
			.build();
		assertEquals(HappyKind.FISH, HappyPathPolicy.decide(snap));
		assertEquals(HappyKind.COOK, HappyPathPolicy.peekAfter(snap));
	}

	@Test
	public void peekCookFullGoesToDeposit()
	{
		GameSnapshot snap = inGame()
			.firstCookDone(true)
			.rawFish(19)
			.emptySlots(0)
			.build();
		assertEquals(HappyKind.COOK, HappyPathPolicy.decide(snap));
		assertEquals(HappyKind.DEPOSIT_KEEP3, HappyPathPolicy.peekAfter(snap));
	}

	@Test
	public void peekDepositKeep3GoesToDouse()
	{
		GameSnapshot snap = inGame()
			.cookedFish(19)
			.depositingKeep3(true)
			.depositKeep3StopAt(3)
			.nearbyFires(2)
			.build();
		assertEquals(HappyKind.DEPOSIT_KEEP3, HappyPathPolicy.decide(snap));
		assertEquals(HappyKind.DOUSE, HappyPathPolicy.peekAfter(snap));
	}

	@Test
	public void peekDepositAllGoesToSpirit()
	{
		GameSnapshot snap = inGame()
			.cookedFish(19)
			.dump16Done(true)
			.douseDone(true)
			.depositingAll(true)
			.depositAllStopAt(0)
			.build();
		assertEquals(HappyKind.DEPOSIT, HappyPathPolicy.decide(snap));
		assertEquals(HappyKind.SPIRIT, HappyPathPolicy.peekAfter(snap));
	}

	@Test
	public void peekSpiritGoesToFish()
	{
		GameSnapshot snap = inGame()
			.dump16Done(true)
			.douseDone(true)
			.needsSpirit(true)
			.spiritPoolUp(true)
			.spiritPoolAttackable(true)
			.energy(40)
			.build();
		assertEquals(HappyKind.SPIRIT, HappyPathPolicy.decide(snap));
		assertEquals(HappyKind.FISH, HappyPathPolicy.peekAfter(snap));
	}

	@Test
	public void peekSkipsRecovery()
	{
		GameSnapshot snap = inGame()
			.cookedFish(19)
			.depositingAll(true)
			.dump16Done(true)
			.waveIncoming(true)
			.build();
		assertEquals(HappyKind.IDLE, HappyPathPolicy.peekAfter(snap));
	}

	private static GameSnapshot.GameSnapshotBuilder inGame()
	{
		return GameSnapshot.builder()
			.inMinigame(true)
			.onIsland(true)
			.hasHammer(true)
			.hasRopeOrOutfit(true)
			.hasHarpoon(true)
			.waterBuckets(4)
			.emptySlots(10)
			.energy(100)
			.intensity(20)
			.hudVisible(true);
	}
}
