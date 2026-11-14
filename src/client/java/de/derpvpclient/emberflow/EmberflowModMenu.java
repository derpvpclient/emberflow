package de.derpvpclient.emberflow;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

public class EmberflowModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		// EmberflowConfigScreen touches YACL classes, so it must not load unless YACL is there.
		return FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3") ? EmberflowConfigScreen::create : parent -> null;
	}

	/** Mod Menu marks the mod in its list when a newer version is out and names both sites it can be had from. */
	@Override
	public UpdateChecker getUpdateChecker() {
		return () -> {
			String newer = UpdateCheck.newer();

			return new UpdateInfo() {
				@Override
				public boolean isUpdateAvailable() {
					return newer != null;
				}

				@Override
				public Component getUpdateMessage() {
					return newer == null ? null : Component.translatable(UpdateCheck.curseForgeFirst ? "emberflow.update.message.curseforge" : "emberflow.update.message", newer);
				}

				@Override
				public String getDownloadLink() {
					return UpdateCheck.link;
				}

				@Override
				public UpdateChannel getUpdateChannel() {
					return UpdateChannel.RELEASE;
				}
			};
		};
	}
}
