package com.playerindicatorsplus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.FriendsChatRank;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanChannelMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

@Singleton
class PlayerIndicatorsPlusOverlay extends Overlay
{
	private static final int OVERHEAD_TEXT_MARGIN = 40;
	private static final int HORIZONTAL_TEXT_MARGIN = 10;
	private static final int WILDERNESS_WEST_X = 2944;
	private static final int WILDERNESS_EAST_X = 3391;
	private static final int WILDERNESS_SOUTH_Y = 3520;
	private static final int WILDERNESS_NORTH_Y = 3967;
	private static final int WILDERNESS_BOUNDARY_VIEW_DISTANCE = 104;

	private final Client client;
	private final PlayerIndicatorsPlusConfig config;
	private final ChatIconManager chatIconManager;

	@Inject
	private PlayerIndicatorsPlusOverlay(Client client, PlayerIndicatorsPlusConfig config,
		ChatIconManager chatIconManager)
	{
		this.client = client;
		this.config = config;
		this.chatIconManager = chatIconManager;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		PlayerNameLocation nameLocation = config.playerNamePosition();
		if (client.getGameState() != GameState.LOGGED_IN ||
			!config.showPlayerNames() || nameLocation == PlayerNameLocation.DISABLED)
		{
			return null;
		}

		WorldView topLevelWorldView = client.getTopLevelWorldView();
		if (topLevelWorldView == null)
		{
			return null;
		}

		int insideWilderness = client.getVarbitValue(VarbitID.INSIDE_WILDERNESS);
		int insidePvpArea = client.getVarbitValue(VarbitID.PVP_AREA_CLIENT);
		renderWorldView(graphics, topLevelWorldView, insideWilderness, insidePvpArea, nameLocation);
		for (WorldView worldView : topLevelWorldView.worldViews())
		{
			renderWorldView(graphics, worldView, insideWilderness, insidePvpArea, nameLocation);
		}

		return null;
	}

	static boolean isPvpZone(int insideWilderness, int insidePvpArea)
	{
		return insideWilderness == 1 || insidePvpArea == 1;
	}

	static Color selectNameColor(boolean inPvpZone, boolean colorWildernessNames,
		Color safeAreaColor, Color wildernessColor)
	{
		return inPvpZone && colorWildernessNames ? wildernessColor : safeAreaColor;
	}

	static boolean isMainWildernessSurface(int worldX, int worldY, int plane)
	{
		return plane >= 0 && plane <= 3 &&
			worldX >= WILDERNESS_WEST_X && worldX <= WILDERNESS_EAST_X &&
			worldY >= WILDERNESS_SOUTH_Y && worldY <= WILDERNESS_NORTH_Y;
	}

	static boolean isImmediatelySouthOfWildernessLine(int worldX, int worldY, int plane)
	{
		return plane >= 0 && plane <= 3 &&
			worldX >= WILDERNESS_WEST_X && worldX <= WILDERNESS_EAST_X &&
			worldY >= WILDERNESS_SOUTH_Y - WILDERNESS_BOUNDARY_VIEW_DISTANCE &&
			worldY < WILDERNESS_SOUTH_Y;
	}

	static boolean isPlayerInPvpZone(int worldX, int worldY, int plane,
		int insideWilderness, int insidePvpArea)
	{
		if (isMainWildernessSurface(worldX, worldY, plane))
		{
			return true;
		}

		if (isImmediatelySouthOfWildernessLine(worldX, worldY, plane))
		{
			return false;
		}

		// Caves, instances, and non-Wilderness PvP areas do not share the surface
		// boundary, so use the client PvP state for those locations.
		return isPvpZone(insideWilderness, insidePvpArea);
	}

	private void renderWorldView(Graphics2D graphics, WorldView worldView,
		int insideWilderness, int insidePvpArea, PlayerNameLocation nameLocation)
	{
		if (worldView == null)
		{
			return;
		}

		for (Player player : worldView.players())
		{
			if (player == null || player.getName() == null)
			{
				continue;
			}

			LocalPoint localPoint = player.getLocalLocation();
			if (localPoint == null || !worldView.contains(localPoint))
			{
				continue;
			}

			Color playerColor = getPlayerColor(player, insideWilderness, insidePvpArea);
			if (playerColor != null)
			{
				renderPlayerName(graphics, player, playerColor, nameLocation);
			}
		}
	}

	Color getPlayerColor(Player player, int insideWilderness, int insidePvpArea)
	{
		WorldPoint worldPoint = player.getWorldLocation();
		boolean playerInPvpZone = worldPoint == null
			? isPvpZone(insideWilderness, insidePvpArea)
			: isPlayerInPvpZone(
				worldPoint.getX(),
				worldPoint.getY(),
				worldPoint.getPlane(),
				insideWilderness,
				insidePvpArea);
		Color othersColor = selectNameColor(
			playerInPvpZone,
			config.colorWildernessNames(),
			config.safeAreaNameColor(),
			config.wildernessNameColor());
		return getCategoryColor(player, playerInPvpZone, othersColor);
	}

	private Color getCategoryColor(Player player, boolean playerInPvpZone, Color othersColor)
	{
		Player localPlayer = client.getLocalPlayer();
		if (player == localPlayer)
		{
			return isEnabled(config.highlightOwnPlayer()) ? config.ownPlayerColor() : null;
		}

		if (player.isFriend())
		{
			Color friendColor = selectFriendColor(
				playerInPvpZone,
				config.highlightFriendsCombatZones(),
				config.friendsCombatZonesColor(),
				config.highlightFriends(),
				config.friendColor());
			if (friendColor != null)
			{
				return friendColor;
			}
		}

		if (player.isFriendsChatMember() && isEnabled(config.highlightFriendsChat()))
		{
			return config.friendsChatColor();
		}

		if (localPlayer != null && player.getTeam() > 0 &&
			player.getTeam() == localPlayer.getTeam() && isEnabled(config.highlightTeamMembers()))
		{
			return config.teamMemberColor();
		}

		if (player.isClanMember() && isEnabled(config.highlightClanMembers()))
		{
			return config.clanMemberColor();
		}

		return isEnabled(config.highlightOthers()) ? othersColor : null;
	}

	private static boolean isEnabled(PlayerIndicatorsPlusConfig.HighlightSetting setting)
	{
		return setting == PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED;
	}

	static Color selectFriendColor(boolean inCombatZone,
		PlayerIndicatorsPlusConfig.HighlightSetting combatZoneSetting, Color combatZoneColor,
		PlayerIndicatorsPlusConfig.HighlightSetting normalSetting, Color normalColor)
	{
		if (inCombatZone && isEnabled(combatZoneSetting))
		{
			return combatZoneColor;
		}

		return isEnabled(normalSetting) ? normalColor : null;
	}

	FriendsChatRank getFriendsChatRank(Player player)
	{
		FriendsChatManager friendsChatManager = client.getFriendsChatManager();
		if (friendsChatManager == null)
		{
			return FriendsChatRank.UNRANKED;
		}

		FriendsChatMember member = friendsChatManager.findByName(Text.removeTags(player.getName()));
		return member == null ? FriendsChatRank.UNRANKED : member.getRank();
	}

	ClanTitle getClanTitle(Player player)
	{
		ClanChannel clanChannel = client.getClanChannel();
		ClanSettings clanSettings = client.getClanSettings();
		if (clanChannel == null || clanSettings == null)
		{
			return null;
		}

		ClanChannelMember member = clanChannel.findMember(player.getName());
		if (member == null)
		{
			return null;
		}

		ClanRank rank = member.getRank();
		return clanSettings.titleForRank(rank);
	}

	private void renderPlayerName(Graphics2D graphics, Player player, Color nameColor,
		PlayerNameLocation nameLocation)
	{
		String name = Text.sanitize(player.getName());
		int zOffset;
		switch (nameLocation)
		{
			case MODEL_CENTER:
			case MODEL_RIGHT:
				zOffset = player.getLogicalHeight() / 2;
				break;
			default:
				zOffset = player.getLogicalHeight() + OVERHEAD_TEXT_MARGIN;
		}

		Point textLocation = player.getCanvasTextLocation(graphics, name, zOffset);
		if (nameLocation == PlayerNameLocation.MODEL_RIGHT)
		{
			textLocation = player.getCanvasTextLocation(graphics, "", zOffset);
			if (textLocation != null)
			{
				textLocation = new Point(textLocation.getX() + HORIZONTAL_TEXT_MARGIN, textLocation.getY());
			}
		}

		if (textLocation == null)
		{
			return;
		}

		BufferedImage rankImage = getRankImage(player);
		if (rankImage != null)
		{
			int imageWidth = rankImage.getWidth();
			int imageTextMargin = nameLocation == PlayerNameLocation.MODEL_RIGHT ? imageWidth : imageWidth / 2;
			int imageNegativeMargin = nameLocation == PlayerNameLocation.MODEL_RIGHT ? 0 : imageWidth / 2;
			int textHeight = graphics.getFontMetrics().getHeight() - graphics.getFontMetrics().getMaxDescent();
			Point imageLocation = new Point(
				textLocation.getX() - imageNegativeMargin - 1,
				textLocation.getY() - textHeight / 2 - rankImage.getHeight() / 2);
			OverlayUtil.renderImageLocation(graphics, imageLocation, rankImage);
			textLocation = new Point(textLocation.getX() + imageTextMargin, textLocation.getY());
		}

		OverlayUtil.renderTextLocation(graphics, textLocation, name, nameColor);
	}

	private BufferedImage getRankImage(Player player)
	{
		if (player.isFriendsChatMember() && config.showFriendsChatRanks())
		{
			FriendsChatRank rank = getFriendsChatRank(player);
			if (rank != FriendsChatRank.UNRANKED)
			{
				return chatIconManager.getRankImage(rank);
			}
		}

		if (player.isClanMember() && config.showClanChatRanks())
		{
			ClanTitle title = getClanTitle(player);
			if (title != null)
			{
				return chatIconManager.getRankImage(title);
			}
		}

		return null;
	}
}
