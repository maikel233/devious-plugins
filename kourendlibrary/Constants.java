package net.runelite.client.plugins.kourendlibrary;


import com.google.common.collect.Lists;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.kourendlibrary.library.AreaLocal;
import net.runelite.client.plugins.kourendlibrary.professor.Gracklebone;
import net.runelite.client.plugins.kourendlibrary.professor.Professor;
import net.runelite.client.plugins.kourendlibrary.professor.Villia;
import net.runelite.client.plugins.kourendlibrary.walking.Room;


public class Constants {

  public static class Items {

    public static final int DARK_MANUSCRIPT = 13514;
    public static final int DARK_MANUSCRIPT_13515 = 13515;
    public static final int DARK_MANUSCRIPT_13516 = 13516;
    public static final int DARK_MANUSCRIPT_13517 = 13517;
    public static final int DARK_MANUSCRIPT_13518 = 13518;
    public static final int DARK_MANUSCRIPT_13519 = 13519;
    public static final int DARK_MANUSCRIPT_13520 = 13520;
    public static final int DARK_MANUSCRIPT_13521 = 13521;
    public static final int DARK_MANUSCRIPT_13522 = 13522;
    public static final int DARK_MANUSCRIPT_13523 = 13523;
    public static final int RADAS_CENSUS = 13524;
    public static final int RICKTORS_DIARY_7 = 13525;
    public static final int EATHRAM__RADA_EXTRACT = 13526;
    public static final int KILLING_OF_A_KING = 13527;
    public static final int HOSIDIUS_LETTER = 13528;
    public static final int WINTERTODT_PARABLE = 13529;
    public static final int TWILL_ACCORD = 13530;
    public static final int BYRNES_CORONATION_SPEECH = 13531;
    public static final int IDEOLOGY_OF_DARKNESS = 13532;
    public static final int RADAS_JOURNEY = 13533;
    public static final int TRANSVERGENCE_THEORY = 13534;
    public static final int TRISTESSAS_TRAGEDY = 13535;
    public static final int TREACHERY_OF_ROYALTY = 13536;
    public static final int TRANSPORTATION_INCANTATIONS = 13537;
    public static final int SOUL_JOURNEY = 19637;
    public static final int VARLAMORE_ENVOY = 21756;

    public static final String ARCANE_KNOWLEDGE = "Book of arcane knowledge";

  }

  public static class Objects {

    public static final String BOOKSHELF = "Bookshelf";

  }

  public static class Professors {

    public static final Professor GRACKLEBONE = new Gracklebone();
    public static final Professor VILLIA = new Villia();

  }

  public static class Rooms {
    public static final Room ROOM_BOTTOM_NE = new Room("BottomNE", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1639, 3819, 0),
                    new WorldPoint(1639, 3832, 0),
                    new WorldPoint(1659, 3832, 0),
                    new WorldPoint(1659, 3814, 0),
                    new WorldPoint(1644, 3814, 0),
                    new WorldPoint(1642, 3817, 0),
                    new WorldPoint(1642, 3820, 0),
                    new WorldPoint(1647, 3820, 0),
                    new WorldPoint(1647, 3817, 0),
                    new WorldPoint(1642, 3817, 0)
            })
    ), new WorldPoint(1645, 3819, 0), null);
    public static final Room ROOM_BOTTOM_NW = new Room("BottomNW", new AreaLocal(
            List.of(new WorldPoint[]{
                    //Deze wordt uigevoerd.
                    new WorldPoint(1622, 3814, 0),
                    new WorldPoint(1607, 3814, 0),
                    new WorldPoint(1607, 3832, 0),
                    new WorldPoint(1626, 3832, 0),
                    new WorldPoint(1627, 3832, 0),
                    new WorldPoint(1627, 3819, 0),
                    new WorldPoint(1622, 3819, 0),
                    new WorldPoint(1622, 3817, 0),
                    new WorldPoint(1618, 3817, 0),
                    new WorldPoint(1618, 3820, 0),
                    new WorldPoint(1622, 3820, 0)
            })
    ), new WorldPoint(1614, 3825, 0), null);
    public static final Room ROOM_BOTTOM_SW = new Room("BottomSW", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1607, 3802, 0),
                    new WorldPoint(1607, 3784, 0),
                    new WorldPoint(1627, 3784, 0),
                    new WorldPoint(1627, 3797, 0),
                    new WorldPoint(1623, 3797, 0),
                    new WorldPoint(1623, 3802, 0),
                    new WorldPoint(1618, 3799, 0),
                    new WorldPoint(1622, 3799, 0),
                    new WorldPoint(1622, 3796, 0),
                    new WorldPoint(1618, 3796, 0)
            })
    ), new WorldPoint(1614, 3796, 0), null);
    public static final Room ROOM_BOTTOM_MIDDLE = new Room("BottomMiddle", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1623, 3799, 0),
                    new WorldPoint(1623, 3817, 0),
                    new WorldPoint(1628, 3817, 0),
                    new WorldPoint(1628, 3799, 0)
            })
    ), null, null);

    public static final Room ROOM_MIDDLE_NE = new Room("MiddleNE", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1640, 3832, 1),
                    new WorldPoint(1640, 3817, 1),
                    new WorldPoint(1642, 3815, 1),
                    new WorldPoint(1660, 3815, 1),
                    new WorldPoint(1660, 3832, 1)
            })
    ), new WorldPoint(1644, 3828, 1), new WorldPoint(1645, 3821, 1));
    public static final Room ROOM_MIDDLE_NW = new Room("MiddleNW", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1626, 3817, 1),
                    new WorldPoint(1624, 3815, 1),
                    new WorldPoint(1606, 3815, 1),
                    new WorldPoint(1606, 3832, 1),
                    new WorldPoint(1626, 3832, 1)
            })
    ), new WorldPoint(1612, 3818, 1), new WorldPoint(1611, 3825, 1));
    public static final Room ROOM_MIDDLE_SW = new Room("MiddleSW", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1624, 3801, 1),
                    new WorldPoint(1606, 3801, 1),
                    new WorldPoint(1606, 3783, 1),
                    new WorldPoint(1626, 3783, 1),
                    new WorldPoint(1626, 3799, 1)
            })
    ), new WorldPoint(1621, 3792, 1), new WorldPoint(1611, 3796, 1));
    public static final Room ROOM_MIDDLE_MIDDLE = new Room("MiddleMiddle", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1626, 3817, 1),
                    new WorldPoint(1624, 3815, 1),
                    new WorldPoint(1624, 3801, 1),
                    new WorldPoint(1626, 3799, 1),
                    new WorldPoint(1642, 3799, 1),
                    new WorldPoint(1642, 3815, 1),
                    new WorldPoint(1640, 3817, 1)
            })
    ), new WorldPoint(1638, 3807, 1), null);


    public static final Room ROOM_TOP_NE = new Room("TopNE", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1640, 3832, 2),
                    new WorldPoint(1659, 3815, 2),
                    new WorldPoint(1642, 3827, 2),
                    new WorldPoint(1645, 3824, 2)
            })
    ), null, new WorldPoint(1646, 3828, 2));


    public static final Room ROOM_TOP_NW = new Room("TopNW", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1613, 3819, 2),
                    new WorldPoint(1617, 3817, 2),
                    new WorldPoint(1606, 3832, 2),
                    new WorldPoint(1625, 3815, 2)
            })

    ), null, new WorldPoint(1609, 3818, 2));


    public static final Room ROOM_TOP_SW = new Room("TopSW", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1612, 3799, 2),
                    new WorldPoint(1616, 3796, 2),
                    new WorldPoint(1606, 3800, 2),
                    new WorldPoint(1625, 3783, 2)
            })
    ), null, new WorldPoint(1621, 3794, 2));


    public static final Room ROOM_TOP_MIDDLE = new Room("TopMiddle", new AreaLocal(
            List.of(new WorldPoint[]{
                    new WorldPoint(1625, 3815, 2),
                    new WorldPoint(1641, 3799, 2),
                    new WorldPoint(1627, 3813, 2),
                    new WorldPoint(1638, 3802, 2)
            })
    ), null, new WorldPoint(1638, 3804, 2));

    public static final List<Room> ALL_ROOMS = Lists.newArrayList(
            ROOM_BOTTOM_NE,
            ROOM_BOTTOM_NW,
            ROOM_BOTTOM_SW,
            ROOM_BOTTOM_MIDDLE,
            ROOM_MIDDLE_NE,
            ROOM_MIDDLE_NW,
            ROOM_MIDDLE_SW,
            ROOM_MIDDLE_MIDDLE,
            ROOM_TOP_NE,
            ROOM_TOP_NW,
            ROOM_TOP_SW,
            ROOM_TOP_MIDDLE
    );
  }
}

