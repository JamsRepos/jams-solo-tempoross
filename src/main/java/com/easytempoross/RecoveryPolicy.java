package com.easytempoross;

/**
 * Client-free recovery waterfall. First match wins.
 * Fires, bucket fills, and spirit-pool are happy-path — not recovery interrupts.
 */
final class RecoveryPolicy
{
	private RecoveryPolicy()
	{
	}

	static RecoveryKind decide(GameSnapshot snap)
	{
		if (snap == null || !snap.isInMinigame())
		{
			return RecoveryKind.NONE;
		}

		if (snap.isWaveIncoming() && !snap.isTethered())
		{
			return snap.isTotemBroken() ? RecoveryKind.WAVE_REPAIR : RecoveryKind.WAVE_TETHER;
		}

		if (!snap.isHasRopeOrOutfit())
		{
			return RecoveryKind.LOST_ROPE;
		}
		if (!snap.isHasHarpoon())
		{
			return RecoveryKind.LOST_HARPOON;
		}
		if (snap.getBuckets() < RotationConstants.BUCKETS_NEEDED)
		{
			return RecoveryKind.LOST_BUCKETS;
		}

		if (snap.getIntensity() >= RotationConstants.INTENSITY_EMERGENCY
			&& !snap.isDepositingKeep3() && !snap.isDepositingAll())
		{
			if (snap.getRawFish() > 0)
			{
				return RecoveryKind.INTENSITY_COOK;
			}
			if (snap.getDumpableFish() > 0)
			{
				return RecoveryKind.INTENSITY_DUMP;
			}
		}

		return RecoveryKind.NONE;
	}

	static boolean isRecover(RecoveryKind kind)
	{
		return kind != null && kind != RecoveryKind.NONE;
	}
}
