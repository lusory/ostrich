package me.lusory.ostrich.qapi;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface QEnum {
    String toString();

    @Nullable
    Condition getIf();

    @Unmodifiable
    List<Feature> getFeatures();
}
