package net.runelite.client.plugins.xhunter;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class xHunterOverlay extends Overlay
{
    private final Client client;
    private final xHunterPlugin plugin;
    private final xHunterConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    private xHunterOverlay(Client client, xHunterPlugin plugin, xHunterConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        this.setPriority(OverlayPriority.HIGHEST);
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "xHunter Overlay"));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (plugin == null)
            return null;

        panelComponent.getChildren().clear();

        TableComponent tableComponent = new TableComponent();
        tableComponent.setColumnAlignments(TableAlignment.LEFT);
        tableComponent.setDefaultColor(Color.ORANGE);

        tableComponent.addRow("X-HOOK Hunter Plugin");
        tableComponent.addRow(plugin.status);
        tableComponent.addRow(plugin.status2);
        long end = System.currentTimeMillis() - plugin.start;

        DateFormat df = new SimpleDateFormat("HH 'H', mm 'M,' ss 'S'");
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        tableComponent.addRow("Time running: " + df.format(new Date(end)));

        long XPPerHour = (int)(plugin.CurrentXP / ((System.currentTimeMillis() - plugin.start) / 3600000.0D));
        tableComponent.addRow("XP Gained: " + plugin.CurrentXP);
        tableComponent.addRow("XP Per hr: " + XPPerHour);

        if (!tableComponent.isEmpty())
        {
            panelComponent.getChildren().add(tableComponent);
        }

        panelComponent.setPreferredSize(new Dimension(250, 100));
        panelComponent.setBackgroundColor(Color.darkGray);

        return panelComponent.render(graphics);
    }
}