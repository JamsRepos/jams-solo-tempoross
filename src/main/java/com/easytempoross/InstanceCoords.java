package com.easytempoross;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/**
 * Tempoross is built as an instance, so the tiles shown on the world map are not the tiles the
 * client reports for scene entities. Every hardcoded tile in {@link RotationConstants} is a
 * world-map tile and has to be translated before it can be compared against anything that came
 * out of the scene, and vice versa.
 */
@Singleton
class InstanceCoords
{
	private final Client client;
	private final Map<WorldPoint, WorldPoint> sceneCache = new HashMap<>();
	private int cachedTick = -1;

	@Inject
	InstanceCoords(Client client)
	{
		this.client = client;
	}

	/**
	 * Translates a world-map tile into the matching tile in the loaded scene, or null when that
	 * tile is not currently loaded.
	 */
	WorldPoint scene(WorldPoint template)
	{
		if (template == null)
		{
			return null;
		}
		int tick = client.getTickCount();
		if (tick != cachedTick)
		{
			sceneCache.clear();
			cachedTick = tick;
		}
		if (sceneCache.containsKey(template))
		{
			return sceneCache.get(template);
		}
		WorldPoint resolved = resolve(template);
		sceneCache.put(template, resolved);
		return resolved;
	}

	/** Translates a scene tile back to the tile it is drawn from on the world map. */
	WorldPoint template(WorldPoint scene)
	{
		WorldView worldView = client.getTopLevelWorldView();
		if (scene == null || worldView == null)
		{
			return scene;
		}
		if (!worldView.isInstance())
		{
			return scene;
		}
		LocalPoint local = LocalPoint.fromWorld(worldView, scene);
		return local == null ? null : WorldPoint.fromLocalInstance(client, local);
	}

	boolean isNorthShore(WorldPoint scene)
	{
		return isNorthShore(scene, null);
	}

	/**
	 * North-shore test with a LocalPoint fallback when the scene tile cannot be mapped through
	 * {@link LocalPoint#fromWorld} (common for far-edge fishing spots).
	 */
	boolean isNorthShore(WorldPoint scene, LocalPoint local)
	{
		return WorkArea.isNorthShore(mapToTemplate(scene, local));
	}

	/**
	 * Maps a scene tile to its world-map template tile. Prefers the entity's {@link LocalPoint}
	 * when available — {@link LocalPoint#fromWorld} can return a wrong non-null mapping for
	 * far-edge tiles, which would skip any later local fallback.
	 */
	WorldPoint mapToTemplate(WorldPoint scene, LocalPoint local)
	{
		if (local != null)
		{
			WorldPoint fromLocal = WorldPoint.fromLocalInstance(client, local);
			if (fromLocal != null)
			{
				return fromLocal;
			}
		}
		return template(scene);
	}

	private WorldPoint resolve(WorldPoint template)
	{
		WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return null;
		}
		if (!worldView.isInstance())
		{
			return LocalPoint.fromWorld(worldView, template) == null ? null : template;
		}
		Player player = client.getLocalPlayer();
		WorldPoint anchor = player == null ? null : player.getWorldLocation();
		WorldPoint best = null;
		int bestDist = Integer.MAX_VALUE;
		for (WorldPoint candidate : WorldPoint.toLocalInstance(worldView, template))
		{
			if (candidate == null || LocalPoint.fromWorld(worldView, candidate) == null)
			{
				continue;
			}
			int dist = anchor == null ? 0 : candidate.distanceTo(anchor);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = candidate;
			}
		}
		return best;
	}
}
