package net.runelite.client.plugins.kourendlibrary.library;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.kourendlibrary.Constants;
import net.runelite.client.plugins.kourendlibrary.walking.Room;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class Bookcase {

	private WorldPoint location;
	private Room room;
	private final List<Integer> index;
	Bookcase(WorldPoint location) {
		this.location = location;
		this.room = findRoom();
		this.index = new ArrayList<>();

	}

	private Room findRoom() {
		for (Room room : Constants.Rooms.ALL_ROOMS) {
			if (location.isLocationWithinArea2(this.location, room.getArea().tiles)) {
				//log.info("Room: " + room);
				return room;
			}
		}
		//throw new IllegalArgumentException("Invalid bookcase location"); // Dont do this script wont run.
		log.error("Returning default room.");
		return Constants.Rooms.ROOM_BOTTOM_MIDDLE;
	}

	private boolean isBookSet;

	/**
	 * Book in this bookcase as found by the player.
	 * Will be correct as long as isBookSet is true, unless the library has reset;
	 */
	private Book book;

	/**
	 * Books that can be in this slot. Will only be populated if library.state != SolvedState.NO_DATA
	 */
	private Set<Book> possibleBooks = new HashSet<>();

	void clearBook() {
		book = null;
		isBookSet = false;
	}

	void setBook(Book book) {
		this.book = book;
		this.isBookSet = true;
	}

	String getLocationString() {
		StringBuilder b = new StringBuilder();

		// Floors 2 and 3
		boolean north = location.getY() > 3815;
		boolean west = location.getX() < 1625;

		// Floor 1 has slightly different dimensions
		if (location.getPlane() == 0) {
			north = location.getY() > 3813;
			west = location.getX() < 1627;
		}

		if (north && west) {
			b.append("Northwest");
		} else if (north) {
			b.append("Northeast");
		} else if (west) {
			b.append("Southwest");
		} else {
			b.append("Center");
		}

		b.append(" ");

		switch (location.getPlane()) {
			case 0:
				b.append("ground floor");
				break;
			case 1:
				b.append("middle floor");
				break;
			case 2:
				b.append("top floor");
				break;
		}

		return b.toString();
	}


	public Book getBook() {
		return book;
	}

	public Set<Book> getPossibleBooks() {
		return possibleBooks;
	}

	public WorldPoint getLocation() {
		return location;
	}

	public Room getRoom() {
		return room;
	}

	public List<Integer> getIndex() {
		return index;
	}

	public boolean isBookSet() {
		return isBookSet;
	}

	public WorldPoint  getPosition() {
		return location;
	}
}
