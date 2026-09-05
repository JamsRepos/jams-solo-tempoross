package com.easytempoross.overlay;

import com.easytempoross.EasyTemporossConfig;
import com.easytempoross.RotationHelper;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Full-screen tint while a deposit-tile fire forces the player onto the safe stand tile.
 */
public class DepositFireTintOverlay extends Overlay
{
	private static final Color TINT = new Color(255, 70, 70, 45);

	private final Client client;
	private final EasyTemporossConfig config;
	private final RotationHelper rotationHelper;

	@Inject
	private DepositFireTintOverlay(Client client, EasyTemporossConfig config, RotationHelper rotationHelper)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		this.client = client;
		this.config = config;
		this.rotationHelper = rotationHelper;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableHelper() || !rotationHelper.isEvadingDepositFire())
		{
			return null;
		}

		graphics.setColor(TINT);
		graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
		return null;
	}
}
