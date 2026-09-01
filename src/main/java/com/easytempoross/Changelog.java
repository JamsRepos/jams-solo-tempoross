package com.easytempoross;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Changelog
{
	static final String VERSION = "1.0.3";

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
