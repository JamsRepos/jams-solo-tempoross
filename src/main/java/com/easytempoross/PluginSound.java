package com.easytempoross;

import net.runelite.api.SoundEffectID;

/**
 * Curated game sound effects for plugin chimes. Labels show in the Advanced config dropdowns.
 */
public enum PluginSound
{
	UI_BOOP("UI boop", SoundEffectID.UI_BOOP),
	GE_PLOP_UP("GE plop up", SoundEffectID.GE_INCREMENT_PLOP),
	GE_PLOP_DOWN("GE plop down", SoundEffectID.GE_DECREMENT_PLOP),
	GE_DING("GE ding", SoundEffectID.GE_ADD_OFFER_DINGALING),
	GE_COIN("Coin tinkle", SoundEffectID.GE_COIN_TINKLE),
	GE_COLLECT("GE collect", SoundEffectID.GE_COLLECT_BLOOP),
	BELL_DING("Bell ding", SoundEffectID.TOWN_CRIER_BELL_DING),
	BELL_DONG("Bell dong", SoundEffectID.TOWN_CRIER_BELL_DONG),
	PLANT_BLOOP("Plant bloop", SoundEffectID.PICK_PLANT_BLOOP),
	ITEM_PICKUP("Item pickup", SoundEffectID.ITEM_PICKUP),
	PRAYER_TWINKLE("Prayer twinkle", SoundEffectID.PRAYER_DEPLETE_TWINKLE),
	TELEPORT("Teleport", SoundEffectID.TELEPORT_VWOOP);

	private final String label;
	private final int soundId;

	PluginSound(String label, int soundId)
	{
		this.label = label;
		this.soundId = soundId;
	}

	public int getSoundId()
	{
		return soundId;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
