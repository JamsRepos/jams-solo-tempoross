package com.easytempoross;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkAreaTest
{
	@Test
	public void westBoatAlwaysUsesNorthIsland()
	{
		WorldPoint spawn = new WorldPoint(3040, 2860, 0);
		WorkArea area = WorkArea.fromSpawn(spawn, spawn);
		assertEquals(RotationConstants.NORTH_SHRINE, area.getShrine());
		assertEquals(RotationConstants.NORTH_SPIRIT, area.getSpiritPool());
	}

	@Test
	public void instanceTilesOverrideTheWorldMapLandmarks()
	{
		WorldPoint spawn = new WorldPoint(2500, 4600, 0);
		WorldPoint shrine = new WorldPoint(2494, 4608, 0);
		WorldPoint spirit = new WorldPoint(2500, 4591, 0);
		WorkArea area = WorkArea.fromSpawn(spawn, spawn, shrine, spirit);
		assertEquals(shrine, area.getShrine());
		assertEquals(spirit, area.getSpiritPool());
		assertTrue(area.isOnIsland(shrine));
	}

	@Test
	public void unresolvedLandmarksDoNotThrow()
	{
		WorldPoint spawn = new WorldPoint(2500, 4600, 0);
		WorkArea area = WorkArea.fromSpawn(spawn, spawn, null, null);
		assertFalse(area.isOnIsland(new WorldPoint(2494, 4608, 0)));
		assertTrue(area.isOnShip(spawn));
	}

	@Test
	public void islandWalkTileIsTowardTheNorthShrine()
	{
		WorldPoint westShip = new WorldPoint(3040, 2885, 0);
		WorkArea area = WorkArea.fromSpawn(westShip, westShip);
		assertTrue(area.getIsland().distanceTo(RotationConstants.NORTH_SHRINE)
			< westShip.distanceTo(RotationConstants.NORTH_SHRINE));
	}

	@Test
	public void southOfTheShorelineIsIgnored()
	{
		assertFalse(WorkArea.isNorthShore(new WorldPoint(3047, 2842, 0)));
		assertTrue(WorkArea.isNorthShore(RotationConstants.NORTH_SPIRIT));
	}
}
