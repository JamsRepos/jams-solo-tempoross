package com.easytempoross;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

@Singleton
public class GameHud
{
	private static final Pattern DIGITS = Pattern.compile("(\\d+)");

	private final Client client;

	@Getter
	private int energy = -1;
	@Getter
	private int intensity = -1;
	@Getter
	private int essence = -1;
	@Getter
	private int points = -1;

	private boolean hidDefaultHud;

	@Inject
	GameHud(Client client)
	{
		this.client = client;
	}

	public void reset()
	{
		restoreDefaultHud();
		clearValues();
	}

	/** The Tempoross HUD interface is loaded, even if we hid it ourselves. */
	public boolean isPresent()
	{
		return client.getWidget(InterfaceID.TemporossHud.CONTENT) != null;
	}

	/** Whether the vanilla HUD is currently shown on screen. */
	public boolean isVisible()
	{
		Widget content = client.getWidget(InterfaceID.TemporossHud.CONTENT);
		return content != null && !content.isHidden();
	}

	public void update()
	{
		if (!isPresent())
		{
			if (!hidDefaultHud)
			{
				clearValues();
			}
			return;
		}
		energy = readBar(
			InterfaceID.TemporossHud.ENERGY_TITLE,
			InterfaceID.TemporossHud.ENERGY,
			InterfaceID.TemporossHud.ENERGY_BAR,
			InterfaceID.TemporossHud.ENERGY_BAR_BACK);
		essence = readBar(
			InterfaceID.TemporossHud.ESSENCE_TITLE,
			InterfaceID.TemporossHud.ESSENCE,
			InterfaceID.TemporossHud.ESSENCE_BAR,
			InterfaceID.TemporossHud.ESSENCE_BAR_BACK);
		intensity = readBar(
			InterfaceID.TemporossHud.STORM_INTENSITY_TITLE,
			InterfaceID.TemporossHud.STORM_INTENSITY,
			InterfaceID.TemporossHud.STORM_INTENSITY_BAR,
			InterfaceID.TemporossHud.STORM_INTENSITY_BAR_BACK);
		points = parseText(InterfaceID.TemporossHud.POINTS_TEXT);
	}

	/**
	 * Hides the vanilla Tempoross bar panel so our status overlay can show the same numbers.
	 */
	void applyReplacement(boolean replace)
	{
		if (!isPresent())
		{
			restoreDefaultHud();
			return;
		}
		if (replace)
		{
			setDefaultHudHidden(true);
		}
		else
		{
			restoreDefaultHud();
		}
	}

	private void restoreDefaultHud()
	{
		if (hidDefaultHud)
		{
			setDefaultHudHidden(false);
		}
	}

	private void setDefaultHudHidden(boolean hidden)
	{
		Widget content = client.getWidget(InterfaceID.TemporossHud.CONTENT);
		if (content == null)
		{
			return;
		}
		content.setHidden(hidden);
		hidDefaultHud = hidden;
	}

	private void clearValues()
	{
		energy = -1;
		intensity = -1;
		essence = -1;
		points = -1;
	}

	/**
	 * Reads a Tempoross HUD bar percentage from its title label, falling back to the vanilla
	 * bar widget width if the label has no parseable text.
	 */
	private int readBar(int titleComponent, int layerComponent, int fillComponent, int backComponent)
	{
		int fromTitle = parseText(titleComponent);
		if (fromTitle >= 0)
		{
			return fromTitle;
		}
		int fromLayer = parseText(layerComponent);
		if (fromLayer >= 0)
		{
			return fromLayer;
		}
		return readBarWidth(fillComponent, backComponent);
	}

	private int parseText(int component)
	{
		return parseWidget(client.getWidget(component));
	}

	private int readBarWidth(int fillComponent, int backComponent)
	{
		Widget fill = client.getWidget(fillComponent);
		Widget back = client.getWidget(backComponent);
		if (fill == null || back == null)
		{
			return -1;
		}
		int backWidth = back.getWidth();
		if (backWidth <= 0)
		{
			return -1;
		}
		return Math.min(100, Math.max(0, fill.getWidth() * 100 / backWidth));
	}

	private int parseWidget(Widget widget)
	{
		if (widget == null)
		{
			return -1;
		}
		int value = firstNumber(widget.getText());
		if (value >= 0)
		{
			return value;
		}
		Widget[] children = widget.getChildren();
		if (children != null)
		{
			for (Widget child : children)
			{
				value = parseWidget(child);
				if (value >= 0)
				{
					return value;
				}
			}
		}
		Widget[] dynamic = widget.getDynamicChildren();
		if (dynamic != null)
		{
			for (Widget child : dynamic)
			{
				value = parseWidget(child);
				if (value >= 0)
				{
					return value;
				}
			}
		}
		return -1;
	}

	static int firstNumber(String text)
	{
		if (text == null || text.isEmpty())
		{
			return -1;
		}
		Matcher matcher = DIGITS.matcher(text);
		if (!matcher.find())
		{
			return -1;
		}
		try
		{
			return Integer.parseInt(matcher.group(1));
		}
		catch (NumberFormatException ex)
		{
			return -1;
		}
	}
}
