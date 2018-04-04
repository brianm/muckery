package org.skife.muckery.jdbi;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import java.sql.ResultSetMetaData;

public class GenTest {

    @Rule
    public H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        DBI dbi = new DBI(h2);
        dbi.useHandle((h) -> {
            h.execute("create table something (id int primary key, name varchar)");
            h.execute("insert into something (id, name) values (1, 'Brian'), (2, 'Kevin')");

            Plan plan = h.createQuery("select id, name from something where id = :id")
                         .bind("id", 1)
                         .map((index, r, ctx) -> {
                             ResultSetMetaData meta = r.getMetaData();

                             for (int i = 1; i <= meta.getColumnCount(); i++) {
                                 String label = meta.getColumnLabel(i);
                                 int type = meta.getColumnType(i);
                                 System.out.printf("%s\t%d\n", label, type);
                             }

                             return new Plan();
                         })
                         .first();

        });


    }

    public static class Plan {

    }
}
