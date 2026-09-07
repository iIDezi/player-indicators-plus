package com.playerindicatorsplus;

import com.google.inject.Provides;
import java.awt.Color;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.ClientTick;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

@PluginDescriptor(
	name = "Player Indicators Plus",
	description = "Customize player names, tiles, minimap labels, ranks, and PvP-area colors",
	tags = {"wilderness", "pvp", "player", "names", "indicators", "minimap", "overlay"}
)
public class PlayerIndicatorsPlusPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PlayerIndicatorsPlusOverlay overlay;

	@Inject
	private PlayerIndicatorsPlusTileOverlay tileOverlay;

	@Inject
	private PlayerIndicatorsPlusMinimapOverlay minimapOverlay;

	@Inject
	private Client client;

	@Inject
	private ChatIconManager chatIconManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private PlayerIndicatorsPlusConfig config;

	@Provides
	PlayerIndicatorsPlusConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerIndicatorsPlusConfig.class);
	}

	@Override
	protected void startUp()
	{
		migrateLegacyConfig();
		overlayManager.add(overlay);
		overlayManager.add(tileOverlay);
		overlayManager.add(minimapOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(tileOverlay);
		overlayManager.remove(minimapOverlay);
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (client.isMenuOpen())
		{
			return;
		}

		int insideWilderness = client.getVarbitValue(VarbitID.INSIDE_WILDERNESS);
		int insidePvpArea = client.getVarbitValue(VarbitID.PVP_AREA_CLIENT);
		for (MenuEntry entry : client.getMenuEntries())
		{
			if (!isPlayerMenuAction(entry.getType()))
			{
				continue;
			}

			Player player = entry.getPlayer();
			if (player == null || player.getName() == null)
			{
				continue;
			}

			Color color = overlay.getPlayerColor(player, insideWilderness, insidePvpArea);
			FriendsChatRank friendsChatRank = player.isFriendsChatMember() && config.showFriendsChatRanks()
				? overlay.getFriendsChatRank(player) : null;
			ClanTitle clanTitle = player.isClanMember() && config.showClanChatRanks()
				? overlay.getClanTitle(player) : null;
			entry.setTarget(decorateTarget(entry.getTarget(), color, friendsChatRank, clanTitle));
		}
	}

	String decorateTarget(String oldTarget, Color color,
		FriendsChatRank friendsChatRank, ClanTitle clanTitle)
	{
		String newTarget = oldTarget;
		if (color != null && config.colorPlayerMenu())
		{
			String prefix = "";
			int arrowIndex = oldTarget.indexOf("->");
			if (arrowIndex != -1)
			{
				prefix = oldTarget.substring(0, arrowIndex + 3);
				oldTarget = oldTarget.substring(arrowIndex + 3);
			}

			int tagEnd = oldTarget.indexOf('>');
			if (oldTarget.startsWith("<col=") && tagEnd != -1)
			{
				oldTarget = oldTarget.substring(tagEnd + 1);
			}
			newTarget = prefix + ColorUtil.prependColorTag(oldTarget, color);
		}

		int image = -1;
		if (friendsChatRank != null && friendsChatRank != FriendsChatRank.UNRANKED &&
			config.showFriendsChatRanks())
		{
			image = chatIconManager.getIconNumber(friendsChatRank);
		}
		else if (clanTitle != null && config.showClanChatRanks())
		{
			image = chatIconManager.getIconNumber(clanTitle);
		}

		return image == -1 ? newTarget : "<img=" + image + ">" + newTarget;
	}

	private static boolean isPlayerMenuAction(MenuAction action)
	{
		switch (action)
		{
			case WALK:
			case WIDGET_TARGET_ON_PLAYER:
			case ITEM_USE_ON_PLAYER:
			case PLAYER_FIRST_OPTION:
			case PLAYER_SECOND_OPTION:
			case PLAYER_THIRD_OPTION:
			case PLAYER_FOURTH_OPTION:
			case PLAYER_FIFTH_OPTION:
			case PLAYER_SIXTH_OPTION:
			case PLAYER_SEVENTH_OPTION:
			case PLAYER_EIGHTH_OPTION:
			case RUNELITE_PLAYER:
				return true;
			default:
				return false;
		}
	}

	private void migrateLegacyConfig()
	{
		Boolean showOwnName = configManager.getConfiguration(
			PlayerIndicatorsPlusConfig.GROUP, "showOwnName", Boolean.class);
		if (showOwnName == null)
		{
			return;
		}

		if (configManager.getConfiguration(
			PlayerIndicatorsPlusConfig.GROUP, "highlightOwnPlayer") == null)
		{
			PlayerIndicatorsPlusConfig.HighlightSetting setting = showOwnName
				? PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED
				: PlayerIndicatorsPlusConfig.HighlightSetting.DISABLED;
			configManager.setConfiguration(
				PlayerIndicatorsPlusConfig.GROUP, "highlightOwnPlayer", setting);
		}
		configManager.unsetConfiguration(PlayerIndicatorsPlusConfig.GROUP, "showOwnName");
	}
}
