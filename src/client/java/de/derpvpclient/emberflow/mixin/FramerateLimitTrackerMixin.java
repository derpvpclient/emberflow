package de.derpvpclient.emberflow.mixin;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import de.derpvpclient.emberflow.EmberflowConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public class FramerateLimitTrackerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
	private void emberflow$capPauseMenu(CallbackInfoReturnable<Integer> cir) {
		int cap = EmberflowConfig.pauseMenuFpsCap;
		if (cap > 0 && cap < cir.getReturnValue() && this.minecraft.level != null && this.minecraft.gui.isPausing()) {
			cir.setReturnValue(cap);
		}
	}
}
