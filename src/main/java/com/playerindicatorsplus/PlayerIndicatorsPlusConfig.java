package com.playerindicatorsplus;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(PlayerIndicatorsPlusConfig.GROUP)
public interface PlayerIndicatorsPlusConfig extends Config
{
	String GROUP = "player-indicators-plus";

	@ConfigSection(
		name = "Highlight options",
		description = "Toggle highlighted players by type and choose their colors",
		position = 99
	)
	String highlightSection = "highlightOptions";

	enum HighlightSetting
	{
		DISABLED,
		ENABLED
	}

	@ConfigItem(
		position = 0,
		keyName = "showPlayerNames",
		name = "Player names",
		description = "Display player names above characters"
	)
	default boolean showPlayerNames()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "colorWildernessNames",
		name = "Display colored names in Wilderness",
		description = "Use the separate Wilderness/PvP color for other players. When disabled, the safe-area color is used"
	)
	default boolean colorWildernessNames()
	{
		return true;
	}

	@ConfigItem(
		position = 0,
		keyName = "highlightOwnPlayer",
		name = "Highlight own player",
		description = "Configures whether your own player name is displayed",
		section = highlightSection
	)
	default HighlightSetting highlightOwnPlayer()
	{
		return HighlightSetting.DISABLED;
	}

	@ConfigItem(
		position = 1,
		keyName = "ownPlayerColor",
		name = "Own player",
		description = "Color of your own player name",
		section = highlightSection
	)
	default Color ownPlayerColor()
	{
		return new Color(0, 184, 212);
	}

	@ConfigItem(
		position = 2,
		keyName = "highlightFriends",
		name = "Highlight friends",
		description = "Use a separate color for friends",
		section = highlightSection
	)
	default HighlightSetting highlightFriends()
	{
		return HighlightSetting.ENABLED;
	}

	@ConfigItem(
		position = 3,
		keyName = "friendColor",
		name = "Friend",
		description = "Color of friend names",
		section = highlightSection
	)
	default Color friendColor()
	{
		return new Color(0, 200, 83);
	}

	@ConfigItem(
		position = 4,
		keyName = "highlightFriendsCombatZones",
		name = "Highlight friends - Combat Zones",
		description = "Use a separate color for friends who are inside Wilderness or PvP combat zones",
		section = highlightSection
	)
	default HighlightSetting highlightFriendsCombatZones()
	{
		return HighlightSetting.ENABLED;
	}

	@ConfigItem(
		position = 5,
		keyName = "friendsCombatZonesColor",
		name = "Friends - Combat Zones",
		description = "Color of friend names while those friends are inside combat zones",
		section = highlightSection
	)
	default Color friendsCombatZonesColor()
	{
		return new Color(255, 140, 0);
	}

	@ConfigItem(
		position = 6,
		keyName = "highlightFriendsChat",
		name = "Highlight friends chat members",
		description = "Use a separate color for friends chat members",
		section = highlightSection
	)
	default HighlightSetting highlightFriendsChat()
	{
		return HighlightSetting.ENABLED;
	}

	@ConfigItem(
		position = 7,
		keyName = "friendsChatColor",
		name = "Friends chat",
		description = "Color of friends chat member names",
		section = highlightSection
	)
	default Color friendsChatColor()
	{
		return new Color(170, 0, 255);
	}

	@ConfigItem(
		position = 8,
		keyName = "highlightTeamMembers",
		name = "Highlight team members",
		description = "Use a separate color for players on your team",
		section = highlightSection
	)
	default HighlightSetting highlightTeamMembers()
	{
		return HighlightSetting.ENABLED;
	}

	@ConfigItem(
		position = 9,
		keyName = "teamMemberColor",
		name = "Team member",
		description = "Color of team member names",
		section = highlightSection
	)
	default Color teamMemberColor()
	{
		return new Color(19, 110, 247);
	}

	@ConfigItem(
		position = 10,
		keyName = "highlightClanMembers",
		name = "Highlight clan members",
		description = "Use a separate color for clan members",
		section = highlightSection
	)
	default HighlightSetting highlightClanMembers()
	{
		return HighlightSetting.ENABLED;
	}

	@ConfigItem(
		position = 11,
		keyName = "clanMemberColor",
		name = "Clan member",
		description = "Color of clan member names",
		section = highlightSection
	)
	default Color clanMemberColor()
	{
		return new Color(36, 15, 171);
	}

	@ConfigItem(
		position = 12,
		keyName = "highlightOthers",
		name = "Highlight others",
		description = "Display players who do not use an enabled category color",
		section = highlightSection
	)
	default HighlightSetting highlightOthers()
	{
		return HighlightSetting.ENABLED;
	}

	@ConfigItem(
		position = 13,
		keyName = "safeAreaNameColor",
		name = "Others - Safe Areas",
		description = "Color of other player names in safe areas",
		section = highlightSection
	)
	default Color safeAreaNameColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		position = 14,
		keyName = "wildernessNameColor",
		name = "Others - Combat Zones",
		description = "Color of other player names in Wilderness and PvP combat zones",
		section = highlightSection
	)
	default Color wildernessNameColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 2,
		keyName = "drawPlayerTiles",
		name = "Draw tiles under players",
		description = "Draw a colored tile underneath highlighted players"
	)
	default boolean drawTiles()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "playerNamePosition",
		name = "Name position",
		description = "Choose where player names are drawn, or disable overhead names"
	)
	default PlayerNameLocation playerNamePosition()
	{
		return PlayerNameLocation.ABOVE_HEAD;
	}

	@ConfigItem(
		position = 4,
		keyName = "drawMinimapNames",
		name = "Draw names on minimap",
		description = "Draw highlighted player names on the minimap"
	)
	default boolean drawMinimapNames()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "colorPlayerMenu",
		name = "Colorize player menu",
		description = "Color player names in the right-click menu"
	)
	default boolean colorPlayerMenu()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "friendsChatMenuIcons",
		name = "Show friends chat ranks",
		description = "Show friends chat rank icons next to player names and in the right-click menu"
	)
	default boolean showFriendsChatRanks()
	{
		return true;
	}

	@ConfigItem(
		position = 7,
		keyName = "clanChatMenuIcons",
		name = "Show clan chat ranks",
		description = "Show clan rank icons next to player names and in the right-click menu"
	)
	default boolean showClanChatRanks()
	{
		return true;
	}
}
