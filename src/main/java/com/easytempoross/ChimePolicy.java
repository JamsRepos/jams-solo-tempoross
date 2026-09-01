package com.easytempoross;

/**
 * Client-free rules for the optional stop chime when an AFK step finishes.
 */
final class ChimePolicy
{
	private ChimePolicy()
	{
	}

	static boolean shouldPlayActionStop(RotationStep previous, RotationStep current, ChimeMode mode)
	{
		if (mode != ChimeMode.ALL_ACTIONS || previous == null || current == null || previous == current)
		{
			return false;
		}
		return isAfkStep(previous) && !isDeposit(previous) && !sameActivity(previous, current);
	}

	static boolean isDeposit(RotationStep step)
	{
		return step == RotationStep.DEPOSIT || step == RotationStep.DEPOSIT_KEEP3;
	}

	static boolean isAfkStep(RotationStep step)
	{
		switch (step)
		{
			case FISH:
			case FISH_DOUBLE:
			case COOK:
			case FILL_PUMP:
			case SPIRIT:
			case DEPOSIT:
			case DEPOSIT_KEEP3:
				return true;
			default:
				return false;
		}
	}

	static boolean sameActivity(RotationStep previous, RotationStep current)
	{
		return (isFish(previous) && isFish(current))
			|| (isDeposit(previous) && isDeposit(current));
	}

	private static boolean isFish(RotationStep step)
	{
		return step == RotationStep.FISH || step == RotationStep.FISH_DOUBLE;
	}
}
