package com.easytempoross;

/**
 * Which clickboxes to outline: the current action, the following destination, both, or neither.
 */
public enum ClickHighlight
{
	THIS("This click"),
	THIS_AND_NEXT("This click & next"),
	NEXT("Next only"),
	OFF("Off");

	private final String label;

	ClickHighlight(String label)
	{
		this.label = label;
	}

	public boolean showsThis()
	{
		return this == THIS || this == THIS_AND_NEXT;
	}

	public boolean showsNext()
	{
		return this == THIS_AND_NEXT || this == NEXT;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
