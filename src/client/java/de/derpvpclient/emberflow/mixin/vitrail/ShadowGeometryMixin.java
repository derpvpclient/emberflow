package de.derpvpclient.emberflow.mixin.vitrail;

import de.derpvpclient.emberflow.ShadowPass;
import dev.vitrail.render.ShadowGeometry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = ShadowGeometry.class, remap = false)
public class ShadowGeometryMixin {
	@Inject(method = "gather", at = @At("HEAD"))
	private static void emberflow$enterGather(CallbackInfo ci) {
		ShadowPass.gathering = true;
	}

	@Inject(method = "gather", at = @At("RETURN"))
	private static void emberflow$leaveGather(CallbackInfo ci) {
		ShadowPass.gathering = false;
	}

	@Inject(method = "draw", at = @At("HEAD"))
	private static void emberflow$enterDraw(CallbackInfo ci) {
		ShadowPass.enterDraw();
	}

	@Inject(method = "draw", at = @At("RETURN"))
	private static void emberflow$leaveDraw(CallbackInfo ci) {
		ShadowPass.leaveDraw();
	}
}
