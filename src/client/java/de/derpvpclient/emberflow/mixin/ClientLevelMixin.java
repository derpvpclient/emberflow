package de.derpvpclient.emberflow.mixin;

import de.derpvpclient.emberflow.WeatherCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Blocks changing on the client, which can move the ground under the rain and change its light. */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(method = "setBlocksDirty", at = @At("HEAD"))
	private void emberflow$blockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		WeatherCache.blocksChanged(pos.getX(), pos.getZ(), 1);
	}

	@Inject(method = "sendBlockUpdated", at = @At("HEAD"))
	private void emberflow$blockUpdated(BlockPos pos, BlockState old, BlockState current, int flags, CallbackInfo ci) {
		WeatherCache.blocksChanged(pos.getX(), pos.getZ(), 1);
	}

	@Inject(method = "setSectionDirtyWithNeighbors", at = @At("HEAD"))
	private void emberflow$sectionChanged(int chunkX, int chunkY, int chunkZ, CallbackInfo ci) {
		// The neighbours count too.
		WeatherCache.blocksChanged((chunkX - 1) << 4, (chunkZ - 1) << 4, 48);
	}
}
