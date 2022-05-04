package me.lusory.ostrich.qapi;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#configuring-the-schema
@Data(staticConstructor = "of")
public class Condition {
    private final ConditionType type;
    // not empty if type is ALL or ANY
    private final List<Condition> conditions;
    // null if type is ALL or ANY
    @Nullable
    private final String value;
}
