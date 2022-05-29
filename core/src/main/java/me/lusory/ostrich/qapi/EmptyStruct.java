package me.lusory.ostrich.qapi;

public class EmptyStruct implements QStruct {
    public EmptyStruct() {
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        return o instanceof EmptyStruct;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "EmptyStruct()";
    }
}
