package com.easytempoross;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.SoundEffectID;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.AnimationID;

@Slf4j
@Singleton
public class RotationHelper
{
	private final Client client;
	private final EasyTemporossConfig config;
	private final InstanceCoords coords;
	private final InventoryChecker inventoryChecker;
	private final SceneTracker sceneTracker;
	private final PathRouter pathRouter;
	private final GameHud gameHud;
	private final OverlaySuppressor overlaySuppressor;
	private final WaveTracker waveTracker;
	private final IdleReminder idleReminder;

	@Getter
	private HelperAction currentAction = HelperAction.idle();

	@Getter
	private InventorySnapshot snapshot = new InventorySnapshot(0, 0, 0, 0, 0, 28,
		false, false, false, false, false);

	@Getter
	private GameSnapshot gameSnapshot = GameSnapshot.empty();

	private WorkArea workArea;
	private WorldPoint islandShrine;
	private WorldPoint islandTotem;
	private WorldPoint shipMast;
	private WorldPoint ammoCrate;
	private boolean dump16Done;
	private boolean firstCookDone;
	private boolean douseDone;
	private boolean sawDouseFires;
	private boolean depositingKeep3;
	private boolean depositingAll;
	private int depositKeep3StopAt = RotationConstants.FIRST_DUMP_KEEP;
	private int depositAllStopAt;
	private int lastDepositsLeft;
	private boolean needsSpirit;
	private boolean sawSpiritPool;
	private boolean sawSpiritHarpoon;
	private int spiritCycles;
	private int lastEnergy = -1;
	private boolean sawMinigame;

	@Inject
	RotationHelper(
		Client client,
		EasyTemporossConfig config,
		InstanceCoords coords,
		InventoryChecker inventoryChecker,
		SceneTracker sceneTracker,
		PathRouter pathRouter,
		GameHud gameHud,
		OverlaySuppressor overlaySuppressor,
		WaveTracker waveTracker,
		IdleReminder idleReminder)
	{
		this.client = client;
		this.config = config;
		this.coords = coords;
		this.inventoryChecker = inventoryChecker;
		this.sceneTracker = sceneTracker;
		this.pathRouter = pathRouter;
		this.gameHud = gameHud;
		this.overlaySuppressor = overlaySuppressor;
		this.waveTracker = waveTracker;
		this.idleReminder = idleReminder;
	}

	public void reset()
	{
		currentAction = HelperAction.idle();
		workArea = null;
		islandShrine = null;
		islandTotem = null;
		shipMast = null;
		ammoCrate = null;
		dump16Done = false;
		firstCookDone = false;
		douseDone = false;
		sawDouseFires = false;
		depositingKeep3 = false;
		depositingAll = false;
		depositKeep3StopAt = RotationConstants.FIRST_DUMP_KEEP;
		depositAllStopAt = 0;
		lastDepositsLeft = 0;
		needsSpirit = false;
		sawSpiritPool = false;
		sawSpiritHarpoon = false;
		spiritCycles = 0;
		lastEnergy = -1;
		sawMinigame = false;
		pathRouter.reset();
		gameHud.reset();
		overlaySuppressor.reset();
		waveTracker.reset();
		idleReminder.reset();
	}

	public void update()
	{
		snapshot = inventoryChecker.scan();
		gameHud.update();
		boolean inMinigame = TemporossArea.isInMinigame(client);
		boolean atUnkah = TemporossArea.isAtUnkah(client, sceneTracker.getSoloLadder(),
			sceneTracker.findDockPump());
		boolean activeMinigame = inMinigame && !atUnkah;
		boolean inContent = activeMinigame || atUnkah;

		if (!inContent)
		{
			if (sawMinigame)
			{
				clearGame();
			}
			gameHud.applyReplacement(false);
			overlaySuppressor.update(false);
			currentAction = HelperAction.idle();
			pathRouter.reset();
			idleReminder.update(false);
			gameSnapshot = GameSnapshot.empty();
			return;
		}

		if (activeMinigame)
		{
			sawMinigame = true;
			updatePhase(gameHud.getEnergy());
			updateDepositFlags(snapshot);
		}
		else if (atUnkah && sawMinigame)
		{
			clearGame();
			waveTracker.reset();
			gameHud.applyReplacement(false);
			gameHud.reset();
			overlaySuppressor.update(false);
			currentAction = HelperAction.idle();
			pathRouter.reset();
		}

		Player player = client.getLocalPlayer();
		WorldPoint loc = player == null ? null : player.getWorldLocation();
		NPC spiritNpc = sceneTracker.northSpiritPool();
		boolean spiritPresent = spiritNpc != null;
		boolean spiritAttackable = TemporossIds.isSpiritPoolAttackable(spiritNpc);
		if (activeMinigame)
		{
			resolveWorkArea(loc);
			rememberLandmarks(loc);
			updateSpiritFlag(snapshot, gameHud.getEnergy(), spiritPresent, spiritAttackable, loc);
		}

		boolean onShip = isOnShip(loc);
		boolean onIsland = isOnIsland(loc);
		if (onShip && onIsland)
		{
			if (shipDistance(loc) <= islandDistance(loc))
			{
				onIsland = false;
			}
			else
			{
				onShip = false;
			}
		}
		boolean busyFishing = isBusyFishing(player);
		boolean busySpirit = isBusySpirit(player);
		boolean busyCooking = isBusyCooking(player);
		boolean victory = sceneTracker.hasVictoryHost();
		int nearbyFires = sceneTracker.fireCountOnSide(workArea);
		if (activeMinigame)
		{
			updateCookFlag(snapshot);
			updateDouseFlag(snapshot, nearbyFires);
		}
		TileObject islandPole = sceneTracker.closestTotem(islandTotem != null ? islandTotem : loc);
		boolean totemBroken = islandPole != null && TemporossIds.isDamagedTotem(islandPole.getId());
		boolean fireOnPlayer = loc != null && sceneTracker.fireThreatWithin(loc, 0);

		gameSnapshot = GameSnapshot.builder()
			.inMinigame(activeMinigame)
			.atUnkah(atUnkah)
			.onShip(onShip)
			.onIsland(onIsland)
			.waveIncoming(waveTracker.isIncoming())
			.tethered(waveTracker.isTethered())
			.totemBroken(totemBroken)
			.hasHammer(snapshot.hasHammerTool())
			.hasRopeOrOutfit(snapshot.hasRopeOrOutfit())
			.hasHarpoon(snapshot.isHasHarpoon())
			.emptyBuckets(snapshot.getEmptyBuckets())
			.waterBuckets(snapshot.getWaterBuckets())
			.rawFish(snapshot.getRawFish())
			.cookedFish(snapshot.getCookedFish())
			.crystalFish(snapshot.getCrystalFish())
			.emptySlots(snapshot.getEmptySlots())
			.energy(gameHud.getEnergy())
			.intensity(gameHud.getIntensity())
			.essence(gameHud.getEssence())
			.points(gameHud.getPoints())
			.spiritPoolUp(spiritPresent)
			.spiritPoolAttackable(spiritAttackable)
			.spiritPoolDone(sawSpiritHarpoon && !spiritAttackable)
			.fireOnPlayerOrBlocking(fireOnPlayer)
			.nearbyFires(nearbyFires)
			.dump16Done(dump16Done)
			.firstCookDone(firstCookDone)
			.douseDone(douseDone)
			.depositingKeep3(depositingKeep3)
			.depositingAll(depositingAll)
			.depositKeep3StopAt(depositKeep3StopAt)
			.depositAllStopAt(depositAllStopAt)
			.needsSpirit(needsSpirit)
			.spiritCycles(spiritCycles)
			.victory(victory)
			.doubleSpotUp(sceneTracker.doubleSpot(loc, workArea) != null)
			.busyFishing(busyFishing)
			.busyCooking(busyCooking)
			.hudVisible(gameHud.isPresent())
			.build();

		idleReminder.update(activeMinigame);

		boolean tidyUi = config.enableHelper() && activeMinigame;
		gameHud.applyReplacement(tidyUi && config.replaceGameHud());
		overlaySuppressor.update(tidyUi && config.hideSkillingOverlays());

		if (!config.enableHelper())
		{
			currentAction = HelperAction.idle();
			pathRouter.reset();
			return;
		}

		RecoveryKind recovery = RecoveryPolicy.decide(gameSnapshot);
		boolean recover = RecoveryPolicy.isRecover(recovery);
		RotationStep step;
		String detail;
		if (recover)
		{
			step = stepForRecovery(recovery);
			detail = detailForRecovery(recovery);
		}
		else
		{
			HappyKind happy = HappyPathPolicy.decide(gameSnapshot);
			step = stepForHappy(happy);
			detail = detailForHappy(happy, gameSnapshot);
		}

		ClickTarget target = targetFor(step, loc);
		if ((step == RotationStep.DEPOSIT || step == RotationStep.DEPOSIT_KEEP3)
			&& shouldEvadeDepositFire(loc))
		{
			detail = "Step aside — fire on the crate tile";
		}
		boolean skipHighlight = shouldAfk(step, gameSnapshot, busySpirit);
		WorldView worldView = client.getTopLevelWorldView();
		WorldPoint dest = destination(target);
		Set<WorldPoint> blocked = sceneTracker.fireTiles(workArea);
		List<WorldPoint> path = dest == null
			? Collections.emptyList()
			: pathRouter.pathTo(worldView, loc, dest, blocked, config.showPath());
		if (step == RotationStep.SOLO_START && loc != null && dest != null
			&& loc.distanceTo(dest) <= RotationConstants.AT_TARGET_TILES + 2)
		{
			path = Collections.emptyList();
		}

		if (log.isDebugEnabled() && currentAction.getStep() != step)
		{
			log.debug("step {} -> {} object={} npc={} tile={} path={} fires={} dump16={} douseDone={}",
				currentAction.getStep(), step,
				target.getObject() == null ? null : target.getObject().getId(),
				target.getNpc() == null ? null : target.getNpc().getId(),
				target.getTile(), path.size(), nearbyFires, dump16Done, douseDone);
		}

		Color color = step.getColor();
		currentAction = new HelperAction(
			step,
			detail,
			path,
			skipHighlight ? null : target.getObject(),
			skipHighlight ? null : target.getNpc(),
			skipHighlight ? null : target.getTile(),
			color,
			recover);

		updateDepositChime();
	}

	/** Counts the last few crate loads down out loud so you can look away from the screen. */
	private void updateDepositChime()
	{
		int left = getDepositActionsLeft();
		if (left == lastDepositsLeft)
		{
			return;
		}
		if (config.depositChime() && left < lastDepositsLeft)
		{
			if (left == 0)
			{
				client.playSoundEffect(SoundEffectID.TOWN_CRIER_BELL_DONG);
			}
			else if (left <= RotationConstants.DEPOSIT_CHIME_FROM)
			{
				client.playSoundEffect(SoundEffectID.UI_BOOP);
			}
		}
		lastDepositsLeft = left;
	}

	private void clearGame()
	{
		workArea = null;
		islandShrine = null;
		islandTotem = null;
		shipMast = null;
		ammoCrate = null;
		dump16Done = false;
		firstCookDone = false;
		douseDone = false;
		sawDouseFires = false;
		depositingKeep3 = false;
		depositingAll = false;
		depositKeep3StopAt = RotationConstants.FIRST_DUMP_KEEP;
		depositAllStopAt = 0;
		lastDepositsLeft = 0;
		needsSpirit = false;
		sawSpiritPool = false;
		sawSpiritHarpoon = false;
		spiritCycles = 0;
		lastEnergy = -1;
		sawMinigame = false;
	}

	private void resolveWorkArea(WorldPoint loc)
	{
		if (loc == null)
		{
			return;
		}
		// Rebuild until the instance is loaded far enough to place the fixed landmarks.
		if (workArea != null && workArea.getShrine() != null && workArea.getSpiritPool() != null)
		{
			return;
		}

		WorldPoint hostTile = loc;
		NPC host = sceneTracker.inferHost(loc);
		if (host != null && host.getWorldLocation() != null
			&& coords.isNorthShore(host.getWorldLocation()))
		{
			hostTile = host.getWorldLocation();
		}
		else
		{
			TileObject pump = sceneTracker.anyShipPump(loc);
			if (pump != null && pump.getWorldLocation() != null
				&& coords.isNorthShore(pump.getWorldLocation()))
			{
				hostTile = pump.getWorldLocation().dx(3).dy(2);
			}
			else
			{
				TileObject mast = sceneTracker.anyMast(loc);
				if (mast != null && mast.getWorldLocation() != null
					&& coords.isNorthShore(mast.getWorldLocation()))
				{
					hostTile = mast.getWorldLocation().dx(0).dy(3);
				}
			}
		}
		workArea = WorkArea.fromSpawn(loc, hostTile, shrineTile(loc), spiritTile(loc));
	}

	private void rememberLandmarks(WorldPoint loc)
	{
		if (loc == null)
		{
			return;
		}

		if (workArea != null)
		{
			islandShrine = workArea.getShrine();
		}

		TileObject mast = sceneTracker.closestMast(loc);
		if (mast != null && mast.getWorldLocation() != null && loc.distanceTo(mast.getWorldLocation()) <= 10)
		{
			shipMast = mast.getWorldLocation();
		}

		if (islandShrine == null)
		{
			WorldPoint shrineHint = shrineTile(loc);
			TileObject shrine = sceneTracker.closestShrine(shrineHint != null ? shrineHint : loc);
			if (shrine != null && shrine.getWorldLocation() != null
				&& loc.distanceTo(shrine.getWorldLocation()) <= 28)
			{
				islandShrine = shrine.getWorldLocation();
			}
		}

		TileObject totem = sceneTracker.closestTotem(loc);
		if (totem != null && totem.getWorldLocation() != null
			&& loc.distanceTo(totem.getWorldLocation()) <= 16)
		{
			islandTotem = totem.getWorldLocation();
		}
	}

	private boolean isOnShip(WorldPoint loc)
	{
		return shipDistance(loc) <= 8;
	}

	private boolean isOnIsland(WorldPoint loc)
	{
		return islandDistance(loc) <= 20;
	}

	private int shipDistance(WorldPoint loc)
	{
		if (loc == null)
		{
			return Integer.MAX_VALUE;
		}
		int best = Integer.MAX_VALUE;
		if (shipMast != null)
		{
			best = Math.min(best, loc.distanceTo(shipMast));
		}
		TileObject mast = sceneTracker.closestMast(loc);
		if (mast != null && mast.getWorldLocation() != null)
		{
			best = Math.min(best, loc.distanceTo(mast.getWorldLocation()));
		}
		TileObject pump = sceneTracker.anyShipPump(loc);
		if (pump != null && pump.getWorldLocation() != null)
		{
			best = Math.min(best, loc.distanceTo(pump.getWorldLocation()));
		}
		return best;
	}

	private int islandDistance(WorldPoint loc)
	{
		if (loc == null)
		{
			return Integer.MAX_VALUE;
		}
		int best = Integer.MAX_VALUE;
		if (islandShrine != null)
		{
			best = Math.min(best, loc.distanceTo(islandShrine));
		}
		if (islandTotem != null)
		{
			best = Math.min(best, loc.distanceTo(islandTotem));
		}
		TileObject shrine = sceneTracker.closestShrine(loc);
		if (shrine != null && shrine.getWorldLocation() != null)
		{
			best = Math.min(best, loc.distanceTo(shrine.getWorldLocation()));
		}
		TileObject totem = sceneTracker.closestTotem(loc);
		if (totem != null && totem.getWorldLocation() != null)
		{
			best = Math.min(best, loc.distanceTo(totem.getWorldLocation()));
		}
		return best;
	}

	private void updatePhase(int energy)
	{
		if (energy < 0)
		{
			return;
		}
		if (lastEnergy >= 0 && lastEnergy <= 5 && energy >= RotationConstants.ENERGY_FULL)
		{
			spiritCycles++;
		}
		lastEnergy = energy;
	}

	private void updateDepositFlags(InventorySnapshot inv)
	{
		int dumpable = inv.getDumpableFish();
		int raw = inv.getRawFish();

		if (depositingKeep3)
		{
			if (raw > 0)
			{
				depositingKeep3 = false;
			}
			else if (dumpable <= depositKeep3StopAt)
			{
				depositingKeep3 = false;
				dump16Done = true;
			}
		}
		else if (!dump16Done && dumpable >= RotationConstants.INVENTORY_TARGET && raw == 0)
		{
			depositingKeep3 = true;
			depositKeep3StopAt = Math.max(
				RotationConstants.FIRST_DUMP_KEEP,
				dumpable - RotationConstants.FIRST_DEPOSIT_COUNT);
		}

		if (depositingAll)
		{
			if (raw > 0)
			{
				depositingAll = false;
			}
			else if (dumpable <= depositAllStopAt)
			{
				depositingAll = false;
				needsSpirit = true;
			}
		}
		else if (dump16Done && dumpable >= RotationConstants.INVENTORY_TARGET && raw == 0)
		{
			depositingAll = true;
			// Deposit exactly 19, never the whole inventory — an over-deposit skips a boss phase.
			depositAllStopAt = Math.max(0, dumpable - RotationConstants.INVENTORY_TARGET);
		}
	}

	private void updateCookFlag(InventorySnapshot inv)
	{
		if (inv.getDumpableFish() >= RotationConstants.FIRST_COOK_AT && inv.getRawFish() == 0)
		{
			firstCookDone = true;
		}
	}

	private void updateDouseFlag(InventorySnapshot inv, int nearbyFires)
	{
		if (!dump16Done || douseDone)
		{
			return;
		}
		// Only track completion while we are actually in the douse window, otherwise a momentary
		// empty fire list during the deposit or fishing legs would end the phase early.
		int keep = Math.max(RotationConstants.FIRST_DUMP_KEEP, depositKeep3StopAt);
		boolean douseWindow = inv.getRawFish() == 0
			&& inv.getDumpableFish() > 0
			&& inv.getDumpableFish() <= keep;
		if (!douseWindow)
		{
			return;
		}
		if (nearbyFires > 0)
		{
			sawDouseFires = true;
			return;
		}
		if (sawDouseFires)
		{
			douseDone = true;
		}
	}

	private WorldPoint shrineTile(WorldPoint from)
	{
		return coords.scene(RotationConstants.NORTH_SHRINE);
	}

	private WorldPoint spiritTile(WorldPoint from)
	{
		return coords.scene(RotationConstants.NORTH_SPIRIT);
	}

	private ClickTarget leaveShipTarget(WorldPoint from)
	{
		NPC spot = sceneTracker.nearestFishingSpot(from, false, workArea);
		if (spot != null)
		{
			return ClickTarget.ofNpc(spot);
		}
		return shrineTarget(from);
	}

	private ClickTarget shrineTarget(WorldPoint from)
	{
		return objectAtLandmark(
			sceneTracker.findNorth(RotationConstants.NORTH_SHRINE, TemporossIds.SHRINE),
			shrineTile(from));
	}

	private ClickTarget spiritTarget(WorldPoint from)
	{
		NPC pool = sceneTracker.northSpiritPool();
		if (pool != null && !TemporossIds.isSpiritPoolAttackable(pool))
		{
			pool = null;
		}
		WorldPoint tile = spiritTile(from);
		if (pool == null)
		{
			return tile == null ? ClickTarget.none() : ClickTarget.ofTile(tile);
		}
		return isWalkTileFor(tile, pool.getWorldLocation())
			? ClickTarget.npcAt(pool, tile)
			: ClickTarget.ofNpc(pool);
	}

	private ClickTarget dockPumpTarget()
	{
		TileObject pump = sceneTracker.findDockPump(gameSnapshot.isInMinigame());
		return objectAtLandmark(pump, coords.scene(RotationConstants.NORTH_DOCK));
	}

	/**
	 * Highlights the object but walks to the landmark tile beside it, so the path ends somewhere
	 * standable. The landmark is only trusted when it really is next to the object we found.
	 */
	private static ClickTarget objectAtLandmark(TileObject object, WorldPoint landmark)
	{
		if (object == null)
		{
			return landmark == null ? ClickTarget.none() : ClickTarget.ofTile(landmark);
		}
		return landmark != null
			&& SceneTracker.distanceTo(object, landmark) <= RotationConstants.LANDMARK_NEAR_TILES
			? ClickTarget.objectAt(object, landmark)
			: ClickTarget.ofObject(object);
	}

	private static boolean isWalkTileFor(WorldPoint landmark, WorldPoint entity)
	{
		return landmark != null && entity != null
			&& landmark.distanceTo(entity) <= RotationConstants.LANDMARK_NEAR_TILES;
	}

	private void updateSpiritFlag(InventorySnapshot inv, int energy, boolean poolPresent,
		boolean poolAttackable, WorldPoint loc)
	{
		// An over-fished remainder left after a capped deposit must not cancel the pool trip, so
		// only a fresh full batch counts as "went fishing instead".
		if (inv != null && inv.getTotalFish() >= RotationConstants.INVENTORY_TARGET)
		{
			needsSpirit = false;
			sawSpiritPool = poolPresent;
			sawSpiritHarpoon = false;
			return;
		}
		if (poolAttackable)
		{
			sawSpiritHarpoon = true;
		}
		if (sawSpiritHarpoon && !poolAttackable)
		{
			needsSpirit = false;
		}
		if (energy >= RotationConstants.ENERGY_FULL && !poolAttackable
			&& (sawSpiritPool || sawSpiritHarpoon || nearSpirit(loc)))
		{
			needsSpirit = false;
		}
		if (lastEnergy >= 0 && lastEnergy < RotationConstants.ENERGY_FULL
			&& energy >= RotationConstants.ENERGY_FULL)
		{
			needsSpirit = false;
		}
		sawSpiritPool = poolPresent;
	}

	private boolean nearSpirit(WorldPoint loc)
	{
		WorldPoint tile = spiritTile(loc);
		return loc != null && tile != null && loc.distanceTo(tile) <= 3;
	}

	private ClickTarget depositTarget(WorldPoint from)
	{
		WorldPoint hint = ammoCrate != null
			? ammoCrate
			: (workArea != null ? workArea.getAmmo() : from);
		NPC crate = sceneTracker.nearestAmmoCrate(hint);
		if (crate != null && crate.getWorldLocation() != null)
		{
			ammoCrate = crate.getWorldLocation();
			return ClickTarget.ofNpc(crate);
		}
		WorldPoint fallback = ammoCrate != null
			? ammoCrate
			: (workArea == null ? null : workArea.getAmmo());
		return fallback == null ? ClickTarget.none() : ClickTarget.ofTile(fallback);
	}

	private ClickTarget depositTargetWithEvade(WorldPoint from)
	{
		if (shouldEvadeDepositFire(from))
		{
			WorldPoint safe = coords.scene(RotationConstants.DEPOSIT_SAFE);
			if (safe != null)
			{
				return ClickTarget.ofTile(safe);
			}
		}
		return depositTarget(from);
	}

	private boolean shouldEvadeDepositFire(WorldPoint player)
	{
		if (player == null)
		{
			return false;
		}
		WorldPoint safe = coords.scene(RotationConstants.DEPOSIT_SAFE);
		if (safe == null || safe.equals(player))
		{
			return false;
		}
		if (sceneTracker.fireThreatWithin(player, RotationConstants.DEPOSIT_FIRE_TILES))
		{
			return true;
		}
		WorldPoint stand = coords.scene(RotationConstants.DEPOSIT_STAND);
		return stand != null && sceneTracker.fireThreatWithin(stand, 0);
	}

	public int getDepositActionsLeft()
	{
		int dumpable = snapshot == null ? 0 : snapshot.getDumpableFish();
		if (depositingKeep3)
		{
			return Math.max(0, dumpable - depositKeep3StopAt);
		}
		if (depositingAll)
		{
			return Math.max(0, dumpable - depositAllStopAt);
		}
		RotationStep step = currentAction == null ? null : currentAction.getStep();
		if (step == RotationStep.DEPOSIT_KEEP3)
		{
			return Math.max(0, dumpable - RotationConstants.FIRST_DUMP_KEEP);
		}
		if (step == RotationStep.DEPOSIT)
		{
			return Math.min(dumpable, RotationConstants.INVENTORY_TARGET);
		}
		return 0;
	}

	private ClickTarget targetFor(RotationStep step, WorldPoint from)
	{
		switch (step)
		{
			case PREP:
				if (!snapshot.isHasHarpoon())
				{
					return objectOrTile(crateOrDock(sceneTracker.harpoonCrate(workArea),
						sceneTracker.getHarpoonCrate()), null);
				}
				if (!snapshot.hasRopeOrOutfit())
				{
					return objectOrTile(crateOrDock(sceneTracker.ropeCrate(workArea),
						sceneTracker.getRopeCrate()), null);
				}
				if (!snapshot.hasHammerTool())
				{
					return objectOrTile(crateOrDock(sceneTracker.hammerCrate(workArea),
						sceneTracker.getHammerCrate()), null);
				}
				return objectOrTile(crateOrDock(sceneTracker.bucketCrate(workArea),
					sceneTracker.getBucketCrate()), null);
			case SOLO_START:
				return ClickTarget.ofObject(sceneTracker.getSoloLadder());
			case FILL_PUMP:
				if (gameSnapshot.isVictory() || !gameSnapshot.isInMinigame())
				{
					return dockPumpTarget();
				}
				return objectOrTile(sceneTracker.shipPump(workArea),
					workArea == null ? null : workArea.getPump());
			case LEAVE_SHIP:
				return leaveShipTarget(from);
			case FISH:
			case FISH_DOUBLE:
				NPC spot = sceneTracker.nearestFishingSpot(from, true, workArea);
				if (spot != null)
				{
					return ClickTarget.ofNpc(spot);
				}
				return shrineTarget(from);
			case COOK:
				return shrineTarget(from);
			case TETHER:
			case REPAIR:
				return objectOrTile(sceneTracker.closestTetherPole(from),
					isOnShip(from) ? shipMast : islandTotem);
			case DEPOSIT:
			case DEPOSIT_KEEP3:
				return depositTargetWithEvade(from);
			case DOUSE:
				return npcOrTile(sceneTracker.nearestOurFire(from, workArea), null);
			case SPIRIT:
				return spiritTarget(from);
			case LEAVE:
				return npcOrTile(sceneTracker.nearestHost(from, workArea),
					workArea == null ? null : workArea.getHost());
			default:
				return ClickTarget.none();
		}
	}

	private static TileObject crateOrDock(TileObject side, TileObject dock)
	{
		return side != null ? side : dock;
	}

	private static ClickTarget objectOrTile(TileObject object, WorldPoint fallback)
	{
		if (object != null)
		{
			return ClickTarget.ofObject(object);
		}
		return fallback == null ? ClickTarget.none() : ClickTarget.ofTile(fallback);
	}

	private static ClickTarget npcOrTile(NPC npc, WorldPoint fallback)
	{
		if (npc != null)
		{
			return ClickTarget.ofNpc(npc);
		}
		return fallback == null ? ClickTarget.none() : ClickTarget.ofTile(fallback);
	}

	private static WorldPoint destination(ClickTarget target)
	{
		if (target == null)
		{
			return null;
		}
		if (target.getTile() != null)
		{
			return target.getTile();
		}
		if (target.getNpc() != null)
		{
			return target.getNpc().getWorldLocation();
		}
		if (target.getObject() != null)
		{
			return target.getObject().getWorldLocation();
		}
		return null;
	}

	private static boolean shouldAfk(RotationStep step, GameSnapshot snap, boolean busySpirit)
	{
		if (snap.isWaveIncoming() && !snap.isTethered())
		{
			return false;
		}
		if (step == RotationStep.FISH && snap.isBusyFishing() && !snap.isDoubleSpotUp())
		{
			return true;
		}
		if (step == RotationStep.FISH_DOUBLE)
		{
			return false;
		}
		if (step == RotationStep.COOK && snap.isBusyCooking() && !snap.isDoubleSpotUp())
		{
			return true;
		}
		return step == RotationStep.SPIRIT && busySpirit;
	}

	private static boolean isBusyFishing(Player player)
	{
		NPC npc = interactingNpc(player);
		return npc != null && TemporossIds.isFishingSpot(npc.getId());
	}

	private static boolean isBusySpirit(Player player)
	{
		NPC npc = interactingNpc(player);
		return npc != null && TemporossIds.isSpiritPool(npc.getId());
	}

	private static NPC interactingNpc(Player player)
	{
		if (player == null || !(player.getInteracting() instanceof NPC))
		{
			return null;
		}
		return (NPC) player.getInteracting();
	}

	private static boolean isBusyCooking(Player player)
	{
		if (player == null)
		{
			return false;
		}
		int anim = player.getAnimation();
		return anim == AnimationID.HUMAN_COOKING
			|| anim == AnimationID.HUMAN_FIRECOOKING
			|| anim == AnimationID.HUMAN_COOKING_LOOP;
	}

	private static RotationStep stepForRecovery(RecoveryKind kind)
	{
		switch (kind)
		{
			case WAVE_REPAIR:
				return RotationStep.REPAIR;
			case WAVE_TETHER:
				return RotationStep.TETHER;
			case LOST_HAMMER:
			case LOST_ROPE:
			case LOST_HARPOON:
			case LOST_BUCKETS:
				return RotationStep.PREP;
			case FILL_BUCKETS:
				return RotationStep.FILL_PUMP;
			case DOUSE_FIRE:
				return RotationStep.DOUSE;
			case INTENSITY_COOK:
				return RotationStep.COOK;
			case INTENSITY_DUMP:
				return RotationStep.DEPOSIT;
			case SPIRIT_POOL:
				return RotationStep.SPIRIT;
			case GO_ISLAND:
				return RotationStep.LEAVE_SHIP;
			case GO_SHIP:
				return RotationStep.DEPOSIT;
			default:
				return RotationStep.IDLE;
		}
	}

	private static String detailForRecovery(RecoveryKind kind)
	{
		switch (kind)
		{
			case WAVE_REPAIR:
				return "Recover: repair the totem";
			case WAVE_TETHER:
				return "Recover: tether to the totem";
			case LOST_HAMMER:
				return "Recover: take a hammer";
			case LOST_ROPE:
				return "Recover: take a rope";
			case LOST_HARPOON:
				return "Recover: take a harpoon";
			case LOST_BUCKETS:
				return "Recover: take buckets";
			case FILL_BUCKETS:
				return "Recover: fill buckets";
			case DOUSE_FIRE:
				return "Recover: douse the fire";
			case INTENSITY_COOK:
				return "Recover: cook before the storm";
			case INTENSITY_DUMP:
				return "Recover: dump before the storm";
			case SPIRIT_POOL:
				return "Recover: harpoon the spirit pool";
			case GO_ISLAND:
				return "Recover: run north to the island";
			case GO_SHIP:
				return "Recover: go to the ship";
			default:
				return "Recover";
		}
	}

	private static RotationStep stepForHappy(HappyKind kind)
	{
		switch (kind)
		{
			case PREP_HARPOON:
			case PREP_ROPE:
			case PREP_HAMMER:
			case PREP_BUCKETS:
				return RotationStep.PREP;
			case SOLO_START:
				return RotationStep.SOLO_START;
			case FILL_SHIP:
			case FILL_PUMP:
			case REFILL_DOCK:
				return RotationStep.FILL_PUMP;
			case LEAVE_SHIP:
				return RotationStep.LEAVE_SHIP;
			case FISH:
				return RotationStep.FISH;
			case FISH_DOUBLE:
				return RotationStep.FISH_DOUBLE;
			case COOK:
				return RotationStep.COOK;
			case DEPOSIT_KEEP3:
				return RotationStep.DEPOSIT_KEEP3;
			case DEPOSIT:
				return RotationStep.DEPOSIT;
			case DOUSE:
				return RotationStep.DOUSE;
			case SPIRIT:
				return RotationStep.SPIRIT;
			case LEAVE_GAME:
				return RotationStep.LEAVE;
			default:
				return RotationStep.IDLE;
		}
	}

	private static String detailForHappy(HappyKind kind, GameSnapshot snap)
	{
		int have = snap.getTotalFish();
		int need = RotationConstants.INVENTORY_TARGET;
		switch (kind)
		{
			case PREP_HARPOON:
				return "Bring a harpoon (equip or inventory)";
			case PREP_ROPE:
				return "Bring a rope, or wear full Spirit Angler";
			case PREP_HAMMER:
				return "Bring a hammer or Imcando hammer";
			case PREP_BUCKETS:
				return "Bring " + RotationConstants.BUCKETS_NEEDED + " buckets";
			case SOLO_START:
				return "Solo-start on the rope ladder";
			case FILL_SHIP:
			case FILL_PUMP:
				return "Fill empty buckets at the ship pump";
			case REFILL_DOCK:
				return "Fill buckets at the north dock pump";
			case LEAVE_SHIP:
				return "Walk off the ship onto the island";
			case FISH:
				if (!snap.isDump16Done() && !snap.isFirstCookDone())
				{
					return "Fish until 8, then cook (" + snap.getTotalFish() + "/8)";
				}
				return "Fish until " + need + " (" + have + "/" + need + ")";
			case FISH_DOUBLE:
				if (!snap.isDump16Done() && !snap.isFirstCookDone())
				{
					if (snap.getTotalFish() >= RotationConstants.FIRST_COOK_AT)
					{
						return "Double spot first, then cook (" + snap.getTotalFish() + "/8)";
					}
					return "Fish until 8 (" + snap.getTotalFish() + "/8)";
				}
				return "Click the double spot (" + have + "/" + need + ")";
			case COOK:
				if (!snap.isDump16Done() && !snap.isFirstCookDone())
				{
					return "Cook 8 at the shrine";
				}
				if (snap.getTotalFish() >= RotationConstants.INVENTORY_TARGET)
				{
					return "Cook before deposit (" + snap.getRawFish() + " raw)";
				}
				return "Cook at the shrine (" + snap.getRawFish() + " raw)";
			case DEPOSIT_KEEP3:
				return "Deposit exactly " + RotationConstants.FIRST_DEPOSIT_COUNT + " fish — keep "
					+ Math.max(RotationConstants.FIRST_DUMP_KEEP, snap.getDepositKeep3StopAt());
			case DEPOSIT:
				return "Deposit exactly " + RotationConstants.INVENTORY_TARGET + " fish";
			case DOUSE:
				return "Douse fires on your island";
			case SPIRIT:
				return "Harpoon the spirit pool until energy is full";
			case LEAVE_GAME:
				return "Click Leave on the NPC";
			default:
				return "Waiting…";
		}
	}
}
