package me.lusory.ostrich.qapi.metadata;

import me.lusory.ostrich.qapi.util.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DefaultCondition implements Condition {
    @NotNull
    private final String value;

    protected DefaultCondition(@NotNull String value) {
        this.value = Preconditions.assertNotNull(value, "value must not be null");
    }

    public @NotNull String getValue() {
        return this.value;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DefaultCondition)) return false;
        final DefaultCondition other = (DefaultCondition) o;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        return Objects.equals(this$value, other$value);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $value = this.getValue();
        result = result * PRIME + $value.hashCode();
        return result;
    }

    public String toString() {
        return "DefaultCondition(value=" + this.getValue() + ")";
    }
}
