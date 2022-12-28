package net.runelite.client.plugins.cannonreloader;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("cannonreloader")
public interface CannonReloaderConfig extends Config
{

	@Range(
			min = 1,
			max = 30
	)

	@ConfigItem(
			keyName = "minReloadAmount",
			name = "Minimum count for reload",
			description = "The minimum cannonball count when we want to reload",
			position = 1
	)
	default int minReloadAmount()
	{
		return 9;
	}

	@Range(
			min = 1,
			max = 30
	)

	@ConfigItem(
			keyName = "maxReloadAmount",
			name = "Maximum count for reload",
			description = "The maximum cannonball count when we want to reload",
			position = 2
	)
	default int maxReloadAmount()
	{
		return 14;
	}
}