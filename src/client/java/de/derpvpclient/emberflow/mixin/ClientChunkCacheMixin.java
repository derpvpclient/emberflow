package de.derpvpclient.emberflow.mixin;

import de.derpvpclient.emberflow.WeatherCache;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Every way a chunk, its biomes or its light change on the client. */
@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
	@Inject(method = "drop", at = @At("HEAD"))
	private void emberflow$dropped(ChunkPos pos, CallbackInfo ci) {
		WeatherCache.chunkChanged(pos.x(), pos.z());
	}

	@Inject(method = "replaceBiomes", at = @At("HEAD"))
	private void emberflow$newBiomes(int chunkX, int chunkZ, net.minecraft.network.FriendlyByteBuf buffer, CallbackInfo ci) {
		WeatherCache.chunkChanged(chunkX, chunkZ);
	}

	@Inject(method = "replaceWithPacketData", at = @At("HEAD"))
	private void emberflow$newChunk(int chunkX, int chunkZ, net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData data,
		CallbackInfoReturnable<?> cir) {
		WeatherCache.chunkChanged(chunkX, chunkZ);
	}

	@Inject(method = "onLightUpdate", at = @At("HEAD"))
	private void emberflow$lightChanged(LightLayer layer, SectionPos pos, CallbackInfo ci) {
		WeatherCache.lightChanged(pos.x(), pos.z());
	}
}
