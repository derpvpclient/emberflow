package de.derpvpclient.emberflow.mixin;

import de.derpvpclient.emberflow.PrecipitationCache;
import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every way a chunk or its biomes change on the client. */
@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
	@Inject(method = {"drop", "replaceBiomes", "replaceWithPacketData"}, at = @At("HEAD"))
	private void emberflow$chunkChanged(CallbackInfo ci) {
		PrecipitationCache.forget();
	}
}
