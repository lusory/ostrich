package me.lusory.ostrich.qapi;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public interface QEnum {
    String name();

    String toString();

    @Nullable
    default Condition getIf() {
        return null;
    }

    @Unmodifiable
    default List<Feature> getFeatures() {
        return Collections.emptyList();
    }
}
