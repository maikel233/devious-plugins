package net.runelite.client.plugins.kourendlibrary.walking;

import com.google.common.collect.Lists;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;


import java.util.*;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import static net.runelite.client.plugins.kourendlibrary.Constants.Rooms.*;
import net.runelite.client.plugins.kourendlibrary.LibraryUtils;
import net.runelite.client.plugins.kourendlibrary.State;
import net.runelite.client.plugins.kourendlibrary.library.SolvedState;
import net.runelite.client.plugins.kourendlibrary.tasks.api.Waiting;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.scene.Tiles;

/**
 * This has got to be up there with the worse code I've written. But it works.
 *
 * Rather than rely on dax walker for this, I wanted to write my own pathfinding. This is possibly
 * the worst way I could have done it, but it does seem to work okay.
 *
 * Maikel233:
 * You dont say i even made it even worser but it works alright...
 */
@Slf4j
public class LibraryWalker {

  private static final LibraryWalker instance = new LibraryWalker();

  public static LibraryWalker get() {
    return instance;
  }

  private LibraryWalker() {
    initialiseLinks();
  }

  private boolean walkRoute(Room startRoom, List<Room> route) {

    Room currentRoom = startRoom;
    log.info("Entry WalkRoute");

    for (Room nextRoom : route) {


      int attempts = 0;

      while (attempts <= 1) {
        log.info("Entry WalkRoute Attempts: " + attempts);
        if (currentRoom.getRoomLinkTypeToRoom(nextRoom) == RoomLinkType.WALK) {

          log.info("Entry RandomNextTile ");
          Tile RandomNextRoomTile = Tiles.getAt(nextRoom.getArea().getRandomLocation());
          walkTo(nextRoom, RandomNextRoomTile, () -> isInRoom(nextRoom), 50);
          log.info("Entry WalkTo ");
          if (!Waiting.waitAfterWalking(() -> isInRoom(nextRoom),  random(1000, 2000))) {
            log.error("Could not walk to " + nextRoom);
            attempts++;
          } else {
            break;
          }

        } else {

          log.info("Entry WalkTo Else Statement ");
          TileObject stairscase = null;
          WorldPoint stairsTile = null;

          if (currentRoom.getRoomLinkTypeToRoom(nextRoom) == RoomLinkType.UP) {
            log.info("Entry WalkTo Else Statement UPSTAIRS ");
            stairsTile = currentRoom.getUpStairs();
          } else {
            log.info("Entry WalkTo Else Statement DOWNSTAIRS");
            log.info("DOWNSTAIRS: " + currentRoom.getDownStairs().getWorldLocation());
            stairsTile = currentRoom.getDownStairs();

          }
          if (stairsTile != null) {
            log.info("Entry WalkTo Else Statement staircase = TileObjects.getnearest");
            stairscase = TileObjects.getNearest(stairsTile, "Stairs");
          }
          if (stairscase == null) { log.error("Why is this staircase null??????"); }

          if (!clickStaircase(stairscase)) {
            log.info("!ClickStairCase ");
            attempts++;
          } else {
            break;
          }
        }
        Time.sleep(40, 70);
      }
      if (attempts > 1) {
        return false;
      } else {
        if (State.get().getLibrary().getState() == SolvedState.COMPLETE) {
          checkForBooks(nextRoom);
        }
        currentRoom = nextRoom;
      }
    }

    return true;
  }

  public boolean walkTo(Room destRoom) {

    if (destRoom != null) { log.info("Room: " + destRoom); }
    else { log.info("Room is null"); }

    Player local = Players.getLocal();
    if (local == null) { return false; }
    Room currentRoom = getCurrentRoom(local.getWorldLocation());
    if (currentRoom == null) { log.info("CurrentRoom is nully"); return false; }

    if (currentRoom.equals(destRoom)) {
      log.info("We're in the current room: " + currentRoom);
      return true;
    }

    List<Room> route = getRouteTo(currentRoom, destRoom);
    log.info("Current room: " + currentRoom);
    log.info("Found route: " + route);

   // log.info("WalkRoute->CurrentRoom route");
    return walkRoute(currentRoom, route);
  }


  public boolean isInRoom(Room room) {
    Player local = Players.getLocal();
    WorldPoint LocalLoc = local.getWorldLocation();
    return room.getWalkTo().getPlane() == local.getPlane()
            && (LocalLoc.isLocationWithinArea2(local.getWorldLocation(), (room.getArea().tiles)));
  }

  Room getCurrentRoom(WorldPoint location) {
    Player local = Players.getLocal();
    return ALL_ROOMS.stream()
            .filter(room -> room.getWalkTo().getPlane() == local.getPlane())
            .filter(room -> location.isLocationWithinArea2(location, room.getArea().tiles))
            .findFirst()
            .orElseGet(() -> {
              if (local.getPlane() == 0) {
                return ROOM_BOTTOM_MIDDLE;
              } else if (local.getPlane() == 2) {
                return ROOM_TOP_MIDDLE;
              } else {
                log.error("We're not sure where we are. Fail safely. Coords: " + local.getWorldLocation());
                Tile W2 = Tiles.getAt(ROOM_BOTTOM_MIDDLE.getWalkTo());
                W2.walkHere();
                return ROOM_BOTTOM_MIDDLE;
              }
            });
  }


  public static int random(int Min, int Max)
  {
    return (int) (Math.random()*(Max-Min))+Min;
  }


  public void walkTo(Room nextRoom, Tile tile, Supplier<Boolean> condition, int timeout) {
    long startTime = System.currentTimeMillis();
    while (!condition.get() && System.currentTimeMillis() - startTime < timeout) {

      TileObject stairscase = null;
      WorldPoint stairsTile = null;

      Player local = Players.getLocal();
      Room currentRoom = getCurrentRoom(local.getWorldLocation());

      if (currentRoom.getRoomLinkTypeToRoom(nextRoom) == RoomLinkType.UP) {
        stairsTile = currentRoom.getUpStairs();
      } else if (currentRoom.getRoomLinkTypeToRoom(nextRoom) == RoomLinkType.DOWN){
        stairsTile = currentRoom.getDownStairs();
      }
      if (currentRoom.getRoomLinkTypeToRoom(nextRoom) == RoomLinkType.UP || currentRoom.getRoomLinkTypeToRoom(nextRoom) == RoomLinkType.DOWN) {
        stairscase = TileObjects.getNearest(stairsTile, "Stairs");
        if (stairscase != null) {
          log.info("Climbing 222");
          stairscase.interact("Climb");
        }
      }
      if (currentRoom.getRoomLinkTypeToRoom(nextRoom) == RoomLinkType.WALK) {
        log.info("Walking to destination 222");
        tile.walkHere();
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
       // Handle InterruptedException
      }
    }
  }

  private void checkForBooks(Room room) {
    log.debug(String.format("Checking current room %s for books", room));

    State.get().getLibrary().getBookcasesInRoom(room).
            stream()
            .filter(LibraryUtils::bookcaseContainsNewBook)
            .forEach(bookshelf -> {
              log.info(String.format("Checking bookshelf as we believe it has the following books: %s", bookshelf.getPossibleBooks()));
              LibraryUtils.clickBookshelf(bookshelf);
            });
  }

  private boolean clickStaircase(TileObject staircase) {
    Player local = Players.getLocal();

    if (staircase != null) {
      log.info("About to click on staircase.");
      if (staircase.getWorldLocation().distanceTo(local.getWorldLocation()) > 15) {
        Tile StairCaseTile = Tiles.getAt(staircase.getWorldLocation());
          log.info("StairCaseTile.WalkHere()");
          StairCaseTile.walkHere();
          return false;
      }
      if (staircase.getWorldLocation().distanceTo(local.getWorldLocation()) < 15) {
        log.info("Climb!");
        staircase.interact("Climb");
        return false;
      }
    }
    else {
      log.error("Could not find staircase! but it does exist!");
      return false;
    }
    return true;
  }

  private void initialiseLinks() {
    Room.connectRoomsWalking(ROOM_BOTTOM_NE, ROOM_BOTTOM_NW);
    Room.connectRoomsWalking(ROOM_BOTTOM_NE, ROOM_BOTTOM_SW);
    Room.connectRoomsWalking(ROOM_BOTTOM_NW, ROOM_BOTTOM_SW);

    Room.connectRoomsWalking(ROOM_BOTTOM_NE, ROOM_BOTTOM_MIDDLE);
    Room.connectRoomsWalking(ROOM_BOTTOM_NW, ROOM_BOTTOM_MIDDLE);
    Room.connectRoomsWalking(ROOM_BOTTOM_SW, ROOM_BOTTOM_MIDDLE);

    Room.connectRoomsWalking(ROOM_TOP_NE, ROOM_TOP_NW);
    Room.connectRoomsWalking(ROOM_TOP_NE, ROOM_TOP_SW);
    Room.connectRoomsWalking(ROOM_TOP_NW, ROOM_TOP_SW);

    Room.connectRoomsWalking(ROOM_TOP_MIDDLE, ROOM_TOP_NE);
    Room.connectRoomsWalking(ROOM_TOP_MIDDLE, ROOM_TOP_NW);
    Room.connectRoomsWalking(ROOM_TOP_MIDDLE, ROOM_TOP_SW);

    Room.connectRoomsStairs(ROOM_BOTTOM_NE, ROOM_MIDDLE_NE);
    Room.connectRoomsStairs(ROOM_MIDDLE_NE, ROOM_TOP_NE);

    Room.connectRoomsStairs(ROOM_BOTTOM_NW, ROOM_MIDDLE_NW);
    Room.connectRoomsStairs(ROOM_MIDDLE_NW, ROOM_TOP_NW);

    Room.connectRoomsStairs(ROOM_BOTTOM_SW, ROOM_MIDDLE_SW);
    Room.connectRoomsStairs(ROOM_MIDDLE_SW, ROOM_TOP_SW);

    Room.connectRoomsStairs(ROOM_MIDDLE_MIDDLE, ROOM_TOP_MIDDLE);
  }

  private List<Room> getRouteTo(Room startRoom, Room destRoom) {

    if (destRoom == null) {
      log.error("EMPTY!!!! DEST ROOM!!!");
    }

    if (startRoom.equals(destRoom)) {
      return Lists.newArrayList();
    }

    Queue<List<Room>> pathQueue = new ArrayDeque<>();

    Set<Room> visitedRooms = new HashSet<>();

    pathQueue.add(new ArrayList<>(Collections.singletonList(startRoom)));

    Room currentRoom;

    List<Room> currentPath;

    while (!pathQueue.isEmpty()) {
      currentPath = pathQueue.remove();

      currentRoom = currentPath.get(currentPath.size()-1);

      if (!visitedRooms.contains(currentRoom)) {
        Set<Room> neighbours = currentRoom.getConnectedRooms();
        for (Room neighbour : neighbours) {

          List<Room> newPath = new ArrayList<>(currentPath);
          newPath.add(neighbour);
          pathQueue.add(newPath);

          if (neighbour.equals(destRoom)) {
            newPath.remove(0);
            return newPath;
          }

        }
        visitedRooms.add(currentRoom);

      }
    }

    log.error(String.format("Could not find path %s to %s", startRoom.getArea(), destRoom.getArea()));
    return Lists.newArrayList();
  }


}
