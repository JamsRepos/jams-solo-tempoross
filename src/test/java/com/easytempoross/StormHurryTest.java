package com.easytempoross;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StormHurryTest
{
	@Test
	public void showsInPhaseOneAtNinety()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.inMinigame(true)
			.dump16Done(false)
			.intensity(90)
			.build();
		assertTrue(RotationHelper.isStormHurry(snap));
	}

	@Test
	public void hiddenAfterFirstDump()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.inMinigame(true)
			.dump16Done(true)
			.intensity(95)
			.build();
		assertFalse(RotationHelper.isStormHurry(snap));
	}

	@Test
	public void hiddenBelowEmergency()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.inMinigame(true)
			.dump16Done(false)
			.intensity(89)
			.build();
		assertFalse(RotationHelper.isStormHurry(snap));
	}

	@Test
	public void hiddenOutsideMinigame()
	{
		GameSnapshot snap = GameSnapshot.builder()
			.inMinigame(false)
			.dump16Done(false)
			.intensity(99)
			.build();
		assertFalse(RotationHelper.isStormHurry(snap));
	}
}
