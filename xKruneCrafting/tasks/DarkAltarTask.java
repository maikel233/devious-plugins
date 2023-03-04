package net.runelite.client.plugins.xKruneCrafting.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.kourendlibrary.walking.Room;
import net.runelite.client.plugins.xKruneCrafting.State;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.plugins.Task;

import net.runelite.client.plugins.xKruneCrafting.xKrunecraftingUtils.*;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;;
@Slf4j
public class DarkAltarTask implements Task {


  //Bypass is used for crafting and walking back after the first trip to DarkAltar.


  @Override
  public boolean validate() {
    if (Inventory.isFull() && Inventory.contains(xKItems.DenseEssenceBlock)) {
      return true;
    }
    if (Inventory.getCount(xKItems.DarkEssenceBlock) == 26) {
      xKSettings.Bypass = true;
    }

    if (xKSettings.Bypass) {
      return true;
    }

    return false;
  }




  @Override
  public int execute()
  {
    State.get().setStatus("DarkAltar");


    var local = Players.getLocal();
    if (local == null) { return 1000; }

    xKSettings.AllowCraftingv1 = false;
    xKSettings.AllowCraftingv2 = false;

    TileObject DarkAltar = TileObjects.getNearest(xKGameObjectsID.DarkAltarID);
    if (Inventory.contains(xKItems.DenseEssenceBlock)) {
      if (DarkAltar == null || local.distanceTo(DarkAltar.getWorldLocation()) > 10) {

        WorldPoint StoneAfterWeClimbedIt = new WorldPoint(1761, 3874, 0);
        if (local.getWorldLocation().equals(StoneAfterWeClimbedIt)) {
          Tile DarkAltarTiles = Tiles.getAt(xKLocations.DarkAltarLocation);
          if (!local.isMoving()) { DarkAltarTiles.walkHere(); }
        }


        Room RuneStone = xKLocations.DenseRuneStoneRoom;
        WorldPoint location = local.getWorldLocation();
        if (location.isLocationWithinArea2(location, RuneStone.getArea().tiles)) {
          TileObject Rocks = TileObjects.getNearest(34741);
          Rocks.interact("Climb");
        }
      }


      if (DarkAltar != null && local.distanceTo(DarkAltar.getWorldLocation()) < 15) {
        if (local.getAnimation() == -1) {
          DarkAltar.interact("Venerate");
        }
        if (local.getAnimation() == xKAnimationID.CraftedRunesAtDarkAltar) {
          if (Inventory.contains(xKItems.DarkEssenceFragments)) {
            xKSettings.SecondTrip = true;
          }
        }
      }
    }

       if (xKSettings.SecondTrip) {
        if (Inventory.contains(xKItems.DarkEssenceBlock) && Inventory.contains(xKItems.DarkEssenceFragments)) {
          xKSettings.Bypass = false;
         }
       }




    if (!xKSettings.SecondTrip && !Inventory.contains(xKItems.DenseEssenceBlock) && local.getAnimation() != xKAnimationID.ClimbRock) {


    //  if (!xKSettings.DarkAltarWalkToStone) {
        Room DarkAltarRocksRoom = xKLocations.PathDarkAltarCloseToRocks;
        WorldPoint location = local.getWorldLocation();
        if (location.isLocationWithinArea2(location, DarkAltarRocksRoom.getArea().tiles)) {
          TileObject Rocks = TileObjects.getNearest(34741);
          Rocks.interact("Climb");
        }


      if (local.getWorldLocation().equals(new WorldPoint(1761,3872, 0))) {
        xKSettings.DarkAltarWalkToStone = true;
      }

      if (!local.isMoving() && !xKSettings.DarkAltarWalkToStone && local.distanceTo(xKLocations.DarkAltarLocation) < 5) {
        int xMin = 1758;
        int xMax = 1761;
        int yMin = 3876;
        int yMax = 3878;

        int yRandom = (int) Math.floor(Math.random() * (yMax - yMin + 1) + yMin);
        int xRandom = (int) Math.floor(Math.random() * (xMax - xMin + 1) + xMin);
        Tile FirstTileToClick = Tiles.getAt(new WorldPoint(xRandom, yRandom, 0));
        FirstTileToClick.walkHere();
        Time.sleep(1000, 2000);
      }

      if (local.distanceTo(xKLocations.DarkAltarLocation) > 5) {
        xKSettings.DarkAltarWalkToStone = true;
      }


      if (xKSettings.DarkAltarWalkToStone) {
        if (local.getWorldLocation().equals(new WorldPoint(1761,3872,0))) {
          int xMin = 1757;
          int xMax = 1761;
          int yMin = 3846;
          int yMax = 3849;

          int yRandom = (int) Math.floor(Math.random() * (yMax - yMin + 1) + yMin);
          int xRandom = (int) Math.floor(Math.random() * (xMax - xMin + 1) + xMin);
          Tile FirstTileToClick = Tiles.getAt(new WorldPoint(xRandom, yRandom, 0));
          FirstTileToClick.walkHere();
        }
        else {
          if (!local.isMoving()) {
            Walker.walkTo(xKLocations.DenseRuneStoneLocation);
          }
        }
    }

      if (Inventory.contains(xKItems.DarkEssenceBlock)) {
        if (!Tabs.isOpen(Tab.INVENTORY)) { Tabs.open(Tab.INVENTORY); }
        Item Chissel = Inventory.getFirst(xKItems.Chissel);
        Item DarkEssence = Inventory.getFirst(xKItems.DarkEssenceBlock);
        Chissel.useOn(DarkEssence);
      }
    }


    //Pretty sure this never runs.
    if (!Inventory.isFull() && Inventory.contains(xKItems.DarkEssenceFragments) && local.getWorldLocation().distanceTo(xKLocations.DenseRuneStoneLocation) < 9) {
      log.info("First trip completed at DarkAltar.");
      xKSettings.Bypass = false;
      return 1000;
    }

    return 100;
  }
}
