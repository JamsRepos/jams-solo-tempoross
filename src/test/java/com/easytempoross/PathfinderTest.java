package com.easytempoross;

import java.util.Arrays;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PathfinderTest
{
	@Test
	public void simplifyDropsCollinearWalkTiles()
	{
		WorldPoint a = new WorldPoint(3040, 3050, 0);
		WorldPoint b = new WorldPoint(3041, 3050, 0);
		WorldPoint c = new WorldPoint(3042, 3050, 0);
		List<WorldPoint> simplified = Pathfinder.simplify(Arrays.asList(a, b, c));
		assertEquals(Arrays.asList(a, c), simplified);
		assertFalse(simplified.contains(b));
	}
}
