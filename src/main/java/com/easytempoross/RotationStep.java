package com.easytempoross;

import java.awt.Color;
import lombok.Getter;

@Getter
public enum RotationStep
{
	PREP("Prep", Color.WHITE),
	SOLO_START("Solo-start", new Color(80, 200, 120)),
	FILL_PUMP("Fill buckets", new Color(80, 160, 255)),
	LEAVE_SHIP("Leave ship", Color.CYAN),
	FISH("Fish", Color.CYAN),
	FISH_DOUBLE("Double spot", new Color(255, 210, 40)),
	COOK("Cook", Color.ORANGE),
	TETHER("Tether", new Color(220, 60, 220)),
	REPAIR("Repair", new Color(255, 140, 40)),
	DEPOSIT("Deposit", new Color(220, 50, 50)),
	DEPOSIT_KEEP3("Deposit 16", new Color(220, 50, 50)),
	DOUSE("Douse", new Color(70, 140, 255)),
	SPIRIT("Spirit pool", new Color(160, 80, 255)),
	LEAVE("Leave", new Color(120, 200, 140)),
	IDLE("Waiting…", Color.GRAY);

	private final String label;
	private final Color color;

	RotationStep(String label, Color color)
	{
		this.label = label;
		this.color = color;
	}
}
