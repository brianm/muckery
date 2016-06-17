package org.skife.muckery.difference;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.Set;

public class ObjectDiff {

    private final PSet<Class<?>> terminals;

    private static final ObjectDiff BASE = new ObjectDiff(HashTreePSet.from(Primitives.allPrimitiveTypes()));

    private ObjectDiff(final PSet<Class<?>> terminals) {
        this.terminals = terminals;
    }

    public static ObjectDiff differ() {
        return BASE;
    }

    public ObjectDiff addTerminal(final Class<?> terminal) {
        return new ObjectDiff(this.terminals.plus(terminal));
    }


    public Set<Difference> diff(final Object left, final Object right) {
        return ImmutableSet.of();
    }
}
