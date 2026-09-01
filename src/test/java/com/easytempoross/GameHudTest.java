package com.easytempoross;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GameHudTest
{
	@Test
	public void firstNumberParsesPercentageLabels()
	{
		assertEquals(10, GameHud.firstNumber("Energy: 10%"));
		assertEquals(55, GameHud.firstNumber("Essence: 55%"));
		assertEquals(19, GameHud.firstNumber("Storm Intensity: 19%"));
		assertEquals(3655, GameHud.firstNumber("Points: 3655"));
	}

	@Test
	public void firstNumberReturnsNegativeWhenMissing()
	{
		assertEquals(-1, GameHud.firstNumber(null));
		assertEquals(-1, GameHud.firstNumber(""));
		assertEquals(-1, GameHud.firstNumber("No numbers here"));
	}
}
