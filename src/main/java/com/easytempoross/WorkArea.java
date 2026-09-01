package com.easytempoross;

import net.runelite.api.coords.WorldPoint;

/**
 * West boat, north island. Solos always spawn there.
 */
final class WorkArea
{
	private final WorldPoint host;
	private final WorldPoint shrine;
	private final WorldPoint totem;
	private final WorldPoint mast;
	private final WorldPoint pump;
	private final WorldPoint ammo;
	private final WorldPoint spiritPool;
	private final WorldPoint island;

	private WorkArea(WorldPoint host, WorldPoint shrine, WorldPoint spiritPool)
	{
		this.host = host;
		this.pump = host.dx(-3).dy(-2);
		this.mast = host.dx(0).dy(-3);
		this.ammo = host.dx(0).dy(-1);
		this.totem = host.dx(8).dy(15);
		this.shrine = shrine;
		this.spiritPool = spiritPool;
		this.island = host.dx(4).dy(-12);
	}

	static WorkArea fromSpawn(WorldPoint spawn, WorldPoint host)
	{
		return fromSpawn(spawn, host, RotationConstants.NORTH_SHRINE, RotationConstants.NORTH_SPIRIT);
	}

	/**
	 * The shrine and spirit pool are fixed points, but Tempoross is instanced so they have to be
	 * passed in already translated into the loaded scene.
	 */
	static WorkArea fromSpawn(WorldPoint spawn, WorldPoint host, WorldPoint shrine, WorldPoint spiritPool)
	{
		return new WorkArea(host != null ? host : spawn, shrine, spiritPool);
	}

	static boolean isNorthShore(WorldPoint point)
	{
		return point != null && point.getY() >= RotationConstants.NORTH_SHORE_MIN_Y;
	}

	WorldPoint getHost()
	{
		return host;
	}

	WorldPoint getShrine()
	{
		return shrine;
	}

	WorldPoint getTotem()
	{
		return totem;
	}

	WorldPoint getMast()
	{
		return mast;
	}

	WorldPoint getPump()
	{
		return pump;
	}

	WorldPoint getAmmo()
	{
		return ammo;
	}

	WorldPoint getSpiritPool()
	{
		return spiritPool;
	}

	WorldPoint getIsland()
	{
		return island;
	}

	boolean isOnShip(WorldPoint player)
	{
		return within(player, host, 12) || within(player, mast, 12)
			|| within(player, pump, 10) || within(player, ammo, 8);
	}

	boolean isOnIsland(WorldPoint player)
	{
		return within(player, shrine, 16) || within(player, totem, 12) || within(player, island, 10);
	}

	/**
	 * Generous "our half of the cove" test. Wide enough to catch every fire that belongs to the
	 * west boat and north island, but still far short of the south shrine.
	 */
	boolean isOurSide(WorldPoint point)
	{
		return within(point, shrine, 26) || within(point, totem, 22)
			|| within(point, host, 20) || within(point, mast, 20) || within(point, ammo, 20);
	}

	private static boolean within(WorldPoint player, WorldPoint landmark, int tiles)
	{
		return player != null && landmark != null && player.distanceTo(landmark) <= tiles;
	}
}
