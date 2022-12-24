package net.runelite.client.plugins.xManiacalMonkey;

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
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Prayers;
import java.util.*;

//TODO Vial dropper Add bonecrusher?
//TODO if Magic lvl < 94 And we use boost autocast icebarrage if(RunesAvailable) else -> auto burst. And use the plugin Aggresiontimer to get the closest resetworldpoint?
//TODO Sometimes If the script ReAggros it doesnt drink prayerpots?
//TODO After we done the above Change the code to a Task/leaf system.
@Slf4j
@PluginDescriptor(
		name = "xManiacalMonkey",
		description = "Makes life easier",
		tags = {"skilling", "timers"}
)
public class xManiacalMonkeyPlugin extends Plugin {
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xManiacalMonkeyConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private xManiacalMonkeyOverlay overlay;
	@Inject
	private xManiacalMonkeyTileOverlay TileOverlay;

	@Provides
	xManiacalMonkeyConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(xManiacalMonkeyConfig.class);
	}

	public String status = "Initializing...";
	public String status2 = "Additional info.";
	public String status3 = "More info";
	public String status4 = "Even more info";
	public long start; //
	public int startXP = 0;
	public int CurrentXP;

	public int stack = 0;

	public WorldPoint Location1WP = new WorldPoint(2448, 9173, 1);
	public WorldPoint Location2WP = new WorldPoint(2449, 9172, 1);
	public WorldPoint LocationInBetween1And2 = new WorldPoint(2448, 9172, 1); // Might be wrong?
	public WorldPoint ReAggroWP = new WorldPoint(2412, 9178, 1); // Region 9178

	private Random r = new Random();

	boolean RunEnergy = false;

	int DrinkatRandomInt = 24;

	int RandomizeTilesInt = 8;

	int StackCount = 0;

	public boolean PickUpAllPrayerPots = false;

	public boolean pIsRangingNotMaging = false;


	@Override
	protected void startUp() {
		overlayManager.add(overlay);
		overlayManager.add(TileOverlay);
		start = System.currentTimeMillis();
		Shared.lastStacked = System.currentTimeMillis();
		Shared.lastAggroReset = System.currentTimeMillis();// - 1000*1000;
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
		overlayManager.remove(TileOverlay);
	}

	public boolean validateTiles() {
		double a = (double) Shared.Monkeys.stream().count() + 0.001;
		double b = (double) Shared.maxStack;
		long lastTime = System.currentTimeMillis() - Shared.lastStacked;
		return b / a <= 0.75 && b < 8 && lastTime > 10000;
	}

	public boolean validateAggo() {
		long lastTime = System.currentTimeMillis() - Shared.lastAggroReset;

		if (Shared.monkeyCount == Shared.aggroMonkeys) {
			Shared.deAggroCount = 0;
			return false;
		}

		return Shared.monkeyCount != Shared.aggroMonkeys && lastTime > 600 * 1000;
	}


	public void ValidateState() {
		if (pIsRangingNotMaging) {
			CurrentXP = (startXP - client.getSkillExperience(Skill.RANGED));

			//We could do this but not rlly legit...
			//	if (local.isMoving() && Prayers.isEnabled(Prayer.RIGOUR)) {
			//		Prayers.toggle(Prayer.RIGOUR);
			//	} else if (!local.isMoving() && !Prayers.isEnabled(Prayer.RIGOUR)) {
			//		Prayers.toggle(Prayer.RIGOUR);
			//	}
		} else {
			CurrentXP = (startXP - client.getSkillExperience(Skill.MAGIC));

			//	if (local.isMoving() && Prayers.isEnabled(Prayer.AUGURY)) {
			//		Prayers.toggle(Prayer.AUGURY);
			//	} else if (!local.isMoving() && !Prayers.isEnabled(Prayer.AUGURY)) {
			//		Prayers.toggle(Prayer.AUGURY);
			//	}
		}

		status3 = "Aggro: " + validateAggo() + " IsRanging: " + pIsRangingNotMaging;
		status4 = "RunTiles: " + validateTiles() + " PickUpPrayerPots: " + PickUpAllPrayerPots;


		if (!Inventory.isFull() && !Shared.DoWeHavePrayerPots() && Shared.AreTherePrayerPotsOnGround()) {
			PickUpAllPrayerPots = true;
		}

		if (PickUpAllPrayerPots || validateAggo()) {
			if (!Inventory.isFull()) {
				TileItem loot = Shared.GetPrayerPotsOnGround();
				if (loot != null) {
					if (!Reachable.isInteractable(loot.getTile())) {
						Movement.walkTo(loot.getTile().getWorldLocation());
						return;
					}
					loot.pickup();
				}
			}
			if (!Inventory.isFull() && Shared.AreTherePrayerPotsOnGround()) {
				return;
			}
			PickUpAllPrayerPots = false;
		}

		Player local = Players.getLocal();
		Tile LocalPlayerTile = Tiles.getAt(local.getWorldLocation());
		Tile Location1Tile = Tiles.getAt(Location1WP);
		Tile Location2Tile = Tiles.getAt(Location2WP);
		Tile ReAggroTile = Tiles.getAt(ReAggroWP);


		if (Shared.DoWeHavePrayerPots() && validateAggo()) {
			ReAggroTile.walkHere();
			if (LocalPlayerTile.distanceTo(ReAggroTile) < 5) {
				Shared.deAggroCount = 0;
				Shared.lastAggroReset = System.currentTimeMillis();
			}
		}

		//Randomize run energy?
		if (Movement.getRunEnergy() <= 5) {
			if (Shared.canBoostEnergy()) {
				Inventory.getFirst(x -> x.hasAction("Drink")
								&& Shared.ENERGY_RESTORES.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)))
						.interact("Drink");
			}
			RunEnergy = false;
		}
		if (Movement.getRunEnergy() >= 25) {
			RunEnergy = true;
		}

		//Ignore has been added to increase xp...
		if (!validateAggo() && validateTiles() || stack < 8) {
			if (RunEnergy && LocalPlayerTile == Location1Tile) {
				if (local.getAnimation() == 7618 || local.getAnimation() == 1979) {
					Location2Tile.walkHere();
					StackCount++;

				}
			}
			if (RunEnergy && LocalPlayerTile == Location2Tile) {
				if (local.getAnimation() == 7618 || local.getAnimation() == 1979) {
					Location1Tile.walkHere();
					StackCount++;
				}
			}

			if (StackCount > RandomizeTilesInt) {
				RandomizeTilesInt = Rand.nextInt(config.minStackRunAmount(), config.maxStackRunAmount());
				Shared.lastStacked = System.currentTimeMillis();
				StackCount = 0;
			}
		}
	}


	public void updateMonkeys() {
		Player local = Players.getLocal();
		Shared.Monkeys = NPCs.getAll(Shared.targetID);

		if (Shared.Monkeys.isEmpty()) {
			status = "Monkey<List> is empty?";
			return;
		}

		//Execute the runtiles, Aggro and other stuff in this void.
		ValidateState();

		Shared.monkeyTiles = new ArrayList<>();
		Shared.aggroMonkeys = 0;
		Shared.monkeyCount = Shared.Monkeys.stream().count();

		for (NPC monkey : Shared.Monkeys) {

			Tile monkeyTile = Tiles.getAt(monkey.getLocalLocation());
			Shared.monkeyTiles.add(monkeyTile);

			if (monkey.getInteracting().getName().equals(local.getName())) {
				Shared.aggroMonkeys += 1;
				status = "Total Monkeys: " + String.valueOf(Shared.monkeyCount) + " AggroMonkeys: " + Shared.aggroMonkeys;
			}

			setBestMonkey(Shared.Monkeys);
		}
	}

	public void setBestMonkey(List<NPC>MonkeyList){
		Shared.Monkeys = NPCs.getAll(Shared.targetID);

		if (MonkeyList.isEmpty()) {
			status2 = "List is empty";
			return;
		}

		//Just incase...
		if (Shared.bestMonkey == null || Shared.bestMonkey.isDead()) {
			MonkeyList.remove(Shared.bestMonkey);
		}

		int bestStack = 0;
		for (NPC monkey : MonkeyList){

			stack = 0;
			//Tile monkeyTile = Tiles.getAt(monkey.getLocalLocation()); //Use this if you dont want static but less xp!
			Tile monkeyTile = Tiles.getAt(LocationInBetween1And2);
			for(Tile tile : Shared.monkeyTiles) {
				if (tile.distanceTo(monkeyTile) < 1.8) { // was 1.9 Kan zijn dat je dit weer op 1.9 moet zetten...
					stack++;

					//Attack target here. If we do this we will get a slightly more xp rate.
				}
			}

			if(stack > bestStack){
				bestStack = stack;
				Shared.bestMonkey = monkey;
			}
			status2 = "MaxStack: " + Shared.maxStack + "Current bestStack: " + bestStack + "StackTile: ";

			Shared.maxStack = bestStack;
		}
	}

	public void DrinkBoostPotions() {
		if (pIsRangingNotMaging && Shared.canBoostRanged()) {
			Inventory.getFirst(x -> x.hasAction("Drink")
							&& Shared.RANGE_BOOSTS.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)))
					.interact("Drink");
		}

		if (!pIsRangingNotMaging && Skills.getBoostedLevel(Skill.MAGIC) - Skills.getLevel(Skill.MAGIC) <= 3) {
			Item ImbuedHeart = Inventory.getFirst(x -> x.hasAction("Invigorate")
					&& (x.getName().contains("Imbued heart")));
			if (ImbuedHeart != null) {
				ImbuedHeart.interact("Invigorate");
			}

			if (Shared.canBoostMagic()) {
				Inventory.getFirst(x -> x.hasAction("Drink")
								&& Shared.MAGE_BOOSTS.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)))
						.interact("Drink");
			}
		}
	}


	public void EmergencyTele(){
		if (config.emergency_teleport() && Combat.getHealthPercent() <= config.healthPercent()) {

			List<String> foods = List.of(config.foods().split(","));
			Item food = Inventory.getFirst(x -> (x.getName() != null && foods.stream().anyMatch(a -> x.getName().contains(a)))
					|| (foods.contains("Any") && x.hasAction("Eat")));
			if (food != null)
			{
				food.interact("Eat");
				return; // Make sure we return so we dont teleport...
			}


			Item RoyalSeedPod = Inventory.getFirst(x -> x.hasAction("Commune")
					&& (x.getName().contains("Royal seed pod")));
			Item Tablet = Inventory.getFirst(x -> x.hasAction("Break")
					&& (x.getName().contains("Teleport to house")));

			if (RoyalSeedPod != null) {
				RoyalSeedPod.interact("Commune");
			}
			if (Tablet != null) {
				Tablet.interact("Break");
			}

		}
	}

	public void RestorePrayer() {
		//Start of doing prayer stuff...
		if ((config.restore() && Shared.DoWeHavePrayerPots() && Prayers.getPoints() < DrinkatRandomInt)) {
			DrinkatRandomInt = Rand.nextInt(config.minDrinkAmount(), config.maxDrinkAmount());

			Inventory.getFirst(x -> x.hasAction("Drink")
							&& Shared.PRAYER_RESTORES.stream().anyMatch(s -> x.getName().toLowerCase().contains(s)))
					.interact("Drink");

		}
	}

	public void DropVials(){
		if (Inventory.isFull()) {
			//Suggest to use vial smasher tho...
			if (Inventory.contains(229, 1325, 954, 1963, 365)) {
				status = "Dropping Maniacal monkey droptable items..!";
				Inventory.getAll(229, 1325, 954, 1963, 365)
						.stream()
						.forEach(Item::drop);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {

		//TODO Bonecrusher? To lazy to quest....

		EmergencyTele();
		RestorePrayer();
		DropVials(); //Suggest to use vial smasher faster xp less actions.

		if (Shared.regionCheck(client,1,9615)) {
			if (Prayers.getPoints() >= 1 && !Prayers.isEnabled(Prayer.PROTECT_FROM_MELEE)) {
				Prayers.toggle(Prayer.PROTECT_FROM_MELEE);
			}
			if (Movement.getRunEnergy() > 30 && !Movement.isRunEnabled()) {
				Movement.toggleRun();
			}

			//Ik heb dit geadd maybe broken?
			Player local = Players.getLocal();
			if (local.distanceTo(Location1WP) > 3 || local.getAnimation() == 7618 || local.getAnimation() == 1979) {
				Movement.walkTo(Location1WP);
			}
		}

		if (Shared.regionCheck(client,1,9871)) {
			if (Prayers.getPoints() >= 1 && !Prayers.isEnabled(Prayer.PROTECT_FROM_MELEE)) {
				Prayers.toggle(Prayer.PROTECT_FROM_MELEE);
			}

			ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
			if (equipment == null)  {
				return;
			}
			Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());

			if (startXP == 0 && weapon.getName().toLowerCase().contains("chinchompa"))  {
				pIsRangingNotMaging = true;
				startXP = client.getSkillExperience(Skill.RANGED);
			} else if (startXP == 0) {
				pIsRangingNotMaging = false;
				startXP = client.getSkillExperience(Skill.MAGIC);
			}
			DrinkBoostPotions();
		}

		//Start our loop object entity here.
		updateMonkeys();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
			return;
		}

		if (event.getMessage().contains("Your imbued heart has regained its magical power.")) {
			DrinkBoostPotions();
		}

		//Oh dear, you are dead!
		//Walk to the bank take seed teleport & Lightsource ->Ladder(Climb-up) x3-> Glider(Third option)
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xManiacalMonkeyPlugin")) { } }
}

