package com.easytempoross;

import lombok.Value;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;

@Value
class ClickTarget
{
	TileObject object;
	NPC npc;
	WorldPoint tile;

	static ClickTarget none()
	{
		return new ClickTarget(null, null, null);
	}

	static ClickTarget ofObject(TileObject object)
	{
		return new ClickTarget(object, null, object == null ? null : object.getWorldLocation());
	}

	static ClickTarget ofNpc(NPC npc)
	{
		return new ClickTarget(null, npc, npc == null ? null : npc.getWorldLocation());
	}

	static ClickTarget ofTile(WorldPoint tile)
	{
		return new ClickTarget(null, null, tile);
	}

	static ClickTarget objectAt(TileObject object, WorldPoint pathTile)
	{
		return new ClickTarget(object, null, pathTile);
	}

	static ClickTarget npcAt(NPC npc, WorldPoint pathTile)
	{
		return new ClickTarget(null, npc, pathTile);
	}

	boolean isEmpty()
	{
		return object == null && npc == null && tile == null;
	}
}
