package net.runelite.client.plugins.kourendlibrary.tasks;


import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import static net.runelite.client.plugins.kourendlibrary.Constants.Items.ARCANE_KNOWLEDGE;
import net.runelite.client.plugins.kourendlibrary.State;

import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;

@Slf4j
public class CashInBooksTask implements Task {

  @Override
  public boolean validate()
  {
    return Inventory.isFull();
  }


  @Override
  public int execute()
  {

      State.get().setStatus("Cashing in books");

      //We dont need this bu
      //[Menu Action] O=Continue | T= | ID=0 | OP=30 | P0=1 | P1=14352385 | ITEM=-1
      //Dialog processed CHAT_OPTION_ONE
    Widget ChatBoxBookDialog = Widgets.get(219, 1, 1);
    if (!Dialog.isOpen() && ChatBoxBookDialog == null) {
      log.info("Reading book");
      Item Book = Inventory.getFirst(ARCANE_KNOWLEDGE);
      if (Book != null) { Book.interact("Read"); }
    }


      if (ChatBoxBookDialog != null) {
        if (Dialog.canContinue()) { Dialog.continueSpace(); }
        log.info("RewardMenu: " + ChatBoxBookDialog.getText());

        ChatBoxBookDialog.interact(1);
        Time.sleep(100,300);
      }

    return 1500;
  }

}
