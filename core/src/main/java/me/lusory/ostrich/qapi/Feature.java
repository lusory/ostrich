package me.lusory.ostrich.qapi;

import java.util.Objects;

public class Feature {
    private final String name;
    private final Condition _if;

    private Feature(String name, Condition _if) {
        this.name = name;
        this._if = _if;
    }

    public static Feature of(String name, Condition _if) {
        return new Feature(name, _if);
    }

    public String getName() {
        return this.name;
    }

    public Condition getIf() {
        return this._if;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Feature)) return false;
        final Feature other = (Feature) o;
        if (!other.canEqual(this)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (!Objects.equals(this$name, other$name)) return false;
        final Object this$_if = this.getIf();
        final Object other$_if = other.getIf();
        return Objects.equals(this$_if, other$_if);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Feature;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $_if = this.getIf();
        result = result * PRIME + ($_if == null ? 43 : $_if.hashCode());
        return result;
    }

    public String toString() {
        return "Feature(name=" + this.getName() + ", _if=" + this.getIf() + ")";
    }
}
