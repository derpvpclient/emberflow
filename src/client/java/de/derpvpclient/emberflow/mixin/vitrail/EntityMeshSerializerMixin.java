package de.derpvpclient.emberflow.mixin.vitrail;

import de.derpvpclient.emberflow.EmberflowClient;
import de.derpvpclient.emberflow.ShadowPass;
import de.derpvpclient.emberflow.TangentGuard;
import dev.vitrail.render.EntityFrame;
import dev.vitrail.render.EntityIdentifiers;
import dev.vitrail.sodium.EntityMeshSerializer;
import java.nio.ByteOrder;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The loop of Vitrail, without the polygon data the running pack does not read in this pass.
 * <p>
 * The loop depends on how Vitrail lays out a vertex, and a later Vitrail may do that differently. So the first meshes
 * are still written by Vitrail itself while this loop writes them into a spare buffer, and only if both agree byte for
 * byte does this loop take over. If they ever differ, mobs stay with Vitrail for the rest of the session.
 */
@Pseudo
@Mixin(value = EntityMeshSerializer.class, remap = false)
public class EntityMeshSerializerMixin {
	@Unique
	private static final int GAME_VERTEX_BYTES = 36;

	@Unique
	private static final int TRIALS = 8;

	@Unique
	private static final boolean FAST_COPY_ALLOWED = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
		&& !Boolean.getBoolean("emberflow.plainCopy");

	/** Of a running trial: the spare buffer, the buffer Vitrail writes, the vertex count, mid texture point written. */
	@Unique
	private static final ThreadLocal<long[]> TRIAL = new ThreadLocal<>();

	@Unique
	private static volatile int emberflow$passedTrials;

	@Unique
	private static volatile boolean emberflow$distrusted;

	@Shadow
	@Final
	private static int WRITTEN;

	@Shadow
	@Final
	private static int IDENTIFIERS;

	@Shadow
	@Final
	private static int CARRIED;

	@Shadow
	@Final
	private static int MID_TEX_COORD;

	@Shadow
	@Final
	private static int TANGENT;

	@Shadow
	@Final
	private static int TEX_COORD;

	@Shadow
	private static void copy(long from, long into, short entity, short blockEntity, short item) {
		throw new AssertionError();
	}

	@Inject(method = "serialize", at = @At("HEAD"), cancellable = true)
	private void emberflow$serializeWhatIsRead(long written, long carried, int vertices, CallbackInfo ci) {
		boolean shadow = ShadowPass.active;
		if (emberflow$distrusted || !TangentGuard.skipsTangent(shadow)) {
			return;
		}

		boolean mid = !TangentGuard.skipsMidTexture(shadow);

		if (emberflow$passedTrials < TRIALS) {
			// Vitrail writes this mesh itself, the comparison follows when it is done.
			emberflow$startTrial(written, carried, vertices, mid);
			return;
		}

		emberflow$write(written, carried, vertices, mid);
		ci.cancel();
	}

	@Inject(method = "serialize", at = @At("RETURN"))
	private void emberflow$compareTrial(long written, long carried, int vertices, CallbackInfo ci) {
		long[] trial = TRIAL.get();
		if (trial == null) {
			return;
		}

		TRIAL.remove();

		try {
			if (emberflow$distrusted || trial[1] != carried || trial[2] != vertices) {
				return;
			}

			int difference = emberflow$firstDifference(trial[0], carried, vertices, trial[3] != 0L);
			if (difference >= 0) {
				emberflow$distrusted = true;
				EmberflowClient.LOGGER.warn("This Vitrail writes mob vertices differently than Emberflow expects (byte {} of a vertex), leaving mobs to Vitrail", difference);
			} else if (++emberflow$passedTrials == TRIALS) {
				EmberflowClient.LOGGER.info("Mob vertices match what Vitrail writes, the faster path is on");
			}
		} finally {
			MemoryUtil.nmemFree(trial[0]);
		}
	}

	@Unique
	private static void emberflow$startTrial(long written, long carried, int vertices, boolean mid) {
		long[] unfinished = TRIAL.get();
		if (unfinished != null) {
			TRIAL.remove();
			MemoryUtil.nmemFree(unfinished[0]);
		}

		if (!emberflow$layoutFits()) {
			emberflow$distrusted = true;
			EmberflowClient.LOGGER.warn("This Vitrail has a mob vertex layout Emberflow does not know, leaving mobs to Vitrail");
			return;
		}

		if (vertices < 4) {
			return;
		}

		long bytes = (long) vertices * CARRIED;
		long ours = MemoryUtil.nmemAlloc(bytes);
		if (ours == 0L) {
			return;
		}

		// Bytes neither side writes have to start out the same.
		MemoryUtil.memCopy(carried, ours, bytes);
		emberflow$write(written, ours, vertices, mid);
		TRIAL.set(new long[] {ours, carried, vertices, mid ? 1L : 0L});
	}

	/** Everything this loop touches has to lie inside one vertex. */
	@Unique
	private static boolean emberflow$layoutFits() {
		return WRITTEN > 0 && CARRIED >= WRITTEN
			&& TEX_COORD >= 0 && TEX_COORD + 8 <= WRITTEN
			&& IDENTIFIERS >= 0 && IDENTIFIERS + 8 <= CARRIED
			&& MID_TEX_COORD >= 0 && MID_TEX_COORD + 8 <= CARRIED
			&& TANGENT >= 0 && TANGENT + 4 <= CARRIED;
	}

	/** The first byte of a vertex where both buffers differ, or -1. Left out is what this loop skips on purpose. */
	@Unique
	private static int emberflow$firstDifference(long ours, long theirs, int vertices, boolean mid) {
		for (int vertex = 0; vertex < vertices; vertex++) {
			long base = (long) vertex * CARRIED;

			for (int offset = 0; offset < CARRIED; offset++) {
				boolean tangent = offset >= TANGENT && offset < TANGENT + 4;
				boolean skippedMid = !mid && offset >= MID_TEX_COORD && offset < MID_TEX_COORD + 8;

				if (!tangent && !skippedMid && MemoryUtil.memGetByte(ours + base + offset) != MemoryUtil.memGetByte(theirs + base + offset)) {
					return offset;
				}
			}
		}

		return -1;
	}

	@Unique
	private static void emberflow$write(long written, long carried, int vertices, boolean mid) {
		boolean fastCopy = FAST_COPY_ALLOWED && WRITTEN == GAME_VERTEX_BYTES;
		short entity = (short) EntityIdentifiers.entity();
		short blockEntity = (short) EntityIdentifiers.blockEntity();
		short item = (short) EntityIdentifiers.item();
		long identifiers = (entity & 0xFFFFL) | (blockEntity & 0xFFFFL) << 16 | (item & 0xFFFFL) << 32;
		int quads = vertices >> 2;
		long from = written;
		long into = carried;

		for (int quad = 0; quad < quads; quad++) {
			float midU = 0.0F;
			float midV = 0.0F;

			if (mid) {
				long second = from + WRITTEN;
				long third = second + WRITTEN;
				long fourth = third + WRITTEN;
				midU = (MemoryUtil.memGetFloat(from + TEX_COORD)
					+ MemoryUtil.memGetFloat(second + TEX_COORD)
					+ MemoryUtil.memGetFloat(third + TEX_COORD)
					+ MemoryUtil.memGetFloat(fourth + TEX_COORD)) * 0.25F;
				midV = (MemoryUtil.memGetFloat(from + TEX_COORD + 4L)
					+ MemoryUtil.memGetFloat(second + TEX_COORD + 4L)
					+ MemoryUtil.memGetFloat(third + TEX_COORD + 4L)
					+ MemoryUtil.memGetFloat(fourth + TEX_COORD + 4L)) * 0.25F;
			}

			for (int corner = 0; corner < 4; corner++) {
				if (fastCopy) {
					emberflow$copyVertex(from, into, identifiers);
				} else {
					copy(from, into, entity, blockEntity, item);
				}

				MemoryUtil.memPutFloat(into + MID_TEX_COORD, midU);
				MemoryUtil.memPutFloat(into + MID_TEX_COORD + 4L, midV);
				MemoryUtil.memPutInt(into + TANGENT, EntityFrame.FLAT);
				from += WRITTEN;
				into += CARRIED;
			}
		}

		for (int vertex = quads << 2; vertex < vertices; vertex++) {
			copy(from, into, entity, blockEntity, item);
			MemoryUtil.memPutFloat(into + MID_TEX_COORD, 0.0F);
			MemoryUtil.memPutFloat(into + MID_TEX_COORD + 4L, 0.0F);
			MemoryUtil.memPutInt(into + TANGENT, EntityFrame.FLAT);
			from += WRITTEN;
			into += CARRIED;
		}
	}

	/** The 36 byte entity vertex of the game as four longs and an int, then the identifiers as one long. */
	@Unique
	private static void emberflow$copyVertex(long from, long into, long identifiers) {
		MemoryUtil.memPutLong(into, MemoryUtil.memGetLong(from));
		MemoryUtil.memPutLong(into + 8L, MemoryUtil.memGetLong(from + 8L));
		MemoryUtil.memPutLong(into + 16L, MemoryUtil.memGetLong(from + 16L));
		MemoryUtil.memPutLong(into + 24L, MemoryUtil.memGetLong(from + 24L));
		MemoryUtil.memPutInt(into + 32L, MemoryUtil.memGetInt(from + 32L));
		MemoryUtil.memPutLong(into + IDENTIFIERS, identifiers);
	}
}
