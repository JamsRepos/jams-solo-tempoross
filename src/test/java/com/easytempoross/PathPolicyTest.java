package com.easytempoross;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathPolicyTest
{
	private static final WorldPoint LADDER = new WorldPoint(3030, 3050, 0);
	private static final WorldPoint EAST = new WorldPoint(3048, 3054, 0);
	private static final WorldPoint CENTER = new WorldPoint(3040, 3050, 0);

	@Test
	public void reuseWhenStillOnThePath()
	{
		List<WorldPoint> path = Arrays.asList(
			new WorldPoint(3040, 3050, 0),
			new WorldPoint(3035, 3050, 0),
			LADDER);
		assertTrue(PathPolicy.canReuse(path.get(0), LADDER, path));
		assertTrue(PathPolicy.canReuse(new WorldPoint(3036, 3050, 0), LADDER, path));
	}

	@Test
	public void dropCacheWhenAdjacentWaypointHeadsAwayFromTheGoal()
	{
		WorldPoint player = new WorldPoint(3040, 3050, 0);
		WorldPoint away = new WorldPoint(3041, 3050, 0);
		List<WorldPoint> stale = Arrays.asList(away, LADDER);
		assertFalse(PathPolicy.canReuse(player, LADDER, stale));
	}

	@Test
	public void dropCacheWhenPlayerWalkedOffIntoAVee()
	{
		List<WorldPoint> stale = Arrays.asList(EAST, LADDER);
		assertFalse(PathPolicy.canReuse(CENTER, LADDER, stale));
	}

	@Test
	public void dropCacheWhenNearestWaypointIsBehindThePlayer()
	{
		WorldPoint closer = new WorldPoint(3034, 3050, 0);
		List<WorldPoint> stale = Arrays.asList(EAST, LADDER);
		assertFalse(PathPolicy.canReuse(closer, LADDER, stale));
	}

	@Test
	public void emptyPathIsNotReusable()
	{
		assertFalse(PathPolicy.canReuse(CENTER, LADDER, Collections.emptyList()));
		assertFalse(PathPolicy.canReuse(CENTER, LADDER, Collections.singletonList(LADDER)));
	}
}
