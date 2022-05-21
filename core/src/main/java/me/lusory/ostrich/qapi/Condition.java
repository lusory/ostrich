package me.lusory.ostrich.qapi;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

// https://www.qemu.org/docs/master/devel/qapi-code-gen.html#configuring-the-schema
public class Condition {
    private final ConditionType type;
    // not empty if type is ALL or ANY
    private final List<Condition> conditions;
    // null if type is ALL or ANY
    @Nullable
    private final String value;

    private Condition(ConditionType type, List<Condition> conditions, @Nullable String value) {
        this.type = type;
        this.conditions = conditions;
        this.value = value;
    }

    public static Condition of(ConditionType type, List<Condition> conditions, @Nullable String value) {
        return new Condition(type, conditions, value);
    }

    public ConditionType getType() {
        return this.type;
    }

    public List<Condition> getConditions() {
        return this.conditions;
    }

    public @Nullable String getValue() {
        return this.value;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Condition)) return false;
        final Condition other = (Condition) o;
        if (!other.canEqual(this)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (!Objects.equals(this$type, other$type)) return false;
        final Object this$conditions = this.getConditions();
        final Object other$conditions = other.getConditions();
        if (!Objects.equals(this$conditions, other$conditions))
            return false;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        return Objects.equals(this$value, other$value);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Condition;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $conditions = this.getConditions();
        result = result * PRIME + ($conditions == null ? 43 : $conditions.hashCode());
        final Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }

    public String toString() {
        return "Condition(type=" + this.getType() + ", conditions=" + this.getConditions() + ", value=" + this.getValue() + ")";
    }
}
