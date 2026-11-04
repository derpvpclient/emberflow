package de.derpvpclient.emberframe;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;

/** Picks the display for the game window. 1 is the Windows main display, 2 the first other one, and so on. */
public final class GameMonitor {
	private GameMonitor() {
	}

	public static long pick() {
		EmberframeConfig.load();
		long primary = GLFW.glfwGetPrimaryMonitor();
		List<Long> others = new ArrayList<>();
		PointerBuffer monitors = GLFW.glfwGetMonitors();

		if (monitors != null) {
			for (int i = monitors.position(); i < monitors.limit(); i++) {
				if (monitors.get(i) != primary) {
					others.add(monitors.get(i));
				}
			}
		}

		int index = EmberframeConfig.gameMonitor - 2;
		return index >= 0 && index < others.size() ? others.get(index) : primary;
	}
}
