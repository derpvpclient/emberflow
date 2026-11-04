package de.derpvpclient.emberframe;

import java.util.Map;

/**
 * Tracks whether the loaded shader pack really reads the per-polygon data of mobs. Vitrail lists the inputs of every
 * program it prepares, after the pack's own preprocessor switches, so this follows the pack's settings as well.
 * Until a program was seen the answer is that the data is needed.
 */
public final class TangentGuard {
	private static final String TANGENT = "at_tangent";
	private static final String MID_TEX_COORD = "mc_midTexCoord";

	private static volatile int load = -1;
	private static volatile boolean tangentInWorld;
	private static volatile boolean tangentInShadow;
	private static volatile boolean midInWorld;
	private static volatile boolean midInShadow;

	private TangentGuard() {
	}

	public static void onProgram(int programLoad, String path, Map<String, String> inputs) {
		if (programLoad != load) {
			tangentInWorld = false;
			tangentInShadow = false;
			midInWorld = false;
			midInShadow = false;
			load = programLoad;
			EmberframeClient.LOGGER.info("Reading mob program inputs of pack load {}", programLoad);
		}

		boolean shadow = path.substring(path.lastIndexOf('/') + 1).startsWith("shadow");
		boolean tangent = inputs.containsKey(TANGENT);
		boolean mid = inputs.containsKey(MID_TEX_COORD);

		if (shadow) {
			tangentInShadow |= tangent;
			midInShadow |= mid;
		} else {
			tangentInWorld |= tangent;
			midInWorld |= mid;
		}

		if (tangent || mid) {
			EmberframeClient.LOGGER.info("{} reads{}{}", path, tangent ? " tangent" : "", mid ? " mid texture" : "");
		}
	}

	public static boolean skipsTangent(boolean shadowPass) {
		return EmberframeConfig.fastEntityShadows && load >= 0 && !(shadowPass ? tangentInShadow : tangentInWorld);
	}

	public static boolean skipsMidTexture(boolean shadowPass) {
		return EmberframeConfig.fastEntityShadows && load >= 0 && !(shadowPass ? midInShadow : midInWorld);
	}
}
