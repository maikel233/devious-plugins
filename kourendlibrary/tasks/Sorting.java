package net.runelite.client.plugins.kourendlibrary.tasks;

import java.util.Arrays;
import java.util.Comparator;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.kourendlibrary.library.Bookcase;
import org.apache.commons.lang3.ArrayUtils;

public class Sorting {
    public static void sortByDistance(Bookcase[] bookcasesArray, WorldPoint local, boolean ascending) {
        Arrays.sort(bookcasesArray, new DistanceComparator(local));
        if (!ascending) {
            ArrayUtils.reverse(bookcasesArray);
        }
    }
}

class DistanceComparator implements Comparator<Bookcase> {
    private WorldPoint local;

    public DistanceComparator(WorldPoint local) {
        this.local = local;
    }

    @Override
    public int compare(Bookcase b1, Bookcase b2) {
        double distance1 = calculateDistance(b1.getLocation(), local);
        double distance2 = calculateDistance(b2.getLocation(), local);
        return Double.compare(distance1, distance2);
    }

    private double calculateDistance(WorldPoint point1, WorldPoint point2) {
        double dx = point1.getX() - point2.getX();
        double dy = point1.getY() - point2.getY();
        double dz = point1.getPlane() - point2.getPlane();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}

