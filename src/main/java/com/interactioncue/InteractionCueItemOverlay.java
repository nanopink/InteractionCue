package com.interactioncue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

public class InteractionCueItemOverlay extends WidgetItemOverlay
{
	private final InteractionCuePlugin plugin;
	private final InteractionCueConfig config;

	@Inject
	InteractionCueItemOverlay(InteractionCuePlugin plugin, InteractionCueConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem item)
	{
		InteractionCue cue = plugin.getPendingCue();
		if (!config.pendingMarker() || !cue.isActive() || !plugin.isPendingSlot(item.getWidget().getIndex()))
		{
			return;
		}

		Rectangle itemBounds = item.getCanvasBounds();
		int iconSize = config.pendingMarkerIconSize();
		Rectangle markerBounds = getMarkerBounds(
			itemBounds,
			iconSize + config.pendingMarkerPaddingLeft() + config.pendingMarkerPaddingRight(),
			iconSize + config.pendingMarkerPaddingTop() + config.pendingMarkerPaddingBottom()
		);
		Rectangle iconBounds = new Rectangle(
			markerBounds.x + config.pendingMarkerPaddingLeft(),
			markerBounds.y + config.pendingMarkerPaddingTop(),
			iconSize,
			iconSize
		);
		Color color = config.pendingMarkerColor();

		if (config.pendingMarkerBackground())
		{
			graphics.setColor(config.pendingMarkerBackgroundColor());
			fillBackground(graphics, markerBounds, config.pendingMarkerCorner());
		}

		if (config.pendingMarkerIcon())
		{
			BufferedImage image = cue.getImage();
			if (image != null)
			{
				drawImage(graphics, image, iconBounds);
			}
		}

		if (config.pendingMarkerBorder())
		{
			Stroke stroke = graphics.getStroke();
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke((float) config.pendingMarkerBorderWidth()));
			drawBorder(graphics, markerBounds, config.pendingMarkerCorner());
			graphics.setStroke(stroke);
		}
	}

	private Rectangle getMarkerBounds(Rectangle itemBounds, int width, int height)
	{
		InteractionCueMarkerOrigin origin = config.pendingMarkerOrigin();
		int x = itemBounds.x;
		int y = itemBounds.y;
		switch (origin)
		{
			case TOP_CENTER:
				x += (itemBounds.width - width) / 2;
				break;
			case TOP_RIGHT:
				x += itemBounds.width - width;
				break;
			case CENTER_LEFT:
				y += (itemBounds.height - height) / 2;
				break;
			case CENTER:
				x += (itemBounds.width - width) / 2;
				y += (itemBounds.height - height) / 2;
				break;
			case CENTER_RIGHT:
				x += itemBounds.width - width;
				y += (itemBounds.height - height) / 2;
				break;
			case BOTTOM_LEFT:
				y += itemBounds.height - height;
				break;
			case BOTTOM_CENTER:
				x += (itemBounds.width - width) / 2;
				y += itemBounds.height - height;
				break;
			case BOTTOM_RIGHT:
				x += itemBounds.width - width;
				y += itemBounds.height - height;
				break;
			default:
				break;
		}

		return new Rectangle(x + config.pendingMarkerXOffset(), y + config.pendingMarkerYOffset(), width, height);
	}

	private void drawImage(Graphics2D graphics, BufferedImage image, Rectangle bounds)
	{
		double scale = Math.min(bounds.width / (double) image.getWidth(), bounds.height / (double) image.getHeight());
		int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
		int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
		graphics.drawImage(image, bounds.x + (bounds.width - width) / 2, bounds.y + (bounds.height - height) / 2, width, height, null);
	}

	private void fillBackground(Graphics2D graphics, Rectangle bounds, int corner)
	{
		if (corner <= 0)
		{
			graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			return;
		}

		graphics.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, corner * 2, corner * 2);
	}

	private void drawBorder(Graphics2D graphics, Rectangle bounds, int corner)
	{
		if (corner <= 0)
		{
			graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
			return;
		}

		graphics.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, corner * 2, corner * 2);
	}
}
