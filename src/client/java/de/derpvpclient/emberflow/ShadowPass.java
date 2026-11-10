package de.derpvpclient.emberflow;

/** Whether the render thread is currently working on Vitrail's shadow map. */
public final class ShadowPass {
	public static volatile boolean active;
	public static volatile boolean gathering;

	private ShadowPass() {
	}
}
