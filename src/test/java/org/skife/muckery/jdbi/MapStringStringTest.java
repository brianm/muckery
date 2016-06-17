package org.skife.muckery.jdbi;

import org.assertj.core.data.MapEntry;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class MapStringStringTest {

    @Rule
    public H2Rule rule = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        final DBI dbi = new DBI(this.rule);
        final Dao dao = dbi.onDemand(Dao.class);
        dao.createKvTable();
        dao.insert("hello", "world");
        dao.insert("bonjour", "monde");
        dao.insert("hola", "mundo");
        dao.insert("kaixo", "mundua");

        final Map<String, String> values = dao.findAll();
        assertThat(values).contains(MapEntry.entry("hello", "world"))
                          .contains(MapEntry.entry("bonjour", "monde"));

    }

    public static abstract class Dao {

        @SqlUpdate("create table kv ( key varchar primary key, value varchar)")
        abstract void createKvTable();

        @SqlUpdate("insert into kv (key, value) values (:key, :value)")
        abstract void insert(@Bind("key") String key, @Bind("value") String value);

        @SqlQuery("select key, value from kv order by key")
        @RegisterMapper(StringStringMapEntryMapper.class)
        abstract List<Map.Entry<String, String>> findKvPairs();

        public Map<String, String> findAll() {
            return findKvPairs().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    public static class StringStringMapEntryMapper implements ResultSetMapper<Map.Entry<String, String>> {


        @Override
        public Map.Entry<String, String> map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException {
            return new AbstractMap.SimpleEntry<String, String>(r.getString("key"), r.getString("value"));
        }
    }
}
