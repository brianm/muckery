package org.skife.muckery.jdbi;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.tweak.ContainerFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalTest {

    @Rule
    public final H2Rule h2 = new H2Rule();

    @Test
    public void testOptional() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        dbi.registerContainerFactory(new ContainerFactory<Optional>() {
            @Override
            public boolean accepts(final Class<?> type) {
                return Optional.class.equals(type);
            }

            @Override
            public ContainerBuilder<Optional> newContainerBuilderFor(final Class<?> type) {
                return new ContainerBuilder<Optional>() {

                    private Optional<?> opt = Optional.empty();

                    @Override
                    public ContainerBuilder<Optional> add(final Object it) {
                        this.opt = Optional.of(it);
                        return this;
                    }

                    @Override
                    public Optional build() {
                        return this.opt;
                    }
                };
            }
        });

        final Dao dao = dbi.onDemand(Dao.class);
        dao.migrate();
        final Optional<String> on = dao.findNameById(1);
        assertThat(on).isEmpty();
    }

    public interface Dao {
        @SqlUpdate("create table something (id int primary key, name varchar)")
        public void migrate();

        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        void insert(@BindBean Something something);

        @SqlQuery("select name from something where id = :id")
        @SingleValueResult(String.class)
        Optional<String> findNameById(@Bind("id") int id);
    }
}
