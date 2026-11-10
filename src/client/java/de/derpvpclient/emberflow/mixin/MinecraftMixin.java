package de.derpvpclient.emberflow.mixin;

import de.derpvpclient.emberflow.AdaptiveScale;
import de.derpvpclient.emberflow.Benchmark;
import de.derpvpclient.emberflow.EmberflowClient;
import de.derpvpclient.emberflow.EntityStateCache;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "renderFrame", at = @At("HEAD"))
	private void emberflow$beforeFrame(boolean advanceGameTime, CallbackInfo ci) {
		Minecraft minecraft = (Minecraft) (Object) this;
		AdaptiveScale.onFrame(minecraft);

		if (minecraft.level == null) {
			EntityStateCache.forget();
		}

		// The benchmark needs the classes of Sodium, so it is only touched when it was asked for.
		if (EmberflowClient.BENCHMARK) {
			Benchmark.onFrame(minecraft);
		}
	}
}
