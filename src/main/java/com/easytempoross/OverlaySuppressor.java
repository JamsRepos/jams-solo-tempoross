package com.easytempoross;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

/**
 * Temporarily removes RuneLite skilling overlays that stack on top of our status panel.
 */
@Singleton
class OverlaySuppressor
{
	private static final Set<String> TARGETS = new HashSet<>(Arrays.asList(
		"FishingOverlay",
		"CookingOverlay"
	));

	private final OverlayManager overlayManager;
	private final List<Overlay> suppressed = new ArrayList<>();

	@Inject
	OverlaySuppressor(OverlayManager overlayManager)
	{
		this.overlayManager = overlayManager;
	}

	void update(boolean active)
	{
		if (active)
		{
			if (suppressed.isEmpty())
			{
				overlayManager.removeIf(overlay ->
				{
					if (TARGETS.contains(overlay.getName()))
					{
						suppressed.add(overlay);
						return true;
					}
					return false;
				});
			}
		}
		else
		{
			restore();
		}
	}

	void reset()
	{
		restore();
	}

	private void restore()
	{
		for (Overlay overlay : suppressed)
		{
			overlayManager.add(overlay);
		}
		suppressed.clear();
	}
}
