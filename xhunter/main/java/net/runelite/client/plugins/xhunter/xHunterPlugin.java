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
package net.runelite.client.plugins.xhunter;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Dialog;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Random;

@Slf4j
@PluginDescriptor(
	name = "xHunter",
	description = "A hunter plugin Use only in the woodlands",
	tags = {"skilling", "timers"}
)
public class xHunterPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xHunterConfig config;
	@Inject
	private xHunterOverlay overlay;

	@Provides
	xHunterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(xHunterConfig.class);
	}

	public String status = "Initializing...";
	public String status2 = ".";

	public long start; //
	public int startXP;
	public int CurrentXP;

	private Random r = new Random();

	enum TASK {Catching, Dropping, MatthiasGetBird}
	TASK currentTask = TASK.Catching;
	int RandomSlots;
	boolean FalconIsInAir = false;
	boolean Catching = false;
	boolean FalconCatchedKebbit = false;

	@Override
	protected void startUp() {
		overlayManager.add(overlay);
		start = System.currentTimeMillis();
		startXP = client.getSkillExperience(Skill.HUNTER);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	private boolean regionCheck() { //Checks if we're in the woodland falconry region.
		return client.getLocalPlayer().getWorldLocation().getPlane() == 0 && ArrayUtils.contains(client.getMapRegions(), 9528);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		if (client.getRealSkillLevel(Skill.HUNTER) < 43) {
			status = "A Hunting level of 43 is required for this plugin!";
			return;
		}

		CurrentXP = (startXP - client.getSkillExperience(Skill.HUNTER));

		if (!regionCheck()) {
			status = "You're not in the right region!";
			return;
		}

		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)  {
			return;
		}
		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (weapon == null)  {
			currentTask = TASK.MatthiasGetBird;
		}


		Player local = Players.getLocal();
		if (local.isMoving() || FalconIsInAir) {
			return;
		}

		if (Inventory.isFull() || Inventory.getFreeSlots() <= 3) {
		RandomSlots = Rand.nextInt(20, 26);
		currentTask = TASK.Dropping;
	   }

		HunterTasks();
	}

	private static final int ProjFalconInAir = 922;
	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event) {
		final Projectile projectile = event.getProjectile();
		switch (projectile.getId())
		{
			case ProjFalconInAir:
					if (Catching) {
						status2 = "Falcon is in the air.";
						FalconIsInAir = true;
					}
					break;
			default:
				break;
		}
	}

	@Subscribe
	private void onNpcSpawned(final NpcSpawned event) {
		final NPC npc = event.getNpc();
		if (!Catching && FalconCatchedKebbit && !FalconIsInAir) {
			status = "Status: Retrieving";
			NPC FalconNpc = NPCs.getNearest(config.Falconids().getFalcon());
			if (FalconNpc != null) {
				FalconNpc.interact("Retrieve");
			}
		}
	}
		public void HunterTasks() {
			switch (currentTask) {
				case MatthiasGetBird:
					status = "Talking to Matthias for a falcon.";
					GetBird();
					break;
				case Dropping:
					status = "Dropping";
					//drop inventory Bones, KebbitFur
					if (!Tabs.isOpen(Tab.INVENTORY)) {
						Tabs.open(Tab.INVENTORY);
					}
					Inventory.getAll(526, config.Falconids().getFur())
							.stream()
							.forEach(Item::drop);
					//Max inv space -1 for gp, min, min space
					if (Inventory.getFreeSlots() >= RandomSlots) {
						currentTask = TASK.Catching;
						break;
					}
					break;
				case Catching:
					status = "Status: Catching";
					Catching();
					break;
			}
	}
	public void GetBird() {
		NPC Matthias = NPCs.getNearest(1340, 1341);
		if (!Dialog.isOpen() && Matthias != null) {
			Matthias.interact("Talk-to");
		}

		if (Dialog.canContinue()) {
			status = "In dialog";
			Dialog.continueSpace();
		}

		if (Dialog.hasOption("Yes, please.")) {
			Dialog.chooseOption(1);
		}

		//Requires 500GP add check
		if (Dialog.hasOption("Could I have a go with your bird?")) {
			Dialog.chooseOption(3);
		}

		if (Dialog.hasOption("Ok, that seems reasonable.")) {
			Dialog.chooseOption(1);
		}

		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)  {
			return;
		}

		//Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		//if (weapon.getName().equals("Falconer's glove") && weapon.getId() == 10023){
			currentTask = TASK.Catching;
		//}
	}

	public void Catching() {
	
		NPC FalconNpc = NPCs.getNearest(config.Falconids().getFalcon());
		if (FalconNpc != null)
			return;
			
			if (currentTask == TASK.Catching) {
				NPC KebbitNpc = NPCs.getNearest(config.Falconids().getId());
				if (KebbitNpc != null) {
						KebbitNpc.interact("Catch");
						Catching = true;
				}
			}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
			return;
		}

		if (event.getMessage().contains("You don't have enough free space to take your reward.")) {
			status2 = "Falcon is not in the air.";
			Catching = false;
			FalconCatchedKebbit = false;
			FalconIsInAir = false;
			currentTask = TASK.Dropping;
		}

		if (event.getMessage().equals("You cannot catch a kebbit without your falcon.")) {
			Catching = false;
			FalconCatchedKebbit = false;
			FalconIsInAir = false;
			currentTask = TASK.MatthiasGetBird;
		}

		if (event.getMessage().contains("You retrieve the falcon as well as the fur of the dead kebbit.")) {
			status2 = "Falcon is not in the air.";
			Catching = false;
			FalconCatchedKebbit = false;
			FalconIsInAir = false;
			currentTask = TASK.Catching;
		}

		if (event.getMessage().equals("The falcon successfully swoops down and captures the kebbit.")) {
			status2 = "Falcon is not in the air.";
			FalconCatchedKebbit = true;
			FalconIsInAir = false;
			Catching = false;
			//Can run faster if you retrieve here, but i like the delay. seems to work fine
		}

		if (event.getMessage().equals("The falcon swoops down on the kebbit, but just barely misses catching it.") || event.getMessage().equals("The falcon swoops down on the kebbit, but just misses catching it.")) {
			status2 = "Falcon is not in the air.";
			Catching = false;
			FalconCatchedKebbit = false;
			FalconIsInAir = false;
			currentTask = TASK.Catching;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xHunterPlugin")) { } }
}
