package de.derpvpclient.emberframe;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class EmberframeModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		// EmberframeConfigScreen touches YACL classes, so it must not load unless YACL is there.
		return FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3") ? EmberframeConfigScreen::create : parent -> null;
	}
}
