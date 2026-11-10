package de.derpvpclient.emberflow;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** The options of the Mod Menu screen, as an Emberflow entry in Sodium's video settings. Only loaded by Sodium. */
public final class EmberflowSodiumPage implements ConfigEntryPoint {
	private static final Identifier ICON = Identifier.fromNamespaceAndPath("emberflow", "textures/gui/config-icon.png");
	private static final int EMBER_ORANGE = 0xFFF08A3C;
	private static final Identifier FAST_ENTITY_DATA = id("fast_entity_data");
	private static final Identifier REUSE_ENTITY_STATES = id("reuse_entity_states");
	private static final Identifier PAUSE_MENU_FPS = id("pause_menu_fps");
	private static final Identifier ADAPTIVE_SCALE = id("adaptive_scale");
	private static final Identifier ADAPTIVE_TARGET_FPS = id("adaptive_target_fps");
	private static final Identifier ADAPTIVE_MIN_SCALE = id("adaptive_min_scale");
	private static final Identifier GAME_MONITOR = id("game_monitor");

	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		builder.registerOwnModOptions()
			.setNonTintedIcon(ICON)
			.setColorTheme(builder.createColorTheme().setBaseThemeRGB(EMBER_ORANGE))
			.addPage(builder.createOptionPage()
				.setName(Component.translatable("emberflow.page.settings"))
				.addOptionGroup(builder.createOptionGroup()
					.setName(Component.translatable("emberflow.group.performance"))
					.addOption(builder.createBooleanOption(FAST_ENTITY_DATA)
						.setName(Component.translatable("emberflow.option.fast_entity_data"))
						.setTooltip(Component.translatable("emberflow.option.fast_entity_data.tooltip"))
						.setImpact(OptionImpact.MEDIUM)
						.setDefaultValue(true)
						.setBinding(value -> EmberflowConfig.fastEntityShadows = value, () -> EmberflowConfig.fastEntityShadows)
						.setStorageHandler(EmberflowConfig::save))
					.addOption(builder.createBooleanOption(REUSE_ENTITY_STATES)
						.setName(Component.translatable("emberflow.option.reuse_entity_states"))
						.setTooltip(Component.translatable("emberflow.option.reuse_entity_states.tooltip"))
						.setImpact(OptionImpact.MEDIUM)
						.setDefaultValue(true)
						.setBinding(value -> EmberflowConfig.reuseEntityStates = value, () -> EmberflowConfig.reuseEntityStates)
						.setStorageHandler(EmberflowConfig::save))
					.addOption(builder.createIntegerOption(PAUSE_MENU_FPS)
						.setName(Component.translatable("emberflow.option.pause_menu_fps"))
						.setTooltip(Component.translatable("emberflow.option.pause_menu_fps.tooltip"))
						.setDefaultValue(0)
						.setRange(0, 240, 10)
						.setValueFormatter(value -> value == 0 ? Component.translatable("emberflow.value.no_limit") : Component.translatable("emberflow.value.fps", value))
						.setBinding(value -> EmberflowConfig.pauseMenuFpsCap = value, () -> EmberflowConfig.pauseMenuFpsCap)
						.setStorageHandler(EmberflowConfig::save)))
				.addOptionGroup(builder.createOptionGroup()
					.setName(Component.translatable("emberflow.group.adaptive_scale"))
					.addOption(builder.createBooleanOption(ADAPTIVE_SCALE)
						.setName(Component.translatable("emberflow.group.adaptive_scale"))
						.setTooltip(Component.translatable("emberflow.option.adaptive_scale.tooltip"))
						.setImpact(OptionImpact.VARIES)
						.setDefaultValue(false)
						.setBinding(value -> EmberflowConfig.adaptiveScale = value, () -> EmberflowConfig.adaptiveScale)
						.setStorageHandler(EmberflowConfig::save))
					.addOption(builder.createIntegerOption(ADAPTIVE_TARGET_FPS)
						.setName(Component.translatable("emberflow.option.adaptive_target_fps"))
						.setTooltip(Component.translatable("emberflow.option.adaptive_target_fps.tooltip"))
						.setEnabledProvider(state -> state.readBooleanOption(ADAPTIVE_SCALE), ADAPTIVE_SCALE)
						.setDefaultValue(200)
						.setRange(30, 360, 5)
						.setValueFormatter(value -> Component.translatable("emberflow.value.fps", value))
						.setBinding(value -> EmberflowConfig.adaptiveTargetFps = value, () -> EmberflowConfig.adaptiveTargetFps)
						.setStorageHandler(EmberflowConfig::save))
					.addOption(builder.createIntegerOption(ADAPTIVE_MIN_SCALE)
						.setName(Component.translatable("emberflow.option.adaptive_min_scale"))
						.setTooltip(Component.translatable("emberflow.option.adaptive_min_scale.tooltip"))
						.setEnabledProvider(state -> state.readBooleanOption(ADAPTIVE_SCALE), ADAPTIVE_SCALE)
						.setDefaultValue(70)
						.setRange(50, 95, 5)
						.setValueFormatter(value -> Component.translatable("emberflow.value.percent", value))
						.setBinding(value -> EmberflowConfig.adaptiveMinScale = value, () -> EmberflowConfig.adaptiveMinScale)
						.setStorageHandler(EmberflowConfig::save)))
				.addOptionGroup(builder.createOptionGroup()
					.setName(Component.translatable("emberflow.group.display"))
					.addOption(builder.createIntegerOption(GAME_MONITOR)
						.setName(Component.translatable("emberflow.option.game_monitor"))
						.setTooltip(Component.translatable("emberflow.option.game_monitor.tooltip"))
						.setFlags(OptionFlag.REQUIRES_GAME_RESTART)
						.setDefaultValue(1)
						.setRange(1, 4, 1)
						.setValueFormatter(value -> value == 1 ? Component.translatable("emberflow.value.main_monitor") : Component.translatable("emberflow.value.monitor", value))
						.setBinding(value -> EmberflowConfig.gameMonitor = value, () -> EmberflowConfig.gameMonitor)
						.setStorageHandler(EmberflowConfig::save))));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("emberflow", path);
	}
}
