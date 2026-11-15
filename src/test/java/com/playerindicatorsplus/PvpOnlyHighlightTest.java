package com.playerindicatorsplus;

import java.awt.Color;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.party.PartyService;
import org.junit.Before;
import org.junit.Test;

import static com.playerindicatorsplus.PlayerIndicatorsPlusConfig.HighlightSetting.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PvpOnlyHighlightTest
{
	private static final WorldPoint WILDERNESS = new WorldPoint(3100, 3600, 0);
	private static final WorldPoint LUMBRIDGE = new WorldPoint(3222, 3218, 0);

	private PlayerIndicatorsPlusConfig config;
	private PlayerIndicatorsPlusOverlay overlay;
	private Player local;
	private Player other;

	@Before
	public void setUp()
	{
		Client client = mock(Client.class);
		config = spy(new PlayerIndicatorsPlusConfig() { });
		local = mock(Player.class);
		other = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(local);
		when(other.getName()).thenReturn("Someone");
		overlay = new PlayerIndicatorsPlusOverlay(
			client, config, mock(ChatIconManager.class), mock(PartyService.class));
	}

	@Test
	public void pvpSettingIsActiveOnlyInPvpZones()
	{
		assertFalse(PlayerIndicatorsPlusOverlay.isActive(DISABLED, true));
		assertTrue(PlayerIndicatorsPlusOverlay.isActive(ENABLED, false));
		assertTrue(PlayerIndicatorsPlusOverlay.isActive(PVP, true));
		assertFalse(PlayerIndicatorsPlusOverlay.isActive(PVP, false));
	}

	@Test
	public void othersInPvpModeShowOnlyInTheWilderness()
	{
		when(config.highlightOthers()).thenReturn(PVP);

		when(other.getWorldLocation()).thenReturn(WILDERNESS);
		assertEquals(Color.RED, overlay.getPlayerColor(other, 1, 0));

		when(other.getWorldLocation()).thenReturn(LUMBRIDGE);
		assertNull(overlay.getPlayerColor(other, 0, 0));
	}

	@Test
	public void othersInPvpModeUseEachPlayersOwnZone()
	{
		// You stand in a safe area while the other player is across the ditch.
		when(config.highlightOthers()).thenReturn(PVP);
		when(local.getWorldLocation()).thenReturn(new WorldPoint(3100, 3500, 0));
		when(other.getWorldLocation()).thenReturn(WILDERNESS);
		assertEquals(Color.RED, overlay.getPlayerColor(other, 0, 0));
	}

	@Test
	public void othersInPvpModeShowInNonWildernessPvpAreas()
	{
		when(config.highlightOthers()).thenReturn(PVP);
		when(other.getWorldLocation()).thenReturn(LUMBRIDGE);
		assertEquals(Color.RED, overlay.getPlayerColor(other, 0, 1));
	}

	@Test
	public void friendsInPvpModeFallBackToOthersOutsidePvp()
	{
		when(other.isFriend()).thenReturn(true);
		when(config.highlightFriends()).thenReturn(PVP);
		when(config.highlightFriendsCombatZones()).thenReturn(DISABLED);

		when(other.getWorldLocation()).thenReturn(WILDERNESS);
		assertEquals(config.friendColor(), overlay.getPlayerColor(other, 1, 0));

		when(other.getWorldLocation()).thenReturn(LUMBRIDGE);
		assertEquals(Color.WHITE, overlay.getPlayerColor(other, 0, 0));
	}

	@Test
	public void ownPlayerInPvpModeShowsOnlyInPvp()
	{
		when(config.highlightOwnPlayer()).thenReturn(PVP);

		when(local.getWorldLocation()).thenReturn(WILDERNESS);
		assertEquals(config.ownPlayerColor(), overlay.getPlayerColor(local, 1, 0));

		when(local.getWorldLocation()).thenReturn(LUMBRIDGE);
		assertNull(overlay.getPlayerColor(local, 0, 0));
	}
}
