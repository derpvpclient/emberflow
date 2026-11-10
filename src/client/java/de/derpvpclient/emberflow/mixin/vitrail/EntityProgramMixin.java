package de.derpvpclient.emberflow.mixin.vitrail;

import com.llamalad7.mixinextras.sugar.Local;
import de.derpvpclient.emberflow.TangentGuard;
import dev.vitrail.glsl.PackProgram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.vitrail.render.EntityProgram", remap = false)
public class EntityProgramMixin {
	@Inject(method = "of", at = @At("HEAD"))
	private static void emberflow$readInputs(
		CallbackInfoReturnable<?> cir,
		@Local(argsOnly = true) PackProgram.Loaded loaded,
		@Local(argsOnly = true) int load
	) {
		TangentGuard.onProgram(load, loaded.path(), loaded.program().synthesized());
	}
}
