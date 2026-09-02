package com.easytempoross;

import com.google.inject.Provides;
import com.easytempoross.overlay.DepositCountdownOverlay;
import com.easytempoross.overlay.IdleTintOverlay;
import com.easytempoross.overlay.NextClickOverlay;
import com.easytempoross.overlay.PathMinimapOverlay;
import com.easytempoross.overlay.StatusOverlay;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Jam's Solo Tempoross",
	description = "Guided walkthrough of the 16/19/19 solo Tempoross rotation with click-here help, pathing, and recovery",
	tags = {"tempoross", "fishing", "solo", "minigame", "skilling", "helper"}
)
public class EasyTemporossPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NextClickOverlay nextClickOverlay;

	@Inject
	private PathMinimapOverlay pathMinimapOverlay;

	@Inject
	private StatusOverlay statusOverlay;

	@Inject
	private DepositCountdownOverlay depositCountdownOverlay;

	@Inject
	private IdleTintOverlay idleTintOverlay;

	@Inject
	private RotationHelper rotationHelper;

	@Inject
	private SceneTracker sceneTracker;

	@Inject
	private WaveTracker waveTracker;

	@Inject
	private ChangelogService changelogService;

	@Inject
	private PathDisplayMigration pathDisplayMigration;

	@Inject
	private ShortestPathBridge shortestPathBridge;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp()
	{
		pathDisplayMigration.run();
		rotationHelper.reset();
		sceneTracker.reset();
		sceneTracker.scanScene();
		overlayManager.add(nextClickOverlay);
		overlayManager.add(pathMinimapOverlay);
		overlayManager.add(statusOverlay);
		overlayManager.add(depositCountdownOverlay);
		overlayManager.add(idleTintOverlay);
		clientThread.invoke(changelogService::maybeAnnounce);
		log.debug("Jam's Solo Tempoross started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(nextClickOverlay);
		overlayManager.remove(pathMinimapOverlay);
		overlayManager.remove(statusOverlay);
		overlayManager.remove(depositCountdownOverlay);
		overlayManager.remove(idleTintOverlay);
		rotationHelper.reset();
		sceneTracker.reset();
		changelogService.reset();
		shortestPathBridge.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!EasyTemporossConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		String key = event.getKey();
		if (EasyTemporossConfig.PATH_DISPLAY_KEY.equals(key)
			|| EasyTemporossConfig.PATH_PROVIDER_KEY.equals(key))
		{
			shortestPathBridge.clear();
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		rotationHelper.update();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		sceneTracker.onSpawn(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		sceneTracker.onDespawn(event.getGameObject());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		sceneTracker.onSpawn(event.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		sceneTracker.onDespawn(event.getDecorativeObject());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		sceneTracker.onSpawn(event.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		sceneTracker.onDespawn(event.getGroundObject());
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		sceneTracker.onSpawn(event.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		sceneTracker.onDespawn(event.getWallObject());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		sceneTracker.onNpcSpawn(event.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		sceneTracker.onNpcDespawn(event.getNpc());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == VarbitID.TEMPOROSS_TETHERED)
		{
			waveTracker.setTethered(event.getValue() > 0);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		waveTracker.onChatMessage(event);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		changelogService.onGameStateChanged(event);
		if (event.getGameState() == GameState.LOADING)
		{
			sceneTracker.reset();
		}
		else if (event.getGameState() == GameState.LOGGED_IN)
		{
			sceneTracker.scanScene();
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			rotationHelper.reset();
			sceneTracker.reset();
		}
	}

	@Provides
	EasyTemporossConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EasyTemporossConfig.class);
	}
}
