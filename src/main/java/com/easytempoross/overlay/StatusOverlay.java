package com.easytempoross.overlay;

import com.easytempoross.EasyTemporossConfig;
import com.easytempoross.EasyTemporossPlugin;
import com.easytempoross.GameSnapshot;
import com.easytempoross.HelperAction;
import com.easytempoross.IdleReminder;
import com.easytempoross.InventorySnapshot;
import com.easytempoross.RotationHelper;
import com.easytempoross.RotationStep;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;

import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class StatusOverlay extends OverlayPanel
{
	private static final Color LABEL = Color.WHITE;
	private static final Color DETAIL = new Color(170, 170, 170);
	private static final Color WARN = new Color(255, 168, 76);
	private static final Color STORM_FLASH = new Color(255, 60, 50);
	private static final Color ENERGY = new Color(0, 200, 220);
	private static final Color ESSENCE = new Color(82, 161, 82);
	private static final Color STORM = new Color(220, 220, 220);
	private static final Color BAR_BACK = new Color(40, 40, 40, 180);
	private static final Dimension BAR_SIZE = new Dimension(150, 14);
	private static final Dimension SIZE = new Dimension(166, 0);
	private static final long STORM_FLASH_MS = 400L;

	private final EasyTemporossConfig config;
	private final RotationHelper rotationHelper;
	private final IdleReminder idleReminder;

	@Inject
	private StatusOverlay(
		EasyTemporossPlugin plugin,
		EasyTemporossConfig config,
		RotationHelper rotationHelper,
		IdleReminder idleReminder)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.config = config;
		this.rotationHelper = rotationHelper;
		this.idleReminder = idleReminder;
		panelComponent.setBorder(new Rectangle(6, 6, 6, 6));
		panelComponent.setGap(new Point(0, 3));
		panelComponent.setPreferredSize(SIZE);
		addMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Jam's Solo Tempoross");
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		HelperAction action = rotationHelper.getCurrentAction();
		GameSnapshot game = rotationHelper.getGameSnapshot();
		boolean showHud = config.replaceGameHud()
			&& game != null
			&& game.isInMinigame()
			&& !game.isAtUnkah()
			&& game.isHudVisible();
		boolean showRotation = config.showStatusPanel()
			&& config.enableHelper()
			&& game != null
			&& (game.isInMinigame() || game.isAtUnkah())
			&& action != null
			&& action.getStep() != RotationStep.IDLE;
		List<String> warnings = config.showStatusPanel() ? idleReminder.getWarnings() : List.of();

		if (!showHud && !showRotation && warnings.isEmpty())
		{
			return null;
		}

		if (showHud)
		{
			renderCompactHud(game);
		}

		if (showRotation)
		{
			if (showHud)
			{
				panelComponent.getChildren().add(spacer());
			}
			Color stepColor = action.getColor() != null ? action.getColor() : Color.WHITE;
			String next = action.isRecover() ? "Recover" : "Next";
			panelComponent.getChildren().add(line(next, action.getStep().getLabel(), stepColor));
			if (action.getDetail() != null && !action.getDetail().isEmpty())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(action.getDetail())
					.leftColor(action.isRecover() ? WARN : DETAIL)
					.leftFont(FontManager.getRunescapeSmallFont())
					.build());
			}

			InventorySnapshot inv = rotationHelper.getSnapshot();
			if (inv != null)
			{
				panelComponent.getChildren().add(line("Raw", String.valueOf(inv.getRawFish()), LABEL));
				panelComponent.getChildren().add(line("Cooked", String.valueOf(inv.getDumpableFish()), LABEL));
				panelComponent.getChildren().add(line("Buckets", String.valueOf(inv.getWaterBuckets())
					+ "/" + inv.getBuckets(), LABEL));
			}
		}

		for (String warning : warnings)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(warning)
				.leftColor(WARN)
				.build());
		}

		return super.render(graphics);
	}

	private void renderCompactHud(GameSnapshot game)
	{
		addBar("Energy", game.getEnergy(), ENERGY);
		addBar("Essence", game.getEssence(), ESSENCE);
		Color stormFill = stormBarColor(game.getIntensity(), rotationHelper.isStormHurry());
		addBar("Storm", game.getIntensity(), stormFill);
		if (game.getPoints() >= 0)
		{
			panelComponent.getChildren().add(line("Points", String.valueOf(game.getPoints()), LABEL));
		}
	}

	private void addBar(String label, int value, Color fill)
	{
		if (value < 0)
		{
			return;
		}
		ProgressBarComponent bar = new ProgressBarComponent();
		bar.setMaximum(100);
		bar.setValue(value);
		bar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.TEXT_ONLY);
		bar.setCenterLabel(label + ": " + value + "%");
		bar.setForegroundColor(fill);
		bar.setBackgroundColor(BAR_BACK);
		bar.setFontColor(LABEL);
		bar.setPreferredSize(BAR_SIZE);
		panelComponent.getChildren().add(bar);
	}

	private static Color stormBarColor(int intensity, boolean hurry)
	{
		if (!hurry || intensity < 90)
		{
			return intensity >= 90 ? WARN : STORM;
		}
		boolean flash = (System.currentTimeMillis() / STORM_FLASH_MS) % 2 == 0;
		return flash ? STORM_FLASH : WARN;
	}

	private static LineComponent spacer()
	{
		return LineComponent.builder().left("").build();
	}

	private static LineComponent line(String left, String right, Color rightColor)
	{
		return LineComponent.builder()
			.left(left)
			.leftColor(LABEL)
			.right(right)
			.rightColor(rightColor)
			.build();
	}
}
