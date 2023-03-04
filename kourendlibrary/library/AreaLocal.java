package net.runelite.client.plugins.kourendlibrary.library;

import java.util.List;
import java.util.Random;
import net.runelite.api.coords.WorldPoint;

public class AreaLocal {
   public final List<WorldPoint> tiles;

    public AreaLocal(List<WorldPoint> tiles) {
        this.tiles = tiles;
    }

    public WorldPoint getRandomLocation() {
        Random random = new Random();
        int randomIndex = random.nextInt(tiles.size());
        return tiles.get(randomIndex);
    }
}