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
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import java.util.ArrayList;
import java.util.List;

//Version: 0.2

//TO:DO
//Prospector gear
//Amulet of glory (Increase gem drops)
//Varrock Armor 1+

@Slf4j
@PluginDescriptor(
		name = "xMiner",
		description = "A simple miner",
		enabledByDefault = false,
		tags = {"skilling", "automatisation", "xhook"}
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
	public int CurrentXP, startXP = 0;

	public static List<Player> TotalPlayerList;
	public static List<Tile> playerTiles;

	public static WorldPoint TileToMine;

	public boolean PlayersDetected = false;

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

		Player local = Players.getLocal();
		if (Inventory.isFull()) {
			if (config.BankOre()) { currentTask = TASK.Bank; }
			else { currentTask = TASK.Drop; }
		}
		else if (!Bank.isOpen() && currentTask == TASK.Mine && local.getWorldX() != config.xmsettings().getWorldX() || local.getWorldX() != config.xmsettings().getWorldY()) {//&& local.getWorldLocation() == location2){
			Walker.walkTo(new WorldPoint(config.xmsettings().getWorldX(), config.xmsettings().getWorldY(), config.xmsettings().getWorldPlane()));
		}

		int pickaxeCount = Inventory.getCount(config.xmsettings().PickAxeIds);
		int freeSlots = 28 - pickaxeCount;
		if (!Bank.isOpen() && Inventory.getFreeSlots() >= freeSlots || Inventory.isEmpty()) { currentTask = TASK.Mine; }

		CurrentXP = (startXP - client.getSkillExperience(Skill.MINING));

		//Execute tasks
		xMinerTasks();
		//if (config.xmsettings().name() == "Mining guild") {
		//PlayersOnOurTile();
		//}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xMinerPlugin")) { } }

	public void xMinerTasks() {
		switch (currentTask) {
			case Mine:
				status = "Mining";
				Mine();
				break;
			case Drop:
				status = "Dropping";
				Drop();
				break;
			case Bank:
				status = "Banking";
				BankOre();
				break;
			case SmeltOre:
				status = "Smelting";
				SmeltOre();
				break;
			default:
				break;
		}
	}

	public void SmeltOre(){ }
	public void BankOre() {

		//Declare in config
		TileObject BankObj = TileObjects.getNearest(4483);
		if (BankObj != null && Inventory.isFull()) {
			if (!Bank.isOpen()) {
				BankObj.interact("Use");
			}
			else if (Bank.isOpen()) {
				int getPickAxeId = 0;
				getPickAxeId = Inventory.getFirst(config.xmsettings().PickAxeIds).getId();
				if (getPickAxeId == 0) { Bank.depositInventory(); }
				else { Bank.depositAllExcept(getPickAxeId); }
			}
		}

		int pickaxeCount = Inventory.getCount(config.xmsettings().PickAxeIds);
		int freeSlots = 28 - pickaxeCount;

		if (Bank.isOpen() && Inventory.getFreeSlots() >= freeSlots || Inventory.isEmpty()) {
			Bank.close();
		}
	}

	public void Drop() {
		if (!Tabs.isOpen(Tab.INVENTORY)) {
			Tabs.open(Tab.INVENTORY);
		}
		if (Inventory.contains(config.xmsettings().getInventoryOreId())) {
			status = "Dropping ore";
			// 1621 uncut emerald 1623 Uncut sapphire. 1619 uncut ruby 1617 uncut diamond Unidentified minerals  Miner guild only

			Inventory.getAll(config.xmsettings().getInventoryOreId(), config.xmsettings().getSecondaryOreId(), 1617, 1619, 1621, 1623 , 23442, 20358, 20359, 20361, 20363, 20365)
					.stream()
					.forEach(Item::drop);
		}
	}

	public void PlayersOnOurTile() {
		//Retrieve all Players through our Obj entity loop.
		TotalPlayerList = Players.getAll();

		if (TotalPlayerList.isEmpty()) {
			PlayersDetected = false;
			return;
		}

		playerTiles = new ArrayList<>();
		//status2 = "Playercount: " + TotalPlayerList.stream().count();

		for (Player AllPlayers : TotalPlayerList) {
			Player local = Players.getLocal();

			if(AllPlayers == local) { TotalPlayerList.remove(local); }

			Tile PlayerTile = Tiles.getAt(AllPlayers.getLocalLocation());
			playerTiles.add(PlayerTile);

			WorldPoint OurMainTile = new WorldPoint(config.xmsettings().getWorldX(), config.xmsettings().getWorldY(), config.xmsettings().getWorldPlane());
			for (Tile tile : playerTiles) {
				if (tile.getWorldLocation().distanceTo(OurMainTile) == 0) {
					PlayersDetected = true;
				}
			}
		}
		PlayersDetected = false;
	}

	public void Mine() {
		Player local = Players.getLocal();
		if (local == null) { return; }

		//Area check?
		if (config.UseSpecialAttack() && Combat.getSpecEnergy() == 100 && !Bank.isOpen()) {
			ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
			if (equipment == null)  { return; }
			Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
			if (weapon != null & weapon.getId() ==  config.xmsettings().getDragonPickAxeId() && !Combat.isSpecEnabled()) { Combat.toggleSpec(); }
		}

		if (local.getInteracting() != null && !Dialog.canContinue()) { return; }
		if (!local.isIdle() || local.isMoving()) { return; }

		//Fix ore ids for each location. It mines now all ores if it close to it...
		TileObject Ore = TileObjects.getNearest(config.xmsettings().ORE_IDS);
		if (Ore != null && (local.distanceTo(Ore.getWorldLocation()) <= 1)) {
			status = "Status: Mining " + Ore.getName();
			Ore.interact("Mine");
		}
	}
}
