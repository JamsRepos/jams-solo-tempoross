package com.easytempoross;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChimePolicyTest
{
	@Test
	public void depositOnlyNeverPlaysActionStop()
	{
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.FISH, RotationStep.COOK, ChimeMode.DEPOSIT_ONLY));
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.COOK, RotationStep.DEPOSIT, ChimeMode.DEPOSIT_ONLY));
	}

	@Test
	public void allActionsPlaysWhenFishingOrCookingFinishes()
	{
		assertTrue(ChimePolicy.shouldPlayActionStop(
			RotationStep.FISH, RotationStep.COOK, ChimeMode.ALL_ACTIONS));
		assertTrue(ChimePolicy.shouldPlayActionStop(
			RotationStep.COOK, RotationStep.DEPOSIT, ChimeMode.ALL_ACTIONS));
		assertTrue(ChimePolicy.shouldPlayActionStop(
			RotationStep.SPIRIT, RotationStep.FISH, ChimeMode.ALL_ACTIONS));
		assertTrue(ChimePolicy.shouldPlayActionStop(
			RotationStep.FILL_PUMP, RotationStep.LEAVE_SHIP, ChimeMode.ALL_ACTIONS));
	}

	@Test
	public void allActionsSkipsDepositBecauseCountdownAlreadyStops()
	{
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.DEPOSIT, RotationStep.SPIRIT, ChimeMode.ALL_ACTIONS));
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.DEPOSIT_KEEP3, RotationStep.DOUSE, ChimeMode.ALL_ACTIONS));
	}

	@Test
	public void allActionsSkipsSameActivityAndNonAfk()
	{
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.FISH, RotationStep.FISH_DOUBLE, ChimeMode.ALL_ACTIONS));
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.FISH, RotationStep.FISH, ChimeMode.ALL_ACTIONS));
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.PREP, RotationStep.SOLO_START, ChimeMode.ALL_ACTIONS));
		assertFalse(ChimePolicy.shouldPlayActionStop(
			RotationStep.IDLE, RotationStep.FISH, ChimeMode.ALL_ACTIONS));
		assertTrue(ChimePolicy.shouldPlayActionStop(
			RotationStep.FISH, RotationStep.TETHER, ChimeMode.ALL_ACTIONS));
	}

	@Test
	public void doubleFishPlaysOnEdgeWhenInventoryHasSpace()
	{
		assertTrue(ChimePolicy.shouldPlayDoubleFish(
			true, true, RotationStep.FISH_DOUBLE, 3, false));
		assertFalse(ChimePolicy.shouldPlayDoubleFish(
			true, true, RotationStep.FISH_DOUBLE, 3, true));
	}

	@Test
	public void doubleFishSkipsWhenFullOrNotDoubleOrDisabled()
	{
		assertFalse(ChimePolicy.shouldPlayDoubleFish(
			true, true, RotationStep.FISH_DOUBLE, 0, false));
		assertFalse(ChimePolicy.shouldPlayDoubleFish(
			true, true, RotationStep.FISH, 3, false));
		assertFalse(ChimePolicy.shouldPlayDoubleFish(
			false, true, RotationStep.FISH_DOUBLE, 3, false));
		assertFalse(ChimePolicy.shouldPlayDoubleFish(
			true, false, RotationStep.FISH_DOUBLE, 3, false));
	}
}
