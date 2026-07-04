package com.interactioncue;

import java.awt.Color;
import java.awt.image.BufferedImage;

class InteractionCue
{
	static final InteractionCue NONE = new InteractionCue(false, "", "", "", null, Color.WHITE);

	private final boolean active;
	private final String action;
	private final String tool;
	private final String target;
	private final BufferedImage image;
	private final Color color;

	InteractionCue(boolean active, String action, String tool, String target, BufferedImage image, Color color)
	{
		this.active = active;
		this.action = action;
		this.tool = tool;
		this.target = target;
		this.image = image;
		this.color = color;
	}

	boolean isActive()
	{
		return active;
	}

	String getAction()
	{
		return action;
	}

	String getTool()
	{
		return tool;
	}

	String getTarget()
	{
		return target;
	}

	BufferedImage getImage()
	{
		return image;
	}

	Color getColor()
	{
		return color;
	}

	String getShortLabel()
	{
		if (action.isEmpty())
		{
			return "";
		}

		return action.length() <= 3 ? action.toUpperCase() : action.substring(0, 3).toUpperCase();
	}
}
