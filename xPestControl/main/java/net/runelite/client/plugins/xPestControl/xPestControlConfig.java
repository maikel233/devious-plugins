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

import net.runelite.client.config.*;
import net.runelite.client.plugins.camera.ControlFunction;

@ConfigGroup("xPestControlplugin")
public interface xPestControlConfig extends Config
{

    @ConfigSection(
            keyName = "gameSettings",
            name = "GameSettings",
            description = "",
            position = 0
    )

    String gameSettings = "GameSettings";


    @ConfigSection(
            keyName = "combatSettings",
            name = "CombatSettings",
            description = "",
            position = 1
    )
    String combatSettings = "combatSettings";

    @ConfigSection(
            keyName = "misc",
            name = "Misc",
            description = "",
            position = 2
    )
    String misc = "Misc";


    @ConfigItem(
            keyName = "Difficulty",
            name = "Difficulty settings",
            description = "Novice Intermediate Pro",
            position = 0,
            section = gameSettings
    )

    default Difficulty difficulty()
    {
        return Difficulty.Veteran;
    }

    @ConfigItem(
            keyName = "SpecialAtt",
            name = "Special Attack",
            description = "Uses special attack on portals",
            position = 0,
            section = combatSettings
    )
    default boolean UseSpecialAttack()
    {
        return true;
    }

    @ConfigItem(
            keyName = "SpecialAttackWep",
            name = "Special Attack weapon",
            description = "Incase specialAttack option is enabled enter your special attack weapon here.\nExample: Dragon claws",
            position = 1,
            section = combatSettings
    )

    default String getSpecialAttackWep()
    {
        return "Dragon claws";
    }

    @ConfigItem(
            keyName = "TreshholdSpecial",
            name = "Treshhold Special Attack",
            description = "Minimal amount you require to use a special Attack with your desired weapon.\nExample:DDS:25 Dragon claws: 50.",
            position = 2,
            section = combatSettings
    )
    @Range(
            min = 25,
            max = 100
    )
    default int TreshholdSpecialAttack()
    {
        return 50;
    }

    @ConfigItem(
            keyName = "MainWeapon",
            name = "Main weapon",
            description = "Incase specialAttack option is enabled enter your default weapon here.\nExample: Abyssal whip",
            position = 3,
            section = combatSettings
    )
    default String getDefaultAttackWep()
    {
        return "Abyssal whip";
    }

    @ConfigItem(
            keyName = "OffHandWeapon",
            name = "Offhand weapon",
            description = "Incase specialAttack option is enabled enter your default offhand here.\nExample: Dragon defender",
            position = 4,
            section = combatSettings
    )
    default String getOffHandShield()
    {
        return "Dragon defender";
    }

    @ConfigItem(
            keyName = "Prayer",
            name = "Enables quick prayer",
            description = "Enable quickpray. You have to manually choose your quick prayers.",
            position = 0,
            section = misc
    )
    default boolean UsePrayer()
    {
        return true;
    }

    //  @ConfigItem(
    //          keyName = "Brews",
    //          name = "Drink brews",
    //          description = "Drinks brews to increase damage",
    //          position = 1,
    //          section = misc
    //  )
    //  default boolean UseCombatBrew()
    //  {
    //      return false;
    //  }
}
