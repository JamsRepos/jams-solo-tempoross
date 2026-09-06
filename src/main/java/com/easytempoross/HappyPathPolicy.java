package com.easytempoross;

/**
 * Client-free happy-path inference from a live snapshot.
 */
final class HappyPathPolicy
{
	private HappyPathPolicy()
	{
	}

	/**
	 * The happy-path step after the current action finishes, or {@link HappyKind#IDLE} when
	 * recovery is active, the current step has no peek, or the following step is unchanged.
	 */
	static HappyKind peekAfter(GameSnapshot snap)
	{
		if (snap == null || RecoveryPolicy.isRecover(RecoveryPolicy.decide(snap)))
		{
			return HappyKind.IDLE;
		}
		HappyKind current = decide(snap);
		GameSnapshot after = after(snap, current);
		if (after == null)
		{
			return HappyKind.IDLE;
		}
		HappyKind next = decide(after);
		if (next == HappyKind.IDLE || next == current)
		{
			return HappyKind.IDLE;
		}
		return next;
	}

	private static GameSnapshot after(GameSnapshot snap, HappyKind current)
	{
		switch (current)
		{
			case FISH:
			case FISH_DOUBLE:
				return afterFish(snap);
			case COOK:
				return afterCook(snap);
			case DEPOSIT_KEEP3:
				return afterDepositKeep3(snap);
			case DEPOSIT:
				return afterDepositAll(snap);
			case SPIRIT:
				return afterSpirit(snap);
			default:
				return null;
		}
	}

	private static GameSnapshot afterFish(GameSnapshot snap)
	{
		int need = Math.max(0, fishTarget(snap) - snap.getTotalFish());
		return snap.toBuilder()
			.rawFish(snap.getRawFish() + need)
			.emptySlots(Math.max(0, snap.getEmptySlots() - need))
			.busyFishing(false)
			.build();
	}

	private static GameSnapshot afterCook(GameSnapshot snap)
	{
		int cooked = snap.getCookedFish() + snap.getRawFish();
		return snap.toBuilder()
			.rawFish(0)
			.cookedFish(cooked)
			.firstCookDone(snap.isFirstCookDone() || cooked >= RotationConstants.FIRST_COOK_AT)
			.busyCooking(false)
			.build();
	}

	private static GameSnapshot afterDepositKeep3(GameSnapshot snap)
	{
		int keep = Math.max(RotationConstants.FIRST_DUMP_KEEP, snap.getDepositKeep3StopAt());
		return snap.toBuilder()
			.cookedFish(keep)
			.crystalFish(0)
			.dump16Done(true)
			.depositingKeep3(false)
			.build();
	}

	private static GameSnapshot afterDepositAll(GameSnapshot snap)
	{
		int leftover = Math.max(0, snap.getDepositAllStopAt());
		return snap.toBuilder()
			.cookedFish(leftover)
			.crystalFish(0)
			.depositingAll(false)
			.needsSpirit(true)
			.build();
	}

	private static GameSnapshot afterSpirit(GameSnapshot snap)
	{
		return snap.toBuilder()
			.energy(100)
			.needsSpirit(false)
			.build();
	}

	static HappyKind decide(GameSnapshot snap)
	{
		if (snap == null)
		{
			return HappyKind.IDLE;
		}

		if (!snap.isInMinigame())
		{
			return dock(snap);
		}

		if (snap.isVictory())
		{
			if (snap.getEmptyBuckets() > 0)
			{
				return HappyKind.REFILL_DOCK;
			}
			return HappyKind.LEAVE_GAME;
		}

		if (snap.isOnShip() && snap.getEmptyBuckets() > 0 && snap.getTotalFish() == 0
			&& !snap.isDump16Done())
		{
			return HappyKind.FILL_SHIP;
		}

		if (snap.isOnShip() && snap.getTotalFish() == 0 && !snap.isDump16Done()
			&& snap.getEmptyBuckets() <= 0
			&& !snap.isDepositingKeep3() && !snap.isDepositingAll())
		{
			return HappyKind.LEAVE_SHIP;
		}

		if (shouldSpirit(snap))
		{
			return HappyKind.SPIRIT;
		}

		if (snap.isDepositingKeep3() && snap.getDumpableFish() > snap.getDepositKeep3StopAt())
		{
			// Stay on the crate for the whole load — raw fish must not yank back to the shrine.
			return HappyKind.DEPOSIT_KEEP3;
		}

		if (snap.isDepositingAll() && snap.getDumpableFish() > snap.getDepositAllStopAt())
		{
			return HappyKind.DEPOSIT;
		}

		if (shouldDepositKeep3(snap))
		{
			return HappyKind.DEPOSIT_KEEP3;
		}

		if (shouldDepositAll(snap))
		{
			return HappyKind.DEPOSIT;
		}

		if (needsDouse(snap))
		{
			return HappyKind.DOUSE;
		}

		if (!snap.isDump16Done() && !snap.isFirstCookDone())
		{
			if (snap.getTotalFish() < RotationConstants.FIRST_COOK_AT)
			{
				return snap.isDoubleSpotUp()
					? HappyKind.FISH_DOUBLE : HappyKind.FISH;
			}
			// Doubles can delay the shrine cook until the first batch is full — not past it.
			if (snap.isDoubleSpotUp()
				&& snap.getEmptySlots() > 0
				&& snap.getTotalFish() < RotationConstants.FIRST_BATCH_TARGET)
			{
				return HappyKind.FISH_DOUBLE;
			}
			if (snap.getRawFish() > 0)
			{
				return HappyKind.COOK;
			}
			// Infernal: already cooked through 8 — keep fishing toward 16 (handled below).
		}

		if (snap.getRawFish() > 0 && shouldCook(snap))
		{
			return HappyKind.COOK;
		}

		if (snap.getTotalFish() >= RotationConstants.FIRST_BATCH_TARGET && !snap.isDump16Done())
		{
			if (snap.getRawFish() > 0)
			{
				return HappyKind.COOK;
			}
			return HappyKind.DEPOSIT_KEEP3;
		}

		if (snap.getTotalFish() >= RotationConstants.INVENTORY_TARGET)
		{
			if (snap.getRawFish() > 0)
			{
				return HappyKind.COOK;
			}
			return HappyKind.DEPOSIT;
		}

		if (snap.isDoubleSpotUp() && snap.getEmptySlots() > 0
			&& snap.getTotalFish() < fishTarget(snap))
		{
			return HappyKind.FISH_DOUBLE;
		}

		if (snap.getTotalFish() < fishTarget(snap))
		{
			return snap.isDoubleSpotUp()
				? HappyKind.FISH_DOUBLE : HappyKind.FISH;
		}

		return HappyKind.FISH;
	}

	private static HappyKind dock(GameSnapshot snap)
	{
		if (!snap.isAtUnkah() && !snap.isHudVisible())
		{
			return HappyKind.IDLE;
		}
		if (snap.isRequireHarpoon() && !snap.isHasHarpoon())
		{
			return HappyKind.PREP_HARPOON;
		}
		if (!snap.isHasRopeOrOutfit())
		{
			return HappyKind.PREP_ROPE;
		}
		if (snap.getBuckets() < RotationConstants.BUCKETS_NEEDED)
		{
			return HappyKind.PREP_BUCKETS;
		}
		if (snap.getEmptyBuckets() > 0)
		{
			return HappyKind.REFILL_DOCK;
		}
		return HappyKind.SOLO_START;
	}

	private static boolean shouldCook(GameSnapshot snap)
	{
		if (snap.getRawFish() <= 0)
		{
			return false;
		}
		if (!snap.isDump16Done() && !snap.isFirstCookDone()
			&& snap.getRawFish() >= RotationConstants.FIRST_COOK_AT)
		{
			return true;
		}
		int target = fishTarget(snap);
		if (snap.getTotalFish() >= target)
		{
			return true;
		}
		if (snap.isDoubleSpotUp() && snap.getEmptySlots() > 0
			&& snap.getTotalFish() < target)
		{
			return false;
		}
		return snap.getEmptySlots() <= 0;
	}

	private static boolean shouldSpirit(GameSnapshot snap)
	{
		if (!snap.isDump16Done())
		{
			return false;
		}
		if (snap.isSpiritPoolDone())
		{
			return false;
		}
		if (snap.isSpiritPoolUp() && !snap.isSpiritPoolAttackable())
		{
			return false;
		}
		// A finished deposit-all sends us to the pool even if an over-fished remainder is still held.
		if (snap.isNeedsSpirit())
		{
			return true;
		}
		if (snap.getTotalFish() > 0)
		{
			return false;
		}
		// First dump keeps 0 fish — do not chase the pool until fires are handled and energy
		// has actually collapsed (post-19 dump). needsSpirit covers the normal happy path.
		if (!snap.isDouseDone())
		{
			return false;
		}
		int energy = snap.getEnergy();
		return energy >= 0 && energy <= RotationConstants.ENERGY_SPIRIT;
	}

	private static boolean needsDouse(GameSnapshot snap)
	{
		if (snap.isDouseDone() || snap.getNearbyFires() <= 0)
		{
			return false;
		}
		if (!snap.isDump16Done())
		{
			return false;
		}
		if (snap.isDepositingAll() || snap.isDepositingKeep3())
		{
			return false;
		}
		if (snap.getRawFish() > 0)
		{
			return false;
		}
		int keep = Math.max(RotationConstants.FIRST_DUMP_KEEP, snap.getDepositKeep3StopAt());
		// First dump keeps 0 fish — douse once with an empty fish inventory.
		// douseDone (set when second-batch fishing starts) prevents later empty-inv fire trips.
		if (keep <= 0)
		{
			return snap.getDumpableFish() == 0;
		}
		return snap.getDumpableFish() > 0 && snap.getDumpableFish() <= keep;
	}

	private static boolean shouldDepositKeep3(GameSnapshot snap)
	{
		return !snap.isDump16Done()
			&& snap.getDumpableFish() >= RotationConstants.FIRST_BATCH_TARGET
			&& snap.getRawFish() == 0;
	}

	private static boolean shouldDepositAll(GameSnapshot snap)
	{
		return snap.isDump16Done()
			&& snap.getDumpableFish() >= RotationConstants.INVENTORY_TARGET
			&& snap.getRawFish() == 0;
	}

	static int fishTarget(GameSnapshot snap)
	{
		if (snap.isDump16Done())
		{
			return RotationConstants.INVENTORY_TARGET;
		}
		if (!snap.isFirstCookDone()
			&& snap.getTotalFish() < RotationConstants.FIRST_COOK_AT)
		{
			return RotationConstants.FIRST_COOK_AT;
		}
		// Cooked (Infernal) counts the same as raw toward the first crate load.
		return RotationConstants.FIRST_BATCH_TARGET;
	}
}
