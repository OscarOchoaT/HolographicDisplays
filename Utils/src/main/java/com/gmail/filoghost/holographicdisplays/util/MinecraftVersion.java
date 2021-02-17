package com.gmail.filoghost.holographicdisplays.util;

public class MinecraftVersion {

    public static final MinecraftVersion

            v1_7 = new MinecraftVersion(1),

    // Since 1.8 we use armor stands instead of wither skulls.
    v1_8 = new MinecraftVersion(2);
    private static MinecraftVersion version;
    private int value;

    private MinecraftVersion(int value) {
        this.value = value;
    }

    public static void set(MinecraftVersion version) {
        if (version == null) {
            throw new NullPointerException("version");
        }
        if (MinecraftVersion.version != null) {
            throw new IllegalArgumentException("version already set");
        }
        MinecraftVersion.version = version;
    }

    public static MinecraftVersion get() {
        return version;
    }

    public static boolean isGreaterEqualThan(MinecraftVersion other) {
        return MinecraftVersion.version.value >= other.value;
    }

}
