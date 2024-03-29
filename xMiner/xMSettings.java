/*
 * Copyright (c) 2019, Jacob M <https://github.com/jacoblairm>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.xMiner;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum xMSettings
{
	//iron ore 11364 11365.
	//depleted 11390
	//depleted 11391
	AlKharid("Al-Kharid", 3295, 3310, 0, 3295, 3310, 440, 440,15),
	Varrock_Tin("Varrock Tin", 3282, 3363, 0, 3282, 3363, 438, 436, 1),
	Varrock_Copper("Varrock Copper", 3289, 3362, 0, 3289, 3362, 436, 438,1),
	MiningGuild("Mining Guild", 3021, 9721, 0, 3029, 9720, 440, 440,60);
	private final String location;
	private final int WorldX, WorldY, WorldPlane, WorldX2, WorldY2;
	private final int InventoryOreId, SecondaryOreId;
	private final int MinimalRequiredMiningLvl;
	//Iron, Tin , copper
	final int[] ORE_IDS = {11364, 11365, 11360, 11361, 10943, 11161};
	final int[] DEPLETED_ORE_IDS = {11390, 11391};
	//final int[] Gems = {1617, 1619, 1621, 1623};
	final int[] PickAxeIds = {1265, 1267, 1269, 12297, 1273, 1271, 1275, 11920, 12797,23677,25376};
	final int DragonPickAxeId = 11920;
	@Override
	public String toString()
	{
		return getLocation();
	}
}

