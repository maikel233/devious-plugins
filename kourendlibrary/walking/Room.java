package net.runelite.client.plugins.kourendlibrary.walking;



import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.kourendlibrary.library.AreaLocal;

public class Room {

  public static void connectRoomsWalking(Room room1, Room room2) {
    room1.addConnectedRoom(room2, RoomLinkType.WALK);
    room2.addConnectedRoom(room1, RoomLinkType.WALK);
  }

  public static void connectRoomsStairs(Room lowerRoom, Room upperRoom) {
    lowerRoom.addConnectedRoom(upperRoom, RoomLinkType.UP);
    upperRoom.addConnectedRoom(lowerRoom, RoomLinkType.DOWN);
  }

  private final String name;
  private final AreaLocal rsArea;

  private final Map<Room, RoomLinkType> connectedRooms;

  public final AreaLocal walkToArea;

  private final WorldPoint upStairs;
  private final WorldPoint downStairs;

  public Room(String name, AreaLocal rsArea, WorldPoint upStairs, WorldPoint downStairs) {
    this.name = name;
    this.walkToArea = rsArea;
    this.rsArea = rsArea;
    this.upStairs = upStairs;
    this.downStairs = downStairs;
    this.connectedRooms = new HashMap<>();
  }

  public Room(String name, AreaLocal walkToArea, AreaLocal rsArea,  WorldPoint upStairs, WorldPoint downStairs) {
    this.name = name;
    this.walkToArea = walkToArea;
    this.rsArea = rsArea;
    this.upStairs = upStairs;
    this.downStairs = downStairs;
    this.connectedRooms = new HashMap<>();
  }


  public AreaLocal getArea() {
    return rsArea;
  }


  public WorldPoint getWalkTo() {
    return walkToArea.getRandomLocation();
  }


  public WorldPoint getUpStairs() {
    return upStairs;
  }

  public WorldPoint getDownStairs() {
    return downStairs;
  }

  public RoomLinkType getRoomLinkTypeToRoom(Room destRoom) {
    return connectedRooms.get(destRoom);
  }

  public Set<Room> getConnectedRooms() {
    return connectedRooms.keySet();
  }

  public void addConnectedRoom(Room room, RoomLinkType roomLinkType) {
    connectedRooms.put(room, roomLinkType);
  }

  @Override
  public String toString() {
    return name;
  }
}
