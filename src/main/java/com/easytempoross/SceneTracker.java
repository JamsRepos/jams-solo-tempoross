package com.easytempoross;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class SceneTracker
{
	private final Client client;
	private final InstanceCoords coords;

	private final List<TileObject> objects = new ArrayList<>();
	private final Map<Integer, NPC> npcs = new HashMap<>();

	@Getter
	private TileObject dockPump;
	@Getter
	private TileObject soloLadder;
	@Getter
	private TileObject bucketCrate;
	@Getter
	private TileObject ropeCrate;
	@Getter
	private TileObject hammerCrate;
	@Getter
	private TileObject harpoonCrate;

	@Inject
	SceneTracker(Client client, InstanceCoords coords)
	{
		this.client = client;
		this.coords = coords;
	}

	public void reset()
	{
		objects.clear();
		npcs.clear();
		dockPump = null;
		soloLadder = null;
		bucketCrate = null;
		ropeCrate = null;
		hammerCrate = null;
		harpoonCrate = null;
	}

	public void onSpawn(TileObject object)
	{
		if (object == null || !TemporossIds.isTrackedObject(object))
		{
			return;
		}
		for (TileObject existing : objects)
		{
			if (existing == object)
			{
				assignDockObject(object, false);
				return;
			}
		}
		objects.add(object);
		assignDockObject(object, false);
	}

	public void onDespawn(TileObject object)
	{
		if (object == null)
		{
			return;
		}
		for (int i = objects.size() - 1; i >= 0; i--)
		{
			if (objects.get(i) == object)
			{
				objects.remove(i);
			}
		}
		assignDockObject(object, true);
	}

	public void onNpcSpawn(NPC npc)
	{
		if (npc == null || !TemporossIds.isTrackedNpc(npc))
		{
			return;
		}
		npcs.put(npc.getIndex(), npc);
	}

	public void onNpcDespawn(NPC npc)
	{
		if (npc == null)
		{
			return;
		}
		npcs.remove(npc.getIndex());
	}

	public void scanScene()
	{
		reset();
		WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		Scene scene = worldView.getScene();
		if (scene == null)
		{
			return;
		}
		Tile[][][] tiles = scene.getTiles();
		if (tiles == null)
		{
			return;
		}
		int plane = worldView.getPlane();
		if (plane < 0 || plane >= tiles.length || tiles[plane] == null)
		{
			return;
		}
		Tile[][] planeTiles = tiles[plane];
		for (int x = 0; x < planeTiles.length; x++)
		{
			Tile[] col = planeTiles[x];
			if (col == null)
			{
				continue;
			}
			for (int y = 0; y < col.length; y++)
			{
				scanTile(col[y]);
			}
		}
		if (worldView.npcs() != null)
		{
			for (NPC npc : worldView.npcs())
			{
				onNpcSpawn(npc);
			}
		}
	}

	public NPC nearestFishingSpot(WorldPoint from, boolean preferDouble, WorkArea area)
	{
		NPC best = null;
		int bestDist = Integer.MAX_VALUE;
		for (NPC npc : liveNpcs())
		{
			if (!TemporossIds.isHarpoonSpot(npc) || !isOurFishingSpot(npc))
			{
				continue;
			}
			int dist = from == null ? 0 : npc.getWorldLocation().distanceTo(from);
			if (preferDouble && TemporossIds.isDoubleSpot(npc.getId()))
			{
				dist -= 1000;
			}
			if (dist < bestDist)
			{
				bestDist = dist;
				best = npc;
			}
		}
		return best;
	}

	public NPC doubleSpot(WorldPoint near, WorkArea area)
	{
		NPC best = null;
		int bestDist = Integer.MAX_VALUE;
		for (NPC npc : liveNpcs())
		{
			if (npc == null || npc.getWorldLocation() == null || !TemporossIds.isDoubleSpot(npc.getId())
				|| !TemporossIds.isHarpoonSpot(npc) || !isOurFishingSpot(npc))
			{
				continue;
			}
			int dist = near == null ? 0 : npc.getWorldLocation().distanceTo(near);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = npc;
			}
		}
		return best;
	}

	public NPC nearestAmmoCrate(WorldPoint prefer)
	{
		NPC best = null;
		int bestDist = Integer.MAX_VALUE;
		for (NPC npc : liveNpcs())
		{
			if (npc == null || npc.getWorldLocation() == null || !TemporossIds.isAmmoCrateNpc(npc))
			{
				continue;
			}
			int dist = prefer == null ? 0 : npc.getWorldLocation().distanceTo(prefer);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = npc;
			}
		}
		return best;
	}

	public NPC nearestFire(WorldPoint from, WorkArea area)
	{
		return nearestNpc(from, area, npc -> isOurWorkFire(area, npc));
	}

	public NPC spiritPool(WorkArea area)
	{
		return nearestSpiritPool(area == null ? null : area.getSpiritPool());
	}

	public NPC nearestSpiritPool(WorldPoint prefer)
	{
		return northSpiritPool();
	}

	public NPC northSpiritPool()
	{
		WorldPoint tile = coords.scene(RotationConstants.NORTH_SPIRIT);
		NPC best = null;
		int bestDist = Integer.MAX_VALUE;
		for (NPC npc : liveNpcs())
		{
			if (npc == null || npc.getWorldLocation() == null || !TemporossIds.isSpiritPool(npc.getId()))
			{
				continue;
			}
			if (!coords.isNorthShore(npc.getWorldLocation()))
			{
				continue;
			}
			int dist = tile == null ? 0 : npc.getWorldLocation().distanceTo(tile);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = npc;
			}
		}
		return best;
	}

	public NPC nearestHost(WorldPoint from, WorkArea area)
	{
		return nearestNpc(from, area, npc -> TemporossIds.isHost(npc.getId())
			|| TemporossIds.hasAction(npc, "Leave")
			|| TemporossIds.hasAction(npc, "Forfeit"));
	}

	public NPC inferHost(WorldPoint from)
	{
		NPC best = null;
		int bestDist = Integer.MAX_VALUE;
		for (NPC npc : npcs.values())
		{
			if (npc == null || npc.getWorldLocation() == null || !TemporossIds.isHost(npc.getId()))
			{
				continue;
			}
			if (!TemporossIds.isWestHost(npc.getId()) && !coords.isNorthShore(npc.getWorldLocation()))
			{
				continue;
			}
			int dist = from == null ? 0 : npc.getWorldLocation().distanceTo(from);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = npc;
			}
		}
		if (best != null)
		{
			return best;
		}
		return nearestNpc(from, null, npc -> TemporossIds.isHost(npc.getId()));
	}

	public boolean hasVictoryHost()
	{
		for (NPC npc : liveNpcs())
		{
			if (npc == null)
			{
				continue;
			}
			if (TemporossIds.isVictoryHost(npc.getId()) || TemporossIds.hasAction(npc, "Leave"))
			{
				return true;
			}
		}
		return false;
	}

	public Set<WorldPoint> fireTiles(WorkArea area)
	{
		Set<WorldPoint> tiles = new HashSet<>();
		for (NPC npc : npcs.values())
		{
			if (npc != null && npc.getWorldLocation() != null && isOurWorkFire(area, npc))
			{
				tiles.add(npc.getWorldLocation());
			}
		}
		for (TileObject object : objects)
		{
			if (object != null && TemporossIds.isCloud(object.getId()) && object.getWorldLocation() != null
				&& onSide(area, object.getWorldLocation())
				&& isOurWorkLocation(area, object.getWorldLocation()))
			{
				tiles.add(object.getWorldLocation());
			}
		}
		return tiles;
	}

	public int fireCountOnSide(WorkArea area)
	{
		if (area == null)
		{
			return 0;
		}
		int n = 0;
		for (NPC npc : liveNpcs())
		{
			if (isOurWorkFire(area, npc))
			{
				n++;
			}
		}
		return n;
	}

	/** Burning fire, incoming fire marker, or smoke cloud within {@code tiles} of a point. */
	public boolean fireThreatWithin(WorldPoint point, int tiles)
	{
		if (point == null)
		{
			return false;
		}
		for (NPC npc : liveNpcs())
		{
			if (npc == null || npc.getWorldLocation() == null)
			{
				continue;
			}
			if (!TemporossIds.isFireTargetNpc(npc.getId()) && !TemporossIds.isActiveFireNpc(npc.getId())
				&& !TemporossIds.hasAction(npc, "Douse"))
			{
				continue;
			}
			if (npc.getWorldLocation().distanceTo(point) <= tiles)
			{
				return true;
			}
		}
		for (TileObject object : objects)
		{
			if (object != null && TemporossIds.isCloud(object.getId())
				&& object.getWorldLocation() != null
				&& object.getWorldLocation().distanceTo(point) <= tiles)
			{
				return true;
			}
		}
		return false;
	}

	public TileObject shrine(WorkArea area)
	{
		return closestShrine(area == null ? null : area.getShrine());
	}

	public TileObject shipPump(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getPump(),
			object -> object.getId() == TemporossIds.SHIP_PUMP);
	}

	public TileObject totem(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getTotem(),
			object -> TemporossIds.isTotem(object.getId()) && !TemporossIds.isDamagedTotem(object.getId()));
	}

	public TileObject mast(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getMast(),
			object -> TemporossIds.isMast(object.getId()) && !TemporossIds.isDamagedMast(object.getId()));
	}

	public TileObject damagedTotem(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getTotem(),
			object -> TemporossIds.isDamagedTotem(object.getId()));
	}

	public TileObject damagedMast(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getMast(),
			object -> TemporossIds.isDamagedMast(object.getId()));
	}

	public TileObject closestShrine(WorldPoint from)
	{
		return findNorth(RotationConstants.NORTH_SHRINE, TemporossIds.SHRINE);
	}

	/**
	 * Finds the object with {@code id} sitting next to a hardcoded world-map landmark. The
	 * landmark is translated into the loaded scene first, then matched against the tracked
	 * objects, falling back to a direct scan of the surrounding tiles.
	 */
	public TileObject findNear(WorldPoint landmark, int id)
	{
		WorldPoint tile = coords.scene(landmark);
		if (tile == null)
		{
			return null;
		}
		TileObject tracked = closestObject(null, tile, object -> object.getId() == id);
		if (tracked != null && distanceTo(tracked, tile) <= RotationConstants.LANDMARK_NEAR_TILES)
		{
			return tracked;
		}
		return scanNear(tile, id);
	}

	/**
	 * Same as {@link #findNear}, but falls back to any matching object on the north shore so a
	 * stale landmark tile cannot leave us with nothing to highlight.
	 */
	public TileObject findNorth(WorldPoint landmark, int id)
	{
		TileObject found = findNear(landmark, id);
		if (found != null)
		{
			return found;
		}
		return closestObject(null, null, object -> object.getId() == id
			&& coords.isNorthShore(object.getWorldLocation()));
	}

	public TileObject findDockPump()
	{
		return findDockPump(true);
	}

	public TileObject findDockPump(boolean northCoveOnly)
	{
		TileObject pump = findNorth(RotationConstants.NORTH_DOCK, TemporossIds.DOCK_PUMP);
		if (pump != null || northCoveOnly)
		{
			return pump;
		}
		return closestObject(null, null, object -> object.getId() == TemporossIds.DOCK_PUMP);
	}

	private TileObject scanNear(WorldPoint tile, int id)
	{
		WorldView worldView = client.getTopLevelWorldView();
		if (tile == null || worldView == null)
		{
			return null;
		}
		LocalPoint local = LocalPoint.fromWorld(worldView, tile);
		Scene scene = worldView.getScene();
		if (local == null || scene == null)
		{
			return null;
		}
		Tile[][][] tiles = scene.getTiles();
		int plane = worldView.getPlane();
		if (tiles == null || plane < 0 || plane >= tiles.length || tiles[plane] == null)
		{
			return null;
		}
		Tile[][] planeTiles = tiles[plane];
		int sx = local.getSceneX();
		int sy = local.getSceneY();
		TileObject best = null;
		int bestDist = Integer.MAX_VALUE;
		for (int dx = -5; dx <= 5; dx++)
		{
			for (int dy = -5; dy <= 5; dy++)
			{
				int x = sx + dx;
				int y = sy + dy;
				if (x < 0 || y < 0 || x >= planeTiles.length)
				{
					continue;
				}
				Tile[] col = planeTiles[x];
				if (col == null || y >= col.length || col[y] == null)
				{
					continue;
				}
				TileObject found = firstId(col[y], id);
				if (found == null)
				{
					continue;
				}
				int dist = distanceTo(found, tile);
				if (dist < bestDist)
				{
					bestDist = dist;
					best = found;
				}
			}
		}
		return best;
	}

	private static TileObject firstId(Tile tile, int id)
	{
		GameObject[] gameObjects = tile.getGameObjects();
		if (gameObjects != null)
		{
			for (GameObject object : gameObjects)
			{
				if (object != null && object.getId() == id)
				{
					return object;
				}
			}
		}
		if (tile.getGroundObject() != null && tile.getGroundObject().getId() == id)
		{
			return tile.getGroundObject();
		}
		if (tile.getDecorativeObject() != null && tile.getDecorativeObject().getId() == id)
		{
			return tile.getDecorativeObject();
		}
		if (tile.getWallObject() != null && tile.getWallObject().getId() == id)
		{
			return tile.getWallObject();
		}
		return null;
	}

	public TileObject closestTotem(WorldPoint from)
	{
		return closestObject(null, from, object -> object.getId() == TemporossIds.TOTEM_A
			|| object.getId() == TemporossIds.TOTEM_B
			|| TemporossIds.isDamagedTotem(object.getId()));
	}

	public TileObject closestMast(WorldPoint from)
	{
		return closestObject(null, from, object -> TemporossIds.isMast(object.getId()));
	}

	public TileObject closestTetherPole(WorldPoint from)
	{
		TileObject totem = closestTotem(from);
		TileObject mast = closestMast(from);
		if (totem == null)
		{
			return mast;
		}
		if (mast == null)
		{
			return totem;
		}
		return distTo(totem, from) <= distTo(mast, from) ? totem : mast;
	}

	public NPC nearestOurFire(WorldPoint player, WorkArea area)
	{
		if (area == null)
		{
			return nearestFireNear(player, null, Integer.MAX_VALUE, null);
		}

		NPC bestShip = null;
		NPC bestOther = null;
		int bestShipDist = Integer.MAX_VALUE;
		int bestOtherDist = Integer.MAX_VALUE;
		for (NPC npc : liveNpcs())
		{
			if (!isOurWorkFire(area, npc))
			{
				continue;
			}
			WorldPoint fire = npc.getWorldLocation();
			int dist = player == null ? 0 : fire.distanceTo(player);
			if (area.isOnShip(fire))
			{
				if (dist < bestShipDist)
				{
					bestShipDist = dist;
					bestShip = npc;
				}
			}
			else if (dist < bestOtherDist)
			{
				bestOtherDist = dist;
				bestOther = npc;
			}
		}
		return bestShip != null ? bestShip : bestOther;
	}

	public NPC nearestFireNear(WorldPoint player, WorldPoint origin, int tiles, WorkArea area)
	{
		NPC best = null;
		int bestDist = Integer.MAX_VALUE;
		for (NPC npc : liveNpcs())
		{
			if (!isOurWorkFire(area, npc))
			{
				continue;
			}
			WorldPoint fire = npc.getWorldLocation();
			if (origin != null && fire.distanceTo(origin) > tiles)
			{
				continue;
			}
			int dist = player == null ? 0 : fire.distanceTo(player);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = npc;
			}
		}
		return best;
	}

	public TileObject anyMast(WorldPoint from)
	{
		return closestObject(null, from, object -> TemporossIds.isMast(object.getId())
			&& sameCoveSide(from, object.getWorldLocation()));
	}

	public TileObject anyShipPump(WorldPoint from)
	{
		return closestObject(null, from, object -> object.getId() == TemporossIds.SHIP_PUMP
			&& sameCoveSide(from, object.getWorldLocation()));
	}

	public TileObject hammerCrate(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getHost(),
			object -> TemporossIds.isHammerCrate(object.getId()));
	}

	public TileObject ropeCrate(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getHost(),
			object -> TemporossIds.isRopeCrate(object.getId()));
	}

	public TileObject harpoonCrate(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getHost(),
			object -> TemporossIds.isHarpoonCrate(object.getId()));
	}

	public TileObject bucketCrate(WorkArea area)
	{
		return closestObject(area, area == null ? null : area.getHost(),
			object -> TemporossIds.isBucketCrate(object.getId()));
	}

	static int distanceTo(TileObject object, WorldPoint loc)
	{
		if (object == null || loc == null)
		{
			return Integer.MAX_VALUE;
		}
		if (object instanceof GameObject)
		{
			GameObject go = (GameObject) object;
			WorldArea area = new WorldArea(go.getWorldLocation(), Math.max(1, go.sizeX()), Math.max(1, go.sizeY()));
			return area.distanceTo(loc);
		}
		return object.getWorldLocation().distanceTo(loc);
	}

	private NPC nearestNpc(WorldPoint from, WorkArea area, NpcMatch match)
	{
		NPC best = null;
		int bestDist = Integer.MAX_VALUE;
		for (NPC npc : npcs.values())
		{
			if (npc == null || npc.getWorldLocation() == null || !match.test(npc))
			{
				continue;
			}
			if (!onSide(area, npc.getWorldLocation()))
			{
				continue;
			}
			int dist = from == null ? 0 : npc.getWorldLocation().distanceTo(from);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = npc;
			}
		}
		return best;
	}

	private TileObject closestObject(WorkArea area, WorldPoint hint, ObjectMatch match)
	{
		TileObject best = null;
		int bestDist = Integer.MAX_VALUE;
		for (TileObject object : objects)
		{
			if (object == null || !match.test(object))
			{
				continue;
			}
			if (!onSide(area, object.getWorldLocation()))
			{
				continue;
			}
			int dist = distTo(object, hint);
			if (dist < bestDist)
			{
				bestDist = dist;
				best = object;
			}
		}
		return best;
	}

	private int distTo(TileObject object, WorldPoint hint)
	{
		if (object == null)
		{
			return Integer.MAX_VALUE;
		}
		LocalPoint objLocal = object.getLocalLocation();
		WorldView worldView = client.getTopLevelWorldView();
		LocalPoint hintLocal = hint == null || worldView == null
			? null
			: LocalPoint.fromWorld(worldView, hint);
		if (objLocal != null && hintLocal != null)
		{
			return objLocal.distanceTo(hintLocal);
		}
		return distanceTo(object, hint);
	}

	private boolean onSide(WorkArea area, WorldPoint point)
	{
		return area == null || coords.isNorthShore(point);
	}

	private boolean isOurFishingSpot(NPC npc)
	{
		return npc != null && npc.getWorldLocation() != null
			&& !TemporossIds.isSouthFishingSpot(npc.getId())
			&& coords.isNorthShore(npc.getWorldLocation());
	}

	private boolean sameCoveSide(WorldPoint from, WorldPoint point)
	{
		if (from == null || point == null)
		{
			return true;
		}
		return coords.isNorthShore(from) == coords.isNorthShore(point);
	}

	private boolean isOurWorkFire(WorkArea area, NPC npc)
	{
		if (npc == null || npc.getWorldLocation() == null)
		{
			return false;
		}
		if (!TemporossIds.isFireNpc(npc.getId()) && !TemporossIds.hasAction(npc, "Douse"))
		{
			return false;
		}
		WorldPoint fire = npc.getWorldLocation();
		if (area == null)
		{
			return coords.isNorthShore(fire);
		}
		return area.isOurSide(fire);
	}

	private boolean isOurWorkLocation(WorkArea area, WorldPoint point)
	{
		if (area == null)
		{
			return true;
		}
		return area.isOurSide(point);
	}

	private void scanTile(Tile tile)
	{
		if (tile == null)
		{
			return;
		}
		GameObject[] gameObjects = tile.getGameObjects();
		if (gameObjects != null)
		{
			for (GameObject object : gameObjects)
			{
				onSpawn(object);
			}
		}
		DecorativeObject deco = tile.getDecorativeObject();
		if (deco != null)
		{
			onSpawn(deco);
		}
		GroundObject ground = tile.getGroundObject();
		if (ground != null)
		{
			onSpawn(ground);
		}
		WallObject wall = tile.getWallObject();
		if (wall != null)
		{
			onSpawn(wall);
		}
	}

	private void assignDockObject(TileObject object, boolean despawn)
	{
		int id = object.getId();
		if (id == TemporossIds.DOCK_PUMP)
		{
			dockPump = despawn ? clear(dockPump, object) : prefer(dockPump, object);
		}
		else if (TemporossIds.isSoloLadder(id))
		{
			soloLadder = despawn ? clear(soloLadder, object) : prefer(soloLadder, object);
		}
		else if (TemporossIds.isBucketCrate(id))
		{
			bucketCrate = despawn ? clear(bucketCrate, object) : prefer(bucketCrate, object);
		}
		else if (TemporossIds.isRopeCrate(id))
		{
			ropeCrate = despawn ? clear(ropeCrate, object) : prefer(ropeCrate, object);
		}
		else if (TemporossIds.isHammerCrate(id))
		{
			hammerCrate = despawn ? clear(hammerCrate, object) : prefer(hammerCrate, object);
		}
		else if (TemporossIds.isHarpoonCrate(id))
		{
			harpoonCrate = despawn ? clear(harpoonCrate, object) : prefer(harpoonCrate, object);
		}
	}

	private static TileObject clear(TileObject current, TileObject gone)
	{
		return current == gone ? null : current;
	}

	private static TileObject prefer(TileObject current, TileObject next)
	{
		return current == null ? next : current;
	}

	private Iterable<NPC> liveNpcs()
	{
		WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null || worldView.npcs() == null)
		{
			return npcs.values();
		}
		List<NPC> live = new ArrayList<>();
		for (NPC npc : worldView.npcs())
		{
			live.add(npc);
		}
		return live;
	}

	private interface NpcMatch
	{
		boolean test(NPC npc);
	}

	private interface ObjectMatch
	{
		boolean test(TileObject object);
	}
}
