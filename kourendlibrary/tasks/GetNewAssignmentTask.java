package net.runelite.client.plugins.kourendlibrary.tasks;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.kourendlibrary.LibraryUtils;
import net.runelite.client.plugins.kourendlibrary.State;
import net.runelite.client.plugins.kourendlibrary.professor.Professor;

import net.runelite.client.plugins.kourendlibrary.tasks.api.Waiting;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Task;

@Slf4j
public class GetNewAssignmentTask implements Task {

  @Override
  public boolean validate()
  {
    return !Inventory.isFull() && !State.getCurrentAssignment().isPresent();
  }

  @Override
  public boolean isBlocking() {
    Professor nextProfessor = State.getCurrentProfessor();

    return LibraryUtils.helpProfessor(nextProfessor)
            && Waiting.waitCondition(() -> State.getCurrentAssignment().isPresent()
            || nextProfessor != State.getCurrentProfessor(), 7000);
  }

  @Override
  public int execute()
  {
    State.get().setStatus("Getting new task");
    log.info("Getting new task");

    return 100;
  }
}
