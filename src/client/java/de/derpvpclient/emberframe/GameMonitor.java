package de.derpvpclient.emberframe;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;

/** Picks the display for the game window. 1 is the Windows main display, 2 the first other one, and so on. */
public final class GameMonitor {
	private GameMonitor() {
	}

	public static int pick() {
		EmberframeConfig.load();
		int primary = SDLVideo.SDL_GetPrimaryDisplay();
		List<Integer> others = new ArrayList<>();
		IntBuffer displays = SDLVideo.SDL_GetDisplays();

		if (displays != null) {
			try {
				for (int i = displays.position(); i < displays.limit(); i++) {
					if (displays.get(i) != primary) {
						others.add(displays.get(i));
					}
				}
			} finally {
				SDLStdinc.SDL_free(displays);
			}
		}

		int index = EmberframeConfig.gameMonitor - 2;
		return index >= 0 && index < others.size() ? others.get(index) : primary;
	}
}
