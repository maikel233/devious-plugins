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
package net.runelite.client.plugins.xCrafter;

import com.google.inject.Provides;

import java.util.stream.Collectors;
import javax.inject.Inject;

import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Skill;
import static net.runelite.api.Skill.*;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.events.LoginStateChanged;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.script.blocking_events.LoginEvent;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;


@Slf4j
@PluginDescriptor(
		name = "xCrafter",
		description = "A crafting plugin to make our lifes easier!",
		enabledByDefault = false,
		tags = {"skilling", "automatisation"}
)
public class xCrafterPlugin extends Plugin {
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xCrafterConfig config;
	@Inject
	private xCrafterOverlay overlay;

	@Inject
	private xCrafterUtils utils;

	@Provides
	xCrafterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(xCrafterConfig.class);
	}

	public String status = "Initializing...";
	public String CurrentTaskStatus = "Initializing...";
	public int TotalTrips = 0, CurrentXP = 0, startXP = 0;

	public long start;

	public boolean RunOnce = false;

	enum TASK {Banking, Crafting, WalkToBank, IDLE}

	TASK currentTask = TASK.Banking;

	@Override
	protected void startUp() {
		overlayManager.add(overlay);
		CurrentXP = 0;
		start = System.currentTimeMillis();
		startXP = client.getSkillExperience(CRAFTING);
		TotalTrips = 0;
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}


	@Subscribe
	public void onGameTick(GameTick event) {
		if (client.getGameState() != GameState.LOGGED_IN) {
			RunOnce = false;
			return;
		}

		CurrentTaskStatus = currentTask.toString();
		CurrentXP = (startXP - client.getSkillExperience(CRAFTING));

		var local = Players.getLocal();

		Widget Multi_Skill_menu = Widgets.get(270, 0);
		if (Dialog.isOpen() && Multi_Skill_menu == null && !Bank.isOpen()) {
			RunOnce = false;
		}


		if (xCrafterUtils.ConfigIsFlaxSelected()) {

			if (local.getPlane() == 2 && Inventory.contains(xCrafterUtils.FlaxId)) {
				TileObject Staircase = TileObjects.getNearest(new WorldPoint(3205, 3208, 2), 16673);
				Staircase.interact("Climb-down");
			}
			if (!Bank.isOpen() && local.getPlane() == 2 && Inventory.contains(xCrafterUtils.BowStringId)) {
				currentTask = TASK.Banking;
			}
			if (local.getPlane() == 1 && Inventory.contains(xCrafterUtils.FlaxId)) {
				TileObject Door = TileObjects.getNearest(new WorldPoint(3207, 3214, 1), 1540);
				if (Door != null) {
					Door.interact("Open");
				}
				if (Door == null && local.getWorldLocation().equals(new WorldPoint(3209, 3213, 1))) {


					TileObject SpinningWheel = TileObjects.getNearest(14889);
					if (Multi_Skill_menu != null) {
						RunOnce = true;
						client.invokeMenuAction("Spin", "Bow String", 1, 57, -1, 17694736);
					} else if (!RunOnce) {
						SpinningWheel.interact("Spin");
						status = "Interacting with: " + SpinningWheel.getName();
					}

				} else {
					//Using Walker to see if there is a difference in speed compared to our walking method.
					Walker.walkTo(new WorldPoint(3209, 3213, 1));
				}

			} else if (local.getPlane() == 1 && Inventory.contains(xCrafterUtils.BowStringId)) {
				TileObject Staircase = TileObjects.getNearest(16672);
				if (local.getWorldLocation().equals(new WorldPoint(3205, 3209, 1))) {
					Staircase.interact("Climb-up");
				} else {
					//Using Walker to see if there is a difference in speed compared to our walking method.
					Walker.walkTo(new WorldPoint(3205, 3209, 1));
				}

			}
		} else if (xCrafterUtils.ConfigIsOther()) {
			if (!Bank.isOpen()) {
				if (Inventory.isEmpty() || !Inventory.contains(config.xcraftersettings().getRequiredCraftingItemsInInventory1()) || !Inventory.contains(config.xcraftersettings().getRequiredCraftingItemsInInventory2())) {
					currentTask = TASK.Banking;
				}
				if (!Inventory.contains(config.xcraftersettings().getCraftingMaterial())) {
					currentTask = TASK.Banking;
				}
				if (Inventory.isFull() && !Inventory.contains(config.xcraftersettings().getCraftingMaterial())) {
					currentTask = TASK.Banking;
				}
				if (Inventory.isFull() && Inventory.contains(config.xcraftersettings().getCraftingMaterial())) {
					currentTask = TASK.Crafting;
				}
			}
		}
		DoCraftingTasks();
	}

	public void DoCraftingTasks() {
		switch (currentTask) {
			case Banking:
				Bank();
				break;
			case Crafting:
				Crafting();
				break;
			case IDLE:
				break;
		}
	}





	public void MakeItem(String Item, int paramid) {
		client.invokeMenuAction("Make", Item, 1, 57, -1, paramid);
	}


	void Crafting() {
		status = "Crafting";

		Item Needle = Inventory.getFirst(config.xcraftersettings().getRequiredCraftingItemsInInventory1());
		Item Material = Inventory.getFirst(config.xcraftersettings().getCraftingMaterial());
		Widget Multi_Skill_menu = Widgets.get(270, 0);

		if (!RunOnce && Multi_Skill_menu == null && Needle != null && Material != null) {
			Needle.useOn(Material);
		}
		//Boots!!! more xp check lvl first!
		if (Multi_Skill_menu != null && config.xcraftersettings().getName() == "Leather") {
			int CLvl = client.getRealSkillLevel(CRAFTING);

			if (CLvl < 7) {
				MakeItem("Leather gloves", 17694734); }
			else if (CLvl == 7 || CLvl == 8) {
				//Get Right Param!!!
				MakeItem("Leather boots", 17694734); }
			else if (CLvl == 9 || CLvl == 10) {
				//Get Right Param!!!
				MakeItem("Leather cowl" , 17694734); }
			else if (CLvl >= 11 && CLvl <= 14) {
				//Get Right Param!!!
				MakeItem("Leather vambraces", 17694734);
			}
			else if (CLvl >= 14 && CLvl <= 18) {
				//Get Right Param!!!
				MakeItem("Leather body", 17694734);
			}
			else if (CLvl >= 18 && CLvl <= 38) {
				//Get Right Param!!!
				MakeItem("Leather chaps", 17694734);
			}
			else  {
				//Get Right Param!!!
				MakeItem("Coif", 17694734);
			}
			RunOnce = true;
		}
	}
	void Bank() {
		status = "Interacting with the bank";

		if (!Bank.isOpen()) {								//Varock Bank and 2x Lumbridge bank
			TileObject BankBooth = TileObjects.getNearest(10583, 18491);
			if (BankBooth != null) {
				BankBooth.interact("Bank");
			}
		} else if (Bank.isOpen()) {
			RunOnce = false;
			if (xCrafterUtils.ConfigIsFlaxSelected()) {
				if (Inventory.contains(xCrafterUtils.BowStringId)) { Bank.depositInventory(); return; }
				if (Bank.contains(xCrafterUtils.FlaxId) && Inventory.isEmpty()) { Bank.withdraw(xCrafterUtils.FlaxId, 28, Bank.WithdrawMode.ITEM); return; }
				currentTask = TASK.IDLE;
			} else if (xCrafterUtils.ConfigIsOther()) {

				if (Inventory.isFull() && Inventory.contains(config.xcraftersettings().getCraftingItem())) {
					Bank.depositAllExcept(config.xcraftersettings().getRequiredCraftingItemsInInventory1(), config.xcraftersettings().getRequiredCraftingItemsInInventory2());
					return;
				}
				if (!Inventory.contains(config.xcraftersettings().getRequiredCraftingItemsInInventory1()) && Bank.contains(config.xcraftersettings().getRequiredCraftingItemsInInventory1())) {
					Bank.withdraw(config.xcraftersettings().getRequiredCraftingItemsInInventory1(), 100, Bank.WithdrawMode.ITEM);
					return;
				}
				if (!Inventory.contains(config.xcraftersettings().getRequiredCraftingItemsInInventory2()) && Bank.contains(config.xcraftersettings().getRequiredCraftingItemsInInventory2())) {
					Bank.withdraw(config.xcraftersettings().getRequiredCraftingItemsInInventory2(), 100, Bank.WithdrawMode.ITEM);
					return;
				}
				if (!Inventory.contains(config.xcraftersettings().getCraftingMaterial()) && Bank.contains(config.xcraftersettings().getCraftingMaterial())) {
					Bank.withdraw(config.xcraftersettings().getCraftingMaterial(), 26, Bank.WithdrawMode.ITEM);
					return;
				}
				if (!Inventory.isFull()) {
					Bank.withdraw(config.xcraftersettings().getCraftingMaterial(), 26, Bank.WithdrawMode.ITEM);
					return;
				}
			}
		}
			 Bank.close();
		}


	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xCrafterPlugin")) { } }
}

