package de.derpvpclient.emberflow.mixin;

import de.derpvpclient.emberflow.EmberflowClient;
import de.derpvpclient.emberflow.EmberflowConfig;
import de.derpvpclient.emberflow.WeatherCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps what the rain and snow columns ask about the world, see {@link WeatherCache}. */
@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererMixin {
	/** For testing: also asks the game every time and reports any column where a kept answer is wrong. */
	@Unique
	private static final boolean CHECK = Boolean.getBoolean("emberflow.weatherCheck");

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void emberflow$beginColumns(ClientLevel level, float partialTicks, Vec3 cameraPos, WeatherRenderState renderState, CallbackInfo ci) {
		if (EmberflowConfig.fastWeather) {
			WeatherCache.begin(level, Mth.floor(cameraPos.x), Mth.floor(cameraPos.y), Mth.floor(cameraPos.z));
		}
	}

	@Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/ClientLevel;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
	private int emberflow$keptHeight(ClientLevel level, Heightmap.Types type, int x, int z) {
		if (!EmberflowConfig.fastWeather) {
			return level.getHeight(type, x, z);
		}

		int slot = WeatherCache.slot(x, z);
		int kept = WeatherCache.height(slot);
		if (!WeatherCache.missing(kept)) {
			if (CHECK && kept != level.getHeight(type, x, z)) {
				EmberflowClient.LOGGER.error("Kept ground height differs at {} {}", x, z);
			}
			return kept;
		}

		int found = level.getHeight(type, x, z);
		WeatherCache.height(slot, found);
		return found;
	}

	@Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
	private Biome.Precipitation emberflow$keptPrecipitation(ClientLevel level, BlockPos pos) {
		if (!EmberflowConfig.fastWeather) {
			return level.getPrecipitationAt(pos);
		}

		int x = pos.getX();
		int z = pos.getZ();
		int slot = WeatherCache.slot(x, z);
		Biome.Precipitation kept = WeatherCache.precipitation(slot);
		if (kept != null) {
			if (CHECK && kept != level.getPrecipitationAt(pos)) {
				EmberflowClient.LOGGER.error("Kept weather differs at {} {}", x, z);
			}
			return kept;
		}

		Biome.Precipitation found = level.getPrecipitationAt(pos);
		WeatherCache.precipitation(slot, found);
		return found;
	}

	@Redirect(method = "extractRenderState", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/util/LightCoordsUtil;getLightCoords(Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/core/BlockPos;)I"))
	private int emberflow$keptLight(BlockAndLightGetter level, BlockPos pos) {
		if (!EmberflowConfig.fastWeather || !WeatherCache.keepsLight()) {
			return LightCoordsUtil.getLightCoords(level, pos);
		}

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		int slot = WeatherCache.slot(x, z);
		int kept = WeatherCache.light(slot, y);
		if (!WeatherCache.missing(kept)) {
			if (CHECK && kept != LightCoordsUtil.getLightCoords(level, pos)) {
				EmberflowClient.LOGGER.error("Kept light differs at {} {} {}", x, y, z);
			}
			return kept;
		}

		int found = LightCoordsUtil.getLightCoords(level, pos);
		WeatherCache.light(slot, y, found);
		return found;
	}
}
