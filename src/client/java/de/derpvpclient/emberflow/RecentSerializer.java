package de.derpvpclient.emberflow;

/** The vertex converter Sodium handed out last, with the two formats it was asked for. Kept as plain objects so this class loads without Sodium. */
public record RecentSerializer(Object source, Object target, Object serializer, int generation) {
}
