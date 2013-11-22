package org.skife.muckery.jdbi;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.stringtemplate.StringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.NamedArgumentFinder;
import org.skife.jdbi.v2.util.StringMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class InClauseExpansionTest
{
    @Test
    public void testFoo() throws Exception
    {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID());

        DBI dbi = new DBI(ds);
        dbi.setStatementLocator(new StringTemplate3StatementLocator(InClauseExpansionTest.class, true, true));

        dbi.withHandle(new HandleCallback<Object>()
        {
            @Override
            public Object withHandle(final Handle handle) throws Exception
            {
                handle.execute("create table something (id int primary key, name varchar)");
                handle.execute("insert into something (id, name) values (7, 'Brian'), (16, 'Tom'), (43, 'Asaf')");

                List<Integer> ids = ImmutableList.of(7, 43);

                List<String> names = handle.createQuery("select name from something where id in (<ids>) order by id")
                                           .map(StringMapper.FIRST)
                                           .define("ids", ArrayArguments.templateValue(ids))
                                           .bindNamedArgumentFinder(ArrayArguments.arguments(ids))
                                           .list();

                assertThat(names).containsExactly("Brian", "Asaf");

                return null;
            }
        });
    }

    public static class ArrayArguments
    {
        public static NamedArgumentFinder arguments(final List<Integer> ints)
        {
            return new NamedArgumentFinder()
            {
                @Override
                public Argument find(final String name)
                {
                    if (!name.startsWith("__ArrayArguments_Integer_")) {
                        return null;
                    }

                    String[] parts = name.split("ArrayArguments_Integer_");
                    final int offset = Integer.parseInt(parts[1]);
                    return new Argument()
                    {
                        @Override
                        public void apply(final int position, final PreparedStatement statement, final StatementContext ctx) throws SQLException
                        {
                            statement.setInt(position, ints.get(offset));
                        }
                    };
                }
            };
        }

        public static String templateValue(Collection<Integer> ints)
        {
            List<String> names = Lists.newArrayListWithExpectedSize(ints.size());
            for (int i = 0; i < ints.size(); i++) {
                names.add(String.format(":__ArrayArguments_Integer_%d", i));
            }
            return Joiner.on(",").join(names);
        }
    }

    @Test
    public void testBar() throws Exception
    {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID());

        DBI dbi = new DBI(ds);
        dbi.setStatementLocator(new StringTemplate3StatementLocator(InClauseExpansionTest.class, true, true));

        dbi.withHandle(new HandleCallback<Object>()
        {
            @Override
            public Object withHandle(final Handle handle) throws Exception
            {
                handle.execute("create table something (id int primary key, name varchar)");
                handle.execute("insert into something (id, name) values (7, 'Brian'), (16, 'Tom'), (43, 'Asaf')");


                InClauseExpansion expansion = new InClauseExpansion(Types.INTEGER, ImmutableList.of(7, 43));

                List<String> names = handle.createQuery("select name from something where id in (<ids>) order by id")
                                           .map(StringMapper.FIRST)
                                           .define("ids", expansion)
                                           .bindNamedArgumentFinder(expansion)
                                           .list();

                assertThat(names).containsExactly("Brian", "Asaf");

                return null;
            }
        });
    }

    public static class InClauseExpansion implements NamedArgumentFinder
    {
        private final String uniquer = UUID.randomUUID().toString().replaceAll("-", "_");

        private final int sqlType;
        private final List<? extends Object> values;

        public InClauseExpansion(int sqlType, List<? extends Object> values)
        {
            this.sqlType = sqlType;
            this.values = values;
        }

        public String templateValue()
        {
            List<String> names = Lists.newArrayListWithExpectedSize(values.size());
            int i = 0;
            for (Object _ : values) {
                names.add(String.format(":%s__%d", uniquer, i++));
            }
            return Joiner.on(",").join(names);
        }

        @Override
        public String toString()
        {
            return templateValue();
        }

        @Override
        public Argument find(final String name)
        {
            if (!name.contains(uniquer)) {
                return null;
            }

            final int offset = Integer.parseInt(name.split("__")[1]);
            return new Argument()
            {
                @Override
                public void apply(final int position, final PreparedStatement statement, final StatementContext ctx) throws SQLException
                {
                    statement.setObject(position, values.get(offset), sqlType);
                }
            };
        }
    }
}
