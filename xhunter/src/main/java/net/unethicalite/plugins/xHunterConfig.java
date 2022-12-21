package net.unethicalite.plugins.xhunter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("xhunterplugin")
public interface xHunterConfig extends Config
{

	@ConfigSection(
			name = "Falcon",
			description = "Falcon settings",
			position = 5,
			closedByDefault = false
	)
	String kebbit = "Kebbit";

	@ConfigItem(
			keyName = "npc",
			name = "Target:",
			description = "type of npc we can catch",
			position = 1,
			section = kebbit
	)
	default falconids Falconids()
	{
		return falconids.Spotted_kebbit;
	}
}
