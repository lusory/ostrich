package me.lusory.ostrich.test.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;

@UtilityClass
public class MiscUtils {
    public String takeChars(String s, int num) {
        return s.substring(0, Math.min(num, s.length()));
    }

    @SneakyThrows({ IOException.class })
    public Path createTempFile(String prefix, String suffix, FileAttribute<?>... attrs) {
        return Files.createTempFile(prefix, suffix, attrs);
    }

    public void assertArrayEqualsUnordered(Object[] expected, Object[] actual) {
        final Object[] expected0 = Arrays.copyOf(expected, expected.length);
        final Object[] actual0 = Arrays.copyOf(actual, actual.length);
        Arrays.sort(expected0);
        Arrays.sort(actual0);

        Assertions.assertArrayEquals(expected0, actual0);
    }
}
