package org.skife.muckery.dagger;

import javax.inject.Inject;

public class Bar
{
    private final Foo foo;

    @Inject
    public Bar(Foo foo) {
        this.foo = foo;
    }
}
