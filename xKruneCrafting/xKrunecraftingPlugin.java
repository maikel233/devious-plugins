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
package net.runelite.client.plugins.xKruneCrafting;

import com.google.inject.Provides;

import java.awt.AWTException;
import java.util.Random;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.xKruneCrafting.tasks.MiningTask;
import net.runelite.client.plugins.xKruneCrafting.tasks.BloodAltarTask;
import net.runelite.client.plugins.xKruneCrafting.tasks.DarkAltarTask;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.plugins.TaskPlugin;


@Slf4j
@PluginDescriptor(
		name = "xKrunecraftingPlugin",
		description = "A runecrafting plugin to make our lifes easier!",
		enabledByDefault = false,
		tags = {"skilling", "automatisation", "xhook"}
)
public class xKrunecraftingPlugin extends TaskPlugin
{
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private xRunecraftingConfig config;
	@Inject
	private xKrunecraftingOverlay overlay;

	@Inject
	private xKrunecraftingUtils utils;

	public long startTime;
	public Integer startXpRuneCrafting = 0, startXpCrafting = 0, startXpMining = 0;


	@Provides
	xRunecraftingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(xRunecraftingConfig.class);
	}

	private final Task[] tasks = new Task[] {
					new MiningTask(),
					new DarkAltarTask(),
					new BloodAltarTask()
	};

	public Task[] getTasks() { return tasks; }



	@Override
	protected void startUp() {
		startTime = System.currentTimeMillis();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	boolean RetrieveXP = false;
	private boolean cameraThreadStarted = false;


	@Subscribe
	public void onGameTick(GameTick event) throws InterruptedException, AWTException {

		if (client.getGameState() != GameState.LOGGED_IN) { return; }

		var local = Players.getLocal();
		if (local == null) { return; }

		if (!RetrieveXP) {
			startXpRuneCrafting = Skills.getExperience(Skill.RUNECRAFT);
			startXpCrafting = Skills.getExperience(Skill.CRAFTING);
			startXpMining = Skills.getExperience(Skill.MINING);
			RetrieveXP = true;
		}

		// TODO Now changes every tcik
		int RANDOM = new Random().nextInt(16) + 25; // generates a random integer between 25-40
		if (!Movement.isRunEnabled() && Movement.getRunEnergy() > RANDOM) {
			if (local.getAnimation() == -1) { Movement.toggleRun(); }
		}

		for (Task task : getTasks())
		{ }
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{ if (event.getGroup().equals("xKrunecraftingPlugin")) { } }
}
