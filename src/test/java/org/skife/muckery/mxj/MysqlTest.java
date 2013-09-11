package org.skife.muckery.mxj;

import com.google.common.collect.ImmutableMap;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MysqlTest
{
    @Test
    @Ignore
    public void testFoo() throws Exception
    {
        Path tmp_dir = Files.createTempDirectory("mysql");

        MysqldResource mysqld = new MysqldResource(tmp_dir.toFile());

        Map<String, String> props = ImmutableMap.of(MysqldResourceI.PORT, "8876",
                                                    MysqldResourceI.INITIALIZE_USER, "true",
                                                    MysqldResourceI.INITIALIZE_USER_NAME, "user",
                                                    MysqldResourceI.INITIALIZE_PASSWORD, "pass",
                                                    "default-time-zone", "+00:00");
        mysqld.start("mysql-daemon", props);
        assertThat(mysqld.isRunning()).isTrue();
        while (!mysqld.isReadyForConnections()) { Thread.sleep(100); }

        MysqlDataSource ds = new MysqlDataSource();
        ds.setURL("jdbc:mysql://localhost:8876/foo?createDatabaseIfNotExist=true");
        ds.setUser("user");
        ds.setPassword("pass");

        Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select 2 + 2");

        assertThat(rs.next()).isTrue();
        int four = rs.getInt(1);
        assertThat(four).isEqualTo(4);

        assertThat(rs.next()).isFalse();
        rs.close();
        stmt.close();
        conn.close();

        mysqld.shutdown();
        while (mysqld.isRunning()) { Thread.sleep(100); }

        Runtime.getRuntime().exec("rm -rf " + tmp_dir.toAbsolutePath() + "").waitFor();
    }
}
