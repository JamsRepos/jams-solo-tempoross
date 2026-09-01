package com.easytempoross;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

@Singleton
class PathRouter
{
	private static final int AT_TARGET = RotationConstants.AT_TARGET_TILES;
	private static final int DRIFT_TILES = 6;

	private List<WorldPoint> cachedPath = Collections.emptyList();
	private WorldPoint cachedEnd;

	@Inject
	PathRouter()
	{
	}

	void reset()
	{
		cachedPath = Collections.emptyList();
		cachedEnd = null;
	}

	List<WorldPoint> pathTo(WorldView worldView, WorldPoint start, WorldPoint end, Set<WorldPoint> blocked, boolean showPath)
	{
		if (!showPath || start == null || end == null || start.distanceTo(end) <= AT_TARGET)
		{
			reset();
			return Collections.emptyList();
		}

		if (end.equals(cachedEnd) && cachedPath.size() > 1)
		{
			List<WorldPoint> trimmed = trim(cachedPath, start);
			if (trimmed != null)
			{
				cachedPath = trimmed;
				return Pathfinder.simplify(trimmed);
			}
		}

		Set<WorldPoint> block = blocked == null ? Collections.emptySet() : blocked;
		List<WorldPoint> path = Pathfinder.find(worldView, start, end, block);
		if (path == null || path.isEmpty())
		{
			if (worldView == null || LocalPoint.fromWorld(worldView, end) == null)
			{
				reset();
				return Collections.emptyList();
			}
			path = Arrays.asList(start, end);
		}
		cachedPath = path;
		cachedEnd = end;
		return Pathfinder.simplify(path);
	}

	private List<WorldPoint> trim(List<WorldPoint> path, WorldPoint start)
	{
		int nearest = 0;
		int best = Integer.MAX_VALUE;
		for (int i = 0; i < path.size(); i++)
		{
			int d = path.get(i).distanceTo(start);
			if (d < best)
			{
				best = d;
				nearest = i;
			}
		}
		if (best > DRIFT_TILES)
		{
			return null;
		}
		if (nearest >= path.size() - 1)
		{
			return Collections.emptyList();
		}
		return path.subList(nearest, path.size());
	}
}
