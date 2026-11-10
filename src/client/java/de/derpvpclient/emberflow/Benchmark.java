package de.derpvpclient.emberflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.caffeinemc.mods.sodium.client.config.ConfigManager;
import net.caffeinemc.mods.sodium.client.config.structure.ModOptions;
import net.caffeinemc.mods.sodium.client.config.structure.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.MinecraftServer;

/**
 * Repeatable measurement, only active with -Demberflow.bench=label. Spins the camera at a fixed rate, then writes
 * average FPS and lows to emberflow/bench.txt and closes the game.
 */
public final class Benchmark {
	public static final String LABEL = System.getProperty("emberflow.bench");
	private static final long WARMUP_NS = Long.getLong("emberflow.benchWarmup", 45L) * 1000000000L;
	private static final long DURATION_NS = Long.getLong("emberflow.benchSeconds", 60L) * 1000000000L;
	private static final boolean SHOT = Boolean.getBoolean("emberflow.benchShot");
	private static final boolean MENU = Boolean.getBoolean("emberflow.benchMenu");
	private static final boolean SODIUM_MENU = Boolean.getBoolean("emberflow.benchSodiumMenu");
	private static final double SHOT_HEIGHT = 14.0;
	private static final double SHOT_BACK = 12.0;
	private static final float DEGREES_PER_SECOND = 18.0F;
	private static final double FLIGHT_SPEED = 10.0;
	private static final double FLIGHT_HEIGHT = 12.0;
	private static final double LOOK_SWEEP = 40.0;
	private static final List<String> SETUP_COMMANDS = List.of(
		"gamemode creative @a",
		"gamerule advance_time false",
		"gamerule advance_weather false",
		"gamerule spawn_mobs false",
		"time set noon",
		"weather rain"
	);
	private static final List<String> STRESS_MOBS = List.of("cow", "sheep", "villager", "zombie");
	private static final int STRESS_RADIUS = 10;
	private static final double ARENA_SECONDS = 20.0;

	private static long[] frames = new long[1 << 20];
	private static int count;
	private static long worldStart;
	private static long recordStart;
	private static long lastFrame;
	private static boolean finished;
	private static boolean menuOpened;
	private static OptionPage sodiumPage;
	private static double startX;
	private static double startY;
	private static double startZ;
	private static int entities;
	private static boolean raining;

	private Benchmark() {
	}

	public static void onFrame(Minecraft minecraft) {
		if (finished || minecraft.player == null || minecraft.gui.overlay() != null) {
			return;
		}

		long now = System.nanoTime();
		if (worldStart == 0L) {
			worldStart = now;
			freezeWorld(minecraft);
		}

		if (SHOT) {
			shot(minecraft, now);
			return;
		}

		if (now - worldStart < WARMUP_NS) {
			hold(minecraft, (now - worldStart) / 1.0E9);
			return;
		}

		if (recordStart == 0L) {
			recordStart = now;
			startX = minecraft.player.getX();
			startY = minecraft.player.getY() + FLIGHT_HEIGHT;
			startZ = minecraft.player.getZ();
			entities = minecraft.level.getEntityCount();
			raining = minecraft.level.isRaining();
		} else if (count < frames.length) {
			frames[count++] = now - lastFrame;
		}

		lastFrame = now;
		fly(minecraft, (now - recordStart) / 1.0E9);

		if (now - recordStart >= DURATION_NS) {
			finished = true;
			write(now - recordStart);
			minecraft.stop();
		}
	}

	/** Holds one fixed view and saves a picture of it, so that two builds can be compared pixel by pixel. */
	private static void shot(Minecraft minecraft, long now) {
		if (startY == 0.0) {
			startX = minecraft.player.getX();
			startY = minecraft.player.getY() + SHOT_HEIGHT;
			startZ = minecraft.player.getZ() - SHOT_BACK;
		}

		minecraft.player.getAbilities().flying = true;
		minecraft.player.setDeltaMovement(0.0, 0.0, 0.0);
		minecraft.player.setPos(startX, startY, startZ);
		minecraft.player.setYRot(0.0F);
		minecraft.player.setXRot(50.0F);

		if ((MENU || SODIUM_MENU) && !menuOpened && now - worldStart >= WARMUP_NS / 2L) {
			menuOpened = true;
			if (SODIUM_MENU) {
				openSodiumPage(minecraft);
			} else {
				minecraft.gui.setScreen(EmberflowConfigScreen.create(null));
			}
		}

		if (sodiumPage != null && now - worldStart >= WARMUP_NS * 3L / 4L) {
			selectSodiumPage(minecraft.gui.screen());
			sodiumPage = null;
		}

		if (now - worldStart >= WARMUP_NS) {
			finished = true;
			Path file = FabricLoader.getInstance().getGameDir().resolve("emberflow").resolve("shot-" + LABEL + ".png");
			Screenshot.takeScreenshot(minecraft.gameRenderer.mainRenderTarget(), image -> {
				try (image) {
					image.writeToFile(file);
				} catch (IOException e) {
					EmberflowClient.LOGGER.error("Could not save the benchmark picture", e);
				}

				minecraft.stop();
			});
		}
	}

	/** Opens the video settings. Reese's Sodium Options swaps in its own screen, so the page is picked once it is open. */
	private static void openSodiumPage(Minecraft minecraft) {
		for (ModOptions options : ConfigManager.CONFIG.getModOptions()) {
			if ("emberflow".equals(options.configId())) {
				sodiumPage = (OptionPage) options.pages().getFirst();
				minecraft.gui.setScreen(VideoSettingsScreen.createScreen(null, sodiumPage));
				return;
			}
		}

		EmberflowClient.LOGGER.warn("The video settings have no Emberflow page");
	}

	private static void selectSodiumPage(Screen screen) {
		if (screen instanceof VideoSettingsScreen video) {
			video.jumpToPage(sodiumPage);
			return;
		}

		try {
			Object frame = screen.getClass().getMethod("rso$getTabFrame").invoke(screen);
			for (Object tab : (List<?>) frame.getClass().getMethod("getTabs").invoke(frame)) {
				ModOptions options = (ModOptions) tab.getClass().getMethod("getModOptions").invoke(tab);
				if ("emberflow".equals(options.configId())) {
					frame.getClass().getMethod("setTab", Optional.class).invoke(frame, Optional.of(tab));
					EmberflowClient.LOGGER.info("Benchmark selected the Emberflow tab in {}", screen.getClass().getSimpleName());
					return;
				}
			}

			EmberflowClient.LOGGER.warn("{} has no Emberflow tab", screen.getClass().getSimpleName());
		} catch (ReflectiveOperationException e) {
			EmberflowClient.LOGGER.warn("Could not select the Emberflow tab in {}", screen.getClass().getName(), e);
		}
	}

	/** Same light, same weather and a player that cannot die, so runs differ only by what is being tested. */
	private static void freezeWorld(Minecraft minecraft) {
		MinecraftServer server = minecraft.getSingleplayerServer();
		if (server == null) {
			return;
		}

		server.execute(() -> {
			for (String command : SETUP_COMMANDS) {
				server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
			}

			if (SHOT) {
				server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "weather clear");
				server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set 2000");
			}

			// About 400 standing mobs around the player, the entity load of a busy base or farm.
			int index = 0;
			for (int x = -STRESS_RADIUS; x < STRESS_RADIUS; x++) {
				for (int z = -STRESS_RADIUS; z < STRESS_RADIUS; z++) {
					String mob = STRESS_MOBS.get(index++ % STRESS_MOBS.size());
					server.getCommands().performPrefixedCommand(
						server.createCommandSourceStack(),
						"execute at @p run summon minecraft:%s ~%d ~ ~%d {NoAI:1b,PersistenceRequired:1b}".formatted(mob, x * 2, z * 2)
					);
				}
			}
		});
	}

	private static void hold(Minecraft minecraft, double seconds) {
		minecraft.player.getAbilities().flying = true;
		minecraft.player.setDeltaMovement(0.0, 0.0, 0.0);
		minecraft.player.setYRot((float) (seconds * DEGREES_PER_SECOND % 360.0));
		minecraft.player.setXRot(10.0F);
	}

	/** Flies east over new ground while looking around, which is what makes chunks load as in real play. */
	private static void fly(Minecraft minecraft, double seconds) {
		if (seconds < ARENA_SECONDS) {
			hold(minecraft, seconds);
			return;
		}

		double flown = seconds - ARENA_SECONDS;
		minecraft.player.getAbilities().flying = true;
		minecraft.player.setDeltaMovement(0.0, 0.0, 0.0);
		minecraft.player.setPos(startX + flown * FLIGHT_SPEED, startY, startZ);
		minecraft.player.setYRot((float) (-90.0 + LOOK_SWEEP * Math.sin(flown / 3.0)));
		minecraft.player.setXRot(10.0F);
	}

	private static void write(long elapsed) {
		long[] sorted = Arrays.copyOf(frames, count);
		Arrays.sort(sorted);
		long median = sorted[count / 2];
		String line = "%s | frames %d | avg %.1f fps | arena %.1f | flight %.1f | 1%% low %.1f | 0.1%% low %.1f | worst %.1f ms | p50 %.2f p99 %.2f ms | scale %.0f%% | gpu %.2f ms | entities %d | rain %b%n".formatted(
			LABEL,
			count,
			count / (elapsed / 1.0E9),
			phaseFps(0.0, ARENA_SECONDS),
			phaseFps(ARENA_SECONDS, Double.MAX_VALUE),
			low(sorted, 0.01),
			low(sorted, 0.001),
			sorted[count - 1] / 1.0E6,
			median / 1.0E6,
			sorted[(int) (count * 0.99)] / 1.0E6,
			AdaptiveScale.averageScale(),
			AdaptiveScale.lastGpuMs(),
			entities,
			raining
		);

		try {
			Path dir = FabricLoader.getInstance().getGameDir().resolve("emberflow");
			Files.createDirectories(dir);
			Files.writeString(dir.resolve("bench.txt"), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			Files.writeString(dir.resolve("spikes-" + LABEL + ".txt"), spikes(median * 3L));
		} catch (IOException e) {
			EmberflowClient.LOGGER.error("Could not write benchmark result", e);
		}
	}

	private static double phaseFps(double fromSeconds, double toSeconds) {
		long time = 0L;
		long phaseTime = 0L;
		int phaseFrames = 0;

		for (int i = 0; i < count; i++) {
			time += frames[i];
			double seconds = time / 1.0E9;
			if (seconds >= fromSeconds && seconds < toSeconds) {
				phaseTime += frames[i];
				phaseFrames++;
			}
		}

		return phaseTime == 0L ? 0.0 : phaseFrames / (phaseTime / 1.0E9);
	}

	/** One line per frame slower than the threshold: seconds into the recording, then its length in ms. */
	private static String spikes(long threshold) {
		StringBuilder result = new StringBuilder();
		long time = 0L;

		for (int i = 0; i < count; i++) {
			time += frames[i];
			if (frames[i] > threshold) {
				result.append("%.3f %.2f%n".formatted(time / 1.0E9, frames[i] / 1.0E6));
			}
		}

		return result.toString();
	}

	/** FPS over the slowest given fraction of frames. */
	private static double low(long[] sorted, double fraction) {
		int slowest = Math.max(1, (int) (sorted.length * fraction));
		long total = 0L;

		for (int i = sorted.length - slowest; i < sorted.length; i++) {
			total += sorted[i];
		}

		return slowest / (total / 1.0E9);
	}
}
