package de.derpvpclient.emberflow;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class EmberflowModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		// EmberflowConfigScreen touches YACL classes, so it must not load unless YACL is there.
		return FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3") ? EmberflowConfigScreen::create : parent -> null;
	}
}
