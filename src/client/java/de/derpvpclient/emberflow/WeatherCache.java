package de.derpvpclient.emberflow;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

/**
 * While it rains or snows, the game asks the same three questions for every column around the camera on every frame:
 * how high the ground is, whether rain or snow falls there, and how bright it is. The answers only change when a chunk,
 * a block or the light changes, so they are kept until then. Render thread only.
 *
 * <p>The columns live in flat arrays around an origin near the camera, and each array is emptied by moving to a new
 * generation, so a frame that can keep nothing costs next to nothing extra.
 */
public final class WeatherCache {
	private static final int MISSING = Integer.MIN_VALUE;
	/** Rain falls at most 10 blocks around the camera, and the origin follows once the camera is 16 blocks away. */
	private static final int RADIUS = 10 + 16 + 1;
	private static final int SIDE = 2 * RADIUS + 1;
	private static final Biome.Precipitation[] PRECIPITATIONS = Biome.Precipitation.values();
	/** Mods that add moving light change the brightness without the light engine knowing, so it is not kept then. */
	private static final boolean MOVING_LIGHT = FabricLoader.getInstance().isModLoaded("lambdynlights")
		|| FabricLoader.getInstance().isModLoaded("sodiumdynamiclights")
		|| FabricLoader.getInstance().isModLoaded("ryoamiclights")
		|| FabricLoader.getInstance().isModLoaded("dynamiclights");

	private static final int[] PRECIPITATION = new int[SIDE * SIDE];
	private static final int[] PRECIPITATION_AGE = new int[SIDE * SIDE];
	private static final int[] HEIGHT = new int[SIDE * SIDE];
	private static final int[] HEIGHT_AGE = new int[SIDE * SIDE];
	private static final int[] LIGHT = new int[SIDE * SIDE];
	private static final int[] LIGHT_Y = new int[SIDE * SIDE];
	private static final int[] LIGHT_AGE = new int[SIDE * SIDE];
	private static int precipitationAge = 1;
	private static int heightAge = 1;
	private static int lightAge = 1;

	private static @Nullable ClientLevel level;
	private static int cameraY = Integer.MIN_VALUE;
	private static int originX;
	private static int originZ;
	private static long tick = Long.MIN_VALUE;
	private static boolean chunksChanged;
	private static boolean blocksChanged;
	private static boolean lightChanged;

	private WeatherCache() {
	}

	/** A chunk came, went or got new biomes. */
	public static void chunkChanged(int chunkX, int chunkZ) {
		if (near(chunkX << 4, chunkZ << 4, 16)) {
			chunksChanged = true;
		}
	}

	/** Blocks in an area changed, which can move the ground and change the light. */
	public static void blocksChanged(int minX, int minZ, int size) {
		if (near(minX, minZ, size)) {
			blocksChanged = true;
		}
	}

	public static void lightChanged(int sectionX, int sectionZ) {
		if (near(sectionX << 4, sectionZ << 4, 16)) {
			lightChanged = true;
		}
	}

	/** Whether anything in the area can touch a kept column, all of which lie within RADIUS of the origin. */
	private static boolean near(int minX, int minZ, int size) {
		return minX + size > originX - RADIUS && minX <= originX + RADIUS && minZ + size > originZ - RADIUS && minZ <= originZ + RADIUS;
	}

	/** Called once per frame before the columns are walked. */
	public static void begin(ClientLevel world, int x, int y, int z) {
		if (chunksChanged || world != level || Math.abs(x - originX) > 16 || Math.abs(z - originZ) > 16) {
			precipitationAge++;
			heightAge++;
			lightAge++;
			level = world;
			originX = x;
			originZ = z;
		} else if (y != cameraY) {
			precipitationAge++;
		}

		// Whatever the events above miss is at most one game tick old.
		long now = world.getGameTime();
		if (blocksChanged || now != tick) {
			heightAge++;
			lightAge++;
		} else if (lightChanged) {
			lightAge++;
		}

		cameraY = y;
		tick = now;
		chunksChanged = false;
		blocksChanged = false;
		lightChanged = false;
	}

	/** The slot of a column, or -1 outside the kept area. */
	public static int slot(int x, int z) {
		int column = x - originX + RADIUS;
		int row = z - originZ + RADIUS;
		return column >= 0 && column < SIDE && row >= 0 && row < SIDE ? row * SIDE + column : -1;
	}

	public static Biome.@Nullable Precipitation precipitation(int slot) {
		return slot >= 0 && PRECIPITATION_AGE[slot] == precipitationAge ? PRECIPITATIONS[PRECIPITATION[slot]] : null;
	}

	public static void precipitation(int slot, Biome.Precipitation value) {
		if (slot >= 0) {
			PRECIPITATION[slot] = value.ordinal();
			PRECIPITATION_AGE[slot] = precipitationAge;
		}
	}

	public static int height(int slot) {
		return slot >= 0 && HEIGHT_AGE[slot] == heightAge ? HEIGHT[slot] : MISSING;
	}

	public static void height(int slot, int value) {
		if (slot >= 0) {
			HEIGHT[slot] = value;
			HEIGHT_AGE[slot] = heightAge;
		}
	}

	public static boolean keepsLight() {
		return !MOVING_LIGHT;
	}

	public static int light(int slot, int y) {
		return slot >= 0 && LIGHT_AGE[slot] == lightAge && LIGHT_Y[slot] == y ? LIGHT[slot] : MISSING;
	}

	public static void light(int slot, int y, int value) {
		if (slot >= 0) {
			LIGHT[slot] = value;
			LIGHT_Y[slot] = y;
			LIGHT_AGE[slot] = lightAge;
		}
	}

	public static boolean missing(int value) {
		return value == MISSING;
	}
}
