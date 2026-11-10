package de.derpvpclient.emberflow.mixin;

import de.derpvpclient.emberflow.AdaptiveScale;
import de.derpvpclient.emberflow.Benchmark;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "renderFrame", at = @At("HEAD"))
	private void emberflow$beforeFrame(boolean advanceGameTime, CallbackInfo ci) {
		AdaptiveScale.onFrame((Minecraft) (Object) this);

		if (Benchmark.LABEL != null) {
			Benchmark.onFrame((Minecraft) (Object) this);
		}
	}
}
