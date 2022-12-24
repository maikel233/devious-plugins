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
package net.runelite.client.plugins.xManiacalMonkey;

import net.runelite.client.config.*;

@ConfigGroup("xManiacalMonkeyplugin")
public interface xManiacalMonkeyConfig extends Config
{

	@ConfigSection(
			name = "ManiacalMonkey",
			description = "ManiacalMonkey settings",
			position = 0,
			closedByDefault = false
	)

	String prayers = "Prayers";
	@ConfigItem(
			keyName = "restore",
			name = "Restore prayer",
			description = "Drinks pots to restore prayer points",
			position = 1,
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
			position = 2,
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
			position = 3,
			section = prayers
	)
	default int maxDrinkAmount()
	{
		return 50;
	}


	@ConfigItem(
			keyName = "flick",
			name = "Flick",
			description = "One ticks quick prayers",
			position = 4,
			section = prayers
	)
	default boolean flick()
	{
		return false;
	}
	String other = "Other";

	@ConfigItem(
			keyName = "foods",
			name = "Food",
			description = "Food to eat, separated by comma. ex: Bones,Coins",
			position = 5,
			section = other
	)
	default String foods()
	{
		return "Any";
	}
	@ConfigItem(
			keyName = "emergency_teleport",
			name = "Emergency teleport",
			description = "Uses any tablet to get out of here!",
			position = 6,
			section = other
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
			position = 7,
			section = other
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
			position = 8,
			section = other
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
			position = 9,
			section = other
	)
	default int maxStackRunAmount()
	{
		return 12;
	}
}
