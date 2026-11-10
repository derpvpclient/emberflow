package de.derpvpclient.emberflow;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmberflowClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Emberflow");

	@Override
	public void onInitializeClient() {
		EmberflowConfig.load();
		BackendCheck.register();
	}
}
