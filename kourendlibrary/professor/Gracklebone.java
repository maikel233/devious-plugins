package net.runelite.client.plugins.kourendlibrary.professor;


import net.runelite.api.NPC;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.scene.Tiles;

public class Gracklebone implements Professor {

  @Override
  public String getName() {
    return "Professor Gracklebone";
  }

  @Override
  public Tile getPosition() {
   // return Tiles.getAt(new WorldPoint(1625, 3800, 0));
    NPC Prof = NPCs.getNearest("Professor Gracklebone");
    Tile ProfessorLocation = null;
    if (Prof == null) { ProfessorLocation = Tiles.getAt(new WorldPoint(1625, 3800, 0)); }
    else { ProfessorLocation = Tiles.getAt(Prof.getWorldLocation()); }

    return ProfessorLocation;
  }
}
