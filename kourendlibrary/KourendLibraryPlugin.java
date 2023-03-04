/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.kourendlibrary;

import com.google.inject.Provides;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.kourendlibrary.library.Book;
import net.runelite.client.plugins.kourendlibrary.library.Library;
import net.runelite.client.plugins.kourendlibrary.tasks.CashInBooksTask;
import net.runelite.client.plugins.kourendlibrary.tasks.DeliverBookTask;
import net.runelite.client.plugins.kourendlibrary.tasks.FindBookTask;
import net.runelite.client.plugins.kourendlibrary.tasks.GetNewAssignmentTask;
import net.runelite.client.plugins.kourendlibrary.tasks.SolveTask;

import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;

import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.plugins.TaskPlugin;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;

//TODO sometimes when it is at the MIDDLE BOTTOM Base floor. It wants to walk to the middle and  and finds books simultaneous
//TODO Resets doesn't do anything at the moment
//TODO Sometime it get stucks in  GetNewAssigmentTask it keeps spamming action "HELP". For now we could just send it the staircase this fixes the issue?
@PluginDescriptor(
		name = "Kourend Library",
		description = "Show where the books are found in the Kourend Library",
		tags = {"arceuus", "magic", "runecrafting", "overlay", "panel"}
)
@Slf4j
public class KourendLibraryPlugin extends TaskPlugin
{
	private static final Pattern NPC_BOOK_EXTRACTOR = Pattern.compile("'<col=0000ff>(.*)</col>'");
	private static final Pattern BOOKCASE_BOOK_EXTRACTOR = Pattern.compile("<col=00007f>(.*)</col>");
	private static final Pattern TAG_MATCHER = Pattern.compile("(<[^>]*>)");

	static final int REGION = 6459;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	private Library library;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KourendLibraryOverlay overlay;

	@Inject
	private KourendLibraryTileOverlay TileOverlay;

	@Inject
	private KourendLibraryConfig config;

	@Inject
	private ItemManager itemManager;

	public int InvSlots;

	public long startTime;

	public Integer startXpMagic, startXpRuneCrafting;
	@Provides
	KourendLibraryConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(KourendLibraryConfig.class);
	}


	private final Task[] tasks = new Task[]
			{
					new SolveTask(),
					new GetNewAssignmentTask(),
					new FindBookTask(),
					new DeliverBookTask(),
					new CashInBooksTask()
			};

	@Override
	protected void startUp() throws Exception
	{
		startTime = System.currentTimeMillis();
		startXpMagic = Skills.getExperience(Skill.MAGIC);
		startXpRuneCrafting = Skills.getExperience(Skill.RUNECRAFT);
		overlayManager.add(overlay);
		overlayManager.add(TileOverlay);
		InvSlots = Inventory.getAll().size();
		State.setCurrentBooksFromInventory(Inventory.getAll());
	}

	public Task[] getTasks()
	{
		return tasks;
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(overlay);
		overlayManager.remove(TileOverlay);
	}


	//Add to Util file.
	public static boolean isLookingTowards(Player animableEntity, WorldPoint positionable, int maxDist)
	{
		if (maxDist > 0 && positionable.getWorldLocation().distanceTo(animableEntity) > maxDist) return false;

		final int orientation = (int) Math.round(animableEntity.getOrientation() / 256.0);
		final int dx = animableEntity.getWorldLocation().getX() - positionable.getWorldLocation().getX();
		final int dy = animableEntity.getWorldLocation().getY() - positionable.getWorldLocation().getY();
		switch (orientation)
		{
			case 0: //south
				return dx == 0 && dy > 0;
			case 1: //south - west
				return dx > 0 && dy > 0 && dx == dy;
			case 2: //west
				return dx > 0 && dy == 0;
			case 3: //north - west
				return dx > 0 && dy < 0 && Math.abs(dx) == Math.abs(dy);
			case 4: //north
				return dx == 0 && dy < 0;
			case 5: //north - east
				return dx < 0 && dy < 0 && dx == dy;
			case 6: //east
				return dx < 0 && dy == 0;
			case 7: //south-east
				return dx < 0 && dy > 0 && Math.abs(dx) == Math.abs(dy);
		}
		return false;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (event.getMessage().equals("You don't find anything useful here."))
			{
				Player local = Players.getLocal();
				State.get().getLastBookcaseTile().ifPresent(tile -> {
						if (isLookingTowards(local, tile, 1)) {
					State.get().getLibrary().mark(tile, null);
					State.get().setLastBookcaseTile(null);
						} else {
						log.info("Misclicked bookcase");
						State.get().setLastBookcaseTile(null);
						}
				});
			}
		}
	}


	@Subscribe
	public void onGameTick(GameTick tick)
	{
		boolean inRegion = client.getLocalPlayer().getWorldLocation().getRegionID() == REGION;

		if (!inRegion) { return; }

		if (Dialog.isOpen()) {
			BookFoundMessage();
			FindNextbookMessage();
		}
		if (Dialog.canContinue()) { Dialog.continueSpace(); }

		if (InvSlots != Inventory.getAll().size()) {
			log.info("setCurrentBooks!");
			InvSlots = Inventory.getAll().size();
			State.setCurrentBooksFromInventory(Inventory.getAll());
		}

		if (!Movement.isRunEnabled() && Movement.getRunEnergy() > 30) {
			Movement.toggleRun();
		}

		for (Task task : getTasks())
		{

		}
	}

	private Optional<Book> getBookFromMatcher(Matcher m) {
		String bookName = TAG_MATCHER.matcher(m.group(1).replace("<br>", " ")).replaceAll("");
		Book book = Book.byName(bookName);

		if (book == null) {
			log.error(String.format("Book %s is not recognised", bookName));
		}
		return Optional.ofNullable(book);
	}

	private void BookFoundMessage() {
		String message = null;
		Widget FoundABook = Widgets.get(193, 2);
		if (FoundABook == null) { return; }
		if (FoundABook != null) { if (FoundABook.isVisible()) { message = FoundABook.getText(); } }

		log.debug(String.format("Received message: '%s'", message));
		if (message.startsWith("You find:")) {
			Matcher m = BOOKCASE_BOOK_EXTRACTOR.matcher(message);
			System.out.println(message);
			if (m.find()) {
				getBookFromMatcher(m).ifPresent(book -> {
					State.get().getLastBookcaseTile().ifPresent(tile -> State.get().getLibrary().mark(tile.getWorldLocation(), book));
					State.get().setLastBookcaseTile(null);
				});
			}
		}
	}

	private void FindNextbookMessage() {
		String message = Dialog.getText();
		if (message == null) { return; }

		if (message.startsWith("Thanks, human") || message.startsWith("Thank you very much")) {
			State.get().swapProfessors();
			State.get().setCurrentAssignment(null);
			Statistics.get().incrementBooksGained();
			return;
		}

		if (message.startsWith("I believe you are currently")
				|| message.startsWith("Thanks for finding my book")
				|| message.startsWith("Thank you for finding my book")
				|| message.startsWith("Aren't you helping someone")) {
			State.get().swapProfessors();
			return;
		}

		Matcher m = NPC_BOOK_EXTRACTOR.matcher(message);

		if (m.find()) {
			getBookFromMatcher(m).ifPresent(book -> {
				log.info(String.format("Next book: %s", book.getName()));
				State.get().setCurrentAssignment(book);
				if (Inventory.contains(book.getName())) { log.info("We have that book!"); }
			});
			return;
		}
		log.debug(String.format("Not recognised: '%s'", message));
	}
}