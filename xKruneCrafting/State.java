package net.runelite.client.plugins.xKruneCrafting;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class State {

  public static final State instance = new State();

  public static State get() {
    return instance;
  }

  private static String status = "Starting...";

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




