package de.derpvpclient.emberframe.mixin;

import de.derpvpclient.emberframe.AdaptiveScale;
import de.derpvpclient.emberframe.Benchmark;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "renderFrame", at = @At("HEAD"))
	private void emberframe$beforeFrame(boolean advanceGameTime, CallbackInfo ci) {
		AdaptiveScale.onFrame((Minecraft) (Object) this);

		if (Benchmark.LABEL != null) {
			Benchmark.onFrame((Minecraft) (Object) this);
		}
	}
}
