package org.skife.muckery.jdbi;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.tweak.ConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class H2Rule extends ExternalResource implements ConnectionFactory {
    private Connection conn;
    private JdbcDataSource pool;


    public synchronized DataSource getDataSource() {
        return this.pool;
    }

    @Override
    protected synchronized void after() {
        try {
            this.conn.close();
        } catch (final SQLException e) {
            throw new IllegalStateException("unable to close last connection to h2", e);
        }
    }

    @Override
    protected synchronized void before() throws Throwable {
        final UUID uuid = UUID.randomUUID();
        this.pool = new JdbcDataSource();

        this.pool.setUrl("jdbc:h2:mem:" + uuid.toString());
        this.pool.setUser("h2");
        this.pool.setPassword("h2");

        this.conn = this.pool.getConnection();
    }

    @Override
    public Connection openConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}
