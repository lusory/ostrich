package me.lusory.ostrich.qapi;

import lombok.Data;

@Data(staticConstructor = "of")
public class Feature {
    private final String name;
    private final Condition _if;
}
