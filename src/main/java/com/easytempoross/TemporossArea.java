package com.easytempoross;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

final class TemporossArea
{
	private TemporossArea()
	{
	}

	static boolean isInMinigame(Client client)
	{
		if (client == null)
		{
			return false;
		}
		WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null || !worldView.isInstance())
		{
			return false;
		}
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return false;
		}
		WorldPoint loc = WorldPoint.fromLocalInstance(client, player.getLocalLocation());
		if (loc == null || loc.getRegionID() != RotationConstants.TEMPOROSS_REGION)
		{
			return false;
		}
		return client.getWidget(InterfaceID.TemporossHud.CONTENT) != null;
	}

	static boolean isAtUnkah(Client client, TileObject soloLadder, TileObject dockPump)
	{
		if (isInMinigame(client))
		{
			return false;
		}
		if (soloLadder != null || dockPump != null)
		{
			return true;
		}
		Widget lobby = client.getWidget(InterfaceID.TemporossLobbyHud.CONTENT);
		return lobby != null && !lobby.isHidden();
	}

	static boolean isOnShip(WorldPoint player, TileObject mast, TileObject shipPump)
	{
		if (player == null)
		{
			return false;
		}
		if (mast != null && mast.getWorldLocation() != null && player.distanceTo(mast.getWorldLocation()) <= 8)
		{
			return true;
		}
		return shipPump != null && shipPump.getWorldLocation() != null
			&& player.distanceTo(shipPump.getWorldLocation()) <= 6;
	}

	static boolean isOnIsland(WorldPoint player, TileObject shrine, TileObject totem)
	{
		if (player == null)
		{
			return false;
		}
		if (shrine != null && shrine.getWorldLocation() != null && player.distanceTo(shrine.getWorldLocation()) <= 24)
		{
			return true;
		}
		return totem != null && totem.getWorldLocation() != null
			&& player.distanceTo(totem.getWorldLocation()) <= 16;
	}
}
