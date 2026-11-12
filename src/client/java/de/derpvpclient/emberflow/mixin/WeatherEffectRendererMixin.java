package de.derpvpclient.emberflow.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.derpvpclient.emberflow.EmberflowClient;
import de.derpvpclient.emberflow.EmberflowConfig;
import de.derpvpclient.emberflow.PrecipitationCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	/** For testing: also asks the game every time and reports any column where the kept answer is wrong. */
	@Unique
	private static final boolean CHECK = Boolean.getBoolean("emberflow.weatherCheck");

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
	private Biome.Precipitation emberflow$keptPrecipitation(ClientLevel level, BlockPos pos, Operation<Biome.Precipitation> original) {
		if (!EmberflowConfig.fastWeather) {
			return original.call(level, pos);
		}

		int x = pos.getX();
		int z = pos.getZ();
		Biome.Precipitation kept = PrecipitationCache.get(level, x, pos.getY(), z);
		if (kept != null) {
			if (CHECK && kept != original.call(level, pos)) {
				EmberflowClient.LOGGER.error("Kept weather differs at {} {}", x, z);
			}
			return kept;
		}

		Biome.Precipitation found = original.call(level, pos);
		PrecipitationCache.put(x, z, found);
		return found;
	}
}
