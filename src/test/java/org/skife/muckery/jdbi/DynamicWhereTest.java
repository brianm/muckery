package org.skife.muckery.jdbi;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.hubspot.rosetta.jdbi.RosettaMapperFactory;
import com.hubspot.rosetta.jdbi.RosettaObjectMapperOverride;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicWhereTest {

    @Rule
    public H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        final DBI dbi = new DBI(this.h2);
        dbi.registerMapper(new RosettaMapperFactory());
        new RosettaObjectMapperOverride(new ObjectMapper().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)).override(
                dbi);

        final Dao dao = dbi.onDemand(Dao.class);

        dao.init();
        dao.insert(new Something(1, "Brian"));
        dao.insert(new Something(2, "Keith"));
        dao.insert(new Something(3, "Eric"));

        final List<Something> justBrian = dao.findBy(ImmutableMap.of("name", "Brian",
                                                                     "id", "1"));
        assertThat(justBrian).containsExactly(new Something(1, "Brian"));

        final List<Something> everyone = dao.findBy(Collections.emptyMap());
        assertThat(everyone).containsExactly(new Something(1, "Brian"),
                                             new Something(2, "Keith"),
                                             new Something(3, "Eric"));
    }

    public static abstract class Dao implements GetHandle {
        @SqlUpdate("create table something (id int primary key, name varchar)")
        public abstract void init();

        @SqlUpdate("insert into something (id, name) values (:s.id, :s.name)")
        public abstract void insert(@BindBean("s") Something s);

        public List<Something> findBy(final Map<String, Object> wheres) {
            final Handle h = getHandle();

            if (wheres.isEmpty()) {
                return h.createQuery("select id, name from something order by id").mapTo(Something.class).list();
            }

            final String where = wheres.entrySet()
                                       .stream()
                                       .map((e) -> WhereField.valueOf(e.getKey()).createPhrase())
                                       .reduce((left, right) -> left + " and " + right)
                                       .orElseThrow(IllegalStateException::new);

            return h.createQuery("select id, name from something where " + where + " order by id")
                    .bindFromMap(wheres)
                    .mapTo(Something.class)
                    .list();
        }

        public enum WhereField {
            id, name;

            String createPhrase() {
                return name() + " = :" + name();
            }
        }
    }
}
