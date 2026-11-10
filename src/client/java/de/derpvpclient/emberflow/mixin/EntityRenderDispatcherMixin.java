package de.derpvpclient.emberflow.mixin;

import de.derpvpclient.emberflow.EntityStateCache;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Inject(method = "extractEntity", at = @At("HEAD"), cancellable = true)
	private <E extends Entity> void emberflow$reuseState(E entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
		EntityRenderState state = EntityStateCache.reuse(entity, partialTicks);
		if (state != null) {
			cir.setReturnValue(state);
		}
	}

	@Inject(method = "extractEntity", at = @At("RETURN"))
	private <E extends Entity> void emberflow$captureState(E entity, float partialTicks, CallbackInfoReturnable<EntityRenderState> cir) {
		EntityStateCache.capture(entity, partialTicks, cir.getReturnValue());
	}
}
