package org.skife.muckery.jdbi;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import com.hubspot.rosetta.jdbi.RosettaMapperFactory;
import com.hubspot.rosetta.jdbi.RosettaObjectMapperOverride;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import static org.assertj.core.api.Assertions.assertThat;

public class RosettaTest {
    @Rule
    public final H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        final DBI dbi = new DBI(this.h2);
        dbi.registerMapper(new RosettaMapperFactory());
        dbi.registerContainerFactory(new OptionalContainerFactory());
        new RosettaObjectMapperOverride(new ObjectMapper().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES))
                .override(dbi);

        final Dao dao = dbi.onDemand(Dao.class);
        dao.migrate();
        dao.insert(new Something(1, "Happy"));
        final Something rs = dao.findById(1);
        assertThat(rs).isNotNull()
                      .isEqualTo(new Something(1, "Happy"));
    }

    private interface Dao {
        @SqlUpdate("create table something (id int primary key, name varchar)")
        void migrate();

        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        void insert(@BindWithRosetta Something something);

        @SqlQuery("select id, name from something where id = :id")
        Something findById(@Bind("id") int id);
    }
}
