package de.derpvpclient.emberflow;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

/**
 * Whether rain or snow falls in a column only depends on the biome there and on the chunk being loaded. The game
 * still asks the biome for every column around the camera on every frame, so the answer is kept until a chunk or its
 * biomes change. Render thread only.
 */
public final class PrecipitationCache {
	private static final Long2ObjectOpenHashMap<Biome.Precipitation> COLUMNS = new Long2ObjectOpenHashMap<>();
	private static @Nullable ClientLevel level;
	private static int height = Integer.MIN_VALUE;
	private static boolean stale;

	private PrecipitationCache() {
	}

	/** A chunk came, went or got new biomes: nothing kept can be trusted any more. */
	public static void forget() {
		stale = true;
	}

	public static Biome.@Nullable Precipitation get(ClientLevel world, int x, int y, int z) {
		if (stale || world != level || y != height) {
			COLUMNS.clear();
			level = world;
			height = y;
			stale = false;
		}
		return COLUMNS.get(key(x, z));
	}

	public static void put(int x, int z, Biome.Precipitation precipitation) {
		if (COLUMNS.size() > 4096) {
			COLUMNS.clear();
		}
		COLUMNS.put(key(x, z), precipitation);
	}

	private static long key(int x, int z) {
		return (long) x << 32 | z & 0xFFFFFFFFL;
	}
}
