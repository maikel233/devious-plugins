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
package net.runelite.client.plugins.xMiner;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;

//Version: 0.1

//TO:DO
//Iron ore
//Prospector gear
//Amulet of glory (Increase gem drops)
//Dragon pickaxe
//Varrock Armor 1+
//More locations
//Mining guild? once i reach the correct lvl

@Slf4j
@PluginDescriptor(
	name = "xPminer",
	description = "A simple powerminer",
	tags = {"skilling"}
)
public class xMinerPlugin extends Plugin {
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xMinerConfig config;
	@Inject
	private xMinerOverlay xOverlay;

	public static String status = "Initializing...";

	public long start;
	public int CurrentXP, startXP = 0;;

	enum TASK {Mine, Drop, Bank, SmeltOre}

	TASK currentTask = TASK.Mine;

	@Provides
	xMinerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(xMinerConfig.class);
	}

	@Override
	protected void startUp() {
		overlayManager.add(xOverlay);
		start = System.currentTimeMillis();
		startXP = client.getSkillExperience(Skill.MINING);
	}

	@Override
	protected void shutDown() throws Exception { overlayManager.remove(xOverlay); }

	@Subscribe
	public void onGameTick(GameTick event) {
		if (client.getGameState() != GameState.LOGGED_IN) { return; }

		if (client.getRealSkillLevel(Skill.MINING) < config.xmsettings().getMinimalRequiredMiningLvl()) {
			status = "A mining lvl of:" + config.xmsettings().getMinimalRequiredMiningLvl() + " is required for this location!";
			return;
		}

		if (Inventory.isFull()) {
			if (config.BankOre()) { currentTask = TASK.Bank; }
			else { currentTask = TASK.Drop; }
		}

		//Execute tasks
		xPminerTasks();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xPminerPlugin")) { } }

	public void xPminerTasks() {
		switch (currentTask) {
			case Mine:
				Mine();
				break;
			case Drop:
				Drop();
				break;
			case Bank:
				BankOre();
				break;
			case SmeltOre:
				SmeltOre();
				break;
			default:
				break;
		}
	}

	public void SmeltOre(){ }
	public void BankOre(){ }

	public void Drop(){
				if (!Tabs.isOpen(Tab.INVENTORY))  { Tabs.open(Tab.INVENTORY); }
				if (Inventory.contains(config.xmsettings().getInventoryOreId())) {
					status = "Dropping ore";
					// 1621 uncut emerald 1623 Uncut sapphire. 1619 uncut ruby 1617 uncut diamond Unidentified minerals  Miner guild only
					Inventory.getAll(config.xmsettings().getInventoryOreId(), 1617, 1619, 1621, 1623)
							.stream()
							.forEach(Item::drop);
				}
			if (Inventory.isEmpty()) { currentTask = TASK.Mine; }
	}

	public void Mine() {
		status = "Status: Mining";
		Player local = Players.getLocal();
		if (local == null) { return; }

		if (local.getWorldLocation().getRegionX() != config.xmsettings().getWorldX() && local.getWorldLocation().getRegionX() != config.xmsettings().getWorldY()) {
			Walker.walkTo(new WorldPoint(config.xmsettings().getWorldX(), config.xmsettings().getWorldY(), config.xmsettings().getWorldPlane()));
		}

		if (local.getInteracting() != null && !Dialog.canContinue()) { return; }
		if (local.isAnimating() && local.isMoving()) { return; }

		TileObject Ore = TileObjects.getNearest(11364, 11365);
		if (Ore != null && (local.distanceTo(Ore.getWorldLocation()) <= 1)) {
			status = "Status: Mining " + Ore.getName();
			Ore.interact("Mine");
		}
	}
}
