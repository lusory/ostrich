package me.lusory.ostrich.qapi.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Preconditions {
    private Preconditions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> @NotNull T assertNotNull(@Nullable T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return value;
    }

    public static <T> @NotNull T assertNotNull(@Nullable T value, @Nullable Object msg) {
        if (value == null) {
            throw new NullPointerException(String.valueOf(msg));
        }
        return value;
    }
}
