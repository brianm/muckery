package org.skife.muckery.jdbi;

import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.tweak.ContainerFactory;

import java.util.Optional;

public class OptionalContainerFactory implements ContainerFactory<Optional<?>> {
    @Override
    public boolean accepts(final Class<?> type) {
        return Optional.class.equals(type);
    }

    @Override
    public ContainerBuilder<Optional<?>> newContainerBuilderFor(final Class<?> type) {
        return new ContainerBuilder<Optional<?>>() {

            private Optional<?> opt = Optional.empty();

            @Override
            public ContainerBuilder<Optional<?>> add(final Object it) {
                this.opt = Optional.of(it);
                return this;
            }

            @Override
            public Optional build() {
                return this.opt;
            }
        };
    }
}
