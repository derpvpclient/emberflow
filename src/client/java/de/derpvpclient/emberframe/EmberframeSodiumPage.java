package de.derpvpclient.emberframe;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** The options of the Mod Menu screen, as an Emberframe entry in Sodium's video settings. Only loaded by Sodium. */
public final class EmberframeSodiumPage implements ConfigEntryPoint {
	private static final Identifier ICON = Identifier.fromNamespaceAndPath("emberframe", "textures/gui/config-icon.png");
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
				.setName(Component.translatable("emberframe.page.settings"))
				.addOptionGroup(builder.createOptionGroup()
					.setName(Component.translatable("emberframe.group.performance"))
					.addOption(builder.createBooleanOption(FAST_ENTITY_DATA)
						.setName(Component.translatable("emberframe.option.fast_entity_data"))
						.setTooltip(Component.translatable("emberframe.option.fast_entity_data.tooltip"))
						.setImpact(OptionImpact.MEDIUM)
						.setDefaultValue(true)
						.setBinding(value -> EmberframeConfig.fastEntityShadows = value, () -> EmberframeConfig.fastEntityShadows)
						.setStorageHandler(EmberframeConfig::save))
					.addOption(builder.createBooleanOption(REUSE_ENTITY_STATES)
						.setName(Component.translatable("emberframe.option.reuse_entity_states"))
						.setTooltip(Component.translatable("emberframe.option.reuse_entity_states.tooltip"))
						.setImpact(OptionImpact.MEDIUM)
						.setDefaultValue(true)
						.setBinding(value -> EmberframeConfig.reuseEntityStates = value, () -> EmberframeConfig.reuseEntityStates)
						.setStorageHandler(EmberframeConfig::save))
					.addOption(builder.createIntegerOption(PAUSE_MENU_FPS)
						.setName(Component.translatable("emberframe.option.pause_menu_fps"))
						.setTooltip(Component.translatable("emberframe.option.pause_menu_fps.tooltip"))
						.setDefaultValue(0)
						.setRange(0, 240, 10)
						.setValueFormatter(value -> value == 0 ? Component.translatable("emberframe.value.no_limit") : Component.translatable("emberframe.value.fps", value))
						.setBinding(value -> EmberframeConfig.pauseMenuFpsCap = value, () -> EmberframeConfig.pauseMenuFpsCap)
						.setStorageHandler(EmberframeConfig::save)))
				.addOptionGroup(builder.createOptionGroup()
					.setName(Component.translatable("emberframe.group.adaptive_scale"))
					.addOption(builder.createBooleanOption(ADAPTIVE_SCALE)
						.setName(Component.translatable("emberframe.group.adaptive_scale"))
						.setTooltip(Component.translatable("emberframe.option.adaptive_scale.tooltip"))
						.setImpact(OptionImpact.VARIES)
						.setDefaultValue(false)
						.setBinding(value -> EmberframeConfig.adaptiveScale = value, () -> EmberframeConfig.adaptiveScale)
						.setStorageHandler(EmberframeConfig::save))
					.addOption(builder.createIntegerOption(ADAPTIVE_TARGET_FPS)
						.setName(Component.translatable("emberframe.option.adaptive_target_fps"))
						.setTooltip(Component.translatable("emberframe.option.adaptive_target_fps.tooltip"))
						.setEnabledProvider(state -> state.readBooleanOption(ADAPTIVE_SCALE), ADAPTIVE_SCALE)
						.setDefaultValue(200)
						.setRange(30, 360, 5)
						.setValueFormatter(value -> Component.translatable("emberframe.value.fps", value))
						.setBinding(value -> EmberframeConfig.adaptiveTargetFps = value, () -> EmberframeConfig.adaptiveTargetFps)
						.setStorageHandler(EmberframeConfig::save))
					.addOption(builder.createIntegerOption(ADAPTIVE_MIN_SCALE)
						.setName(Component.translatable("emberframe.option.adaptive_min_scale"))
						.setTooltip(Component.translatable("emberframe.option.adaptive_min_scale.tooltip"))
						.setEnabledProvider(state -> state.readBooleanOption(ADAPTIVE_SCALE), ADAPTIVE_SCALE)
						.setDefaultValue(70)
						.setRange(50, 95, 5)
						.setValueFormatter(value -> Component.translatable("emberframe.value.percent", value))
						.setBinding(value -> EmberframeConfig.adaptiveMinScale = value, () -> EmberframeConfig.adaptiveMinScale)
						.setStorageHandler(EmberframeConfig::save)))
				.addOptionGroup(builder.createOptionGroup()
					.setName(Component.translatable("emberframe.group.display"))
					.addOption(builder.createIntegerOption(GAME_MONITOR)
						.setName(Component.translatable("emberframe.option.game_monitor"))
						.setTooltip(Component.translatable("emberframe.option.game_monitor.tooltip"))
						.setFlags(OptionFlag.REQUIRES_GAME_RESTART)
						.setDefaultValue(1)
						.setRange(1, 4, 1)
						.setValueFormatter(value -> value == 1 ? Component.translatable("emberframe.value.main_monitor") : Component.translatable("emberframe.value.monitor", value))
						.setBinding(value -> EmberframeConfig.gameMonitor = value, () -> EmberframeConfig.gameMonitor)
						.setStorageHandler(EmberframeConfig::save))));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("emberframe", path);
	}
}
