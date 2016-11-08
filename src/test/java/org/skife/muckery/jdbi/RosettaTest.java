package org.skife.muckery.jdbi;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.rosetta.jdbi.BindWithRosetta;
import com.hubspot.rosetta.jdbi.RosettaMapperFactory;
import com.hubspot.rosetta.jdbi.RosettaObjectMapperOverride;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterContainerMapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class RosettaTest {
    @Rule
    public final H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        final DBI dbi = new DBI(this.h2);
        new RosettaObjectMapperOverride(new ObjectMapper().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES))
                .override(dbi);

        final Dao dao = dbi.onDemand(Dao.class);
        dao.migrate();
        dao.insert(new Something(1, "Happy"));
        final Something rs = dao.findById(1);
        assertThat(rs).isNotNull()
                      .isEqualTo(new Something(1, "Happy"));
    }

    @Test
    public void testOverrideMapper() throws Exception {

    }

    @RegisterContainerMapper(OptionalContainerFactory.class)
    @RegisterMapperFactory(RosettaMapperFactory.class)
    private interface Dao {
        @SqlUpdate("create table something (id int primary key, name varchar)")
        void migrate();

        @SqlUpdate("insert into something (id, name) values (:id, :name)")
        void insert(@BindWithRosetta Something something);

        @SqlQuery("select id, name from something where id = :id")
        Something findById(@Bind("id") int id);


        @SqlQuery("select id, name from something where id = :id")
        @Mapper(SpecialMapper.class)
        Something findById2(@Bind("id") int id);
    }

    public static class SpecialMapper implements ResultSetMapper<Something> {

        @Override
        public Something map(final int i, final ResultSet resultSet, final StatementContext statementContext) throws SQLException {
            return null;
        }
    }
}
