package org.skife.muckery.difference;

public class Difference {
    private final String path;
    private final Object lhs;
    private final Object rhs;

    Difference(final String path, final Object lhs, final Object rhs) {
        this.path = path;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public String getPath() {
        return this.path;
    }

    public Object getLeft() {
        return this.lhs;
    }

    public Object getRight() {
        return this.rhs;
    }

    @Override
    public String toString() {
        return String.format("path: %s, left: %s, right: %s", getPath(), getLeft(), getRight());
    }
}
