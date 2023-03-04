package net.runelite.client.plugins.kourendlibrary.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.kourendlibrary.LibraryUtils;
import net.runelite.client.plugins.kourendlibrary.State;
import net.runelite.client.plugins.kourendlibrary.library.Book;

import net.runelite.client.plugins.kourendlibrary.tasks.api.Waiting;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Task;

@Slf4j
public class DeliverBookTask implements Task {

  public boolean contains(int... values) {
    for (int value : values) {
      for (Book b : Book.values()) {
        if (b.getItem() == value) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean validate()
  {
    return !Inventory.isFull() && State.get().getCurrentAssignment().isPresent()
     && State.get().getCurrentBooks().contains(State.get().getCurrentAssignment().get());

  }

  @Override
  public boolean isBlocking()
  {
   return LibraryUtils.helpProfessor(State.get().getCurrentProfessor())
            && Waiting.waitAfterWalking(() -> !State.get().getCurrentAssignment().isPresent(), 1000);
  }

  @Override
  public int execute()
  {
    log.info("Delivering task");
    State.get().setStatus("Delivering book");

    return 100;
  }
}
