package org.skife.muckery.jdbi.immutables;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import com.hubspot.rosetta.jdbi.RosettaMapperFactory;
import com.hubspot.rosetta.jdbi.RosettaObjectMapperOverride;
import org.immutables.value.Value;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.muckery.jdbi.H2Rule;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ImmutableMappingTest {

    @Rule
    public final H2Rule h2 = new H2Rule();
    private DBI dbi;

    @Before
    public void setUp() throws Exception {
        this.dbi = new DBI(this.h2);
        try (Handle h = this.dbi.open()) {
            h.execute("create table something (id int primary key, name varchar) ");
        }
    }

    @Test
    public void testBinding() throws Exception {
        try (Handle h = this.dbi.open()) {
            rosetify(h);
            final Dao dao = h.attach(Dao.class);
            dao.insert(ImmutableSomething.builder().id(1).name("Brian").build());
            final Something b = dao.find(1);
            assertThat(b).isEqualTo(ImmutableSomething.builder().id(1).name("Brian").build());
        }
    }

    public interface Dao {
        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        void insert(@BindWithRosetta Something s);

        @SqlQuery("select id, name from something where id = :id")
        Something find(@Bind("id") int id);
    }


    @Test
    public void testExplicit() throws Exception {
        try (Handle h = this.dbi.open()) {
            h.execute("insert into something (id, name) values (1, 'Brian'), (2, 'Derrick'), (3, 'Francisco')");

            final List<Something> rs = h.createQuery("select id, name from something order by id")
                                        .map((_i, r, _c) -> Something.create(r.getInt("id"), r.getString("name")))
                                        .list();

            assertThat(rs).containsExactly(Something.create(1, "Brian"),
                                           Something.create(2, "Derrick"),
                                           Something.create(3, "Francisco"));
        }
    }

    @Test
    public void testWithRosetta() throws Exception {
        try (Handle h = this.dbi.open()) {
            rosetify(h);
            h.execute("insert into something (id, name) values (1, 'Brian'), (2, 'Derrick'), (3, 'Francisco')");

            final List<Something> rs = h.createQuery("select id, name from something order by id")
                                        .mapTo(Something.class)
                                        .list();

            assertThat(rs).containsExactly(Something.create(1, "Brian"),
                                           Something.create(2, "Derrick"),
                                           Something.create(3, "Francisco"));
        }
    }


    @Test
    public void testJacksonStuff() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"name\":\"Tatu\",\"id\":\"42\"}", Something.class);
    }

    @Value.Immutable
    @JsonDeserialize(as = ImmutableSomething.class)
    public interface Something {
        Integer id();

        String name();

        static ImmutableSomething.Builder builder() {
            return ImmutableSomething.builder();
        }

        static Something create(final int id, final String name) {
            return ImmutableSomething.builder().id(id).name(name).build();
        }
    }

    private static void rosetify(final Handle h) {
        new RosettaObjectMapperOverride(new ObjectMapper().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES))
                .override(h);
        h.registerMapper(new RosettaMapperFactory());
    }
}
