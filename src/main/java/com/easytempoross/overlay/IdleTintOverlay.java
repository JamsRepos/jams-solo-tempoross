package com.easytempoross.overlay;

import com.easytempoross.EasyTemporossConfig;
import com.easytempoross.IdleReminder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class IdleTintOverlay extends Overlay
{
	private static final Color TINT = new Color(255, 70, 70, 35);

	private final Client client;
	private final EasyTemporossConfig config;
	private final IdleReminder idleReminder;

	@Inject
	private IdleTintOverlay(Client client, EasyTemporossConfig config, IdleReminder idleReminder)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		this.client = client;
		this.config = config;
		this.idleReminder = idleReminder;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.idleFlash() || !idleReminder.isIdle())
		{
			return null;
		}

		graphics.setColor(TINT);
		graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
		return null;
	}
}
