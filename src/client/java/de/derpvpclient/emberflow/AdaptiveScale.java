package de.derpvpclient.emberflow;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/**
 * Keeps the frame rate at a target by moving Vitrail's render scale, which applies at the next frame without a pack
 * reload. Vitrail and Reflex stay optional and are only reached through method handles.
 * <p>
 * Frame time alone cannot tell a slow GPU from a frame limit or from Reflex sleeping on purpose, and lowering the
 * resolution helps with neither. Where Reflex reports its GPU time estimate, the scale only drops if the GPU really
 * spends the frame budget.
 */
public final class AdaptiveScale {
	private static final long WINDOW_NS = 500000000L;
	private static final long COOLDOWN_NS = 1000000000L;
	private static final int STEP = 5;
	private static final int MAX_STEP = 15;
	private static final int WINDOWS_BEFORE_DOWN = 2;
	private static final int WINDOWS_BEFORE_UP = 6;
	private static final double DOWN_MARGIN = 0.97;
	private static final double UP_MARGIN = 1.05;
	private static final double GPU_BUSY_SHARE = 0.85;

	private static final MethodHandle WANTED;
	private static final MethodHandle CEILING;
	private static final MethodHandle REFLEX_SCHEDULER;
	private static final MethodHandle REFLEX_GPU_TIME;

	private static int scale = -1;
	private static int frames;
	private static long windowStart;
	private static long cooldownUntil;
	private static int slowWindows;
	private static int fastWindows;
	private static long scaleSum;
	private static long scaleSamples;
	private static double lastGpuMs;

	static {
		MethodHandle wanted = null;
		MethodHandle ceiling = null;
		MethodHandle scheduler = null;
		MethodHandle gpuTime = null;
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		if (FabricLoader.getInstance().isModLoaded("vitrail")) {
			try {
				wanted = lookup.findStatic(Class.forName("dev.vitrail.render.RenderScale"), "wanted", MethodType.methodType(void.class, int.class));
				ceiling = lookup.findStatic(Class.forName("dev.vitrail.render.PackChoice"), "renderScale", MethodType.methodType(int.class));
			} catch (ReflectiveOperationException e) {
				EmberflowClient.LOGGER.warn("This Vitrail has no render scale API, adaptive scale is off", e);
				wanted = null;
			}
		}

		if (FabricLoader.getInstance().isModLoaded("reflex")) {
			try {
				Class<?> schedulerClass = Class.forName("io.tythee.ReflexScheduler");
				scheduler = lookup.findStatic(Class.forName("io.tythee.ReflexClient"), "getScheduler", MethodType.methodType(schedulerClass))
					.asType(MethodType.methodType(Object.class));
				gpuTime = lookup.findVirtual(schedulerClass, "getEstimateGpuTime", MethodType.methodType(Long.class))
					.asType(MethodType.methodType(Long.class, Object.class));
			} catch (ReflectiveOperationException e) {
				EmberflowClient.LOGGER.warn("This Reflex has no GPU time estimate, adaptive scale reads frame time only", e);
				gpuTime = null;
			}
		}

		WANTED = wanted;
		CEILING = ceiling;
		REFLEX_SCHEDULER = scheduler;
		REFLEX_GPU_TIME = gpuTime;
	}

	private AdaptiveScale() {
	}

	public static void onFrame(Minecraft minecraft) {
		if (WANTED == null || !EmberflowConfig.adaptiveScale) {
			return;
		}

		frames++;
		long now = System.nanoTime();
		if (now - windowStart < WINDOW_NS) {
			return;
		}

		double fps = frames / ((now - windowStart) / 1.0E9);
		boolean firstWindow = windowStart == 0L;
		windowStart = now;
		frames = 0;

		if (firstWindow || minecraft.level == null || minecraft.gui.isPausing() || minecraft.gui.overlay() != null) {
			slowWindows = 0;
			fastWindows = 0;
			return;
		}

		try {
			update(fps, now);
		} catch (Throwable t) {
			EmberflowClient.LOGGER.error("Adaptive scale failed, turning it off", t);
			EmberflowConfig.adaptiveScale = false;
		}
	}

	public static double averageScale() {
		return scaleSamples == 0L ? 100.0 : (double) scaleSum / scaleSamples;
	}

	public static double lastGpuMs() {
		return lastGpuMs;
	}

	private static void update(double fps, long now) throws Throwable {
		int ceiling = (int) CEILING.invokeExact();
		if (scale < 0 || scale > ceiling) {
			scale = ceiling;
		}

		scaleSum += scale;
		scaleSamples++;
		// A pack reload puts Vitrail back on its saved number, so ours is repeated every window.
		WANTED.invokeExact(scale);

		int target = EmberflowConfig.adaptiveTargetFps;
		double budgetMs = 1000.0 / target;
		double gpuMs = estimateGpuMs();
		lastGpuMs = gpuMs;

		if (now < cooldownUntil) {
			return;
		}

		boolean gpuBusy = gpuMs <= 0.0 || gpuMs > budgetMs * GPU_BUSY_SHARE;
		slowWindows = fps < target * DOWN_MARGIN && gpuBusy ? slowWindows + 1 : 0;

		// Cost follows the pixel count, so a step up is only taken if the target would still hold after it.
		double ratio = (double) scale / (scale + STEP);
		boolean roomToGrow = gpuMs > 0.0 ? gpuMs / (ratio * ratio) * UP_MARGIN < budgetMs : fps * ratio * ratio > target * UP_MARGIN;
		fastWindows = scale < ceiling && roomToGrow ? fastWindows + 1 : 0;

		if (slowWindows >= WINDOWS_BEFORE_DOWN && scale > EmberflowConfig.adaptiveMinScale) {
			int needed = (int) (scale * Math.sqrt(fps / target));
			int drop = Math.clamp((scale - needed) / STEP * STEP, STEP, MAX_STEP);
			apply(Math.max(scale - drop, EmberflowConfig.adaptiveMinScale), now);
		} else if (fastWindows >= WINDOWS_BEFORE_UP) {
			apply(Math.min(scale + STEP, ceiling), now);
		}
	}

	/** GPU milliseconds per frame from Reflex, or 0 when there is no estimate. */
	private static double estimateGpuMs() throws Throwable {
		if (REFLEX_GPU_TIME == null) {
			return 0.0;
		}

		Long nanos = (Long) REFLEX_GPU_TIME.invokeExact((Object) REFLEX_SCHEDULER.invokeExact());
		return nanos == null ? 0.0 : nanos / 1.0E6;
	}

	private static void apply(int newScale, long now) throws Throwable {
		scale = newScale;
		slowWindows = 0;
		fastWindows = 0;
		cooldownUntil = now + COOLDOWN_NS;
		WANTED.invokeExact(scale);
	}
}
