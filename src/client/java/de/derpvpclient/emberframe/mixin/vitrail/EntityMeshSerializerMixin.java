package de.derpvpclient.emberframe.mixin.vitrail;

import de.derpvpclient.emberframe.EmberframeClient;
import de.derpvpclient.emberframe.ShadowPass;
import de.derpvpclient.emberframe.TangentGuard;
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

/** Vitrail's loop, without the polygon data the running pack does not read in this pass. */
@Pseudo
@Mixin(value = EntityMeshSerializer.class, remap = false)
public class EntityMeshSerializerMixin {
	@Unique
	private static final int GAME_VERTEX_BYTES = 36;

	@Unique
	private static final boolean FAST_COPY_ALLOWED = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
		&& !Boolean.getBoolean("emberframe.plainCopy");

	@Unique
	private static final boolean VERIFY_COPY = Boolean.getBoolean("emberframe.verifyCopy");

	@Unique
	private static long verifyChecks;

	@Unique
	private static long verifyMismatches;

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
	private void emberframe$serializeWhatIsRead(long written, long carried, int vertices, CallbackInfo ci) {
		boolean shadow = ShadowPass.active;
		if (!TangentGuard.skipsTangent(shadow)) {
			return;
		}

		boolean mid = !TangentGuard.skipsMidTexture(shadow);
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
					emberframe$copyVertex(from, into, identifiers);
					if (VERIFY_COPY) {
						emberframe$verify(from, into, entity, blockEntity, item);
					}
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

		ci.cancel();
	}

	/** Test only: writes the same vertex through Vitrail's own copy and compares every byte. */
	@Unique
	private static void emberframe$verify(long from, long written, short entity, short blockEntity, short item) {
		long reference = MemoryUtil.nmemCalloc(1L, CARRIED);

		try {
			copy(from, reference, entity, blockEntity, item);
			for (int offset = 0; offset < IDENTIFIERS + 8; offset++) {
				if (MemoryUtil.memGetByte(reference + offset) != MemoryUtil.memGetByte(written + offset)) {
					EmberframeClient.LOGGER.error("Vertex copy differs at byte {}", offset);
					verifyMismatches++;
				}
			}

			if (++verifyChecks % 1000000L == 0L) {
				EmberframeClient.LOGGER.info("Vertex copy check: {} vertices, {} differences", verifyChecks, verifyMismatches);
			}
		} finally {
			MemoryUtil.nmemFree(reference);
		}
	}

	/** The game's 36 byte entity vertex as four longs and an int, then the identifiers as one long. */
	@Unique
	private static void emberframe$copyVertex(long from, long into, long identifiers) {
		MemoryUtil.memPutLong(into, MemoryUtil.memGetLong(from));
		MemoryUtil.memPutLong(into + 8L, MemoryUtil.memGetLong(from + 8L));
		MemoryUtil.memPutLong(into + 16L, MemoryUtil.memGetLong(from + 16L));
		MemoryUtil.memPutLong(into + 24L, MemoryUtil.memGetLong(from + 24L));
		MemoryUtil.memPutInt(into + 32L, MemoryUtil.memGetInt(from + 32L));
		MemoryUtil.memPutLong(into + IDENTIFIERS, identifiers);
	}
}
