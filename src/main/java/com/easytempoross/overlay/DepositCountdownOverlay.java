package com.easytempoross.overlay;

import com.easytempoross.EasyTemporossConfig;
import com.easytempoross.RotationHelper;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class DepositCountdownOverlay extends Overlay
{
	private static final Color CALM = new Color(120, 210, 235);
	private static final Color ALERT = new Color(255, 180, 60);
	private static final Color STOP = new Color(235, 80, 70);
	private static final Color CAPTION = new Color(190, 190, 190);
	private static final Color BACKDROP = new Color(0, 0, 0, 140);
	private static final int ALERT_AT = 3;
	private static final int PAD = 10;
	private static final int GAP = 2;

	private final EasyTemporossConfig config;
	private final RotationHelper rotationHelper;

	@Inject
	private DepositCountdownOverlay(EasyTemporossConfig config, RotationHelper rotationHelper)
	{
		setPosition(OverlayPosition.TOP_CENTER);
		setPriority(Overlay.PRIORITY_HIGH);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.config = config;
		this.rotationHelper = rotationHelper;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableHelper() || !config.showDepositCountdown())
		{
			return null;
		}

		int left = rotationHelper.getDepositActionsLeft();
		if (left <= 0)
		{
			return null;
		}

		boolean alert = left <= ALERT_AT;
		Object hint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		float size = config.depositCountdownSize() * (alert ? 1f : 0.6f);
		Font numberFont = FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, size);
		String count = String.valueOf(left);
		String caption = left == 1 ? "last deposit" : "deposits left";

		graphics.setFont(numberFont);
		FontMetrics numbers = graphics.getFontMetrics();
		graphics.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics captions = graphics.getFontMetrics();

		int numberWidth = numbers.stringWidth(count);
		int captionWidth = captions.stringWidth(caption);
		int width = Math.max(numberWidth, captionWidth) + PAD * 2;
		int height = numbers.getAscent() + GAP + captions.getHeight() + PAD * 2;

		graphics.setColor(BACKDROP);
		graphics.fillRoundRect(0, 0, width, height, 8, 8);

		int numberY = PAD + numbers.getAscent();
		graphics.setFont(numberFont);
		draw(graphics, count, (width - numberWidth) / 2, numberY, color(left));

		graphics.setFont(FontManager.getRunescapeSmallFont());
		draw(graphics, caption, (width - captionWidth) / 2, numberY + GAP + captions.getAscent(), CAPTION);

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			hint != null ? hint : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		return new Dimension(width, height);
	}

	private static Color color(int left)
	{
		if (left == 1)
		{
			return STOP;
		}
		return left <= ALERT_AT ? ALERT : CALM;
	}

	private static void draw(Graphics2D graphics, String text, int x, int y, Color color)
	{
		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);
		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}
