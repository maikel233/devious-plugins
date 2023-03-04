package net.runelite.client.plugins.kourendlibrary.professor;

import net.runelite.api.NPC;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.scene.Tiles;

public class Villia implements Professor {

  @Override
  public String getName() {
    return "Villia";
  }

  @Override
  public Tile getPosition() {

   // return Tiles.getAt(new WorldPoint(1625, 3815, 0));
    NPC Prof = NPCs.getNearest("Villia");
    Tile ProfessorLocation = null;
    if (Prof == null) { ProfessorLocation = Tiles.getAt(new WorldPoint(1625, 3815, 0)); }
   else { ProfessorLocation = Tiles.getAt(Prof.getWorldLocation()); }
    return ProfessorLocation;
  }
}
