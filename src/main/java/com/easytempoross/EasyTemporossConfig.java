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
	String SEEN_CHANGELOG_VERSION_KEY = "seenChangelogVersion";
	String PATH_DISPLAY_KEY = "pathDisplay";
	String PATH_PROVIDER_KEY = "pathProvider";
	/** Replaced by {@link #PATH_DISPLAY_KEY}; still read once by {@link PathDisplayMigration}. */
	String LEGACY_SHOW_PATH_KEY = "showPath";
	/** Replaced by {@link #PATH_DISPLAY_KEY}; still read once by {@link PathDisplayMigration}. */
	String LEGACY_SHOW_MINIMAP_PATH_KEY = "showMinimapPath";

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
		keyName = "highlightNextClick",
		name = "Highlight next click",
		description = "Highlight the next object's or NPC's clickbox",
		section = helperSection,
		position = 1
	)
	default boolean highlightNextClick()
	{
		return true;
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

	@Range(max = 127)
	@ConfigItem(
		keyName = "soundVolume",
		name = "Sound volume",
		description = "Volume for plugin chimes. This does not change the game's own sound-effect slider.",
		section = soundsSection,
		position = 2
	)
	default int soundVolume()
	{
		return 64;
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

	@ConfigItem(
		keyName = SEEN_CHANGELOG_VERSION_KEY,
		name = "Seen changelog version",
		description = "Last Jam's Solo Tempoross version whose update notes were shown in chat.",
		hidden = true
	)
	default String seenChangelogVersion()
	{
		return "";
	}
}
