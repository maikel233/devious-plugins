package net.runelite.client.plugins.xhunter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum falconids {
	//Woodlands
	Spotted_kebbit(5531, 10125, 1342),
	Dark_kebbit(5532, 10115, 1344),
	Dashing_kebbit(5533, 10127, 1345); // Can also be 1343?

	private final int id;
	private final int fur;
	private final int Falcon;
}
