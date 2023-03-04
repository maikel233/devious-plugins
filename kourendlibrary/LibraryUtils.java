package net.runelite.client.plugins.kourendlibrary;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import java.util.*;
import static java.util.stream.Collectors.toSet;
import net.runelite.client.plugins.kourendlibrary.library.Book;
import net.runelite.client.plugins.kourendlibrary.library.Bookcase;
import net.runelite.client.plugins.kourendlibrary.professor.Professor;
import net.runelite.client.plugins.kourendlibrary.tasks.api.Waiting;

import net.runelite.client.plugins.kourendlibrary.walking.LibraryWalker;
import net.runelite.client.plugins.kourendlibrary.walking.Room;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Dialog;

@Slf4j
public class LibraryUtils {

  public static boolean clickBookshelf(Bookcase bookcase) {

    Room room = bookcase.getRoom();
    if (room == null) {
      log.error("Bookcase does not have a valid room assigned.");
      return false;
    }

    if (LibraryWalker.get().walkTo(room)) {
      log.info("Bookcase is not null.");
      log.info(bookcase.getRoom().toString());

      Player local = Players.getLocal();
      if (local == null) { return false; }

      TileObject BookShelfObj = TileObjects.getNearest(bookcase.getPosition(), Constants.Objects.BOOKSHELF);
      if (BookShelfObj == null) {
        return false;
      }

      if (local.isMoving() || local.getAnimation() == 832 || Dialog.isOpen()) {
        Time.sleep(100, 200);
        return false;
      }

      if (local.getWorldLocation().distanceTo(BookShelfObj.getWorldLocation()) > 3) { //> 8 && Walking.blindWalkTo(BookShelfObj)) {
        Tile BookShelfTile = Tiles.getAt(BookShelfObj.getWorldLocation());
          log.info("Walking to bookshelf!" + BookShelfObj.getWorldLocation());
          BookShelfTile.walkHere();
        Waiting.waitCondition(() -> local.getWorldLocation().distanceTo(BookShelfObj.getWorldLocation()) > 5, 7000);
      }


      //if (local.getAnimation() == 832) { Time.sleep(1000); return false; }
      //if (Dialog.isOpen()) { return false; }
      if (!local.isAnimating()) { BookShelfObj.interact("Search"); }

      State.get().setLastBookcaseTile(bookcase.getPosition());
      return Waiting.waitAfterWalking(() -> !State.get().getLastBookcaseTile().isPresent(), 7000);
    }


    log.error("We couldn't click on " + bookcase.getLocation());

    return false;
  }

  public static boolean helpProfessor(Professor professor) {
    log.info("Executing helpprofessor.");

    Player local = Players.getLocal();
    if (local.getPlane() == 0) {
      NPC Prof = NPCs.getNearest(professor.getName());
      if (Prof != null) {
        if (local.getWorldLocation().distanceTo(Prof.getWorldLocation()) > 5) {
          log.info("Professor walkhere 1");
          professor.getPosition().walkHere();
        }
        else {
          Prof.interact("Help");
        }

        if (Dialog.isOpen()) { return true;  }
      }
      else {
        log.info("Professor walkhere 2");
        professor.getPosition().walkHere();
      }
    }
    else if (LibraryWalker.get().walkTo(Constants.Rooms.ROOM_BOTTOM_MIDDLE)) {

      if (local.getPlane() != 0) {
        log.error("We're not at the floor.");
        return false;
      }
      if (local.getWorldLocation().distanceTo(professor.getPosition()) > 4) { //&& Walking.blindWalkTo(professor.getPosition())) {
        Waiting.waitCondition(() -> local.getWorldLocation().distanceTo(professor.getPosition()) > 5, 7000);
      }

      NPC Prof = NPCs.getNearest(professor.getName());
      if (Prof != null) {
        Prof.interact("Help");
        return true; // ????
      }
    }

    log.error("Failed to walk to professor");
    return false;
  }

  public static boolean bookcaseContainsNewBook(Bookcase bookcase) {
    Set<Book> booksInBookcase = bookcase.getPossibleBooks()
            .stream()
            .filter(book -> !book.isDarkManuscript())
            .filter(book -> !book.equals(State.get().getCurrentAssignment().orElse(null)))
            .collect(toSet());

    return booksInBookcase.size() > 0 && Collections.disjoint(State.get().getCurrentBooks(), booksInBookcase);
  }

  public static Bookcase findBook(Book lookingForBook) {
    int attempts = 0;

    while (attempts < 3) {

      List<Bookcase> bookcases = State.get().getLibrary().getBookcases();

      for (Bookcase bookcase : bookcases) {

        boolean isBookKnown = bookcase.isBookSet();
        Book book = bookcase.getBook();
        Set<Book> possibleBooks = bookcase.getPossibleBooks();

        if (isBookKnown && book == null) {
          book = possibleBooks.stream()
                  .filter(b -> b != null && b.isDarkManuscript())
                  .findFirst()
                  .orElse(null);
        }

        if (!isBookKnown && possibleBooks.size() == 1) {
          book = possibleBooks.stream().findFirst().orElse(null);
          isBookKnown = true;
        }

        if (isBookKnown && book == lookingForBook) {
          return bookcase;
        } else if ((book == null || !book.isDarkManuscript()) && possibleBooks.contains(lookingForBook)) {
          return bookcase;
        }
      }
      attempts++;
      Time.sleep(500,1000);

      log.debug("Looking for: " + lookingForBook);

      bookcases.stream()
              .filter(bookcase -> bookcase.getPossibleBooks().size() > 0)
              .forEach(bookcase -> log.debug(String.format("Bookcase: %s, isSet: %s, book: %s, books: %s", bookcase, bookcase.isBookSet(), bookcase.getBook(), bookcase.getPossibleBooks())));
    }

    throw new RuntimeException("Could not find bookcase containing book " + lookingForBook);

  }
}
