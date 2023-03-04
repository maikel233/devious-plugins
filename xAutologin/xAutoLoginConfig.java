package net.runelite.client.plugins.xAutologin;

import javax.inject.Inject;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("xautologin")
public interface xAutoLoginConfig extends Config
{

	@ConfigItem(
			keyName = "fastLogin",
			name = "Fastlogin",
			description = "Select one of our accounts.",
			position = 0
	)
	default xAutoLoginSettings xAutoLoginSettings()
	{
		return xAutoLoginSettings.Custom;
	}

	@ConfigItem(
			keyName = "username",
			name = "Username",
			description = "Username",
			position = 2
	)

	default String username()
	{
		return "Username";
	}

	@ConfigItem(
			keyName = "password",
			name = "Password",
			description = "Password",
			secret = true,
			position = 3
	)
	default String password()
	{
		return "Password";
	}

	@ConfigItem(
			keyName = "auth",
			name = "Authenticator",
			description = "Authenticator",
			secret = true,
			position = 4
	)
	default String auth()
	{
		return "Authenticator";
	}

	@ConfigItem(
			keyName = "useWorld",
			name = "Select world",
			description = "Select world to login to",
			position = 5
	)
	default boolean useWorld()
	{
		return false;
	}

	@ConfigItem(
			keyName = "world",
			name = "World",
			description = "World Selector",
			position = 6,
			hidden = true,
			unhide = "useWorld"
	)
	default int world()
	{
		return 301;
	}

	@ConfigItem(
			keyName = "lastWorld",
			name = "Save last world",
			description = "Save last world",
			position = 7,
			hidden = true,
			unhide = "useWorld"
	)
	default boolean lastWorld()
	{
		return false;
	}

	@ConfigItem(
			keyName = "welcomeScreen",
			name = "Complete Welcome screen",
			description = "Automatically presses the 'Click here to Play' button after login",
			position = 8
	)
	default boolean welcomeScreen()
	{
		return false;
	}
}
