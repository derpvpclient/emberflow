package de.derpvpclient.emberflow;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Keeps the render states the world pass built this frame, so the shadow pass does not build the same state a second
 * time. Lives for one frame on the render thread.
 */
public final class EntityStateCache {
	private static final Map<Entity, EntityRenderState> STATES = new IdentityHashMap<>();
	private static final Map<Entity, Float> PARTIAL_TICKS = new IdentityHashMap<>();
	private static boolean capturing;

	private EntityStateCache() {
	}

	public static void begin() {
		STATES.clear();
		PARTIAL_TICKS.clear();
		capturing = EmberflowConfig.reuseEntityStates;
	}

	public static void end() {
		capturing = false;
	}

	public static void capture(Entity entity, float partialTick, EntityRenderState state) {
		if (capturing) {
			STATES.put(entity, state);
			PARTIAL_TICKS.put(entity, partialTick);
		}
	}

	public static @Nullable EntityRenderState reuse(Entity entity, float partialTick) {
		if (!ShadowPass.gathering || !EmberflowConfig.reuseEntityStates) {
			return null;
		}

		Float captured = PARTIAL_TICKS.get(entity);
		return captured != null && captured == partialTick ? STATES.get(entity) : null;
	}
}
