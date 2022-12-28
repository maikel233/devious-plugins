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
package net.runelite.client.plugins.xPestControl;

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
import net.runelite.client.plugins.xhunter.xHunterPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.StopWatch;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.Walker;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Walker bugs:
//Sometimes runs forward/backwards to the first waypoint. Its looks like it skips the waypoints?
//Sometimes it wants to run out of bounds??? Happends twice in 180 games.
//If there are gates that are damaged and have the "REPAIR" option do not open. The script thinks it can open the gate but the gate is stuck and does not open...

//Would be nice to implent a traceRay between Player->Target. So if a NPC Brawler is in between YOU and the TARGET/Tile Walk around it or kill it?
//When a NPC is close to the fence/gate Use Walker and not interact.

//Portal ids:
//Shielded   Purple:1743 Blue:1744 Yellow:1745 Red:1746
//Unshielded Purple:1739 Blue:1740 Yellow:1741 Red:1742

@Slf4j
@PluginDescriptor(
	name = "xPestControl",
	description = "A extended PestControl plugin",
	tags = {"skilling", "minigames"}
)
public class xPestControlPlugin extends Plugin {
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xPestControlConfig config;
	@Inject
	private xPestControlOverlay xOverlay;
	@Inject
	private PestControlOverlay_Extended overlay;

	public static String status = "Initializing...", TotalPlayedInfo = ".";
	public static String PortalName = "";
	public static int WonGames = 0, ReceivedTickets = 0, LostGames = 0, LackOfZealGames = 0;
	boolean InitializeLocation = false;
	public WorldPoint BluePortal, YellowPortal, RedPortal,PurplePortal, Mid;
	private final Pattern SHIELD_DROP = Pattern.compile("The ([a-z]+), [^ ]+ portal shield has dropped!", Pattern.CASE_INSENSITIVE);
	public long start;
	enum TASK {EnterBoat, WaitingOnBoat, WeArrived, Fight}
	TASK currentTask = TASK.EnterBoat;

	@Provides
	xPestControlConfig provideConfig(ConfigManager configManager) { return configManager.getConfig(xPestControlConfig.class); }

	@Override
	protected void startUp() {
		overlayManager.add(xOverlay);
		overlayManager.add(overlay);
		start = System.currentTimeMillis();
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(xOverlay);
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (client.getGameState() != GameState.LOGGED_IN) { return; }

		    //Mhhmm RegionId 10537 seem to return false? well we just do this instead
			NPC Squire = NPCs.getNearest(1773, 1772, 1771);
			if (Squire != null) {
			if (Dialog.isOpen()) {
					status = "Status: Executing Tasks";
					//Seems like the below message doesnt work?

					if (Dialog.getText().contains("lack of zeal")) {
						currentTask = TASK.EnterBoat;
						LackOfZealGames++;
						Dialog.continueSpace();
					}
					if (Dialog.getText().contains("Congratulations! You managed to destroy all the portals!")) {
						currentTask = TASK.EnterBoat;
						if (Dialog.getText().contains("3")){ ReceivedTickets = ReceivedTickets +3; }
						else if (Dialog.getText().contains("4")){ ReceivedTickets = ReceivedTickets +4; }
						else if (Dialog.getText().contains("5")){ ReceivedTickets = ReceivedTickets +5; }
						WonGames++;
						Dialog.continueSpace();
					}

					if (Dialog.getText().contains("The Void Knight was killed, another of our Order")) {
						currentTask = TASK.EnterBoat;
						LostGames++;
						Dialog.continueSpace();
					}
				}
				status = null;
				YellowPortal = null;
				RedPortal  = null;
				PurplePortal = null;
				BluePortal = null;

				//Just incase we get no dialog...
				Player local = Players.getLocal();
				if (local.getWorldX() == config.difficulty().getDockX()) { currentTask = TASK.EnterBoat; }
			}


		if (currentTask != TASK.Fight) {
			//Same as above this RegionId: 10536 returns false?
			NPC PCM = NPCs.getNearest("Spinner","Brawler", "Defiler", "Ravager", "Torcher", "Shifter");
			if (PCM != null) {
				currentTask = TASK.Fight;
			}
		}

		TotalPlayedInfo = "WonGames: " + WonGames + " LostGames: " + LostGames + " FailGames: " + LackOfZealGames + "\nTickets: " + ReceivedTickets;

		PestControlTasks();
	}

	public boolean AttackOther(){
		Player local = Players.getLocal();
		if (local.getInteracting() != null && !Dialog.canContinue()) { return false; }

		if (local.isMoving())
			return false;

		//Make sure we equip the right item
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)  { return false; }

		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (!weapon.getName().equals(config.getDefaultAttackWep())) { EquipDefaultWeapons(); }

		NPC Spinners = NPCs.getNearest("Spinner");
		if (Spinners != null && Spinners.getWorldLocation().distanceTo(local.getWorldLocation()) < 13) {
			if (!Reachable.isInteractable(Spinners)) {
				Movement.walkTo(Spinners.getWorldLocation());
				return false;
			}
			status = "Status: Attacking Spinners!";


			Spinners.interact("Attack");
			return true;
		} else {
			NPC OtherMonsters = NPCs.getNearest("Brawler", "Defiler", "Ravager", "Torcher", "Shifter");
			if (OtherMonsters != null && OtherMonsters.getWorldLocation().distanceTo(local.getWorldLocation()) < 13) {

				if (!Reachable.isInteractable(OtherMonsters)) {
					Movement.walkTo(OtherMonsters.getWorldLocation());
					return false;
				}

				if (!OtherMonsters.isDead()) {
					status = "Status: Attacking " + OtherMonsters.getName() + "!";
					OtherMonsters.interact("Attack");
					return true;
				}
			}
		}
		return false;
	}

	public void EquipDefaultWeapons() {
		if (!config.getDefaultAttackWep().isEmpty()) {
			Item Abyssal_whip = Inventory.getFirst(x -> x.hasAction("Wield")
					&& (x.getName().contains(config.getDefaultAttackWep())));

			if (Abyssal_whip != null) {
				Tabs.open(Tab.INVENTORY);
				if (Tabs.isOpen(Tab.INVENTORY)) { Abyssal_whip.interact("Wield"); }
			}
		}

		if (!config.getOffHandShield().isEmpty()) {
			Item Dragon_defender = Inventory.getFirst(x -> x.hasAction("Wield")
					&& (x.getName().contains(config.getOffHandShield())));

			if (Dragon_defender != null) {
				Tabs.open(Tab.INVENTORY);
				if (Tabs.isOpen(Tab.INVENTORY)) { Dragon_defender.interact("Wield"); }
			}
		}
	}

	public void PestControlTasks() {
		switch (currentTask) {
			case EnterBoat:
				status = "Status: Entering boat.";
				InitializeLocation = false;
				EnterBoat();
				break;
			case WaitingOnBoat:
				status = "Status: Idle on boat.";

				NPC Squire = NPCs.getNearest(2949);
				if (Squire != null) {
					status = "Status: OnIsle";
					Player local = Players.getLocal();
					if (!InitializeLocation) {
						//Instance so our XY values are always different.
						YellowPortal = new WorldPoint(local.getWorldX() + 13, local.getWorldY() - 37, 0);
						RedPortal = new WorldPoint(local.getWorldX() - 12, local.getWorldY() - 39, 0);
						PurplePortal = new WorldPoint(local.getWorldX() - 29, local.getWorldY() - 17, 0);
						BluePortal = new WorldPoint(local.getWorldX() + 23, local.getWorldY() - 20, 0);
						Mid = new WorldPoint(local.getWorldX(), local.getWorldY() - 18, 0);
						InitializeLocation = true;
					}
					//
					Walker.walkTo(Mid);

					if (local.getWorldLocation().distanceTo(Mid.getWorldLocation()) < 5) { currentTask = TASK.Fight; }
				}
				break;
			case Fight:

				if (PortalName.isEmpty() || PortalName.isBlank()) { AttackOther(); }

				Player local = Players.getLocal();
					switch (PortalName) {
						case "BLUE":
							if (local.getWorldLocation().distanceTo(BluePortal.getWorldLocation()) > 15) { Walker.walkTo(BluePortal); }
							break;
						case "RED":
							if (local.getWorldLocation().distanceTo(RedPortal.getWorldLocation()) > 15) { Walker.walkTo(RedPortal); }
							break;
						case "PURPLE":
							if (local.getWorldLocation().distanceTo(PurplePortal.getWorldLocation()) > 15) { Walker.walkTo(PurplePortal); }
							break;
						case "YELLOW":
							if (local.getWorldLocation().distanceTo(YellowPortal.getWorldLocation()) > 15) { Walker.walkTo(YellowPortal); }
							break;
						default: AttackOther();
					}


					NPC PortalEnt = NPCs.getNearest(1739,1740,1741,1742);
		if (config.UseSpecialAttack() && PortalEnt == null || Combat.getSpecEnergy() < 50) { EquipDefaultWeapons(); }

				if (PortalEnt != null && Reachable.isInteractable(PortalEnt) && PortalEnt.getWorldLocation().distanceTo(local.getWorldLocation()) < 25) {

					if (config.UsePrayer() && !Prayers.isQuickPrayerEnabled()) { Prayers.toggleQuickPrayer(true); }

					if (!Reachable.isInteractable(PortalEnt)) {
						Movement.walkTo(PortalEnt.getWorldLocation());
						return;
					}

					if (config.UseSpecialAttack() && Combat.getSpecEnergy() >= config.TreshholdSpecialAttack()) {
						Item DragonClaws = Inventory.getFirst(x -> x.hasAction("Wield")
								&& (x.getName().contains(config.getSpecialAttackWep())));

						if (DragonClaws != null) {
							Tabs.open(Tab.INVENTORY);
							if (Tabs.isOpen(Tab.INVENTORY)) { DragonClaws.interact("Wield"); }
						}

						if (!Combat.isSpecEnabled()) { Combat.toggleSpec(); }
					}
					if (local.getInteracting() != null && !Dialog.canContinue()) { return; }
					status = "Status: Attacking Portal!";
					PortalEnt.interact("Attack");
				}
				else { AttackOther();}
				break;
		}
	}
	public void EnterBoat(){
		//Make a menu item for this.
		Player local = Players.getLocal();
		if (local.getWorldX() == config.difficulty().getDockX()) {
			status = "Status: Entering boat!!!";
			TileObject Plank = TileObjects.getFirstAt(config.difficulty().getWorldX(), config.difficulty().getWorldY(), 0, config.difficulty().getPlankID());
			if (Plank != null) {
				Plank.interact("Cross");
				currentTask = TASK.WaitingOnBoat;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		GameState gameState = event.getGameState();
		if (gameState == GameState.CONNECTION_LOST || gameState == GameState.LOGIN_SCREEN || gameState == GameState.HOPPING) {
			currentTask = TASK.EnterBoat;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {
		if (overlay.getGame() != null && chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
			Matcher matcher = SHIELD_DROP.matcher(chatMessage.getMessage());
			if (matcher.lookingAt()) {
				overlay.getGame().fall(matcher.group(1));
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xPestControlPlugin")) { } }
}
