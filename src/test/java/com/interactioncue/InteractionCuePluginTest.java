package com.interactioncue;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class InteractionCuePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(InteractionCuePlugin.class);
		RuneLite.main(args);
	}
}
