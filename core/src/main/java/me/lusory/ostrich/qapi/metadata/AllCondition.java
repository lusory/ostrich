package me.lusory.ostrich.qapi.metadata;

import me.lusory.ostrich.qapi.util.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AllCondition implements Condition {
    @Unmodifiable
    private final List<Condition> conditions;

    protected AllCondition(@NotNull List<Condition> conditions) {
        this.conditions = Collections.unmodifiableList(new ArrayList<>(Preconditions.assertNotNull(conditions, "conditions must not be null")));
    }

    public @Unmodifiable List<Condition> getConditions() {
        return this.conditions;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AllCondition)) return false;
        final AllCondition other = (AllCondition) o;
        final Object this$conditions = this.getConditions();
        final Object other$conditions = other.getConditions();
        return Objects.equals(this$conditions, other$conditions);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $conditions = this.getConditions();
        result = result * PRIME + ($conditions == null ? 43 : $conditions.hashCode());
        return result;
    }

    public String toString() {
        return "AllCondition(conditions=" + this.getConditions() + ")";
    }
}

