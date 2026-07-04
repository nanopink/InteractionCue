package com.interactioncue;

public enum InteractionCueMarkerOrigin
{
	TOP_LEFT("Top left"),
	TOP_CENTER("Top center"),
	TOP_RIGHT("Top right"),
	CENTER_LEFT("Center left"),
	CENTER("Center"),
	CENTER_RIGHT("Center right"),
	BOTTOM_LEFT("Bottom left"),
	BOTTOM_CENTER("Bottom center"),
	BOTTOM_RIGHT("Bottom right");

	private final String name;

	InteractionCueMarkerOrigin(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
