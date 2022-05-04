package me.lusory.ostrich.qapi;

import java.util.List;

public interface QEnum {
    String toString();

    Condition getIf();

    List<Feature> getFeatures();
}
