package com.easytempoross;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(EasyTemporossConfig.GROUP)
public interface EasyTemporossConfig extends Config
{
	String GROUP = "jams-solo-tempoross";
	String PATH_DISPLAY_KEY = "pathDisplay";
	String PATH_PROVIDER_KEY = "pathProvider";
	String CLICK_HIGHLIGHT_KEY = "clickHighlight";
	/** Replaced by {@link #PATH_DISPLAY_KEY}; still read once by {@link PathDisplayMigration}. */
	String LEGACY_SHOW_PATH_KEY = "showPath";
	/** Replaced by {@link #PATH_DISPLAY_KEY}; still read once by {@link PathDisplayMigration}. */
	String LEGACY_SHOW_MINIMAP_PATH_KEY = "showMinimapPath";
	/** Replaced by {@link #CLICK_HIGHLIGHT_KEY}; still read once by {@link ClickHighlightMigration}. */
	String LEGACY_HIGHLIGHT_NEXT_CLICK_KEY = "highlightNextClick";

	@ConfigSection(
		name = "Helper",
		description = "Click-here solo Tempoross guidance",
		position = 0
	)
	String helperSection = "helper";

	@ConfigSection(
		name = "Sounds",
		description = "Plugin chimes, independent of the in-game sound-effect volume",
		position = 1
	)
	String soundsSection = "sounds";

	@ConfigSection(
		name = "Reminders",
		description = "Idle warning",
		position = 2
	)
	String reminderSection = "reminders";

	@ConfigSection(
		name = "Advanced",
		description = "Pick which game sound plays for each chime",
		position = 3,
		closedByDefault = true
	)
	String advancedSection = "advanced";

	@ConfigItem(
		keyName = "enableHelper",
		name = "Enable helper",
		description = "Show the next-step overlay and status panel",
		section = helperSection,
		position = 0
	)
	default boolean enableHelper()
	{
		return true;
	}

	@ConfigItem(
		keyName = CLICK_HIGHLIGHT_KEY,
		name = "Click highlight",
		description = "This click outlines the current target. Next outlines the following destination (labelled Next) while you are already fishing, cooking, depositing, or harpooning the pool.",
		section = helperSection,
		position = 1
	)
	default ClickHighlight clickHighlight()
	{
		return ClickHighlight.THIS_AND_NEXT;
	}

	@ConfigItem(
		keyName = PATH_DISPLAY_KEY,
		name = "Path display",
		description = "Where to draw the path to your next destination. Click highlights are unaffected.",
		section = helperSection,
		position = 2
	)
	default PathDisplay pathDisplay()
	{
		return PathDisplay.FLOOR_AND_MINIMAP;
	}

	@ConfigItem(
		keyName = PATH_PROVIDER_KEY,
		name = "Path source",
		description = "Plugin lines draws this plugin's own path. Shortest Path hands the destination to the Shortest Path plugin instead, coloured by the current step, and falls back to plugin lines when that plugin is not running.",
		section = helperSection,
		position = 3
	)
	default PathProvider pathProvider()
	{
		return PathProvider.PLUGIN;
	}

	@ConfigItem(
		keyName = "showStatusPanel",
		name = "Show status panel",
		description = "Compact panel for the current step, fish counts, and energy",
		section = helperSection,
		position = 4
	)
	default boolean showStatusPanel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showDepositCountdown",
		name = "Deposit countdown",
		description = "Large on-screen count of crate deposits left so you can AFK until it hits 0",
		section = helperSection,
		position = 5
	)
	default boolean showDepositCountdown()
	{
		return true;
	}

	@Range(min = 24, max = 96)
	@ConfigItem(
		keyName = "depositCountdownSize",
		name = "Countdown size",
		description = "Size of the deposit countdown once it reaches the last 3 deposits",
		section = helperSection,
		position = 6
	)
	default int depositCountdownSize()
	{
		return 48;
	}

	@ConfigItem(
		keyName = "replaceGameHud",
		name = "Replace game HUD",
		description = "Hide the default Tempoross bars and show energy, storm, and points in the status panel",
		section = helperSection,
		position = 7
	)
	default boolean replaceGameHud()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideSkillingOverlays",
		name = "Hide fishing and cooking overlays",
		description = "Hide RuneLite's fishing and cooking stat overlays while you are in a Tempoross game",
		section = helperSection,
		position = 8
	)
	default boolean hideSkillingOverlays()
	{
		return true;
	}

	@ConfigItem(
		keyName = "requireHarpoon",
		name = "Require harpoon",
		description = "When off, skip harpoon prep and recovery so you can fish barehanded (barbarian training)",
		section = helperSection,
		position = 9
	)
	default boolean requireHarpoon()
	{
		return true;
	}

	@ConfigItem(
		keyName = "depositChime",
		name = "Countdown chime",
		description = "Play a chime at 3, 2, and 1 deposits left, then a louder stop at 0",
		section = soundsSection,
		position = 0
	)
	default boolean depositChime()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chimeMode",
		name = "Final chime",
		description = "All Actions also plays the stop sound when fishing, cooking, filling, or the spirit pool finishes. Deposit Only keeps that sound on the crate countdown.",
		section = soundsSection,
		position = 1
	)
	default ChimeMode chimeMode()
	{
		return ChimeMode.ALL_ACTIONS;
	}

	@ConfigItem(
		keyName = "doubleFishChime",
		name = "Double-fish chime",
		description = "Play a soft cue when the helper wants the double fishing spot and your inventory still has space",
		section = soundsSection,
		position = 2
	)
	default boolean doubleFishChime()
	{
		return true;
	}

	@ConfigItem(
		keyName = "depositFireChime",
		name = "Deposit fire alert",
		description = "Play a sound when a fire forces you off the crate tile during a deposit",
		section = soundsSection,
		position = 3
	)
	default boolean depositFireChime()
	{
		return true;
	}

	@Range(max = 127)
	@ConfigItem(
		keyName = "soundVolume",
		name = "Sound volume",
		description = "Volume for plugin chimes (0–127). Independent of the game's sound-effect slider.",
		section = soundsSection,
		position = 4
	)
	default int soundVolume()
	{
		return 32;
	}

	@ConfigItem(
		keyName = "countdownTickSound",
		name = "Countdown tick sound",
		description = "Sound at 3, 2, and 1 deposits left",
		section = advancedSection,
		position = 0
	)
	default PluginSound countdownTickSound()
	{
		return PluginSound.UI_BOOP;
	}

	@ConfigItem(
		keyName = "stopChimeSound",
		name = "Stop chime sound",
		description = "Sound when the deposit countdown hits 0, and for Final chime (All Actions)",
		section = advancedSection,
		position = 1
	)
	default PluginSound stopChimeSound()
	{
		return PluginSound.BELL_DONG;
	}

	@ConfigItem(
		keyName = "doubleFishSound",
		name = "Double-fish sound",
		description = "Sound when the helper wants the double fishing spot",
		section = advancedSection,
		position = 2
	)
	default PluginSound doubleFishSound()
	{
		return PluginSound.GE_DING;
	}

	@ConfigItem(
		keyName = "depositFireSound",
		name = "Deposit fire sound",
		description = "Sound when a fire forces you off the crate tile",
		section = advancedSection,
		position = 3
	)
	default PluginSound depositFireSound()
	{
		return PluginSound.PRAYER_TWINKLE;
	}

	@Range(min = 3, max = 120)
	@ConfigItem(
		keyName = "idleReminderSeconds",
		name = "Idle reminder (seconds)",
		description = "Warn if you stand still at Tempoross this long",
		section = reminderSection,
		position = 0
	)
	default int idleReminderSeconds()
	{
		return 15;
	}

	@ConfigItem(
		keyName = "idleFlash",
		name = "Idle screen tint",
		description = "Gently tint the screen when idle. Off by default.",
		section = reminderSection,
		position = 1
	)
	default boolean idleFlash()
	{
		return false;
	}
}
