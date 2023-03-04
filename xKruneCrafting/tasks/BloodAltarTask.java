package net.runelite.client.plugins.xKruneCrafting.tasks;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
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
public class BloodAltarTask implements Task {

  @Override
  public boolean validate() {
    if (Inventory.isFull()) {

      if (Inventory.contains(xKItems.DarkEssenceFragments) && Inventory.contains(xKItems.DarkEssenceBlock))
        return true;
      }
    var local = Players.getLocal();
    if (local.distanceTo(xKLocations.BloodAltarLocation) < 15) {
      return true;
    }

    return false;
  }

  //Ghetto for now.

  boolean CTM = false;
  boolean CTMAtAltar = false;

  Tile FirstTileToClickv1 = null;

  @Override
  public int execute()
  {
    State.get().setStatus("BloodAltar");



    var local = Players.getLocal();
    if (local == null) { return 1000; }

    xKSettings.SecondTrip = false;
    xKSettings.Bypass = false;


    TileObject BloodAltar = TileObjects.getNearest(xKGameObjectsID.BloodAltarID);
    if (Inventory.contains(xKItems.DarkEssenceFragments) || Inventory.contains(xKItems.DarkEssenceBlock)) {

      if (local.isMoving()) {
        Time.sleep(1000,2000);
      }


      if (!local.isMoving()) {
        TileObject DarkAltar = TileObjects.getNearest(xKGameObjectsID.DarkAltarID);
        if (DarkAltar != null) {
          if (local.distanceTo(DarkAltar.getWorldLocation()) < 10) {
            //Randomize Tile.

            int yMin = 3856;
            int yMax = 3854;
            int xMin = 1723;
            int xMax = 1727;

            int yRandom = (int)Math.floor(Math.random() * (yMax - yMin + 1) + yMin);
            int xRandom = (int)Math.floor(Math.random() * (xMax - xMin + 1) + xMin);
            FirstTileToClickv1 = Tiles.getAt(new WorldPoint(xRandom, yRandom, 0));
            FirstTileToClickv1.walkHere();
            Time.sleep(1000,2000);
            CTM = true;
          }
        }
      }

      if (local.distanceTo(FirstTileToClickv1.getWorldLocation()) < 15) {
        CTM = false;
      }

      if (!local.isMoving() && CTM) {
        Time.sleep(100,200);
        CTM = false;
      }
      if (local.distanceTo(xKLocations.BloodAltarLocation) > 15 && !CTM) {
        Walker.walkTo(xKLocations.BloodAltarLocation);
      }

      if (local.distanceTo(xKLocations.BloodAltarLocation) < 15) {
        CTM = true;
      }

      if (Inventory.isFull() && !local.isAnimating()) {
        if (local.distanceTo(BloodAltar.getWorldLocation()) < 25 || local.distanceTo(xKLocations.BloodAltarLocation) < 25) {
          BloodAltar.interact("Bind");
        }
      }
    }

    //Add delay else tbe script will run to fast.
    if (local.getAnimation() == xKAnimationID.CraftedRunesAtBloodAltar) {
      xKSettings.AllowCraftingv1 = true;
    }
    if (!Inventory.contains(xKItems.DarkEssenceFragments) &&  xKSettings.AllowCraftingv1) {
      xKSettings.AllowCraftingv2 = true;
    }

    if (Inventory.contains(xKItems.DarkEssenceBlock) &&  xKSettings.AllowCraftingv2) {
      if (!Tabs.isOpen(Tab.INVENTORY)) { Tabs.open(Tab.INVENTORY); }
      Item Chissel = Inventory.getFirst(xKItems.Chissel);
      Item DarkEssence = Inventory.getFirst(xKItems.DarkEssenceBlock);
      Chissel.useOn(DarkEssence);
    }

    if (!Inventory.contains(xKItems.DarkEssenceBlock) && Inventory.contains(xKItems.DarkEssenceFragments)) {
      BloodAltar.interact("Bind");
    }

    if (!Inventory.contains(xKItems.DarkEssenceBlock) && !Inventory.contains(xKItems.DarkEssenceFragments)) {

      if (local.isMoving()) {
        Time.sleep(1000,2000);
      }

      if (!local.isMoving()) {

        if (BloodAltar != null) {
          if (local.distanceTo(BloodAltar.getWorldLocation()) < 10) {
            //Randomize Tile.

            int yMin = 3840;
            int yMax = 3847;
            int xMin = 1735;
            int xMax = 1739;

            int yRandom = (int) Math.floor(Math.random() * (yMax - yMin + 1) + yMin);
            int xRandom = (int) Math.floor(Math.random() * (xMax - xMin + 1) + xMin);
            Tile FirstTileToClick = Tiles.getAt(new WorldPoint(xRandom, yRandom, 0));
            FirstTileToClick.walkHere();
            Time.sleep(1000, 2000);
            CTMAtAltar = true;
          }
        }
      }

      if (!local.isMoving() && CTMAtAltar) {
        Time.sleep(100,200);
        CTMAtAltar = false;
      }

      if (!CTMAtAltar) {
        Walker.walkTo(xKLocations.DenseRuneStoneLocation);
      }
    }

    return 100;
  }
}
