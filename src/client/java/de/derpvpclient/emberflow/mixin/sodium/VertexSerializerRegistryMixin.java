package de.derpvpclient.emberflow.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import de.derpvpclient.emberflow.EmberflowConfig;
import de.derpvpclient.emberflow.RecentSerializer;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sodium looks the converter between two vertex formats up for every box of every mob, behind a lock and a hash map.
 * A frame asks for the same pair thousands of times in a row, so the last answer is kept.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.vertex.serializers.VertexSerializerRegistryImpl", remap = false)
public class VertexSerializerRegistryMixin {
	/** Counts registrations: an answer from before one is not trusted. */
	@Unique
	private volatile int emberflow$generation;

	/** Immutable, so it can be shared between threads without a lock. */
	@Unique
	private RecentSerializer emberflow$recent;

	@WrapMethod(method = "get")
	private VertexSerializer emberflow$recent(VertexFormat source, VertexFormat target, Operation<VertexSerializer> original) {
		if (!EmberflowConfig.fastVertexLookup) {
			return original.call(source, target);
		}

		int generation = this.emberflow$generation;
		RecentSerializer recent = this.emberflow$recent;
		if (recent != null && recent.source() == source && recent.target() == target && recent.generation() == generation) {
			return (VertexSerializer) recent.serializer();
		}

		VertexSerializer found = original.call(source, target);
		this.emberflow$recent = new RecentSerializer(source, target, found, generation);
		return found;
	}

	@Inject(method = "registerSerializer", at = @At("RETURN"))
	private void emberflow$forget(CallbackInfo ci) {
		this.emberflow$generation++;
	}
}
