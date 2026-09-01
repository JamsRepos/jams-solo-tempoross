package com.easytempoross;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;

@Singleton
class PathRouter
{
	private static final int AT_TARGET = RotationConstants.AT_TARGET_TILES;

	private List<WorldPoint> cachedPath = Collections.emptyList();
	private WorldPoint cachedEnd;
	private boolean useDirect;
	private WorldPoint directFrom;

	@Inject
	PathRouter()
	{
	}

	void reset()
	{
		cachedPath = Collections.emptyList();
		cachedEnd = null;
		useDirect = false;
		directFrom = null;
	}

	List<WorldPoint> pathTo(WorldView worldView, WorldPoint start, WorldPoint end, Set<WorldPoint> blocked, boolean showPath)
	{
		if (!showPath || start == null || end == null || start.distanceTo(end) <= AT_TARGET)
		{
			reset();
			return Collections.emptyList();
		}

		Set<WorldPoint> block = blocked == null ? Collections.emptySet() : blocked;
		if (end.equals(cachedEnd) && useDirect && directFrom != null
			&& start.distanceTo(directFrom) < 3)
		{
			return directLine(worldView, start, end);
		}
		if (end.equals(cachedEnd) && PathPolicy.canReuse(start, end, cachedPath))
		{
			int nearest = PathPolicy.nearestIndex(cachedPath, start);
			List<WorldPoint> trimmed = new ArrayList<>(cachedPath.subList(nearest, cachedPath.size()));
			cachedPath = trimmed;
			return Pathfinder.simplify(Pathfinder.smooth(worldView, trimmed, block));
		}

		List<WorldPoint> path = Pathfinder.find(worldView, start, end, block);
		if (path == null || path.isEmpty())
		{
			useDirect = true;
			directFrom = start;
			cachedPath = Collections.emptyList();
			cachedEnd = end;
			return directLine(worldView, start, end);
		}
		useDirect = false;
		directFrom = null;
		path = Pathfinder.smooth(worldView, path, block);
		cachedPath = path;
		cachedEnd = end;
		return Pathfinder.simplify(path);
	}

	private List<WorldPoint> directLine(WorldView worldView, WorldPoint start, WorldPoint end)
	{
		WorldPoint clamped = Pathfinder.clampToScene(worldView, start, end);
		if (clamped == null || start.distanceTo(clamped) <= AT_TARGET)
		{
			return Collections.emptyList();
		}
		return Arrays.asList(start, clamped);
	}
}
