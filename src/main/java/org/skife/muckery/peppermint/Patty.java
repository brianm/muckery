package org.skife.muckery.peppermint;

import java.util.function.Function;

public class Patty<T>
{
    public <M> Patty<T> on(Class<M> m, Function<M, T> f) {
        return this;
    }

    public T match(final Object it)
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }
}
