package de.derpvpclient.emberflow.mixin;

import com.mojang.blaze3d.platform.Window;
import de.derpvpclient.emberflow.GameMonitor;
import org.lwjgl.sdl.SDLVideo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
	@Shadow
	private int x;

	@Shadow
	private int y;

	@Shadow
	public long handle() {
		throw new AssertionError();
	}

	@Redirect(
		method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/renderpearl/api/device/GpuBackend;I)V",
		at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLVideo;SDL_GetPrimaryDisplay()I")
	)
	private int emberflow$useGameMonitor() {
		return GameMonitor.pick();
	}

	// Fullscreen goes to whichever display holds the window, so it has to be there before setMode.
	@Inject(
		method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/renderpearl/api/device/GpuBackend;I)V",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setMode()V")
	)
	private void emberflow$moveToGameMonitor(CallbackInfo ci) {
		SDLVideo.SDL_SetWindowPosition(this.handle(), this.x, this.y);
		SDLVideo.SDL_SyncWindow(this.handle());
	}
}
