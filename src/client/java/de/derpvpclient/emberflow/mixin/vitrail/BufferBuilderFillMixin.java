package de.derpvpclient.emberflow.mixin.vitrail;

import com.mojang.blaze3d.vertex.BufferBuilder;
import de.derpvpclient.emberflow.PolygonFill;
import dev.vitrail.render.EntityFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Hooks the method Vitrail adds to the game's vertex buffer, see {@link PolygonFill}. Without Vitrail nothing matches. */
@Mixin(value = BufferBuilder.class, priority = 1500)
public class BufferBuilderFillMixin {
	@Redirect(
		method = "vitrail$fillQuad",
		at = @At(value = "INVOKE", target = "Ldev/vitrail/render/EntityFrame;faceNormal([FFFFFFFFFFFFF)Z", remap = false),
		remap = false,
		require = 0,
		expect = 0
	)
	private boolean emberflow$skipUnusedFaceNormal(
		float[] face, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz
	) {
		if (PolygonFill.unusedFaceNormal((BufferBuilder) (Object) this)) {
			return false;
		}

		return EntityFrame.faceNormal(face, ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz);
	}

	@Redirect(
		method = "vitrail$fillQuad",
		at = @At(value = "INVOKE", target = "Ldev/vitrail/render/EntityFrame;tangent(FFFZFFFFFFFFFFFFFFF)I", remap = false),
		remap = false,
		require = 0,
		expect = 0
	)
	private int emberflow$skipUnreadTangent(
		float nx, float ny, float nz, boolean smooth,
		float ax, float ay, float az, float au, float av,
		float bx, float by, float bz, float bu, float bv,
		float cx, float cy, float cz, float cu, float cv
	) {
		if (PolygonFill.flatTangent()) {
			return PolygonFill.flat();
		}

		return EntityFrame.tangent(nx, ny, nz, smooth, ax, ay, az, au, av, bx, by, bz, bu, bv, cx, cy, cz, cu, cv);
	}
}
