package com.easytempoross;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class IdleReminder
{
	private final Client client;
	private final EasyTemporossConfig config;

	@Getter
	private final List<String> warnings = new ArrayList<>();

	@Getter
	private boolean idle;

	private Instant lastMoveAt = Instant.now();
	private WorldPoint lastTile;

	@Inject
	IdleReminder(Client client, EasyTemporossConfig config)
	{
		this.client = client;
		this.config = config;
	}

	public void reset()
	{
		warnings.clear();
		idle = false;
		lastMoveAt = Instant.now();
		lastTile = null;
	}

	public void update(boolean inContent)
	{
		warnings.clear();
		idle = false;
		if (!inContent)
		{
			return;
		}
		updateIdle();
		if (idle)
		{
			warnings.add("Idle — click the highlight");
		}
	}

	private void updateIdle()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}
		if (player.getAnimation() != -1 || player.getPoseAnimation() != player.getIdlePoseAnimation())
		{
			lastTile = player.getWorldLocation();
			lastMoveAt = Instant.now();
			idle = false;
			return;
		}
		WorldPoint now = player.getWorldLocation();
		if (lastTile == null || now == null || lastTile.distanceTo(now) > 0)
		{
			lastTile = now;
			lastMoveAt = Instant.now();
			idle = false;
			return;
		}
		idle = Duration.between(lastMoveAt, Instant.now()).getSeconds() >= config.idleReminderSeconds();
	}
}
