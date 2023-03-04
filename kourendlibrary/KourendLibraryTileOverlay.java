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

import java.util.Optional;
import net.runelite.api.Client;
import net.runelite.client.plugins.kourendlibrary.library.Bookcase;
import net.runelite.client.ui.overlay.*;
import javax.inject.Inject;
import java.awt.*;

public class KourendLibraryTileOverlay extends Overlay
{
	private final Client client;
	private final KourendLibraryPlugin plugin;
	private final KourendLibraryConfig config;


	@Inject
	private KourendLibraryTileOverlay(Client client, KourendLibraryPlugin plugin, KourendLibraryConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin == null)
			return null;

		//Debug stuff
		//Room test = LibraryWalker.get().getCurrentRoom(LocalPlayer.get().getWorldLocation());
		//test.getArea().getRandomLocation().outline(client, graphics, Color.green);

		State.get().getLibrary().getBookcases().stream()
				.filter(bookcase -> bookcase.getPossibleBooks().stream().anyMatch(book -> !book.isDarkManuscript())
						|| Optional.ofNullable(bookcase.getBook()).filter(book -> !book.isDarkManuscript()).isPresent())
				.map(Bookcase::getPosition)
				.forEach(tile -> tile.outline(client,graphics,Color.cyan));//graphics.draw(Projection.getTileBoundsPoly(tile, 0)));

		graphics.setColor(Color.RED);
		State.get().getLastBookcaseTile()
				.ifPresent(tile -> tile.outline(client,graphics,Color.cyan));

		return null;
	}
}
