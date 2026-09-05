package com.easytempoross;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DepositFireAlertTest
{
	@Test
	public void playsOnlyOnRisingEdge()
	{
		assertTrue(RotationHelper.shouldPlayDepositFireAlert(true, true, false));
		assertFalse(RotationHelper.shouldPlayDepositFireAlert(true, true, true));
		assertFalse(RotationHelper.shouldPlayDepositFireAlert(true, false, true));
		assertFalse(RotationHelper.shouldPlayDepositFireAlert(true, false, false));
	}

	@Test
	public void respectsChimeToggle()
	{
		assertFalse(RotationHelper.shouldPlayDepositFireAlert(false, true, false));
	}
}
