package net.runelite.client.plugins.kourendlibrary.tasks;


import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.client.plugins.kourendlibrary.LibraryUtils;
import net.runelite.client.plugins.kourendlibrary.State;
import net.runelite.client.plugins.kourendlibrary.library.Bookcase;
import net.runelite.client.plugins.kourendlibrary.library.SolvedState;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Task;


@Slf4j
public class SolveTask implements Task {

  @Override
  public boolean validate() {
    return !Inventory.isFull() && State.get().getLibrary().getState() == SolvedState.NO_DATA;
  }

  @Override
  public boolean isBlocking()
  {

    Player local = Players.getLocal();
    Bookcase[] bookcasesArray = State.get().getLibrary().getBookcasesOnLevel(local.getPlane())
            .stream()
            .filter(bookcase -> !bookcase.isBookSet())
            .filter(bookcase ->
                    bookcase.getPossibleBooks().stream().anyMatch(book -> !book.isDarkManuscript()) || State.get().getLibrary().getState() == SolvedState.NO_DATA)
            .toArray(Bookcase[]::new);

    if (bookcasesArray.length == 0) {
      bookcasesArray = State.get().getLibrary().getBookcases()
              .stream()
              .filter(bookcase -> !bookcase.isBookSet())
              .filter(bookcase ->
                      bookcase.getPossibleBooks().stream().anyMatch(book -> !book.isDarkManuscript()) || State.get().getLibrary().getState() == SolvedState.NO_DATA)
              .toArray(Bookcase[]::new);
      log.info(String.format("We are unsure on %s bookcases total", bookcasesArray.length));

      if (bookcasesArray.length == 0) {
        log.error("We're confused. Hop to reset");
        Game.logout();

        // int world; //= WorldHopper.getRandomWorld(true, false);
        // if (!WorldHelper.isPvp(world)) {
        //  WorldHopper.changeWorld(world);
        // }
        // }
        // } else {

      }
      log.info(String.format("We are unsure on %s bookcases this floor", bookcasesArray.length));
    }

    Sorting.sortByDistance(bookcasesArray, local.getWorldLocation(), true);

    return Arrays.stream(bookcasesArray)
            .filter(bookcase -> bookcase.getLocation().getWorldLocation().getWorldX() >= 1599)
            .findFirst()
            .map(LibraryUtils::clickBookshelf)
            .orElse(false);
  }




  @Override
  public int execute()
  {
    State.get().setStatus("Solving the library");
    log.info("Solving!");

    return 100;
  }
}