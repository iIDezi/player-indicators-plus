package com.playerindicatorsplus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
class PlayerIndicatorsPlusTileOverlay extends Overlay
{
	private final Client client;
	private final PlayerIndicatorsPlusConfig config;
	private final PlayerIndicatorsPlusOverlay playerNamesOverlay;

	@Inject
	private PlayerIndicatorsPlusTileOverlay(Client client, PlayerIndicatorsPlusConfig config,
		PlayerIndicatorsPlusOverlay playerNamesOverlay)
	{
		this.client = client;
		this.config = config;
		this.playerNamesOverlay = playerNamesOverlay;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.getGameState() != GameState.LOGGED_IN || !config.drawTiles())
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
		renderWorldView(graphics, topLevelWorldView, insideWilderness, insidePvpArea);
		for (WorldView worldView : topLevelWorldView.worldViews())
		{
			renderWorldView(graphics, worldView, insideWilderness, insidePvpArea);
		}

		return null;
	}

	private void renderWorldView(Graphics2D graphics, WorldView worldView,
		int insideWilderness, int insidePvpArea)
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

			Color color = playerNamesOverlay.getPlayerColor(player, insideWilderness, insidePvpArea);
			Polygon tile = player.getCanvasTilePoly();
			if (color != null && tile != null)
			{
				OverlayUtil.renderPolygon(graphics, tile, color);
			}
		}
	}
}
