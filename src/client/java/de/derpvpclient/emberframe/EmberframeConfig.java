package de.derpvpclient.emberframe;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class EmberframeConfig {
	public static int gameMonitor = 1;
	public static int pauseMenuFpsCap = 0;
	public static boolean adaptiveScale = false;
	public static int adaptiveTargetFps = 200;
	public static int adaptiveMinScale = 70;
	public static boolean fastEntityShadows = true;
	public static boolean reuseEntityStates = true;

	private EmberframeConfig() {
	}

	public static void load() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("emberframe.properties");
		Properties properties = new Properties();

		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				properties.load(reader);
			} catch (IOException | IllegalArgumentException e) {
				EmberframeClient.LOGGER.warn("Could not read {}, using defaults", file, e);
			}
		}

		gameMonitor = parseInt(properties, "gameMonitor", gameMonitor);
		pauseMenuFpsCap = parseInt(properties, "pauseMenuFpsCap", pauseMenuFpsCap);
		adaptiveScale = Boolean.parseBoolean(properties.getProperty("adaptiveScale", String.valueOf(adaptiveScale)));
		adaptiveTargetFps = parseInt(properties, "adaptiveTargetFps", adaptiveTargetFps);
		adaptiveMinScale = parseInt(properties, "adaptiveMinScale", adaptiveMinScale);
		fastEntityShadows = Boolean.parseBoolean(properties.getProperty("fastEntityShadows", String.valueOf(fastEntityShadows)));
		reuseEntityStates = Boolean.parseBoolean(properties.getProperty("reuseEntityStates", String.valueOf(reuseEntityStates)));

		save();
	}

	public static void save() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("emberframe.properties");
		Properties properties = new Properties();
		properties.setProperty("gameMonitor", String.valueOf(gameMonitor));
		properties.setProperty("pauseMenuFpsCap", String.valueOf(pauseMenuFpsCap));
		properties.setProperty("adaptiveScale", String.valueOf(adaptiveScale));
		properties.setProperty("adaptiveTargetFps", String.valueOf(adaptiveTargetFps));
		properties.setProperty("adaptiveMinScale", String.valueOf(adaptiveMinScale));
		properties.setProperty("fastEntityShadows", String.valueOf(fastEntityShadows));
		properties.setProperty("reuseEntityStates", String.valueOf(reuseEntityStates));

		try (Writer writer = Files.newBufferedWriter(file)) {
			properties.store(writer, "Emberframe. gameMonitor 1 = Hauptbildschirm, 2 = zweiter Bildschirm. pauseMenuFpsCap 0 = kein Limit");
		} catch (IOException e) {
			EmberframeClient.LOGGER.warn("Could not write {}", file, e);
		}
	}

	private static int parseInt(Properties properties, String key, int fallback) {
		try {
			return Integer.parseInt(properties.getProperty(key, String.valueOf(fallback)).trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
