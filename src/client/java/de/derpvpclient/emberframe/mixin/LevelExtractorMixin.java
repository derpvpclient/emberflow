package de.derpvpclient.emberframe.mixin;

import de.derpvpclient.emberframe.EntityStateCache;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {
	@Inject(method = "extractVisibleEntities", at = @At("HEAD"))
	private void emberframe$beginCapture(CallbackInfo ci) {
		EntityStateCache.begin();
	}

	@Inject(method = "extractVisibleEntities", at = @At("RETURN"))
	private void emberframe$endCapture(CallbackInfo ci) {
		EntityStateCache.end();
	}
}
