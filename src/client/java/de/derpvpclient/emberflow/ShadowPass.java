package de.derpvpclient.emberflow;

/** Whether the render thread is currently working on Vitrail's shadow map. */
public final class ShadowPass {
	public static volatile boolean active;
	public static volatile boolean gathering;
	private static volatile Thread drawing;

	private ShadowPass() {
	}

	public static void enterDraw() {
		drawing = Thread.currentThread();
		active = true;
	}

	public static void leaveDraw() {
		active = false;
	}

	/** True only on the thread that draws the shadow map, while it does. Other threads build meshes of their own. */
	public static boolean drawingHere() {
		return active && Thread.currentThread() == drawing;
	}
}
