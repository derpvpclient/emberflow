package de.derpvpclient.emberflow;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmberflowClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Emberflow");
	/** Whether the game was started with -Demberflow.bench, only then is the benchmark class loaded at all. */
	public static final boolean BENCHMARK = System.getProperty("emberflow.bench") != null;

	@Override
	public void onInitializeClient() {
		EmberflowConfig.load();
		BackendCheck.register();
	}
}
