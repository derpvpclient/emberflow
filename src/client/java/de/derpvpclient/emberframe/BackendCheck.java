package de.derpvpclient.emberframe;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;
import java.util.Locale;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PreferredGraphicsApi;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

/**
 * Emberframe only speeds things up on Minecraft's Vulkan renderer. Started on OpenGL, the title screen asks once
 * whether to switch to Vulkan, which needs a restart. Whoever goes straight into a world gets a line in chat instead.
 */
final class BackendCheck {
	private static boolean asked;
	private static boolean told;

	private BackendCheck() {
	}

	static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(BackendCheck::tick);
	}

	private static void tick(Minecraft minecraft) {
		if (asked && told) {
			return;
		}

		String backend = backendName();
		if (backend == null) {
			return;
		}
		if (backend.toLowerCase(Locale.ROOT).contains("vulkan")) {
			asked = told = true;
			return;
		}

		if (!asked && minecraft.gui.screen() instanceof TitleScreen title) {
			asked = told = true;
			EmberframeClient.LOGGER.info("Running on {}, not Vulkan: asking to switch", backend);
			minecraft.gui.setScreen(new ConfirmScreen(
				switchNow -> {
					if (switchNow) {
						switchToVulkan(minecraft);
					} else {
						minecraft.gui.setScreen(title);
					}
				},
				Component.literal("Emberframe").withStyle(ChatFormatting.GOLD),
				Component.translatable("emberframe.notice.switch_question", backend),
				Component.translatable("emberframe.notice.switch"),
				Component.translatable("emberframe.notice.keep", backend)));
			return;
		}

		if (!told && minecraft.player != null) {
			asked = told = true;
			EmberframeClient.LOGGER.info("Running on {}, not Vulkan: Emberframe has nothing to speed up", backend);
			minecraft.gui.hud.getChat().addClientSystemMessage(Component.literal("[Emberframe] ").withStyle(ChatFormatting.GOLD)
				.append(Component.translatable("emberframe.notice.not_vulkan", backend).withStyle(ChatFormatting.YELLOW)));
		}
	}

	/** Saves Vulkan as the graphics API and closes the game, it only changes at the next start. */
	private static void switchToVulkan(Minecraft minecraft) {
		minecraft.options.preferredGraphicsBackend().set(PreferredGraphicsApi.VULKAN);
		minecraft.options.save();
		EmberframeClient.LOGGER.info("Switched the graphics API to Vulkan, closing the game for the restart");
		minecraft.stop();
	}

	/** "Vulkan" or "OpenGL", null if the device is not there yet. */
	static String backendName() {
		GpuDevice device = RenderSystem.tryGetDevice();
		return device != null ? device.getDeviceInfo().backendName() : null;
	}
}
