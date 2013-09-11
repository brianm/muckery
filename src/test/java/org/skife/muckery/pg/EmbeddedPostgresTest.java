package org.skife.muckery.pg;

import com.nesscomputing.db.postgres.embedded.EmbeddedPostgreSQL;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class EmbeddedPostgresTest
{
    @Test
    public void testFoo() throws Exception
    {
        try (EmbeddedPostgreSQL db = EmbeddedPostgreSQL.start()) {
            try (Handle h = DBI.open(db.getPostgresDatabase())) {
                h.execute("CREATE DATABASE breakfast");
                h.execute("CREATE USER brianm with password 'secret'");
                h.execute("grant all privileges on database breakfast to brianm");
            }

            try (Handle h = DBI.open(db.getDatabase("brianm", "breakfast"))) {
                h.execute("create table food (id serial primary key, name text)");
                h.execute("insert into food (name) values ('pancake')");
            }
        }
    }
}
