/*
 * Copyright (c) 2017, Robin Weymans <Robin.weymans@gmail.com>
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
package net.runelite.client.plugins.xTutorialIsland;

import com.google.inject.Provides;

import javax.inject.Inject;
import javax.swing.*;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;

import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.Spell;
import net.unethicalite.api.magic.SpellBook;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.packets.DialogPackets;
import net.unethicalite.api.packets.Packets;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;


import java.awt.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static net.runelite.client.plugins.xTutorialIsland.xTutorialIslandState.*;

//Bugs rat pit stuck after killing rat, equipment view

@Slf4j
@PluginDescriptor(
		name = "xTutorialIsland",
		description = "Easy accounts",
		enabledByDefault = false,
		tags = {"skilling", "automatisation"}
)
public class xTutorialIslandPlugin extends Plugin {
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xTutorialIslandConfig config;
	@Inject
	private xTutorialIslandOverlay overlay;

	@Provides
	xTutorialIslandConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(xTutorialIslandConfig.class);
	}

	xTutorialIslandState statusv5;

	private static final WidgetInfo[] BANK_PIN_NUMBERS =
			{
					WidgetInfo.BANK_PIN_10,
					WidgetInfo.BANK_PIN_1,
					WidgetInfo.BANK_PIN_2,
					WidgetInfo.BANK_PIN_3,
					WidgetInfo.BANK_PIN_4,
					WidgetInfo.BANK_PIN_5,
					WidgetInfo.BANK_PIN_6,
					WidgetInfo.BANK_PIN_7,
					WidgetInfo.BANK_PIN_8,
					WidgetInfo.BANK_PIN_9,
			};

	public String status = "Initializing...";
	public String status2 = ".";
	public String status3 = ".";
	public String status4 = ".";
	public long start; //


	public int Shrimps = 2514, FishingNet = 303, LogsId = 2511, TinderboxId = 590, BronzeAxeId = 1351, CookedShrimp = 315;
	public int PotOfFlourId = 2516, WaterBucketId = 1929, Bread_DoughId = 2307, BreadId = 2309;
	public int PickAxe = 1265, TinOreId = 438, CopperOreId = 436, CopperBarId = 2349, HammerId = 2347;
	public int BronzeDagger = 1205, BronzeSword = 1277, WoodenShield = 1171, Arrows = 882, Bow = 841;
	int tutorialSectionProgress;
	int ironmanProgress;

	int varbitValue;

	Random random = new Random();

	@Override
	protected void startUp() {
		start = System.currentTimeMillis();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		//Widget 162 33 TimePlayed

		status4 = "1: " + Dialog.canContinueTutIsland() + "2: " + Dialog.canContinueTutIsland2() + "3: " + Dialog.canContinueTutIsland3();
		status = Dialog.getText();

		statusv5 = checkPlayerStatus();
		switch (statusv5) {
			case ANIMATING:
			case NULL_PLAYER:
			case TICK_TIMER:
				break;
			case MOVING:
				shouldRun();
				break;
		}

	}


	private void shouldRun() {
		if (!Movement.isRunEnabled() && Movement.getRunEnergy() > 15) {
				Movement.toggleRun();
			}
	}

	private void answerExperienceQuestions()
	{
		for(Widget option : client.getWidget(219,1).getChildren()){
			if(option.getText().contains("experienced")){
				//pressOption(option.getIndex());
				status4 = "Pressing Option:" + option.getIndex();
				Dialog.chooseOption(3);
			}
		}
	}

	public WidgetItem createWidgetItem(Widget item) {
		boolean isDragged = item.isWidgetItemDragged(item.getItemId());

		int dragOffsetX = 0;
		int dragOffsetY = 0;

		if (isDragged) {
			Point p = item.getWidgetItemDragOffsets().getAwtPoint();
			dragOffsetX = (int) p.getX();
			dragOffsetY = (int) p.getY();
		}
		// set bounds to same size as default inventory
		Rectangle bounds = item.getBounds();
		bounds.setBounds(bounds.x - 1, bounds.y - 1, 32, 32);
		Rectangle dragBounds = item.getBounds();
		dragBounds.setBounds(bounds.x + dragOffsetX, bounds.y + dragOffsetY, 32, 32);

		return new WidgetItem(item.getItemId(), item.getItemQuantity(), item.getIndex(), bounds, item, dragBounds);
	}

	public Collection<WidgetItem> getInventoryWidgetItems() {
		Widget inventoryWidget = Bank.isOpen() ? client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER) : client.getWidget(WidgetInfo.INVENTORY);

		if (inventoryWidget == null) {
			return new ArrayList<>();
		}

		Widget[] children = inventoryWidget.getDynamicChildren();

		if (children == null) {
			return new ArrayList<>();
		}

		Collection<WidgetItem> widgetItems = new ArrayList<>();
		for (Widget item : children) {
			if (item.getItemId() != 6512) {
				widgetItems.add(createWidgetItem(item));
			}
		}
		return widgetItems;
	}

	public WidgetItem getInventoryWidgetItem(int id)
	{
		Widget inventoryWidget = Bank.isOpen() ? client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER) : client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = getInventoryWidgetItems();
			for (WidgetItem item : items)
			{
				if (item.getId() == id)
				{
					return item;
				}
			}
		}
		return null;
	}

	public WidgetItem getInventoryWidgetItem(Collection<Integer> ids)
	{
		Widget inventoryWidget = Bank.isOpen() ? client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER) : client.getWidget(WidgetInfo.INVENTORY);
		if (inventoryWidget != null)
		{
			Collection<WidgetItem> items = getInventoryWidgetItems();
			for (WidgetItem item : items)
			{
				if (ids.contains(item.getId()))
				{
					return item;
				}
			}
		}
		return null;
	}


	private void equipItem(int id) {
		Item ItemsWeHaveToWear = Inventory.getFirst(x -> x.hasAction("Wield")
				&& (x.getId() == id));
		if (ItemsWeHaveToWear != null) { ItemsWeHaveToWear.interact("Wield"); }
	}
	private void changeLook(int id) {
		client.invokeMenuAction("", "", 1, 57, -1, id);
	}

	private void changeLookClient(int id) {
		client.invokeMenuAction("", "", 1, 57, -1, id);
	}

	private void ChangePlayerLook() {
		changeLookClient(44498957); // Head
		Time.sleep(1);
		changeLookClient(44498977); // Legs
		Time.sleep(1);
		changeLookClient(44498996); //Legs color
		Time.sleep(1);
		changeLookClient(44498965); // Torso
		Time.sleep(1);
		changeLookClient(44498992); // Torso Color
		Time.sleep(1);
		changeLookClient(44498988); // HeadColor
		Time.sleep(1);
		changeLookClient(44499004); // SkinColor
		Time.sleep(2);
		changeLookClient(44499012);
	}

	private void openTab(int param1) {
		client.invokeMenuAction("", "", 1, 57, -1, param1);
	}

	private void selectName() {
		client.invokeMenuAction("", "", 1, 57, -1, 36569104);
	}
	private void setName() {
		client.invokeMenuAction("", "", 1, 57, -1, 36569107);
	}
	private void selectIron() {
		client.invokeMenuAction("", "", 1, 57, -1, 14090249);
	}

	private void selectHardcore() {
		client.invokeMenuAction("", "", 1, 57, -1, 14090250);
	}

	private static void clickNumber(Client client, int number)
	{
		for (WidgetInfo widgetInfo : BANK_PIN_NUMBERS)
		{
			Widget numberBox = Widgets.get(widgetInfo);
			if (!Widgets.isVisible(numberBox))
			{
				continue;
			}

			if (numberBox.getChildren() == null || numberBox.getChildren().length < 2)
			{
				continue;
			}

			if (numberBox.getChild(1).getText().equals(String.valueOf(number)))
			{
				client.invokeWidgetAction(1, numberBox.getChild(0).getId(), 0, -1, "Select");
				break;
			}
		}
	}

	private void smithDagger() {
		client.invokeMenuAction("", "", 1, 57, -1, 20447241);
	}

	public void OpenNearbyDoor(int id) {
		TileObject Door = TileObjects.getNearest(id);
		Door.interact("Open");
	}

	private xTutorialIslandState checkPlayerStatus()
	{
		Player player = client.getLocalPlayer();
		if(player==null){
			return NULL_PLAYER;
		}

		if(player.getPoseAnimation()!=813 && player.getPoseAnimation()!=5160 && player.getPoseAnimation()!=808){
			return xTutorialIslandState.MOVING;
		}
		if (player.isAnimating()) {
			return ANIMATING;
		}

		return getRegularState();
	}

	private xTutorialIslandState getRegularState() {
		Player local = Players.getLocal();
		NPC SurvivalExpert = NPCs.getNearest(8503);
		NPC QuestGuide = NPCs.getNearest(3312);
		NPC Mining_Instructor = NPCs.getNearest(3311);
		NPC Combat_Instructor = NPCs.getNearest(3307);
		NPC Account_Guide = NPCs.getNearest(3310);
		NPC Brother_Brace = NPCs.getNearest(3319);
		NPC Magic_Instructor = NPCs.getNearest(3309);
		NPC Rat = NPCs.getNearest(3313);
		varbitValue = client.getVarpValue(281);
		status3 = "Progress: " + varbitValue;
		switch (varbitValue) {
			case 1:
				switch (tutorialSectionProgress) {
					case 0:
						if (client.getWidget(558, 13) != null && !client.getWidget(558, 13).isHidden()) {
							if (client.getWidget(558, 13).getText().contains("whether")) {
								Widget TextBox = client.getWidget(558, 9);
								Mouse.click(TextBox.getRelativeX(), TextBox.getRelativeY(), true);
								int idx = new Random().nextInt(xTutorialIslandNames.Usernames.length);
								String RandomString = (xTutorialIslandNames.Usernames[idx]);
								String RandomUserNameWithNumbers = RandomString + random.nextInt(30);
								Dialog.enterText(RandomUserNameWithNumbers);
								Widget Button = client.getWidget(558, 18);
								Mouse.click(Button.getRelativeX(), Button.getRelativeY(), true);
								Time.sleep(1000);
								break;
							} else if (client.getWidget(558, 13).getText().contains("not available")) {
								selectName();
								break;
							} else if (client.getWidget(558, 13).getText().contains("may set this")) {
								setName();
								break;
							}
						} else if (config.female()) {
							changeLookClient(44499010);
							ChangePlayerLook();
							tutorialSectionProgress++;
							break;
						} else if (!config.female()) {
							ChangePlayerLook();
							tutorialSectionProgress++;
							break;
						}
						break;
					/*case 1:
					case 2:
						changeLook(44498956);
						if(utils.getRandomIntBetweenRange(0,2)==0){
							tutorialSectionProgress++;
						}
						break;
					case 3:
						changeLook(44498991);
						if(utils.getRandomIntBetweenRange(0,2)==0){
							tutorialSectionProgress++;
						}
						tutorialSectionProgress++;
						break;
					case 4:
					case 5:
						changeLook(44498995);
						if(utils.getRandomIntBetweenRange(0,2)==0){
							tutorialSectionProgress++;
						}
						tutorialSectionProgress++;
						break;
					*/
					case 6:
						changeLook(44499012);
						tutorialSectionProgress++;
						break;
				}
				break;
			case 2:
				if (client.getWidget(219, 1) != null && client.getWidget(219, 1).getChild(0).getText().contains("your experience")) {
					answerExperienceQuestions();
					break;
				} else {
					switch (tutorialSectionProgress) {
						case 0:
							NPC EntryNpc = NPCs.getNearest(3308);
							EntryNpc.interact("Talk-to");
							tutorialSectionProgress++;
							break;
						case 1:
							Dialog.continueSpace();
							break;
					}
				}
				break;
			case 3:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						openTab(10747944);
						break;
				}
				break;
			case 7:
				if (client.getWidget(548, 3) != null && !client.getWidget(548, 3).isHidden()) {
					openTab(10747951);
					break;
				}
				switch (tutorialSectionProgress) {
					case 0:
						NPC EntryNpc = NPCs.getNearest(3308);
						EntryNpc.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 10:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
					//	Walker.walkTo(SurvivalExpert.getWorldLocation());
						OpenNearbyDoor(9398);
						break;
				}
				break;
			case 20:
				SurvivalExpert.interact("Talk-to");
				break;
			case 30:
				switch (tutorialSectionProgress) {
					case 0:
					case 1:
					case 2:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 3:
						openTab(10747958);
						break;
				}
				break;
			case 40:
				NPC Fishing_Spot = NPCs.getNearest(3317);
				if (Inventory.contains(FishingNet)) { Fishing_Spot.interact("Net"); }
				break;
			case 50:
				openTab(10747956);
				break;
			case 60:
				switch (tutorialSectionProgress) {
					case 0:
						SurvivalExpert.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 70:
				switch (tutorialSectionProgress) {
					case 0:
					case 1:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 2:
						TileObject Tree = TileObjects.getNearest(9730);
						Tree.interact("Chop down");
						break;
				}
				break;
			case 80:
					Item Tinderbox = Inventory.getFirst(TinderboxId);
					Item Log = Inventory.getFirst(2511);
					if (Log != null && Tinderbox != null) {
						Tinderbox.useOn(Log);
					}
				break;
			case 90:
				TileObject Fire = TileObjects.getNearest(26185);
				if (Fire != null) {
					Item Shrimp = Inventory.getFirst(Shrimps);
					Shrimp.useOn(Fire);
				}

				break;
			case 120:
				OpenNearbyDoor(9470);
				break;
			case 130:
				OpenNearbyDoor(9709);
				break;
			case 140:
				switch (tutorialSectionProgress) {
					case 0:
						NPC MasterChef = NPCs.getNearest(3305);
						MasterChef.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 150:
				if (Inventory.contains(PotOfFlourId) && Inventory.contains(WaterBucketId)) {
					Item PotOfFlour = Inventory.getFirst(PotOfFlourId);
					Item WaterbucketId = Inventory.getFirst(WaterBucketId);
					WaterbucketId.useOn(PotOfFlour);
				}
				break;
			case 160:
				if (Inventory.contains(Bread_DoughId)) {
					Item BreadDough = Inventory.getFirst(Bread_DoughId);
					TileObject CookingRange = TileObjects.getNearest(9736);
					BreadDough.useOn(CookingRange);
				}
				break;
			case 170:
				OpenNearbyDoor(9710);
				break;
			case 200:
				OpenNearbyDoor(9716);
				break;
			case 220:
				QuestGuide.interact("Talk-to");
				break;
			case 230:
				switch (tutorialSectionProgress) {
					case 0:
						//	utils.pressKey(KeyEvent.VK_SPACE);
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						//Dialog.invokeDialog(10747957, 57);
						openTab(10747957);
						break;
				}
				break;
			case 240:
				switch (tutorialSectionProgress) {
					case 0:
						QuestGuide.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						if (client.getWidget(162, 53) != null && !client.getWidget(162, 53).isHidden()) {
							Dialog.continueSpace();
						} else {
							tutorialSectionProgress = 0;
						}
						break;
				}
				break;
			case 250:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						TileObject Ladder = TileObjects.getNearest(9726);
						Ladder.interact("Climb-down");
						break;
				}
				break;
			case 260:
				switch (tutorialSectionProgress) {
					case 0:
						Tile MiningInstructorTile = Tiles.getAt(3081,9506,0);
						MiningInstructorTile.walkHere();
						tutorialSectionProgress++;
						break;
					case 1:
						Mining_Instructor.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 2:
						Dialog.continueSpace();
						break;
				}
				break;
			case 270:
				Dialog.continueSpace();
				break;
			case 300:
				TileObject TinVein = TileObjects.getNearest(10080);
				if (TinVein != null) { TinVein.interact("Mine"); }
				break;
			case 310:
				TileObject CopperVein = TileObjects.getNearest(10079);
				if (CopperVein != null) { CopperVein.interact("Mine"); }
				break;
			case 320:
				TileObject Furnace = TileObjects.getNearest(10082);
				if (Furnace != null) { Furnace.interact("Use"); }
				break;
			case 330:
				switch (tutorialSectionProgress) {
					case 0:
						Mining_Instructor.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 340:
				TileObject Anvil = TileObjects.getNearest(2097);
				Item BronzeBar = Inventory.getFirst(CopperBarId);
				BronzeBar.useOn(Anvil);
				break;
			case 350:
				smithDagger();
				break;
			case 360:
				//Walker.walkTo(new WorldPoint(3106, 9508, 0));
				OpenNearbyDoor(9718);
				break;
			case 370:
				switch (tutorialSectionProgress) {
					case 0:
						Combat_Instructor.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 390:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						openTab(10747959);
						break;
				}
				break;
			case 400:
				openTab(25362433);
				break;
			case 405:
				switch (tutorialSectionProgress) {
					case 0:
						DialogPackets.closeInterface();
						tutorialSectionProgress++;
						break;
					case 1:
						equipItem(BronzeDagger);
						break;
				}
				break;
			case 410:
				Combat_Instructor.interact("Talk-to");
				break;
			case 420:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						equipItem(BronzeSword);
						tutorialSectionProgress++;
						break;
					case 2:
						equipItem(WoodenShield);
						break;
				}
				break;
			case 430:
					openTab(10747955);
				break;
			case 440:
				OpenNearbyDoor(9719);
				break;
			case 450:
				if (Rat != null) { Rat.interact("Attack"); }
				if (Rat.isDead()) { Walker.walkTo(Combat_Instructor.getWorldLocation()); }
				break;
			case 470:
				//Use walker?
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						OpenNearbyDoor(9719);
						tutorialSectionProgress++;
						break;
					case 2:
						Combat_Instructor.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 3:
						Dialog.continueSpace();
						break;
				}
				break;
			case 480:
				switch (tutorialSectionProgress) {
					case 0:
								equipItem(841);
						tutorialSectionProgress++;
						break;
					case 1:
								equipItem(882);
;

						tutorialSectionProgress++;
						break;
					case 2:
						if (Rat != null) { Rat.interact("Attack"); }
						break;
				}
				break;
			case 490:
				if (local.isIdle()) {
					Tile WalkTile = Tiles.getAt(new WorldPoint(3110,9514, 0));
					if (local.getWorldLocation().distanceTo(WalkTile.getWorldLocation()) > 8) { WalkTile.walkHere(); }
					if (Rat != null) { Rat.interact("Attack"); }
					break;
				}
			case 500:
				//interactObject(9727,3);
				TileObject Ladder = TileObjects.getNearest(9727);
				Ladder.interact("Climb-up");
				break;
			case 510:
				TileObject Bank_booth = TileObjects.getNearest(10083);
				Bank_booth.interact("Use");
				break;
			case 520:
				switch (tutorialSectionProgress) {
					case 0:
						DialogPackets.closeInterface();
						//	closeInterface(786434);
						tutorialSectionProgress++;
						break;
					case 1:
						TileObject Votingbooth = TileObjects.getNearest(26815);
						Votingbooth.interact("Use");
						tutorialSectionProgress++;
						break;
					case 2:
						Dialog.continueSpace();
						break;
				}
				break;
			case 525:
				switch (tutorialSectionProgress) {
					case 0:
						DialogPackets.closeInterface();
						//	closeInterface(22609922);
						tutorialSectionProgress++;
						break;
					case 1:
						OpenNearbyDoor(9721);
						break;
				}
				break;
			case 530:
			case 532:
				switch (tutorialSectionProgress) {
					case 0:
						Account_Guide.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 531:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						openTab(10747942);
						break;
				}
				break;
			case 540:
				//Test
				//Walker.walkTo(new WorldPoint(3124, 3106, 0));

				OpenNearbyDoor(9722);
				break;
			case 550:
				switch (tutorialSectionProgress) {
					case 0:
						Tile BrotherBraceTile = Tiles.getAt(new WorldPoint(3128, 3106, 0));
						BrotherBraceTile.walkHere();
						tutorialSectionProgress++;
						break;
					case 1:
						Brother_Brace.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 2:
						Dialog.continueSpace();
						break;
				}
				break;
			case 560:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
							openTab(10747960);
						break;
				}
				break;
			case 570:
			case 600:
				switch (tutorialSectionProgress) {
					case 0:
						Brother_Brace.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 580:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						//Dialog.invokeDialog(10747943, 57);
						openTab(10747943);
						break;
				}
				break;
			case 610:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						//Walker.walkTo(new WorldPoint(3141, 3090, 0));
						OpenNearbyDoor(9723);
						break;
				}
				break;
			case 620:
				if (config.type().equals(xTutorialIslandType.REGULAR)) {
					switch (tutorialSectionProgress) {
						case 0:

							Tile WizzardTile = Tiles.getAt(new WorldPoint(3141,3090,0));
							WizzardTile.walkHere();
							tutorialSectionProgress++;
							break;
						case 1:
							Magic_Instructor.interact("Talk-to");
							tutorialSectionProgress++;
							break;
						case 2:
							Dialog.continueSpace();
							break;
					}
				} else {
					switch (tutorialSectionProgress) {
						case 0:
							Tile unkTile = Tiles.getAt(new WorldPoint(3131, 3087, 0));
							unkTile.walkHere();
							tutorialSectionProgress++;
							break;
						case 1:
							NPC Iron_Man_tutor = NPCs.getNearest(7941);
							Iron_Man_tutor.interact("Talk-to");
							tutorialSectionProgress++;
							ironmanProgress = 0;
							break;
						case 2:
							makeIronman();
							break;
						case 3:
						case 4:
							Magic_Instructor.interact("Talk-to");
							tutorialSectionProgress++;
							break;
						case 5:
							Dialog.continueSpace();
							break;
					}
				}
				break;
			case 630:
				//Dialog.invokeDialog(10747961, 57);
					openTab(10747961);
				break;
			case 640:
				switch (tutorialSectionProgress) {
					case 0:
						Magic_Instructor.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
						Dialog.continueSpace();
						break;
				}
				break;
			case 650:
				switch (tutorialSectionProgress) {
					case 0:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 1:
						//Improve walk to gate, dist check cast?
						NPC Chicken = NPCs.getNearest(3316);
						Spell WindWave = SpellBook.Standard.WIND_STRIKE;
						Magic.cast(WindWave, Chicken);
						break;
				}
				break;
			case 670:
				switch (tutorialSectionProgress) {
					case 0:
						Magic_Instructor.interact("Talk-to");
						tutorialSectionProgress++;
						break;
					case 1:
					case 3:
						Dialog.continueSpace();
						tutorialSectionProgress++;
						break;
					case 2:
						Dialog.chooseOption(1);
						tutorialSectionProgress++;
						break;
					case 4:
						if (client.getWidget(219, 1) != null && client.getWidget(219, 1).getChild(1).getText().contains("Iron")) {
							Dialog.chooseOption(3);
							break;
						}
						Dialog.continueSpace();
						break;
				}
				break;
			case 1000:
				client.setMouseIdleTicks(Integer.MAX_VALUE);
				client.setKeyboardIdleTicks(Integer.MAX_VALUE);
				SwingUtilities.invokeLater(() -> Plugins.stopPlugin(this));
				break;
			default:
			return xTutorialIslandState.UNKNOWN;
		}

		return xTutorialIslandState.WORKING;
	}

	@Subscribe
	private void onVarbitChanged(VarbitChanged event)
	{
		if(event.getIndex()==281){
			tutorialSectionProgress=0;
		}
	}

	private void makeIronman()
	{
		log.info(String.valueOf(ironmanProgress));
		switch(ironmanProgress){
			default:
				Dialog.continueSpace();
				ironmanProgress++;
				break;
			case 1:
			case 17:
			case 21:
				if(client.getWidget(219,1)!=null && !client.getWidget(219,1).isHidden()){
					Dialog.chooseOption(3);
					ironmanProgress++;
				}
				break;
			case 19:
				switch(config.type()){
					case IRONMAN:
								selectIron();
						ironmanProgress++;
						break;
					case HARDCORE_IRONMAN:
							selectHardcore();
						ironmanProgress++;
						break;
				}
				break;
			case 26:
			case 22:
				clickNumber(client, (config.bankPin().charAt(0)));
				ironmanProgress++;
				break;
			case 27:
			case 23:
				clickNumber(client, (config.bankPin().charAt(1)));
				ironmanProgress++;
				break;
			case 28:
			case 24:
				clickNumber(client, (config.bankPin().charAt(2)));
				ironmanProgress++;
				break;
			case 29:
			case 25:
				clickNumber(client, (config.bankPin().charAt(3)));
				ironmanProgress++;
				break;
			case 30:
				tutorialSectionProgress++;
				break;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
			return;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xTutorialIslandPlugin")) { } }
}
