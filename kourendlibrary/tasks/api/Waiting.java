package net.runelite.client.plugins.kourendlibrary.tasks.api;



import java.awt.AWTException;
import java.awt.Robot;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;

public class Waiting {

  

  
  

  public static boolean waitAfterWalking(BooleanSupplier supplier, long timeout) {
    return waitAfterWalking(supplier, timeout, 50);
  }

  public static boolean waitAfterWalking(BooleanSupplier supplier, long timeout, long checkEvery) {
    Player local = Players.getLocal();
  
    if (waitCondition(local::isMoving, 1000)) {
      waitCondition(() -> !local.isMoving(), 8153); // TODO RANDOM THIS!
    }
    return waitWithABC(supplier, timeout);
  }

  public static boolean waitWithABC(BooleanSupplier supplier, long timeout) {
    
    return waitCondition(supplier, timeout);
  }

  public static boolean waitCondition(BooleanSupplier supplier, long timeout) {
    return Time.sleepUntil(supplier, 50);
  }

  public static <T> Optional<T> waitCondition(Supplier<T> supplier, long timeout) {
    if (waitCondition(() -> supplier.get() != null, timeout)) {
      return Optional.ofNullable(supplier.get());
    }
    return Optional.empty();
  }
}
