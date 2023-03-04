package net.runelite.client.plugins.xKruneCrafting;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javax.imageio.ImageIO;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.game.Skills;

public class xKrunecraftingOverlay extends Overlay
{
    private final Client client;
    private final xKrunecraftingPlugin plugin;
    private final xRunecraftingConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    private final State state;

    long runTime;


	private final Image RuneCraftImage;
    private final Image CraftingImage;
    private final Image MiningImage;
	public static Optional<Image> getImage(String url) {
		try {
			URL u = new URL(url);
			return Optional.of(ImageIO.read(u.openStream()));
		} catch(IOException e) {
			System.out.println("Unable to get paint image");
			e.printStackTrace();
			return Optional.empty();
		}
	}


    @Inject
    private xKrunecraftingOverlay(Client client, xKrunecraftingPlugin plugin, xRunecraftingConfig config, State state)
    {
        this.RuneCraftImage = getImage("https://oldschool.runescape.wiki/images/Runecraft_icon.png").orElse(null);
        this.CraftingImage = getImage("https://oldschool.runescape.wiki/images/Crafting_icon.png").orElse(null);
        this.MiningImage = getImage("https://oldschool.runescape.wiki/images/Mining_icon.png").orElse(null);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.state = state;

        this.setPriority(OverlayPriority.HIGHEST);
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "xRunecrafting Overlay"));
    }

    public static String numberFormat(double num) {
        if (num < 1000.0) {
            return Integer.toString((int) num);
        } else if (Math.round(num) / 10000.0 < 100.0) {
            return String.format("%.1fk", Math.round(num) / 1000.0);
        } else {
            return String.format("%.1fm", Math.round(num) / 1000000.0);
        }
    }


    public String getSkillXP(Skill skill) {
        int currentXP = client.getSkillExperience(skill);
        int xpGained = 0;


        switch (skill) {
            case RUNECRAFT:
                xpGained = currentXP - plugin.startXpRuneCrafting;
                break;
            case CRAFTING:
                xpGained = currentXP - plugin.startXpCrafting;
                break;
            case MINING:
                xpGained = currentXP - plugin.startXpMining;
                break;
            default:
                return "Invalid skill selected.";
        }

        return String.format("XP Gained: %s (%s/h)", numberFormat(xpGained),
                numberFormat((xpGained * 3600000D / runTime)));
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

        long end = System.currentTimeMillis() - plugin.startTime;
        DateFormat df = new SimpleDateFormat("HH 'H', mm 'M,' ss 'S'");
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));

        runTime = System.currentTimeMillis() - plugin.startTime;
        tableComponent.addRow("X-H Bloods plugin", "State: " + State.get().getStatus(), "Running: " + df.format(new Date(end)));

        tableComponent.addRow(getSkillXP(Skill.RUNECRAFT), getSkillXP(Skill.CRAFTING), getSkillXP(Skill.MINING));

        if (!tableComponent.isEmpty()) {
            panelComponent.getChildren().add(tableComponent);
        }

        panelComponent.setPreferredSize(new Dimension(500, 100));
        panelComponent.setBackgroundColor(Color.darkGray);

        return panelComponent.render(graphics);
    }
}