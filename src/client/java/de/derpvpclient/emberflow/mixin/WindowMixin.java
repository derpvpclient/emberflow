package de.derpvpclient.emberflow.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.Window;
import de.derpvpclient.emberflow.GameMonitor;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
	@Shadow
	private int x;

	@Shadow
	private int y;

	@Shadow
	@Final
	private long handle;

	// Not a redirect: two mods redirecting the same call would crash the game.
	@ModifyExpressionValue(
		method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/blaze3d/systems/GpuBackend;)V",
		at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetPrimaryMonitor()J")
	)
	private long emberflow$useGameMonitor(long primary) {
		return GameMonitor.pick();
	}

	// Fullscreen goes to whichever display holds the window, so it has to be there before setMode.
	@Inject(
		method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/blaze3d/systems/GpuBackend;)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setMode()V")
	)
	private void emberflow$moveToGameMonitor(CallbackInfo ci) {
		GLFW.glfwSetWindowPos(this.handle, this.x, this.y);
	}
}
