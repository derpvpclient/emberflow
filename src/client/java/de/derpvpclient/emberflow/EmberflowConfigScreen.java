package de.derpvpclient.emberflow;

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

public final class EmberflowConfigScreen {
	private EmberflowConfigScreen() {
	}

	public static Screen create(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
			.title(Component.literal("Emberflow"))
			.category(ConfigCategory.createBuilder()
				.name(Component.translatable("emberflow.group.performance"))
				.group(OptionGroup.createBuilder()
					.name(Component.translatable("emberflow.group.adaptive_scale"))
					.option(toggle("adaptive_scale.enabled", "adaptive_scale",
						false, () -> EmberflowConfig.adaptiveScale, value -> EmberflowConfig.adaptiveScale = value))
					.option(slider("adaptive_target_fps", "adaptive_target_fps", 30, 360, 5,
						200, () -> EmberflowConfig.adaptiveTargetFps, value -> EmberflowConfig.adaptiveTargetFps = value))
					.option(slider("adaptive_min_scale.percent", "adaptive_min_scale", 50, 95, 5,
						70, () -> EmberflowConfig.adaptiveMinScale, value -> EmberflowConfig.adaptiveMinScale = value))
					.build())
				.option(toggle("fast_entity_data", "fast_entity_data",
					true, () -> EmberflowConfig.fastEntityShadows, value -> EmberflowConfig.fastEntityShadows = value))
				.option(toggle("reuse_entity_states", "reuse_entity_states",
					true, () -> EmberflowConfig.reuseEntityStates, value -> EmberflowConfig.reuseEntityStates = value))
				.option(toggle("fast_vertex_lookup", "fast_vertex_lookup",
					true, () -> EmberflowConfig.fastVertexLookup, value -> EmberflowConfig.fastVertexLookup = value))
				.option(toggle("fast_weather", "fast_weather",
					true, () -> EmberflowConfig.fastWeather, value -> EmberflowConfig.fastWeather = value))
				.option(slider("pause_menu_fps", "pause_menu_fps", 0, 240, 10,
					0, () -> EmberflowConfig.pauseMenuFpsCap, value -> EmberflowConfig.pauseMenuFpsCap = value))
				.build())
			.category(ConfigCategory.createBuilder()
				.name(Component.translatable("emberflow.group.display"))
				.option(slider("game_monitor", "game_monitor", 1, 4, 1,
					1, () -> EmberflowConfig.gameMonitor, value -> EmberflowConfig.gameMonitor = value))
				.build())
			.save(EmberflowConfig::save)
			.build()
			.generateScreen(parent);
	}

	// Names and tooltips come from the lang files: German for a German game, English for every other language.
	private static Option<Boolean> toggle(String name, String tooltip, boolean fallback, Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
			.name(Component.translatable("emberflow.option." + name))
			.description(OptionDescription.of(Component.translatable("emberflow.option." + tooltip + ".tooltip")))
			.binding(fallback, getter, setter)
			.controller(TickBoxControllerBuilder::create)
			.build();
	}

	private static Option<Integer> slider(String name, String tooltip, int min, int max, int step, int fallback, Supplier<Integer> getter, Consumer<Integer> setter) {
		return Option.<Integer>createBuilder()
			.name(Component.translatable("emberflow.option." + name))
			.description(OptionDescription.of(Component.translatable("emberflow.option." + tooltip + ".tooltip")))
			.binding(fallback, getter, setter)
			.controller(option -> IntegerSliderControllerBuilder.create(option).range(min, max).step(step))
			.build();
	}
}
