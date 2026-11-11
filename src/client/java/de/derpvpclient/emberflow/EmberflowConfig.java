package de.derpvpclient.emberflow;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class EmberflowConfig {
	public static int gameMonitor = 1;
	public static int pauseMenuFpsCap = 0;
	public static boolean adaptiveScale = false;
	public static int adaptiveTargetFps = 200;
	public static int adaptiveMinScale = 70;
	public static boolean fastEntityShadows = true;
	public static boolean reuseEntityStates = true;
	public static boolean fastVertexLookup = true;
	/** The player chose to stay on a renderer that is not Vulkan, so the question is not asked again. */
	public static boolean keepBackend = false;

	private EmberflowConfig() {
	}

	public static void load() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("emberflow.properties");
		// The mod used to be called Emberframe: keep the old settings.
		Path old = FabricLoader.getInstance().getConfigDir().resolve("emberframe.properties");
		if (!Files.exists(file) && Files.exists(old)) {
			file = old;
		}
		Properties properties = new Properties();

		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				properties.load(reader);
			} catch (IOException | IllegalArgumentException e) {
				EmberflowClient.LOGGER.warn("Could not read {}, using defaults", file, e);
			}
		}

		// The file can be edited by hand, so every number is brought into a range the mod can work with.
		gameMonitor = Math.clamp(parseInt(properties, "gameMonitor", gameMonitor), 1, 8);
		pauseMenuFpsCap = Math.clamp(parseInt(properties, "pauseMenuFpsCap", pauseMenuFpsCap), 0, 1000);
		adaptiveScale = Boolean.parseBoolean(properties.getProperty("adaptiveScale", String.valueOf(adaptiveScale)));
		adaptiveTargetFps = Math.clamp(parseInt(properties, "adaptiveTargetFps", adaptiveTargetFps), 10, 1000);
		adaptiveMinScale = Math.clamp(parseInt(properties, "adaptiveMinScale", adaptiveMinScale), 25, 100);
		fastEntityShadows = Boolean.parseBoolean(properties.getProperty("fastEntityShadows", String.valueOf(fastEntityShadows)));
		reuseEntityStates = Boolean.parseBoolean(properties.getProperty("reuseEntityStates", String.valueOf(reuseEntityStates)));
		fastVertexLookup = Boolean.parseBoolean(properties.getProperty("fastVertexLookup", String.valueOf(fastVertexLookup)));
		keepBackend = Boolean.parseBoolean(properties.getProperty("keepBackend", String.valueOf(keepBackend)));

		save();
	}

	public static void save() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("emberflow.properties");
		Properties properties = new Properties();
		properties.setProperty("gameMonitor", String.valueOf(gameMonitor));
		properties.setProperty("pauseMenuFpsCap", String.valueOf(pauseMenuFpsCap));
		properties.setProperty("adaptiveScale", String.valueOf(adaptiveScale));
		properties.setProperty("adaptiveTargetFps", String.valueOf(adaptiveTargetFps));
		properties.setProperty("adaptiveMinScale", String.valueOf(adaptiveMinScale));
		properties.setProperty("fastEntityShadows", String.valueOf(fastEntityShadows));
		properties.setProperty("reuseEntityStates", String.valueOf(reuseEntityStates));
		properties.setProperty("fastVertexLookup", String.valueOf(fastVertexLookup));
		properties.setProperty("keepBackend", String.valueOf(keepBackend));

		try (Writer writer = Files.newBufferedWriter(file)) {
			properties.store(writer, "Emberflow. gameMonitor 1 = Hauptbildschirm, 2 = zweiter Bildschirm. pauseMenuFpsCap 0 = kein Limit");
		} catch (IOException e) {
			EmberflowClient.LOGGER.warn("Could not write {}", file, e);
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
