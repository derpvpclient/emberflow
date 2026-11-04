package de.derpvpclient.emberframe;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EmberframeConfigScreen {
	private EmberframeConfigScreen() {
	}

	public static Screen create(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
			.title(Component.literal("Emberframe"))
			.category(ConfigCategory.createBuilder()
				.name(Component.translatable("emberframe.group.performance"))
				.group(OptionGroup.createBuilder()
					.name(Component.translatable("emberframe.group.adaptive_scale"))
					.option(toggle("adaptive_scale.enabled", "adaptive_scale",
						false, () -> EmberframeConfig.adaptiveScale, value -> EmberframeConfig.adaptiveScale = value))
					.option(slider("adaptive_target_fps", "adaptive_target_fps", 30, 360, 5,
						200, () -> EmberframeConfig.adaptiveTargetFps, value -> EmberframeConfig.adaptiveTargetFps = value))
					.option(slider("adaptive_min_scale.percent", "adaptive_min_scale", 50, 95, 5,
						70, () -> EmberframeConfig.adaptiveMinScale, value -> EmberframeConfig.adaptiveMinScale = value))
					.build())
				.option(toggle("fast_entity_data", "fast_entity_data",
					true, () -> EmberframeConfig.fastEntityShadows, value -> EmberframeConfig.fastEntityShadows = value))
				.option(toggle("reuse_entity_states", "reuse_entity_states",
					true, () -> EmberframeConfig.reuseEntityStates, value -> EmberframeConfig.reuseEntityStates = value))
				.option(slider("pause_menu_fps", "pause_menu_fps", 0, 240, 10,
					0, () -> EmberframeConfig.pauseMenuFpsCap, value -> EmberframeConfig.pauseMenuFpsCap = value))
				.build())
			.category(ConfigCategory.createBuilder()
				.name(Component.translatable("emberframe.group.display"))
				.option(slider("game_monitor", "game_monitor", 1, 4, 1,
					1, () -> EmberframeConfig.gameMonitor, value -> EmberframeConfig.gameMonitor = value))
				.build())
			.save(EmberframeConfig::save)
			.build()
			.generateScreen(parent);
	}

	// Names and tooltips come from the lang files: German for a German game, English for every other language.
	private static Option<Boolean> toggle(String name, String tooltip, boolean fallback, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
			.name(Component.translatable("emberframe.option." + name))
			.description(OptionDescription.of(Component.translatable("emberframe.option." + tooltip + ".tooltip")))
			.binding(fallback, getter, setter)
			.controller(TickBoxControllerBuilder::create)
			.build();
	}

	private static Option<Integer> slider(String name, String tooltip, int min, int max, int step, int fallback, Supplier<Integer> getter, Consumer<Integer> setter) {
		return Option.<Integer>createBuilder()
			.name(Component.translatable("emberframe.option." + name))
			.description(OptionDescription.of(Component.translatable("emberframe.option." + tooltip + ".tooltip")))
			.binding(fallback, getter, setter)
			.controller(option -> IntegerSliderControllerBuilder.create(option).range(min, max).step(step))
			.build();
	}
}
