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

import net.runelite.client.config.*;

@ConfigGroup("xMinerplugin")
public interface xMinerConfig extends Config
{

    @ConfigSection(
            keyName = "gameSettings",
            name = "GameSettings",
            description = "",
            position = 0
    )

    String gameSettings = "GameSettings";

    @ConfigItem(
            keyName = "Mine location",
            name = "Mine location",
            description = "Select where you want to mine",
            position = 0,
            section = gameSettings
    )

    default xMSettings xmsettings()
    {
        return xMSettings.AlKharid;
    }

    @ConfigItem(
            keyName = "SpecialAtt",
            name = "Special Attack",
            description = "Uses special attack to boost mining lvl",
            position = 0,
            section = gameSettings
    )
    default boolean UseSpecialAttack() { return true; }
    @ConfigItem(
            keyName = "bank ore",
            name = "Bank ore",
            description = "When selected the script will bank the ore.\nIf disabled it will drop the ore..",
            position = 1,
            section = gameSettings
    )
    default boolean BankOre() { return true; }


}
