package com.playerindicatorsplus;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PlayerIndicatorsPlusPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PlayerIndicatorsPlusPlugin.class);
		RuneLite.main(args);
	}
}
