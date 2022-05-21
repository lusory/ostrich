package me.lusory.ostrich.process.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

@UtilityClass
public class MiscUtils {
    public <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
