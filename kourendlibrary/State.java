package net.runelite.client.plugins.kourendlibrary;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.kourendlibrary.library.Book;
import net.runelite.client.plugins.kourendlibrary.library.Library;
import net.runelite.client.plugins.kourendlibrary.professor.Professor;
import static java.util.stream.Collectors.toList;
@Slf4j
@Getter
public class State {

  public static final State instance = new State();

  public static State get() {
    return instance;
  }
  private final Library library;

  public static void setCurrentBooksFromInventory(List<Item> inventory) {
    State.get().setCurrentBooks(inventory.stream().map(rsItem -> Book.byId(rsItem.getId())).collect(toList()));
  }

  private static String status = "Starting...";
  private static WorldPoint lastBookcaseTile;
  private static List<Book> currentBooks;
  private static Book currentAssignment;
  private static Professor currentProfessor;

  State() {
    this.library = new Library();
  }
  public Library getLibrary() {
    return library;
  }

  public static Optional<WorldPoint> getLastBookcaseTile() {
    return Optional.ofNullable(lastBookcaseTile);
  }

  public static void setLastBookcaseTile(WorldPoint lastBookcaseTile) {
    State.lastBookcaseTile = lastBookcaseTile;
  }

  public static List<Book> getCurrentBooks() {
    return currentBooks;
  }


  //Debug stuff
  public static void removeBook(String bookName) {
    Iterator<Book> iterator = currentBooks.iterator();
    while (iterator.hasNext()) {
      Book book = iterator.next();
      if (book.getName().equals(bookName)) {
        iterator.remove();
        break;
      }
    }
  }
  //Debug stuff
  public boolean doesBookExist(String bookName) {
    for (Book book : currentBooks) {
      if (book.getName().equals(bookName)) {
        return true;
      }
    }
    return false;
  }
  //Debug stuff
  public static boolean containsBook(List<Book> books, String name) {
    for (Book book : books) {
      if (book.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public void setCurrentBooks(List<Book> currentBooks) {
    this.currentBooks = currentBooks;
  }

  public static Optional<Book> getCurrentAssignment() {
    return Optional.ofNullable(currentAssignment);
  }

  public void setCurrentAssignment(Book currentAssignment) {
    this.currentAssignment = currentAssignment;
  }

  public static Professor getCurrentProfessor() {
   return Optional.ofNullable(currentProfessor).orElse(Constants.Professors.GRACKLEBONE);
  }

  //TODO not really state, remove from here
  public void swapProfessors() {
    currentProfessor = getPossibleProfessors().stream()
            .filter(professor -> professor != getCurrentProfessor())
            .findAny()
            .orElse(Constants.Professors.GRACKLEBONE);
  }

  private Set<Professor> getPossibleProfessors() {
    return Sets.newHashSet(Constants.Professors.GRACKLEBONE, Constants.Professors.VILLIA);
  }

  public String getStatus() {
    return status;
  }

  public static void setStatus(String newStatus) {
    if (!newStatus.equals(status)) {
      log.info(newStatus);
      status = newStatus;
    }
  }
}




