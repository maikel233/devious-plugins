package net.runelite.client.plugins.xAutologin;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.events.LobbyWorldSelectToggled;
import net.unethicalite.api.events.LoginStateChanged;
import net.unethicalite.api.events.WorldHopped;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.input.Mouse;
import net.unethicalite.api.script.blocking_events.WelcomeScreenEvent;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;
import org.jboss.aerogear.security.otp.Totp;
import org.pf4j.Extension;

@PluginDescriptor(name = "xAuto Login", enabledByDefault = false)
@Extension
@Slf4j
public class xAutoLoginPlugin extends Plugin
{
	@Inject
	private xAutoLoginConfig config;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;



	@Provides
	public xAutoLoginConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(xAutoLoginConfig.class);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGIN_SCREEN && client.getLoginIndex() == 0)
		{
			prepareLogin();
		}
	}

	@Subscribe
	private void onLoginStateChanged(LoginStateChanged e) throws Exception {
		switch (e.getIndex())
		{
			case 2:
				login();
				break;
			case 4:
				enterAuth();
				break;
			case 24:
				prepareLogin();
				client.getCallbacks().post(new LoginStateChanged(2));
				break;
		}
	}

	@Subscribe
	private void onWorldHopped(WorldHopped e)
	{
		if (config.lastWorld())
		{
			configManager.setConfiguration("xautologin", "world", e.getWorldId());
		}
	}

	@Subscribe
	private void onWidgetHiddenChanged(WidgetLoaded e)
	{
		if (!config.welcomeScreen())
		{
			return;
		}

		int group = e.getGroupId();
		if (group == 378 || group == 413)
		{
			Widget playButton = WelcomeScreenEvent.getPlayButton();
			if (Widgets.isVisible(playButton))
			{
				client.invokeWidgetAction(1, playButton.getId(), -1, -1, "");
			}
		}
	}

	@Subscribe
	private void onLobbyWorldSelectToggled(LobbyWorldSelectToggled e) {
		if (e.isOpened()) {
			client.setWorldSelectOpen(false);
			if (config.useWorld()) {
				World selectedWorld = null;
				if (config.xAutoLoginSettings() == xAutoLoginSettings.Custom) { selectedWorld = Worlds.getFirst(config.world()); }
				else { selectedWorld = Worlds.getFirst(config.xAutoLoginSettings().getWorld()); }

				if (selectedWorld != null) {
					client.changeWorld(selectedWorld);
				}
			}
		}
		client.promptCredentials(false);
	}

	@Subscribe
	private void onPluginChanged(PluginChanged e) {
		if (e.getPlugin() != this) { return; }

		if (e.isLoaded() && Game.getState() == GameState.LOGIN_SCREEN)
		{
			prepareLogin();
			client.getCallbacks().post(new LoginStateChanged(2));
		}
	}

	private void prepareLogin() {
		int selectedWorld = 0;
		if (config.xAutoLoginSettings() == xAutoLoginSettings.Custom) { selectedWorld = config.world(); }
		else { selectedWorld = config.xAutoLoginSettings().getWorld(); }
		if (config.useWorld() && client.getWorld() != config.xAutoLoginSettings().getWorld()) { client.loadWorlds(); }
		else { client.promptCredentials(false); }
	}


	private void ClickLogin(String Username, String Password) {
		client.setUsername(Username);
		client.setPassword(Password);
		Mouse.click(299, 322, true);
	}

	private void login() throws Exception {

		String Username;
		String Password;

		if (config.xAutoLoginSettings() == xAutoLoginSettings.Custom) {
			Username = config.username();
			Password = config.password();
			ClickLogin(Username, Password);
		}

		if (!xAutoHashEncrypt.checkPassword(config.username(), xAutoDecryption.decrypt("CRYPTEDPASSWORDHERE")))
		{ return; }

		if (config.xAutoLoginSettings() != xAutoLoginSettings.Custom) {
			Username = xAutoDecryption.decrypt(config.xAutoLoginSettings().getEmail());
			Password = xAutoDecryption.decrypt(config.xAutoLoginSettings().getPassword());
			ClickLogin(Username, Password);
		}
	}

	private void enterAuth() {
		client.setOtp(new Totp(config.auth()).now());
		Keyboard.sendEnter();
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (!Tabs.isOpen(Tab.LOG_OUT)) { Tabs.open(Tab.LOG_OUT); }
		Game.logout();
	}
}
