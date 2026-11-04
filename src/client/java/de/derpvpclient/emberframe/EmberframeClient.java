package de.derpvpclient.emberframe;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmberframeClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Emberframe");

	@Override
	public void onInitializeClient() {
		EmberframeConfig.load();
		BackendCheck.register();
	}
}
