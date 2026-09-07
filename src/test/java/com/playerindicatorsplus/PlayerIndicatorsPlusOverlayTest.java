package com.playerindicatorsplus;

import java.awt.Color;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlayerIndicatorsPlusOverlayTest
{
	@Test
	public void activatesInTheWilderness()
	{
		assertTrue(PlayerIndicatorsPlusOverlay.isPvpZone(1, 0));
	}

	@Test
	public void activatesInOtherPvpAreas()
	{
		assertTrue(PlayerIndicatorsPlusOverlay.isPvpZone(0, 1));
	}

	@Test
	public void identifiesSafeAreas()
	{
		assertFalse(PlayerIndicatorsPlusOverlay.isPvpZone(0, 0));
	}

	@Test
	public void defaultsToWhiteInSafeAreasAndRedInPvp()
	{
		PlayerIndicatorsPlusConfig config = new PlayerIndicatorsPlusConfig() { };
		assertTrue(config.showPlayerNames());
		assertTrue(config.colorWildernessNames());
		assertEquals(PlayerIndicatorsPlusConfig.HighlightSetting.DISABLED, config.highlightOwnPlayer());
		assertEquals(new Color(0, 184, 212), config.ownPlayerColor());
		assertEquals(PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED, config.highlightFriends());
		assertEquals(new Color(0, 200, 83), config.friendColor());
		assertEquals(PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED, config.highlightFriendsCombatZones());
		assertEquals(new Color(255, 140, 0), config.friendsCombatZonesColor());
		assertEquals(PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED, config.highlightFriendsChat());
		assertEquals(new Color(170, 0, 255), config.friendsChatColor());
		assertEquals(PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED, config.highlightTeamMembers());
		assertEquals(new Color(19, 110, 247), config.teamMemberColor());
		assertEquals(PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED, config.highlightClanMembers());
		assertEquals(new Color(36, 15, 171), config.clanMemberColor());
		assertEquals(PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED, config.highlightOthers());
		assertEquals(Color.WHITE, config.safeAreaNameColor());
		assertEquals(Color.RED, config.wildernessNameColor());
		assertFalse(config.drawTiles());
		assertEquals(PlayerNameLocation.ABOVE_HEAD, config.playerNamePosition());
		assertFalse(config.drawMinimapNames());
		assertTrue(config.colorPlayerMenu());
		assertTrue(config.showFriendsChatRanks());
		assertTrue(config.showClanChatRanks());
	}

	@Test
	public void exposesAllPlayerNamePositionsWithReadableLabels()
	{
		assertEquals("Disabled", PlayerNameLocation.DISABLED.toString());
		assertEquals("Above head", PlayerNameLocation.ABOVE_HEAD.toString());
		assertEquals("Center of model", PlayerNameLocation.MODEL_CENTER.toString());
		assertEquals("Right of model", PlayerNameLocation.MODEL_RIGHT.toString());
	}

	@Test
	public void groupsHighlightColorsInCollapsibleSection() throws Exception
	{
		ConfigSection section = PlayerIndicatorsPlusConfig.class
			.getField("highlightSection").getAnnotation(ConfigSection.class);
		assertEquals("Highlight options", section.name());
		assertEquals("highlightOptions", PlayerIndicatorsPlusConfig.highlightSection);
		assertEquals("highlightOptions", PlayerIndicatorsPlusConfig.class
			.getMethod("friendColor").getAnnotation(ConfigItem.class).section());
		assertEquals("Friends - Combat Zones", PlayerIndicatorsPlusConfig.class
			.getMethod("friendsCombatZonesColor").getAnnotation(ConfigItem.class).name());
		assertEquals("Others - Safe Areas", PlayerIndicatorsPlusConfig.class
			.getMethod("safeAreaNameColor").getAnnotation(ConfigItem.class).name());
		assertEquals("Others - Combat Zones", PlayerIndicatorsPlusConfig.class
			.getMethod("wildernessNameColor").getAnnotation(ConfigItem.class).name());
	}

	@Test
	public void selectsSeparateFriendColorInCombatZones()
	{
		assertEquals(new Color(255, 140, 0), PlayerIndicatorsPlusOverlay.selectFriendColor(
			true,
			PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED,
			new Color(255, 140, 0),
			PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED,
			new Color(0, 200, 83)));
	}

	@Test
	public void disabledCombatFriendColorFallsBackToNormalFriendColor()
	{
		assertEquals(new Color(0, 200, 83), PlayerIndicatorsPlusOverlay.selectFriendColor(
			true,
			PlayerIndicatorsPlusConfig.HighlightSetting.DISABLED,
			new Color(255, 140, 0),
			PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED,
			new Color(0, 200, 83)));
	}

	@Test
	public void safeFriendsKeepNormalFriendColor()
	{
		assertEquals(new Color(0, 200, 83), PlayerIndicatorsPlusOverlay.selectFriendColor(
			false,
			PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED,
			new Color(255, 140, 0),
			PlayerIndicatorsPlusConfig.HighlightSetting.ENABLED,
			new Color(0, 200, 83)));
	}

	@Test
	public void selectsSafeAreaColorOutsidePvp()
	{
		assertEquals(Color.WHITE,
			PlayerIndicatorsPlusOverlay.selectNameColor(false, true, Color.WHITE, Color.RED));
	}

	@Test
	public void selectsWildernessColorInsidePvp()
	{
		assertEquals(Color.RED,
			PlayerIndicatorsPlusOverlay.selectNameColor(true, true, Color.WHITE, Color.RED));
	}

	@Test
	public void wildernessColorToggleFallsBackToSafeAreaColor()
	{
		assertEquals(Color.WHITE,
			PlayerIndicatorsPlusOverlay.selectNameColor(true, false, Color.WHITE, Color.RED));
	}

	@Test
	public void playerNorthOfWildernessLineUsesPvpColorWhileLocalPlayerIsSafe()
	{
		assertTrue(PlayerIndicatorsPlusOverlay.isPlayerInPvpZone(3087, 3520, 0, 0, 0));
	}

	@Test
	public void playerSouthOfWildernessLineUsesSafeColorWhileLocalPlayerIsInWilderness()
	{
		assertFalse(PlayerIndicatorsPlusOverlay.isPlayerInPvpZone(3087, 3519, 0, 1, 0));
	}

	@Test
	public void wildernessBoundaryOnlyAppliesInsideItsHorizontalSpan()
	{
		assertFalse(PlayerIndicatorsPlusOverlay.isMainWildernessSurface(2943, 3520, 0));
		assertFalse(PlayerIndicatorsPlusOverlay.isImmediatelySouthOfWildernessLine(3392, 3519, 0));
	}

	@Test
	public void otherPvpAreasStillUseClientPvpState()
	{
		assertTrue(PlayerIndicatorsPlusOverlay.isPlayerInPvpZone(3200, 3200, 0, 0, 1));
	}
}
