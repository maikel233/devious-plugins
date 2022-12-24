package net.runelite.client.plugins.xManiacalMonkey;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;



public class xManiacalMonkeyOverlay extends Overlay {
	private final Client client;
	private final xManiacalMonkeyPlugin plugin;
	private final xManiacalMonkeyConfig config;
	private final PanelComponent panelComponent = new PanelComponent();



	@Inject
	private xManiacalMonkeyOverlay(Client client, xManiacalMonkeyPlugin plugin, xManiacalMonkeyConfig config) {
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		this.setPriority(OverlayPriority.HIGHEST);
		this.setPosition(OverlayPosition.BOTTOM_LEFT);
		this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "xManiacalMonkey Overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (plugin == null)
			return null;

		panelComponent.getChildren().clear();


		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT);
		tableComponent.setDefaultColor(Color.ORANGE);

		final BufferedImage image = ImageUtil.getResourceStreamFromClass(xManiacalMonkeyPlugin.class, "chin.png");
		if (image != null) {
			final int baseX = tableComponent.getBounds().x;
			final int baseY = tableComponent.getBounds().y;
			final int size = Math.max(140, 100);
			final int imageX = baseX + (size - image.getWidth(null)) / 2;
			final int imageY = baseY + (size - image.getHeight(null)) / 2;
			graphics.drawImage(image, imageX, imageY, null);
		}

		tableComponent.addRow("X-HOOK ManiacalMonkey Plugin");
		tableComponent.addRow(plugin.status);
		tableComponent.addRow(plugin.status2);
		tableComponent.addRow(plugin.status3);
		tableComponent.addRow(plugin.status4);
		long end = System.currentTimeMillis() - plugin.start;

		DateFormat df = new SimpleDateFormat("HH 'H', mm 'M,' ss 'S'");
		df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		tableComponent.addRow("Time running: " + df.format(new Date(end)));

		long XPPerHour = (int) (plugin.CurrentXP / ((System.currentTimeMillis() - plugin.start) / 3600000.0D));
		tableComponent.addRow("XP Gained: " + plugin.CurrentXP);
		tableComponent.addRow("XP Per hr: " + XPPerHour);


		if (!tableComponent.isEmpty()) {
			panelComponent.getChildren().add(tableComponent);
		}


		//maxStack

		panelComponent.setPreferredSize(new Dimension(280, 100));
		panelComponent.setBackgroundColor(Color.darkGray);



		return panelComponent.render(graphics);
	}
}



