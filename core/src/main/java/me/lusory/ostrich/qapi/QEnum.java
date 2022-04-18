package me.lusory.ostrich.qapi;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * https://www.qemu.org/docs/master/devel/qapi-code-gen.html#enumeration-types
 */
public interface QEnum {
    @Nullable
    String getIf();

    @Unmodifiable
    List<String> getFeatures();
}
