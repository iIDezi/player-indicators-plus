package com.playerindicatorsplus;

import java.awt.Color;
import java.lang.reflect.Field;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.events.ClientTick;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import org.junit.Before;
import org.junit.Test;

import static com.playerindicatorsplus.PlayerIndicatorsPlusConfig.HighlightSetting.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PartyMemberHighlightTest
{
    private Client client;
    private PartyService party;
    private PlayerIndicatorsPlusConfig config;
    private PlayerIndicatorsPlusOverlay overlay;
    private Player local;
    private Player member;
    private PartyMember partyMember;

    @Before
    public void setUp()
    {
        client = mock(Client.class);
        party = mock(PartyService.class);
        config = spy(new PlayerIndicatorsPlusConfig() { });
        local = mock(Player.class);
        member = mock(Player.class);
        partyMember = mock(PartyMember.class);
        when(client.getLocalPlayer()).thenReturn(local);
        when(member.getName()).thenReturn("Party Friend");
        when(party.isInParty()).thenReturn(true);
        when(party.getMemberByDisplayName("Party Friend")).thenReturn(partyMember);
        overlay = new PlayerIndicatorsPlusOverlay(client, config, mock(ChatIconManager.class), party);
    }

    @Test
    public void usesSelectedPartyColorInSafeAndCombatAreasEvenWhenOthersAreHidden()
    {
        when(config.partyMemberColor()).thenReturn(Color.MAGENTA);
        when(config.highlightOthers()).thenReturn(DISABLED);
        assertEquals(Color.MAGENTA, overlay.getPlayerColor(member, 0, 0));
        assertEquals(Color.MAGENTA, overlay.getPlayerColor(member, 1, 0));
        assertEquals(Color.MAGENTA, overlay.getPlayerColor(member, 0, 1));
    }

    @Test
    public void partyColorTakesPriorityOverOtherRelationships()
    {
        when(member.isFriend()).thenReturn(true);
        when(member.isFriendsChatMember()).thenReturn(true);
        when(member.isClanMember()).thenReturn(true);
        when(member.getTeam()).thenReturn(1);
        when(local.getTeam()).thenReturn(1);
        assertEquals(config.partyMemberColor(), overlay.getPlayerColor(member, 1, 0));
    }

    @Test
    public void joiningLeavingAndChangingPartyAreReflectedWithoutRestarting()
    {
        when(party.isInParty()).thenReturn(false);
        assertEquals(Color.WHITE, overlay.getPlayerColor(member, 0, 0));
        when(party.isInParty()).thenReturn(true);
        assertEquals(config.partyMemberColor(), overlay.getPlayerColor(member, 0, 0));
        when(party.isInParty()).thenReturn(false);
        assertEquals(Color.WHITE, overlay.getPlayerColor(member, 0, 0));
        // A new party can contain a different set of players.
        when(party.isInParty()).thenReturn(true);
        when(party.getMemberByDisplayName("Party Friend")).thenReturn(null);
        assertEquals(Color.WHITE, overlay.getPlayerColor(member, 0, 0));
    }

    @Test
    public void memberLeavingOrGoingOfflineRestoresTheirOtherColor()
    {
        when(member.isFriend()).thenReturn(true);
        assertEquals(config.partyMemberColor(), overlay.getPlayerColor(member, 0, 0));
        // RuneLite's service returns null when the member leaves or is offline.
        when(party.getMemberByDisplayName("Party Friend")).thenReturn(null);
        assertEquals(config.friendColor(), overlay.getPlayerColor(member, 0, 0));
        assertEquals(config.friendsCombatZonesColor(), overlay.getPlayerColor(member, 1, 0));
        when(party.getMemberByDisplayName("Party Friend")).thenReturn(partyMember);
        assertEquals(config.partyMemberColor(), overlay.getPlayerColor(member, 0, 0));
    }

    @Test
    public void disablingPartyHighlightRestoresFriendsAndReenablingAppliesNewColor()
    {
        when(member.isFriend()).thenReturn(true);
        when(config.highlightPartyMembers()).thenReturn(DISABLED);
        assertEquals(config.friendsCombatZonesColor(), overlay.getPlayerColor(member, 1, 0));
        verifyNoInteractions(party);
        when(config.highlightPartyMembers()).thenReturn(ENABLED);
        when(config.partyMemberColor()).thenReturn(Color.CYAN);
        assertEquals(Color.CYAN, overlay.getPlayerColor(member, 1, 0));
    }

    @Test
    public void ownPlayerRemainsControlledByOwnPlayerSettings()
    {
        when(client.getLocalPlayer()).thenReturn(member);
        assertNull(overlay.getPlayerColor(member, 0, 0));
        when(config.highlightOwnPlayer()).thenReturn(ENABLED);
        assertEquals(config.ownPlayerColor(), overlay.getPlayerColor(member, 0, 0));
        verifyNoInteractions(party);
    }

    @Test
    public void missingNamesAndNonMembersDoNotUsePartyColor()
    {
        when(member.getName()).thenReturn(null);
        assertEquals(Color.WHITE, overlay.getPlayerColor(member, 0, 0));
        verify(party, never()).getMemberByDisplayName(null);
        when(member.getName()).thenReturn("Stranger");
        assertEquals(Color.RED, overlay.getPlayerColor(member, 1, 0));
        when(config.highlightOthers()).thenReturn(DISABLED);
        assertNull(overlay.getPlayerColor(member, 0, 0));
    }

    @Test
    public void playerMenuUsesTheSamePartyColorAndUpdatesWhenMemberLeaves() throws Exception
    {
        PlayerIndicatorsPlusPlugin plugin = new PlayerIndicatorsPlusPlugin();
        inject(plugin, "client", client);
        inject(plugin, "config", config);
        inject(plugin, "overlay", overlay);
        MenuEntry entry = mock(MenuEntry.class);
        when(entry.getType()).thenReturn(MenuAction.PLAYER_FIRST_OPTION);
        when(entry.getPlayer()).thenReturn(member);
        when(entry.getTarget()).thenReturn("<col=ffffff>Party Friend");
        when(client.getMenuEntries()).thenReturn(new MenuEntry[]{entry});
        plugin.onClientTick(new ClientTick());
        verify(entry).setTarget("<col=ea7b5b>Party Friend");
        when(party.getMemberByDisplayName("Party Friend")).thenReturn(null);
        plugin.onClientTick(new ClientTick());
        verify(entry).setTarget("<col=ffffff>Party Friend");
    }

    private static void inject(Object target, String name, Object value) throws Exception
    {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
