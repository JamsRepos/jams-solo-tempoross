package com.easytempoross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Changelog
{
	static final String VERSION = "1.0.11";

	static final List<Release> RELEASES = List.of(
		new Release("1.0.0",
			"Jam's Solo Tempoross: click-here guidance, path, and a status panel.",
			"Guides the 16/19/19 west-boat rotation, then loops the next game.",
			"Recovers if you AFK, miss a wave, lose tools, or dump the wrong amount."
		),
		new Release("1.0.1",
			"Added Tempoross plugin icon."
		),
		new Release("1.0.2",
			"Renamed from Easy Tempoross to Jam's Solo Tempoross in the plugin panel and Hub."
		),
		new Release("1.0.3",
			"Moved plugin chimes into a Sounds section with their own volume, separate from the game's sound slider.",
			"Added a Final chime dropdown: All Actions bells when fishing, cooking, filling, or the spirit pool finishes; Deposit Only keeps that sound on the crate countdown."
		),
		new Release("1.0.4",
			"Stopped path lines from snapping to an old waypoint and drawing a V across the dock.",
			"If collision cannot walk a route, the line still draws from your current tile to the target.",
			"Updated the plugin icon."
		),
		new Release("1.0.5",
			"The two path checkboxes are now one Path display dropdown: floor & minimap, floor only, minimap only, or off. Your old choice carries over.",
			"Added a Path source dropdown so the Shortest Path plugin can draw the route instead, coloured by the current step.",
			"Fixed the minimap path drawing nothing when the floor path was turned off."
		),
		new Release("1.0.6",
			"Click highlight is now a dropdown: this click, this click & next, next only, or off. Your old on/off choice carries over.",
			"The current-click outline no longer disappears while you fish, cook, or harpoon the pool.",
			"While an action is underway, the following destination can be outlined and labelled Next."
		),
		new Release("1.0.7",
			"Hammer is no longer required for this rotation — dock prep and mid-game recovery skip it.",
			"Fixed Sound volume so the slider actually changes plugin chime loudness (and lowered the default).",
			"Added a Double-fish chime when the helper wants the double spot and inventory still has space.",
			"Advanced settings let you pick the sound for countdown ticks, the stop chime, and the double-fish cue."
		),
		new Release("1.0.8",
			"Far-side double fishing spots are detected by NPC id even when Harpoon actions are not loaded yet.",
			"Stops sending you to a double while you are already cooking; after Tempoross retreats, the helper goes to refill/leave instead of fishing."
		),
		new Release("1.0.9",
			"Rolled back the 1.0.8 double-spot detection changes after they caused delayed action updates and wrong cook/deposit overlays."
		),
		new Release("1.0.10",
			"Far-side double spots are detected by NPC id again (without the 1.0.8 cook-hold / victory-chat changes).",
			"Staying on Deposit for the whole crate load so leftover raw fish cannot flip the helper back to Cook mid-dump.",
			"Normal Fish paths no longer stealth-route to a double (blue line); yellow + the double chime wait until the helper actually wants FISH_DOUBLE.",
			"Doubles are no longer ignored while island fires are burning — that was delaying the yellow path and chime until fires cleared."
		),
		new Release("1.0.11",
			"First crate load is always catch 16 / deposit 16 so you reach the ship before island fires.",
			"At 90% storm before the first dump, the Storm bar flashes and a large HURRY overlay warns you to dump before the game ends.",
			"When a fire forces you off the crate tile during a deposit, a chime plays and the screen tints until you step onto the safe tile."
		)
	);

	private Changelog()
	{
	}

	static boolean isUnseen(String seenVersion)
	{
		return !unseenSince(seenVersion).isEmpty();
	}

	static List<Release> unseenSince(String seenVersion)
	{
		String seen = seenVersion == null ? "" : seenVersion;
		List<Release> unseen = new ArrayList<>();
		for (Release release : RELEASES)
		{
			if (compareVersions(release.version, seen) > 0)
			{
				unseen.add(release);
			}
		}
		return Collections.unmodifiableList(unseen);
	}

	static int compareVersions(String left, String right)
	{
		int[] a = parseVersion(left);
		int[] b = parseVersion(right);
		int n = Math.max(a.length, b.length);
		for (int i = 0; i < n; i++)
		{
			int av = i < a.length ? a[i] : 0;
			int bv = i < b.length ? b[i] : 0;
			if (av != bv)
			{
				return Integer.compare(av, bv);
			}
		}
		return 0;
	}

	private static int[] parseVersion(String version)
	{
		if (version == null || version.isEmpty())
		{
			return new int[0];
		}
		String[] parts = version.split("\\.");
		int[] values = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
		{
			try
			{
				values[i] = Integer.parseInt(parts[i]);
			}
			catch (NumberFormatException ex)
			{
				values[i] = 0;
			}
		}
		return values;
	}

	static final class Release
	{
		final String version;
		final List<String> notes;

		Release(String version, String... notes)
		{
			this.version = version;
			this.notes = List.of(notes);
		}
	}
}
