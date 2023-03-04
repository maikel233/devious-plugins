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
package net.runelite.client.plugins.xRunecrafting;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
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
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;

@Slf4j
@PluginDescriptor(
		name = "xRunecrafting",
		description = "A runecrafting plugin to make our lifes easier!",
		enabledByDefault = false,
		tags = {"skilling", "automatisation", "xhook"}
)
public class xRunecraftingPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xRunecraftingConfig config;
	@Inject
	private xRunecraftingOverlay overlay;

	@Inject
	private xRunecraftingUtils utils;

	@Provides
	xRunecraftingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(xRunecraftingConfig.class);
	}

	public String status = "Initializing...";
	public String CurrentTaskStatus = "Initializing...";
	public int TotalTrips = 0, CurrentXP = 0, startXP = 0;

	public long start;
	enum TASK {Banking, Craft, WalkToAltar, WalkToBank, IDLE}
	public TASK currentTask = TASK.Banking;

	boolean WeAlreadyTeleported = false;


	@Override
	protected void startUp() {
		overlayManager.add(overlay);
		CurrentXP = 0;
		start = System.currentTimeMillis();
		startXP = client.getSkillExperience(Skill.RUNECRAFT);
		TotalTrips = 0;
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameTick(GameTick event) {

		if (client.getGameState() != GameState.LOGGED_IN) { return; }

		if (client.getRealSkillLevel(Skill.RUNECRAFT) < config.xrunecraftingsettings().getRequiredLevel()) {
			status = "Runecrafting lvl: " + config.xrunecraftingsettings().getRequiredLevel() + " is required!";
			return;
		}


		CurrentTaskStatus = currentTask.toString();
		CurrentXP = (startXP - client.getSkillExperience(Skill.RUNECRAFT));

		Player local = Players.getLocal();

		TileObject BankBooth = TileObjects.getNearest(config.xrunecraftingsettings().getBankId(), 3194);
		if (BankBooth != null) {
			if (Inventory.contains(config.xrunecraftingsettings().getMagicRune()) || Inventory.isEmpty()) {
				currentTask = TASK.Banking;
			}
			if (!xRunecraftingUtils.DuelRingsTeleportLeft()) {
				if (!Tabs.isOpen(Tab.EQUIPMENT)) { Tabs.open(Tab.EQUIPMENT); }
				xRunecraftingUtils.DuelRingItem_Interact("Remove");
				currentTask = TASK.Banking;
			}
			if (Inventory.contains(xRunecraftingUtils.DuelRing1) || Inventory.contains(xRunecraftingUtils.DuelRing8)) {
				currentTask = TASK.Banking;
			}

			if (Inventory.contains(xRunecraftingUtils.PreviousHeadItem) || Inventory.contains(xRunecraftingUtils.PreviousRingItem)) {
				currentTask = TASK.Banking;
			}
		}

		TileObject InAltarObj = TileObjects.getNearest(config.xrunecraftingsettings().getAltarInstanceObj());
		if (InAltarObj != null && Inventory.isFull()) {
			status = "Crafting runes";
			currentTask = TASK.IDLE;
			InAltarObj.interact("Craft-rune");
		}

		if (!Bank.isOpen() && Inventory.isFull()) {

			TileObject AltarObj = TileObjects.getNearest(config.xrunecraftingsettings().getAltarOutsideInstance());
			if (xRunecraftingUtils.ConfigIsAirAltarSelected() && InAltarObj == null && AltarObj == null) {
				currentTask = TASK.WalkToAltar;
			}

			if (!WeAlreadyTeleported && xRunecraftingUtils.ConfigIsFireAltarSelected() && BankBooth != null && xRunecraftingUtils.DuelRingsTeleportLeft()) {
				if (!Tabs.isOpen(Tab.EQUIPMENT)) { Tabs.open(Tab.EQUIPMENT); }
				xRunecraftingUtils.DuelRingItem_Interact("PvP Arena");
				WeAlreadyTeleported = true;
				currentTask = TASK.WalkToAltar;
			}

			if (AltarObj != null) {
				status = "Entering altar";
				currentTask = TASK.IDLE;

				if ((xRunecraftingUtils.ConfigIsFireAltarSelected()  && local != null && AltarObj.distanceTo(local.getWorldLocation()) < 5)) {
					AltarObj.interact("Enter");
					return;
				}

				//pretty sure this doesnt do shit
				if (xRunecraftingUtils.ConfigIsFireAltarSelected() && Reachable.isWalled(local.getWorldLocation(), AltarObj.getWorldLocation())) {
					Walker.walkTo(AltarObj.getWorldLocation());
				}
				else if (xRunecraftingUtils.ConfigIsFireAltarSelected()) { AltarObj.interact("Enter"); }
				else if(xRunecraftingUtils.ConfigIsAirAltarSelected()) {
					Walker.walkTo(AltarObj.getWorldLocation());
					if (local != null && AltarObj.distanceTo(local.getWorldLocation()) < 5) {
						AltarObj.interact("Enter");
					}
				}
			}
		}


		TileObject PortalObj = TileObjects.getNearest(config.xrunecraftingsettings().getPortal());
		if (PortalObj != null && Inventory.contains(config.xrunecraftingsettings().getMagicRune())) {
			if (!WeAlreadyTeleported && xRunecraftingUtils.ConfigIsFireAltarSelected()) {
				if (!Tabs.isOpen(Tab.EQUIPMENT)) { Tabs.open(Tab.EQUIPMENT); }
				xRunecraftingUtils.DuelRingItem_Interact("Castle Wars");
				WeAlreadyTeleported = true;
			}
			else {
				status = "exiting through the Portal";
				currentTask = TASK.IDLE;
				PortalObj.interact("Use");
			}
		}

		if (local.getGraphic() == 111) { WeAlreadyTeleported = true; }
		else { WeAlreadyTeleported = false; }

		if (xRunecraftingUtils.ConfigIsAirAltarSelected()) {
		if (BankBooth == null && !Bank.isOpen() && InAltarObj == null && Inventory.contains(config.xrunecraftingsettings().getMagicRune())) { currentTask = TASK.WalkToBank; }
		}

		DoRunecraftingTasks();
	}

	public void DoRunecraftingTasks() {
		switch (currentTask) {
			case Banking:
				Bank();
				break;
			case WalkToAltar:
				status = "Walking to altar:";
				if (xRunecraftingUtils.ConfigIsFireAltarSelected()) {
					Player local = Players.getLocal();

					if (local.getWorldLocation().getRegionID() == 13106 || local.getWorldLocation().getRegionID() == 10315) {
						Walker.walkTo(config.xrunecraftingsettings().getAltarLocation());
					} }
				else if (xRunecraftingUtils.ConfigIsAirAltarSelected()) { Walker.walkTo(config.xrunecraftingsettings().getAltarLocation()); }
				break;
			case WalkToBank:
				status = "Walking to bank";
				Walker.walkTo(config.xrunecraftingsettings().getBankLocation());
				break;
			case IDLE:
				break;
		}
	}

	void Bank(){
		status = "Interacting with the bank";
		if (!Bank.isOpen()) {
			TileObject BankBooth = TileObjects.getNearest(config.xrunecraftingsettings().getBankId(), 3194);
			if (xRunecraftingUtils.ConfigIsFireAltarSelected()) {

				if (!xRunecraftingUtils.DuelRingsTeleportLeft()) { if (!Tabs.isOpen(Tab.EQUIPMENT)) { Tabs.open(Tab.EQUIPMENT); } xRunecraftingUtils.DuelRingItem_Interact("Remove");  }
				if (Inventory.contains(xRunecraftingUtils.DuelRing8) && !xRunecraftingUtils.DoWeHaveARingEquipped()) {
					if (!Tabs.isOpen(Tab.INVENTORY)) { Tabs.open(Tab.INVENTORY); }
					Inventory.getFirst(xRunecraftingUtils.DuelRing8).interact("Wear");  }
				if (Movement.getRunEnergy() > 30 && !Movement.isRunEnabled()) { Movement.toggleRun(); }
					if (xRunecraftingUtils.DuelRingsTeleportLeft() && Inventory.contains(xRunecraftingUtils.DuelRing8)) {
						BankBooth.interact(config.xrunecraftingsettings().getBankInteractionAction());
						return;
					}

				if (Inventory.contains(xRunecraftingUtils.DuelRing1)) {
					BankBooth.interact(config.xrunecraftingsettings().getBankInteractionAction());
				}

			}

			if (Inventory.contains(xRunecraftingUtils.PreviousHeadItem) || Inventory.contains(xRunecraftingUtils.PreviousRingItem)) {
				BankBooth.interact(config.xrunecraftingsettings().getBankInteractionAction());
			}

			if (Inventory.contains(config.xrunecraftingsettings().getMagicRune()) || Inventory.isEmpty() || Inventory.contains(xRunecraftingUtils.DuelRing1)) {
				BankBooth.interact(config.xrunecraftingsettings().getBankInteractionAction());
			}

		}

		if (Bank.isOpen()) {
			if (xRunecraftingUtils.PreviousHeadItem != 0 || xRunecraftingUtils.PreviousRingItem != 0) {
				Bank.depositInventory();
				xRunecraftingUtils.PreviousHeadItem = 0;
				xRunecraftingUtils.PreviousRingItem = 0;
				return;
			}
			if (Inventory.contains(xRunecraftingUtils.DuelRing8) && xRunecraftingUtils.DuelRingsTeleportLeft()) {
				Bank.depositInventory();
				return;
			}
			if (Inventory.contains(config.xrunecraftingsettings().getMagicRune())) {
				status = "Depositing items.";
				if (!Inventory.contains(xRunecraftingUtils.DuelRing1)) { TotalTrips++; }
				Bank.depositInventory();
				return;
			}

			if (!xRunecraftingUtils.DoWeHaveATiara() && !Inventory.contains(xRunecraftingUtils.Tiara)) {
				if(xRunecraftingUtils.ConfigIsAirAltarSelected() && Bank.contains(xRunecraftingUtils.AirTiara)) {
					if (Inventory.isFull()) { Bank.depositInventory(); } //TODO ADDED

					Bank.withdraw(xRunecraftingUtils.AirTiara, 1, Bank.WithdrawMode.ITEM);
					return;
				}
				else if (xRunecraftingUtils.ConfigIsFireAltarSelected() && Bank.contains(xRunecraftingUtils.FireTiara)) {
					if (Inventory.isFull()) { Bank.depositInventory(); } //TODO ADDED

					Bank.withdraw(xRunecraftingUtils.FireTiara, 1, Bank.WithdrawMode.ITEM);
					return;
				}
			}

			if (!xRunecraftingUtils.DoWeHaveATiara() && Inventory.contains(xRunecraftingUtils.Tiara)) {
				if (!Tabs.isOpen(Tab.INVENTORY)) { Tabs.open(Tab.INVENTORY); }
					Bank.close();
					Inventory.getFirst(xRunecraftingUtils.Tiara).interact("Wear");
					return;
			}

			if (xRunecraftingUtils.ConfigIsFireAltarSelected())
			{
				if (!Inventory.contains(xRunecraftingUtils.DuelRing8) && !xRunecraftingUtils.DoWeHaveARingEquipped() && Bank.contains(xRunecraftingUtils.DuelRing8)) {
					if (Inventory.isFull()) { Bank.depositInventory(); } //TODO ADDED
					Bank.withdraw(xRunecraftingUtils.DuelRing8, 1, Bank.WithdrawMode.ITEM);
					return;
				}
				if (Inventory.contains(xRunecraftingUtils.DuelRing8) && xRunecraftingUtils.DoWeHaveARingEquipped()) {
					Bank.deposit(xRunecraftingUtils.DuelRing8, 1);
					return;
				}
				if (Inventory.contains(xRunecraftingUtils.DuelRing) && !xRunecraftingUtils.DoWeHaveARingEquipped()) {
					if (!Tabs.isOpen(Tab.INVENTORY)) { Tabs.open(Tab.INVENTORY); }
					Bank.close();
					Inventory.getFirst(xRunecraftingUtils.DuelRing).interact("Wear");
					return;
				}
			}
			if (!Inventory.contains(xRunecraftingUtils.PureEssence) && Bank.contains(xRunecraftingUtils.PureEssence)) {
				Bank.withdraw(xRunecraftingUtils.PureEssence, 28, Bank.WithdrawMode.ITEM);
				return;
			}
		}
		if (Bank.isOpen()) { Bank.close(); }
	}


	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
			return;
		}
		//Sometimes the inventory is empty but keeps spamming the button... This should fix it but not tested it.
		if (event.getMessage().contains("You have nothing to deposit.")) {
			if (Bank.isOpen()) {
				if (!Inventory.contains(xRunecraftingUtils.PureEssence) && Bank.contains(xRunecraftingUtils.PureEssence)) {
					Bank.withdraw(xRunecraftingUtils.PureEssence, 28, Bank.WithdrawMode.ITEM);
				}
			}
		}
	}



	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xRunecraftingPlugin")) { } }
}
