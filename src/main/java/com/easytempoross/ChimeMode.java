package com.easytempoross;

/**
 * When the louder stop chime plays. Deposit countdown ticks (3, 2, 1) always stay on crate loads.
 */
public enum ChimeMode
{
	ALL_ACTIONS("All Actions"),
	DEPOSIT_ONLY("Deposit Only");

	private final String label;

	ChimeMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
