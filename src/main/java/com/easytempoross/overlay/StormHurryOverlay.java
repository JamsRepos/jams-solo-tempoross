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

/**
 * Phase-1 alarm when storm intensity hits 90% before the first dump is finished.
 */
public class StormHurryOverlay extends Overlay
{
	private static final Color TITLE = new Color(255, 70, 60);
	private static final Color TITLE_FLASH = new Color(255, 200, 40);
	private static final Color CAPTION = new Color(230, 210, 200);
	private static final Color BACKDROP = new Color(0, 0, 0, 160);
	private static final int PAD = 12;
	private static final int GAP = 4;
	private static final long FLASH_MS = 400L;

	private final EasyTemporossConfig config;
	private final RotationHelper rotationHelper;

	@Inject
	private StormHurryOverlay(EasyTemporossConfig config, RotationHelper rotationHelper)
	{
		setPosition(OverlayPosition.TOP_CENTER);
		setPriority(Overlay.PRIORITY_HIGHEST);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		this.config = config;
		this.rotationHelper = rotationHelper;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableHelper() || !rotationHelper.isStormHurry())
		{
			return null;
		}

		Object hint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		float size = Math.max(36f, config.depositCountdownSize() * 0.9f);
		Font titleFont = FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, size);
		String title = "HURRY";
		String caption = "dump before the storm";

		graphics.setFont(titleFont);
		FontMetrics titles = graphics.getFontMetrics();
		graphics.setFont(FontManager.getRunescapeSmallFont());
		FontMetrics captions = graphics.getFontMetrics();

		int titleWidth = titles.stringWidth(title);
		int captionWidth = captions.stringWidth(caption);
		int width = Math.max(titleWidth, captionWidth) + PAD * 2;
		int height = titles.getAscent() + GAP + captions.getHeight() + PAD * 2;

		graphics.setColor(BACKDROP);
		graphics.fillRoundRect(0, 0, width, height, 8, 8);

		boolean flash = (System.currentTimeMillis() / FLASH_MS) % 2 == 0;
		int titleY = PAD + titles.getAscent();
		graphics.setFont(titleFont);
		draw(graphics, title, (width - titleWidth) / 2, titleY, flash ? TITLE_FLASH : TITLE);

		graphics.setFont(FontManager.getRunescapeSmallFont());
		draw(graphics, caption, (width - captionWidth) / 2, titleY + GAP + captions.getAscent(), CAPTION);

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			hint != null ? hint : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		return new Dimension(width, height);
	}

	private static void draw(Graphics2D graphics, String text, int x, int y, Color color)
	{
		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);
		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}
}
