package com.interactioncue;

import java.awt.Color;
import java.awt.image.BufferedImage;

class InteractionCueLabelPart
{
	private final String text;
	private final Color color;
	private final BufferedImage image;
	private final Color imageColor;

	InteractionCueLabelPart(String text, Color color)
	{
		this(text, color, null, null);
	}

	InteractionCueLabelPart(BufferedImage image, Color color, String text)
	{
		this(text, null, image, color);
	}

	private InteractionCueLabelPart(String text, Color color, BufferedImage image, Color imageColor)
	{
		this.text = text;
		this.color = color;
		this.image = image;
		this.imageColor = imageColor;
	}

	String getText()
	{
		return text;
	}

	Color getColor()
	{
		return color;
	}

	BufferedImage getImage()
	{
		return image;
	}

	Color getImageColor()
	{
		return imageColor;
	}

	boolean isImage()
	{
		return image != null || imageColor != null;
	}
}
