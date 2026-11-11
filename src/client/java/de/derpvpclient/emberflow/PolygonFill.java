package de.derpvpclient.emberflow;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Vitrail works a tangent out for every polygon that goes through the game's own vertex buffer. In the shadow map
 * that is work for nothing when the pack's shadow programs do not read it, the flat tangent every vertex starts
 * with does there.
 * <p>
 * Both the flat value and the mid block field are Vitrail's own. If a later Vitrail names them differently they are
 * not found, and every polygon stays with Vitrail.
 */
public final class PolygonFill {
	private static final boolean KNOWN;
	private static final int FLAT;
	private static final VarHandle MID_BLOCK;

	static {
		boolean known = false;
		int flat = 0;
		VarHandle midBlock = null;

		try {
			flat = Class.forName("dev.vitrail.render.EntityFrame").getField("FLAT").getInt(null);
			midBlock = MethodHandles.privateLookupIn(BufferBuilder.class, MethodHandles.lookup())
				.findVarHandle(BufferBuilder.class, "vitrail$midBlock", int.class);
			known = true;
		} catch (ReflectiveOperationException | RuntimeException e) {
			EmberflowClient.LOGGER.warn("This Vitrail builds its polygons differently, leaving them to Vitrail: {}", e.toString());
		}

		KNOWN = known;
		FLAT = flat;
		MID_BLOCK = midBlock;
	}

	private PolygonFill() {
	}

	/** True while the shadow map is drawn on this thread and its programs do not read the tangent. */
	public static boolean flatTangent() {
		return KNOWN && ShadowPass.drawingHere() && TangentGuard.skipsTangent(true);
	}

	/**
	 * The face normal only feeds the tangent, except in a buffer with a mid block: there Vitrail also writes it back
	 * into the vertices, and that stays.
	 */
	public static boolean unusedFaceNormal(BufferBuilder builder) {
		return flatTangent() && (int) MID_BLOCK.get(builder) < 0;
	}

	public static int flat() {
		return FLAT;
	}
}
