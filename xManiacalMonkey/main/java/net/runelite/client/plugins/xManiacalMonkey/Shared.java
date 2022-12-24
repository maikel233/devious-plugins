package net.runelite.client.plugins.xManiacalMonkey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.*;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Inventory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter

public class Shared {

    public static List<NPC> Monkeys;
    public static long monkeyCount = 0;
    public static List<Tile> monkeyTiles;
    public static NPC bestMonkey;
    public static int maxStack = 0;
    public static int aggroMonkeys = 0;
    public static int bestPileSize = 0;

    public static int targetID = 7118;

    public static long lastStacked;
    public static long lastAggroReset;

    public static int deAggroCount = 0;



    //Utils

    public  static final List<String> RANGE_BOOSTS = List.of(
            "ranging potion", "bastion potion"
    );
    public  static final List<String> MAGE_BOOSTS = List.of(
            "magic potion", "battlemage potion", "Imbued heart"
    );
    public  static final List<String> PRAYER_RESTORES = List.of(
            "prayer potion", "super restore potion", "sanfew serum"
    );

    public  static final List<String> ENERGY_RESTORES = List.of(
            "stamina potion", "super energy", "energy potion", "strange fruit"
    );

    static boolean canBoostEnergy()
    {
        return  Inventory.contains(x -> x.hasAction("Drink")
                && ENERGY_RESTORES.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)));
    }
    static boolean canBoostMagic()
    {
        return
               Inventory.contains(x -> x.hasAction("Drink")
                && MAGE_BOOSTS.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)));
    }

    static boolean canBoostRanged()
    {
        return Skills.getBoostedLevel(Skill.RANGED) - Skills.getLevel(Skill.RANGED) <= 5
                && Inventory.contains(x -> x.hasAction("Drink")
                && RANGE_BOOSTS.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)));
    }

    static boolean regionCheck(Client client, int Plane, int RegionID) {
        return client.getLocalPlayer().getWorldLocation().getPlane() == Plane && ArrayUtils.contains(client.getMapRegions(), RegionID);
    }

    static boolean DoWeHavePrayerPots() {
      //  return Prayers.getPoints() <= 15
        return  Inventory.contains(x -> x.hasAction("Drink")
                && PRAYER_RESTORES.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)));
    }
    static TileItem GetPrayerPotsOnGround() {
        Player local = Players.getLocal();
        TileItem loot = TileItems.getNearest(x ->
                x.getTile().getWorldLocation().distanceTo(local.getWorldLocation()) < 15
                        && ((x.getName() != null && Shared.PRAYER_RESTORES.stream().anyMatch(s -> x.getName().toLowerCase().contains(s))))
        );
        return loot;
    }
    static boolean AreTherePrayerPotsOnGround() {
        Player local = Players.getLocal();
        TileItem loot = TileItems.getNearest(x ->
                x.getTile().getWorldLocation().distanceTo(local.getWorldLocation()) < 15
                        && ((x.getName() != null && Shared.PRAYER_RESTORES.stream().anyMatch(s -> x.getName().toLowerCase().contains(s))))
        );
        if (loot == null) {
            return false;
        }
        return true;
    }
}
