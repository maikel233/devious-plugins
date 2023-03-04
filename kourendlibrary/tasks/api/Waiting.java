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

  public static void moveHumanized(Robot robot, int x, int y, int margin) {
    // Calculate the random offset for the target coordinates
    int offsetX = ThreadLocalRandom.current().nextInt(-margin, margin + 1);
    int offsetY = ThreadLocalRandom.current().nextInt(-margin, margin + 1);

    // Add the offset to the target coordinates
    int targetX = x + offsetX;
    int targetY = y + offsetY;

    // Calculate the delay before and after the mouse movement
    int delayBefore = ThreadLocalRandom.current().nextInt(100, 501);
    int delayAfter = ThreadLocalRandom.current().nextInt(100, 501);

    // Move the mouse to the target coordinates with the delays and offset applied
    robot.delay(delayBefore);
    robot.mouseMove(targetX, targetY);
    robot.delay(delayAfter);
  }

  public static void hover(Robot robot, int x, int y, int margin) {
   // moveHumanized(robot, x, y, margin);
  }

  public static void move(int x, int y, int margin) {
    try {
      Robot robot = new Robot();
      Random rand = new Random();
      int newX = x + rand.nextInt(margin) - margin/2;
      int newY = y + rand.nextInt(margin) - margin/2;
      robot.mouseMove(newX, newY);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  
  public static void performActions() {
    if (!Tabs.isOpen(Tab.SKILLS)) {
      Tabs.open(Tab.SKILLS);
    }

    Point CraftingLocation = new Point(643, 348);
    Point MagicLocation = new Point(579, 378);
    Point RuneCraftingLocation = new Point(579, 409);

    Random random = new Random();
    int randomIndex = random.nextInt(3);
    Point selectedPoint;
    switch (randomIndex) {
      case 0:
        selectedPoint = CraftingLocation;
        break;
      case 1:
        selectedPoint = MagicLocation;
        break;
      case 2:
        selectedPoint = RuneCraftingLocation;
        break;
      default:
        selectedPoint = CraftingLocation; // Default to crafting location if something goes wrong
    }

    move(selectedPoint.getX(), selectedPoint.getY(), 3);
  }

  public static boolean waitAfterWalking(BooleanSupplier supplier, long timeout) {
    return waitAfterWalking(supplier, timeout, 50);
  }

  public static boolean waitAfterWalking(BooleanSupplier supplier, long timeout, long checkEvery) {
    Player local = Players.getLocal();
    performActions(); // TODO Add something like this?
    if (waitCondition(local::isMoving, 1000)) {
      waitCondition(() -> !local.isMoving(), 8153); // TODO RANDOM THIS!
    }
    return waitWithABC(supplier, timeout);
  }

  public static boolean waitWithABC(BooleanSupplier supplier, long timeout) {
    performActions(); // TODO Add something like this?
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
