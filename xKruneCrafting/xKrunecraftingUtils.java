package net.runelite.client.plugins.xKruneCrafting;

import java.awt.AWTException;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import javax.inject.Inject;
import net.runelite.client.plugins.kourendlibrary.library.AreaLocal;
import net.runelite.client.plugins.kourendlibrary.walking.Room;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;

@Getter
public class xKrunecraftingUtils {
    static xRunecraftingConfig config;

    @Inject
    public xKrunecraftingUtils(xRunecraftingConfig config) {
        this.config = config;
    }

    public static class xKSettings {
        public static boolean SecondTrip = false;
        public static boolean Bypass =  false;

        public static boolean  AllowCraftingv1 = false;
        public static boolean  AllowCraftingv2 = false;


        public static boolean DarkAltarWalkToStone = false;
    }

    public static class xKItems {
        public static final int Chissel = 1755;
        public static final int DenseEssenceBlock = 13445;
        public static final int DarkEssenceBlock = 13446;
        public static final int DarkEssenceFragments = 7938;
        public static final int BloodRunes = 565;
    }
    public static class xKLocations {
        public static final WorldPoint DarkAltarLocation = new WorldPoint(1719, 3881, 0);
        public static final WorldPoint BloodAltarLocation = new WorldPoint(1722, 3829, 0);
        public static final WorldPoint DenseRuneStoneLocation = new WorldPoint(1762, 3850, 0);


        public static final Room DenseRuneStoneRoom = new Room("RuneStones", new AreaLocal(
                List.of(new WorldPoint[]{
                        new WorldPoint(1756, 3868, 0),
                        new WorldPoint(1776, 3868, 0),
                        new WorldPoint(1756, 3846, 0),
                        new WorldPoint(1776, 3846, 0),
                })
                //From DarkAltar                       From BloodAltar to RuneStones
        ), new WorldPoint(1761, 3873, 0), new WorldPoint(1743, 3854, 0));

        public static final Room PathDarkAltarCloseToRocks = new Room("DarkALtarRocks", new AreaLocal(
                List.of(new WorldPoint[]{
                        new WorldPoint(1764, 3880, 0),
                        new WorldPoint(1764, 3875, 0),
                        new WorldPoint(1755, 3880, 0),
                        new WorldPoint(1755, 3875, 0),
                })
                //From DarkAltar
        ), new WorldPoint(1761, 3873, 0), null);
    }

    public static class xKGameObjectsID {
        public static final int DarkAltarID = 27979;
        public static final int BloodAltarID = 27978;
        public static final int DenseRuneStone1 = 8981;
        public static final int DenseRuneStone2 = 10796;
    }

    public static class xKAnimationID {
        public static final int MiningOreDefault = 3873;
        public static final int MiningOreLastStep = 7201; // The Animation after mining.
        public static final  int ClimbRock = 839;
        public static final  int CraftedRunesAtDarkAltar = 645;
        public static final  int CraftedRunesAtBloodAltar = 791;
    }
}





