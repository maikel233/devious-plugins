package net.runelite.client.plugins.kourendlibrary.tasks;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.kourendlibrary.LibraryUtils;
import net.runelite.client.plugins.kourendlibrary.State;
import net.runelite.client.plugins.kourendlibrary.library.Book;
import net.runelite.client.plugins.kourendlibrary.library.Bookcase;

import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Task;

@Slf4j
public class FindBookTask implements Task {

    @Override
    public boolean validate() {
        return !Inventory.isFull() && State.get().getCurrentAssignment().isPresent()
                && !State.get().getCurrentBooks().contains(State.get().getCurrentAssignment().get());
    }

    @Override
    public boolean isBlocking() {
        List<Bookcase> bookcases = State.get().getLibrary().getBookcases();
        log.info("Looking for book" + bookcases.size());
        return State.get().getCurrentAssignment()
                .map(LibraryUtils::findBook)
                .map(LibraryUtils::clickBookshelf)
                .orElseGet(() -> {
                    log.error(String.format("We can't find %s",
                            State.get().getCurrentAssignment().map(Book::getName).orElse("Unknown")));
                    bookcases.stream()
                            .filter(bookcase -> bookcase.getPossibleBooks().size() > 0 && bookcase.getLocation().getX() >= 1599)
                            .forEach(bookcase -> log.debug(String.format("Bookcase: %s, books: %s", bookcase, bookcase.getPossibleBooks())));
                    // force reset?
                    return false;
                });
    }

    @Override
    public int execute()
    {
        State.get().setStatus("Looking for books");

        return  100;
    }
}