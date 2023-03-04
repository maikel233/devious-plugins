package net.runelite.client.plugins.xKruneCrafting.tasks;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.xKruneCrafting.State;
import net.runelite.client.plugins.xKruneCrafting.xKrunecraftingUtils;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.plugins.Task;

import net.runelite.client.plugins.xKruneCrafting.xKrunecraftingUtils.*;

@Slf4j
public class MiningTask implements Task {



  @Override
  public boolean validate()
  {
    var local = Players.getLocal();
    if (!Inventory.isFull() && local.getWorldLocation().distanceTo(xKLocations.DenseRuneStoneLocation) < 9
    && !Inventory.contains(xKItems.DarkEssenceBlock)) // Make sure we have no DarkessenceBlocks.
    {
      return true;
    }

    if (!Inventory.isFull() &&
            !Inventory.contains(xKItems.DarkEssenceBlock)
    && !Inventory.contains(xKItems.DenseEssenceBlock)
    && !Inventory.contains(xKItems.DarkEssenceFragments)
    && Inventory.contains(xKItems.Chissel)
    && Inventory.contains(xKItems.BloodRunes)) {
      Walker.walkTo(xKLocations.DenseRuneStoneLocation);
    }

    return false;
  }


  public static boolean isLookingTowards(Player animableEntity, TileObject DenseStone, int maxDist)
  {
    WorldPoint positionable = DenseStone.getWorldLocation();

    if (maxDist > 0 && positionable.getWorldLocation().distanceTo(animableEntity) > maxDist) return false;

    final int orientation = (int) Math.round(animableEntity.getOrientation() / 256.0);
    final int dx = animableEntity.getWorldLocation().getX() - positionable.getWorldLocation().getX();
    final int dy = animableEntity.getWorldLocation().getY() - positionable.getWorldLocation().getY();

    if (DenseStone.hasAction("Check")) {
      switch (orientation) {
        case 0: //south
          return dx == 0 && dy > 0;
        case 1: //south - west
          return dx > 0 && dy > 0 && dx == dy;
        case 2: //west
          return dx > 0 && dy == 0;
        case 3: //north - west
          return dx > 0 && dy < 0 && Math.abs(dx) == Math.abs(dy);
        case 4: //north
          return dx == 0 && dy < 0;
        case 5: //north - east
          return dx < 0 && dy < 0 && dx == dy;
        case 6: //east
          return dx < 0 && dy == 0;
        case 7: //south-east
          return dx < 0 && dy > 0 && Math.abs(dx) == Math.abs(dy);
      }
    }
    return false;
  }

  boolean Ignore = false;




  @Override
  public int execute()
  {
    State.get().setStatus("Mining");

    var local = Players.getLocal();
    if (local == null) { return 1000; }

    xKSettings.DarkAltarWalkToStone = false;

    TileObject DenseRuneStone1 = TileObjects.getNearest(xKGameObjectsID.DenseRuneStone1);
    TileObject DenseRuneStone2 = TileObjects.getNearest(xKGameObjectsID.DenseRuneStone2);

    List<TileObject> DenseRuneStone = new ArrayList<>();
    DenseRuneStone.add(DenseRuneStone1);
    DenseRuneStone.add(DenseRuneStone2);

    List<TileObject> newList = new ArrayList<>();

    for (TileObject tileObject : DenseRuneStone) {
      if (tileObject.hasAction("Break")) {
        if (isLookingTowards(local, tileObject, 1)) {
          log.info("BREAK!!!");
          if (!local.isMoving()) {
            //Not actually required because the Rocks(Break) Action is ClientSide
            Time.sleep(200,600);
            //Incase we are in the last anim state wait a bit...
            if (local.getAnimation() != xKAnimationID.MiningOreLastStep) { Ignore = true; }
          }
        }
      }
      if (tileObject.hasAction("Chip")) {
        newList.add(tileObject); // Wait until the iteration is complete
      }
    }

    DenseRuneStone = newList;

    boolean ClickNotEveryTick = false;

    if (local.getAnimation() == xKAnimationID.MiningOreDefault || local.getAnimation() == xKAnimationID.MiningOreLastStep) {
      ClickNotEveryTick = false;


      Time.sleep(2000,3000);
    }
    if (local.getAnimation() == -1) {
      ClickNotEveryTick = true;
    }

    if (ClickNotEveryTick || Ignore) {
      for (TileObject tileObject : newList) {
        log.info("Mining!");
        tileObject.interact("Chip");
        Ignore = false;
      }
    }

    return 100;
  }
}
