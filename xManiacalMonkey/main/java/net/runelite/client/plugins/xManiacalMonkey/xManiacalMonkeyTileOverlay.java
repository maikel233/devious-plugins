package net.runelite.client.plugins.xManiacalMonkey;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;
import javax.inject.Inject;
import java.awt.*;



public class xManiacalMonkeyTileOverlay extends Overlay
{
	private final Client client;
	private final xManiacalMonkeyPlugin plugin;
	private final xManiacalMonkeyConfig config;


	@Inject
	private xManiacalMonkeyTileOverlay(Client client, xManiacalMonkeyPlugin plugin, xManiacalMonkeyConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin == null)
			return null;

if (Shared.bestMonkey != null) {
	WorldPoint BestmonkeyWP = Shared.bestMonkey.getWorldLocation();
	BestmonkeyWP.outline(client,graphics, Color.orange, "Best target:" + Shared.bestMonkey.getName());
}
		plugin.Location1WP.outline(client, graphics, Color.cyan, "BaseTile");
		plugin.Location2WP.outline(client, graphics, Color.cyan, "StackTile");
		plugin.ReAggroWP.outline(client,graphics, Color.white,  "ReAggroTile");

		return null;
	}
}

