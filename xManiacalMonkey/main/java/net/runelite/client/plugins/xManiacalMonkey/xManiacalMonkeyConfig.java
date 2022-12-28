package net.runelite.client.plugins.xManiacalMonkey;

import net.runelite.client.config.*;

@ConfigGroup("xManiacalMonkeyplugin")
public interface xManiacalMonkeyConfig extends Config
{

	@ConfigSection(
			keyName = "prayers",
			name = "Prayers",
			description = "",
			position = 0
	)
	String prayers = "Prayers";

	@ConfigSection(
			keyName = "emergencySettings",
			name = "EmergencySettings",
			description = "",
			position = 1
	)
	String emergencySettings = "emergencySettings";




	@ConfigItem(
			keyName = "restore",
			name = "Restore prayer",
			description = "Drinks pots to restore prayer points",
			position = 0,
			section = prayers
	)
	default boolean restore()
	{
		return false;
	}

	@Range(
			min = 5,
			max = 50
	)

	@ConfigItem(
			keyName = "minDrink",
			name = "Min pts to drink a prayer potion.",
			description = "The minimum points to drink a restore/prayer potion.",
			position = 1,
			section = prayers
	)
	default int minDrinkAmount()
	{
		return 25;
	}

	@Range(
			min = 5,
			max = 50
	)

	@ConfigItem(
			keyName = "maxDrink",
			name = "Max pts to drink a prayer potion.",
			description = "The maximum points to drink a restore/prayer potion.",
			position = 2,
			section = prayers
	)
	default int maxDrinkAmount()
	{
		return 50;
	}


	@ConfigItem(
			keyName = "foods",
			name = "Food",
			description = "Food to eat, separated by comma. ex: Bones,Coins",
			position = 0,
			section = emergencySettings
	)
	default String foods()
	{
		return "Any";
	}
	@ConfigItem(
			keyName = "emergency_teleport",
			name = "Emergency teleport",
			description = "Uses Royal seed pod or Teleport to house to get out of here!",
			position = 1,
			section = emergencySettings
	)
	default boolean emergency_teleport()
	{
		return false;
	}

	@Range(max = 100)
	@ConfigItem(
			keyName = "TeleAtHealthPercent",
			name = "Tele at Health %",
			description = "Health % to teleport at",
			position = 2,
			section = emergencySettings
	)
	default int healthPercent()
	{
		return 15;
	}


	@Range(
			min = 5,
			max = 50
	)

	@ConfigItem(
			keyName = "minStackRun",
			name = "Min tiles we run  A->B",
			description = "Runs from tile(a) to tile(b) to stack",
			position = 3,
			section = emergencySettings
	)
	default int minStackRunAmount()
	{
		return 8;
	}

	@Range(
			min = 5,
			max = 50
	)

	@ConfigItem(
			keyName = "maxStackRun",
			name = "Max tiles we run A->B",
			description = "Runs from tile(a) to tile(b) to stack",
			position = 4,
			section = emergencySettings
	)
	default int maxStackRunAmount()
	{
		return 12;
	}
}
