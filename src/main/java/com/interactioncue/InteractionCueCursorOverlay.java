package com.interactioncue;

import java.awt.Canvas;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class InteractionCueCursorOverlay extends Overlay
{
	private final Client client;
	private final InteractionCuePlugin plugin;
	private final InteractionCueConfig config;

	@Inject
	InteractionCueCursorOverlay(Client client, InteractionCuePlugin plugin, InteractionCueConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.cursorIndicator() || isMenuInteractionActive())
		{
			return null;
		}

		InteractionCue cue = plugin.getCurrentCue();
		if (!cue.isActive())
		{
			return null;
		}

		Point mouse = client.getMouseCanvasPosition();
		if (!isMouseInsideCanvas(mouse))
		{
			return null;
		}

		int size = config.iconSize();
		FontMetrics metrics = graphics.getFontMetrics();
		List<InteractionCueLabelPart> label = parseLabel(cue);
		if (label.isEmpty())
		{
			return null;
		}

		int contentWidth = getLabelWidth(metrics, label, size);
		int contentHeight = getLabelHeight(metrics, label, size);
		int left = config.backgroundPaddingLeft();
		int top = config.backgroundPaddingTop();
		int right = config.backgroundPaddingRight();
		int bottom = config.backgroundPaddingBottom();
		Rectangle bounds = getTooltipBounds(mouse.getX(), mouse.getY(), contentWidth + left + right, contentHeight + top + bottom);
		int x = bounds.x + left;
		int y = bounds.y + top;
		Color backgroundColor = config.backgroundColor();

		if (config.background() && backgroundColor.getAlpha() > 0)
		{
			graphics.setColor(backgroundColor);
			fillBackground(graphics, bounds, config.backgroundCorner());
		}

		if (config.tooltipBorder() && config.tooltipBorderWidth() > 0 && config.tooltipBorderColor().getAlpha() > 0)
		{
			Stroke stroke = graphics.getStroke();
			graphics.setColor(config.tooltipBorderColor());
			graphics.setStroke(new BasicStroke((float) config.tooltipBorderWidth()));
			drawBorder(graphics, bounds, config.backgroundCorner());
			graphics.setStroke(stroke);
		}

		drawLabel(graphics, label, x, y, contentHeight, size, metrics);

		return null;
	}

	private boolean isMenuInteractionActive()
	{
		return client.isMenuOpen() || client.getMouseCurrentButton() == MouseEvent.BUTTON3;
	}

	private boolean isMouseInsideCanvas(Point mouse)
	{
		if (mouse == null || mouse.getX() < 0 || mouse.getY() < 0 || mouse.getX() >= client.getCanvasWidth() || mouse.getY() >= client.getCanvasHeight())
		{
			return false;
		}

		Canvas canvas = client.getCanvas();
		return canvas == null || canvas.getMousePosition() != null;
	}

	private Rectangle getTooltipBounds(int mouseX, int mouseY, int width, int height)
	{
		InteractionCueTooltipOrigin origin = config.tooltipOrigin();
		if (origin == InteractionCueTooltipOrigin.AUTO)
		{
			origin = InteractionCueTooltipOrigin.CENTER_LEFT;
		}

		Rectangle bounds = getTooltipBoundsForOrigin(origin, mouseX, mouseY, width, height);
		if (bounds.x < 0 || bounds.x + bounds.width > client.getCanvasWidth())
		{
			origin = mirrorHorizontal(origin);
			bounds = getTooltipBoundsForOrigin(origin, mouseX, mouseY, width, height);
		}
		if (bounds.y < 0 || bounds.y + bounds.height > client.getCanvasHeight())
		{
			origin = mirrorVertical(origin);
			bounds = getTooltipBoundsForOrigin(origin, mouseX, mouseY, width, height);
		}
		bounds.x = clamp(bounds.x, 0, Math.max(0, client.getCanvasWidth() - bounds.width));
		bounds.y = clamp(bounds.y, 0, Math.max(0, client.getCanvasHeight() - bounds.height));
		return bounds;
	}

	private InteractionCueTooltipOrigin mirrorHorizontal(InteractionCueTooltipOrigin origin)
	{
		switch (origin)
		{
			case TOP_LEFT:
				return InteractionCueTooltipOrigin.TOP_RIGHT;
			case TOP_RIGHT:
				return InteractionCueTooltipOrigin.TOP_LEFT;
			case CENTER_LEFT:
				return InteractionCueTooltipOrigin.CENTER_RIGHT;
			case CENTER_RIGHT:
				return InteractionCueTooltipOrigin.CENTER_LEFT;
			case BOTTOM_LEFT:
				return InteractionCueTooltipOrigin.BOTTOM_RIGHT;
			case BOTTOM_RIGHT:
				return InteractionCueTooltipOrigin.BOTTOM_LEFT;
			default:
				return origin;
		}
	}

	private InteractionCueTooltipOrigin mirrorVertical(InteractionCueTooltipOrigin origin)
	{
		switch (origin)
		{
			case TOP_LEFT:
				return InteractionCueTooltipOrigin.BOTTOM_LEFT;
			case TOP_CENTER:
				return InteractionCueTooltipOrigin.BOTTOM_CENTER;
			case TOP_RIGHT:
				return InteractionCueTooltipOrigin.BOTTOM_RIGHT;
			case BOTTOM_LEFT:
				return InteractionCueTooltipOrigin.TOP_LEFT;
			case BOTTOM_CENTER:
				return InteractionCueTooltipOrigin.TOP_CENTER;
			case BOTTOM_RIGHT:
				return InteractionCueTooltipOrigin.TOP_RIGHT;
			default:
				return origin;
		}
	}

	private Rectangle getTooltipBoundsForOrigin(InteractionCueTooltipOrigin origin, int mouseX, int mouseY, int width, int height)
	{
		int xOffset = config.xOffset();
		int yOffset = config.yOffset();
		switch (origin)
		{
			case TOP_LEFT:
				return new Rectangle(mouseX + xOffset, mouseY + yOffset, width, height);
			case TOP_CENTER:
				return new Rectangle(mouseX + xOffset - width / 2, mouseY + yOffset, width, height);
			case TOP_RIGHT:
				return new Rectangle(mouseX - xOffset - width, mouseY + yOffset, width, height);
			case CENTER_LEFT:
				return new Rectangle(mouseX + xOffset, mouseY + yOffset - height / 2, width, height);
			case CENTER:
				return new Rectangle(mouseX + xOffset - width / 2, mouseY + yOffset - height / 2, width, height);
			case CENTER_RIGHT:
				return new Rectangle(mouseX - xOffset - width, mouseY + yOffset - height / 2, width, height);
			case BOTTOM_LEFT:
				return new Rectangle(mouseX + xOffset, mouseY - yOffset - height, width, height);
			case BOTTOM_CENTER:
				return new Rectangle(mouseX + xOffset - width / 2, mouseY - yOffset - height, width, height);
			case BOTTOM_RIGHT:
				return new Rectangle(mouseX - xOffset - width, mouseY - yOffset - height, width, height);
			default:
				return new Rectangle(mouseX + xOffset, mouseY + yOffset, width, height);
		}
	}

	private int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}

	private void drawImage(Graphics2D graphics, BufferedImage image, int x, int y, int size)
	{
		double scale = Math.min(size / (double) image.getWidth(), size / (double) image.getHeight());
		int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
		int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
		graphics.drawImage(image, x + (size - width) / 2, y + (size - height) / 2, width, height, null);
	}

	private List<InteractionCueLabelPart> parseLabel(InteractionCue cue)
	{
		List<InteractionCueLabelPart> parts = new ArrayList<>();
		parseFormat(config.labelFormat(), cue, parts, new ColorState(Color.WHITE));
		return parts;
	}

	private void parseFormat(String format, InteractionCue cue, List<InteractionCueLabelPart> parts, ColorState state)
	{
		StringBuilder text = new StringBuilder();

		for (int i = 0; i < format.length(); )
		{
			if (format.charAt(i) == '{')
			{
				int end = findTokenEnd(format, i);
				if (end < 0)
				{
					text.append(format.charAt(i));
					i++;
				}
				else
				{
					String token = format.substring(i + 1, end);
					if (parseToken(token, cue, parts, text, state))
					{
						i = end + 1;
						continue;
					}

					text.append(format, i, end + 1);
					i = end + 1;
				}
			}
			else
			{
				text.append(format.charAt(i));
				i++;
			}
		}

		addPart(parts, text, state.color);
	}

	private boolean parseToken(String token, InteractionCue cue, List<InteractionCueLabelPart> parts, StringBuilder text, ColorState state)
	{
		List<String> conditional = splitTopLevel(token);
		if (conditional.size() > 1)
		{
			addPart(parts, text, state.color);
			String condition = conditional.get(0).trim();
			boolean inverted = condition.startsWith("!");
			String value = getValue(inverted ? condition.substring(1).trim() : condition, cue);
			boolean matches = !value.isEmpty();
			String branch = matches != inverted ? conditional.get(1) : conditional.size() > 2 ? conditional.get(2) : "";
			parseFormat(branch.replace("@1", value), cue, parts, state);
			return true;
		}

		if ("action".equals(token))
		{
			text.append(cue.getAction());
			return true;
		}

		if ("tool".equals(token))
		{
			text.append(cue.getTool());
			return true;
		}

		if ("target".equals(token))
		{
			text.append(cue.getTarget());
			return true;
		}

		if ("action_icon".equals(token) || "icon".equals(token))
		{
			addPart(parts, text, state.color);
			parts.add(new InteractionCueLabelPart(cue.getImage(), cue.getColor(), cue.getShortLabel()));
			return true;
		}

		if (token.startsWith("#"))
		{
			Color parsed = parseColor(token.substring(1));
			if (parsed != null)
			{
				addPart(parts, text, state.color);
				state.color = parsed;
				return true;
			}
		}

		return false;
	}

	private void addPart(List<InteractionCueLabelPart> parts, StringBuilder text, Color color)
	{
		if (text.length() == 0)
		{
			return;
		}

		parts.add(new InteractionCueLabelPart(text.toString(), color));
		text.setLength(0);
	}

	private void drawLabel(Graphics2D graphics, List<InteractionCueLabelPart> parts, int x, int y, int height, int iconSize, FontMetrics metrics)
	{
		int labelX = x;
		int baseline = y + (height + metrics.getAscent() - metrics.getDescent()) / 2;
		for (InteractionCueLabelPart part : parts)
		{
			if (part.isImage())
			{
				drawIcon(graphics, part, labelX, y + (height - iconSize) / 2, iconSize, metrics);
				labelX += iconSize;
				continue;
			}

			graphics.setColor(part.getColor());
			graphics.drawString(part.getText(), labelX, baseline);
			labelX += metrics.stringWidth(part.getText());
		}
	}

	private void drawIcon(Graphics2D graphics, InteractionCueLabelPart part, int x, int y, int size, FontMetrics metrics)
	{
		BufferedImage image = part.getImage();
		if (image != null)
		{
			drawImage(graphics, image, x, y, size);
			return;
		}

		graphics.setColor(part.getImageColor());
		graphics.fillRoundRect(x, y, size, size, 6, 6);
		graphics.setColor(Color.WHITE);
		String fallback = part.getText();
		graphics.drawString(fallback, x + (size - metrics.stringWidth(fallback)) / 2, y + (size + metrics.getAscent() - metrics.getDescent()) / 2);
	}

	private int getLabelWidth(FontMetrics metrics, List<InteractionCueLabelPart> parts, int iconSize)
	{
		int width = 0;
		for (InteractionCueLabelPart part : parts)
		{
			width += part.isImage() ? iconSize : metrics.stringWidth(part.getText());
		}
		return width;
	}

	private int getLabelHeight(FontMetrics metrics, List<InteractionCueLabelPart> parts, int iconSize)
	{
		int height = 0;
		for (InteractionCueLabelPart part : parts)
		{
			height = Math.max(height, part.isImage() ? iconSize : metrics.getHeight());
		}
		return Math.max(1, height);
	}

	private int findTokenEnd(String value, int start)
	{
		int depth = 0;
		for (int i = start; i < value.length(); i++)
		{
			char ch = value.charAt(i);
			if (ch == '{')
			{
				depth++;
			}
			else if (ch == '}')
			{
				depth--;
				if (depth == 0)
				{
					return i;
				}
			}
		}

		return -1;
	}

	private List<String> splitTopLevel(String value)
	{
		List<String> parts = new ArrayList<>();
		int depth = 0;
		int start = 0;
		for (int i = 0; i < value.length(); i++)
		{
			char ch = value.charAt(i);
			if (ch == '{')
			{
				depth++;
			}
			else if (ch == '}')
			{
				depth--;
			}
			else if (ch == ':' && depth == 0)
			{
				parts.add(value.substring(start, i));
				start = i + 1;
				if (parts.size() == 2)
				{
					break;
				}
			}
		}

		if (!parts.isEmpty())
		{
			parts.add(value.substring(start));
		}

		return parts;
	}

	private String getValue(String name, InteractionCue cue)
	{
		switch (name)
		{
			case "action":
				return cue.getAction();
			case "tool":
				return cue.getTool();
			case "target":
				return cue.getTarget();
			default:
				return "";
		}
	}

	private Color parseColor(String value)
	{
		if (value.length() == 3)
		{
			String expanded = "" + value.charAt(0) + value.charAt(0) + value.charAt(1) + value.charAt(1) + value.charAt(2) + value.charAt(2);
			return parseColor(expanded);
		}

		if (value.length() != 6)
		{
			return null;
		}

		try
		{
			return new Color(Integer.parseInt(value, 16));
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
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

	private static class ColorState
	{
		private Color color;

		private ColorState(Color color)
		{
			this.color = color;
		}
	}
}
