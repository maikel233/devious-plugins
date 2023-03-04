/*
 * Copyright (c) 2018 Abex
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
package net.runelite.client.plugins.kourendlibrary;

import com.google.inject.Inject;
import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javax.imageio.ImageIO;
import net.runelite.api.Client;
import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import net.runelite.api.Skill;
import net.runelite.client.plugins.kourendlibrary.library.Library;
import net.runelite.client.ui.overlay.Overlay;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import net.runelite.client.ui.overlay.components.PanelComponent;


class KourendLibraryOverlay extends Overlay {
	private static final int MAXIMUM_DISTANCE = 24;
	private final Library library;
	private final Client client;
	private final KourendLibraryConfig config;
	private final KourendLibraryPlugin plugin;
	private final State state;
	private final PanelComponent panelComponent = new PanelComponent();

	long runTime;
	/*
	private final Image image;
	public static Optional<Image> getImage(String url) {
		try {
			URL u = new URL(url);
			return Optional.of(ImageIO.read(u.openStream()));
		} catch(IOException e) {
			System.out.println("Unable to get paint image");
			e.printStackTrace();
			return Optional.empty();
		}
	}
	*/
	@Inject
	private KourendLibraryOverlay(Library library, Client client, KourendLibraryConfig config, KourendLibraryPlugin plugin, State state) {
		//this.image = getImage("https://toppng.com/uploads/preview/spongebob-dank-patrick-freetoedit-spongebob-transparent-dank-11563597569sikcng7xvh.png").orElse(null);
		this.library = library;
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		this.state = state;

		this.setPriority(OverlayPriority.HIGHEST);
		this.setPosition(OverlayPosition.BOTTOM_LEFT);
		this.getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "X-Kourend Overlay"));

	}

	public static String numberFormat(double num) {
		if (num < 1000.0) {
			return Integer.toString((int) num);
		} else if (Math.round(num) / 10000.0 < 100.0) {
			return String.format("%.1fk", Math.round(num) / 1000.0);
		} else {
			return String.format("%.1fm", Math.round(num) / 1000000.0);
		}
	}

	public String GetMagicXP() {
		int CurrentXP = client.getSkillExperience(Skill.MAGIC);
		int xpGained = CurrentXP - plugin.startXpMagic;
		return String.format("XP Gained: %s (%s/h)", numberFormat(xpGained),
				numberFormat((xpGained * 3600000D / runTime)));
	}
	public String GetRCXP() {
		int CurrentXP = client.getSkillExperience(Skill.RUNECRAFT);
		int xpGained = CurrentXP - plugin.startXpRuneCrafting;
		return String.format("XP Gained: %s (%s/h)", numberFormat(xpGained),
				numberFormat((xpGained * 3600000D / runTime)));
	}

	public String getTotalResets() {
		int numberOfResets = Statistics.get().getResets();
		return String.format("Resets: %s (%.2f/h)", numberOfResets, (numberOfResets * 3600000D / runTime));
	}

	@Override
	public Dimension render(Graphics2D g) {
		if (plugin == null)
			return null;

		panelComponent.getChildren().clear();

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT);
		tableComponent.setDefaultColor(Color.ORANGE);

		tableComponent.addRow("X-Kourend Plugin");
		tableComponent.addRow("State:" + State.get().getStatus(), getTotalResets());

		runTime = System.currentTimeMillis() - plugin.startTime;
		int booksGained = Statistics.get().getBooksGained();

		tableComponent.addRow(String.format("Books Gained: %s (%.2f/h)", booksGained, (booksGained * 3600000D / runTime)));
		tableComponent.addRow(GetMagicXP(), GetRCXP());


		if (!tableComponent.isEmpty()) {
			panelComponent.getChildren().add(tableComponent);
		}

		panelComponent.setPreferredSize(new Dimension(500, 1000));
		//panelComponent.setBackgroundColor(Color.darkGray);

		return panelComponent.render(g);
	}
}


