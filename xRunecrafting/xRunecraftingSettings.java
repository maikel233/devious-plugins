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
package net.runelite.client.plugins.xRunecrafting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
@AllArgsConstructor
public enum xRunecraftingSettings
{
	//Fire tiara item id: 5537
	//							BankLocation:								AltarLocation											//Altar Craft
	AirAltar("AirAltar", new WorldPoint(3011, 3356, 0), new WorldPoint(2985, 3294, 0), 556, 34760, 34813,34748, 24101, "Bank", 1),
	FireAltar("FireAltar", new WorldPoint(2443, 3083, 0), new WorldPoint(2985, 3294, 0), 554, 34764, 34817, 34752, 4483, "Use", 14);

	private final String name;
	private final WorldPoint BankLocation;
	private final WorldPoint AltarLocation;
	private final int MagicRune;
	private final int AltarInstanceObj;
	private final int AltarOutsideInstance;
	private final int Portal;
	private final int BankId;
	private final String BankInteractionAction;
	private final int RequiredLevel;

	@Override
	public String toString()
	{
		return getName();
	}
}

