package com.easytempoross;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import lombok.Value;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;

@Value
public class HelperAction
{
	RotationStep step;
	String detail;
	List<WorldPoint> path;
	TileObject highlightObject;
	NPC highlightNpc;
	WorldPoint highlightTile;
	Color color;
	boolean recover;
	TileObject upcomingObject;
	NPC upcomingNpc;
	WorldPoint upcomingTile;
	Color upcomingColor;

	public static HelperAction idle()
	{
		return new HelperAction(RotationStep.IDLE, "Waiting…", Collections.emptyList(),
			null, null, null, Color.GRAY, false, null, null, null, null);
	}

	public static HelperAction of(RotationStep step, String detail, boolean recover)
	{
		return new HelperAction(step, detail, Collections.emptyList(),
			null, null, null, step.getColor(), recover, null, null, null, null);
	}
}
