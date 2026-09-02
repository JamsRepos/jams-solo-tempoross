package com.easytempoross.overlay;

import com.easytempoross.ClickHighlight;
import com.easytempoross.EasyTemporossConfig;
import com.easytempoross.HelperAction;
import com.easytempoross.RotationHelper;
import com.easytempoross.RotationStep;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class NextClickOverlay extends Overlay
{
	private static final int PULSE_MS = 1200;
	private static final Stroke PATH_OUTLINE = new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final Stroke PATH_LINE = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	private final Client client;
	private final EasyTemporossConfig config;
	private final RotationHelper rotationHelper;

	@Inject
	private NextClickOverlay(Client client, EasyTemporossConfig config, RotationHelper rotationHelper)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.config = config;
		this.rotationHelper = rotationHelper;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableHelper())
		{
			return null;
		}

		HelperAction action = rotationHelper.getCurrentAction();
		if (action == null || action.getStep() == RotationStep.IDLE)
		{
			return null;
		}

		Color base = action.getColor() != null ? action.getColor() : Color.CYAN;
		if (config.pathDisplay().showsFloor())
		{
			renderPath(graphics, action.getPath(), base);
		}
		ClickHighlight mode = config.clickHighlight();
		if (mode.showsNext())
		{
			Color upcoming = action.getUpcomingColor() != null ? action.getUpcomingColor() : base;
			Color fill = new Color(upcoming.getRed(), upcoming.getGreen(), upcoming.getBlue(), 140);
			renderHighlight(graphics, action.getUpcomingNpc(), action.getUpcomingObject(),
				action.getUpcomingTile(), fill, "Next");
		}
		if (mode.showsThis())
		{
			renderHighlight(graphics, action.getHighlightNpc(), action.getHighlightObject(),
				action.getHighlightTile(), pulse(base), null);
		}
		return null;
	}

	private void renderPath(Graphics2D graphics, List<WorldPoint> path, Color base)
	{
		if (path == null || path.size() < 2)
		{
			return;
		}

		Player player = client.getLocalPlayer();
		WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return;
		}

		int plane = worldView.getPlane();
		Path2D.Float line = new Path2D.Float();
		boolean started = false;

		if (player != null)
		{
			LocalPoint playerLocal = player.getLocalLocation();
			Point canvas = playerLocal == null ? null : Perspective.localToCanvas(client, playerLocal, plane);
			if (canvas != null)
			{
				line.moveTo(canvas.getX(), canvas.getY());
				started = true;
			}
		}

		WorldPoint here = player == null ? null : player.getWorldLocation();
		boolean pendingGap = false;
		for (int i = 0; i < path.size(); i++)
		{
			WorldPoint tile = path.get(i);
			if (i == 0 && here != null && tile.distanceTo(here) <= 1 && path.size() > 1)
			{
				continue;
			}

			LocalPoint local = LocalPoint.fromWorld(worldView, tile);
			if (local == null)
			{
				pendingGap = true;
				continue;
			}
			Point canvas = Perspective.localToCanvas(client, local, plane);
			if (canvas == null)
			{
				pendingGap = true;
				continue;
			}
			if (!started)
			{
				line.moveTo(canvas.getX(), canvas.getY());
				started = true;
			}
			else if (pendingGap)
			{
				line.moveTo(canvas.getX(), canvas.getY());
			}
			else
			{
				line.lineTo(canvas.getX(), canvas.getY());
			}
			pendingGap = false;
		}

		if (!started)
		{
			return;
		}

		Stroke oldStroke = graphics.getStroke();
		Object oldHint = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setStroke(PATH_OUTLINE);
		graphics.setColor(new Color(0, 0, 0, 140));
		graphics.draw(line);
		graphics.setStroke(PATH_LINE);
		graphics.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 220));
		graphics.draw(line);
		graphics.setStroke(oldStroke);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			oldHint != null ? oldHint : RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void renderHighlight(Graphics2D graphics, NPC npc, TileObject object, WorldPoint tile,
		Color color, String label)
	{
		if (npc != null)
		{
			renderNpc(graphics, npc, color);
			if (label != null)
			{
				renderLabel(graphics, npc.getLocalLocation(), npc.getLogicalHeight(), label, color);
			}
			return;
		}
		if (object != null)
		{
			renderObject(graphics, object, color);
			if (label != null)
			{
				renderLabel(graphics, object.getLocalLocation(), 0, label, color);
			}
			return;
		}
		if (tile != null)
		{
			renderTile(graphics, tile, color);
			if (label != null)
			{
				WorldView worldView = client.getTopLevelWorldView();
				LocalPoint local = worldView == null ? null : LocalPoint.fromWorld(worldView, tile);
				renderLabel(graphics, local, 0, label, color);
			}
		}
	}

	private void renderLabel(Graphics2D graphics, LocalPoint local, int height, String label, Color color)
	{
		if (local == null)
		{
			return;
		}
		Point text = Perspective.getCanvasTextLocation(client, graphics, local, label, height);
		if (text != null)
		{
			Color readable = new Color(color.getRed(), color.getGreen(), color.getBlue());
			OverlayUtil.renderTextLocation(graphics, text, label, readable);
		}
	}

	private void renderTile(Graphics2D graphics, WorldPoint tile, Color color)
	{
		if (tile == null)
		{
			return;
		}
		WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return;
		}
		LocalPoint local = LocalPoint.fromWorld(worldView, tile);
		if (local == null)
		{
			return;
		}
		Polygon poly = Perspective.getCanvasTilePoly(client, local);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color);
		}
	}

	private void renderObject(Graphics2D graphics, TileObject object, Color color)
	{
		if (object == null)
		{
			return;
		}

		Shape area = object.getClickbox();
		if (area == null && object instanceof GameObject)
		{
			area = ((GameObject) object).getConvexHull();
		}
		if (area != null)
		{
			Point mouse = client.getMouseCanvasPosition();
			OverlayUtil.renderHoverableArea(graphics, area, mouse, color, color, color);
			return;
		}

		LocalPoint local = object.getLocalLocation();
		if (local == null)
		{
			return;
		}
		int size = 1;
		if (object instanceof GameObject)
		{
			GameObject gameObject = (GameObject) object;
			size = Math.max(1, Math.max(gameObject.sizeX(), gameObject.sizeY()));
		}
		Polygon poly = size > 1
			? Perspective.getCanvasTileAreaPoly(client, local, size)
			: Perspective.getCanvasTilePoly(client, local);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color);
		}
	}

	private void renderNpc(Graphics2D graphics, NPC npc, Color color)
	{
		if (npc == null)
		{
			return;
		}
		Shape hull = npc.getConvexHull();
		if (hull != null)
		{
			Point mouse = client.getMouseCanvasPosition();
			OverlayUtil.renderHoverableArea(graphics, hull, mouse, color, color, color);
			return;
		}
		LocalPoint local = npc.getLocalLocation();
		if (local == null)
		{
			return;
		}
		int size = 1;
		if (npc.getTransformedComposition() != null)
		{
			size = Math.max(1, npc.getTransformedComposition().getSize());
		}
		else if (npc.getComposition() != null)
		{
			size = Math.max(1, npc.getComposition().getSize());
		}
		Polygon poly = size > 1
			? Perspective.getCanvasTileAreaPoly(client, local, size)
			: Perspective.getCanvasTilePoly(client, local);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, color);
		}
	}

	private static Color pulse(Color base)
	{
		long t = System.currentTimeMillis() % PULSE_MS;
		float phase = t < PULSE_MS / 2
			? t / (float) (PULSE_MS / 2)
			: (PULSE_MS - t) / (float) (PULSE_MS / 2);
		int alpha = 60 + (int) (phase * 120);
		return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
	}
}
