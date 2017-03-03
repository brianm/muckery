package org.skife.muckery.jdbi;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jdbi.v2.st4.ST4StatementLocator;
import org.jdbi.v2.st4.UseST4StatementLocator;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.Update;
import org.skife.jdbi.v2.logging.PrintStreamLog;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizer;
import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizerFactory;
import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizingAnnotation;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.unstable.BindIn;
import org.stringtemplate.v4.ST;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicUpdateTest {
    @Rule
    public H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        DBI dbi = new DBI(h2);
        dbi.setStatementLocator(ST4StatementLocator.fromClasspath("/"));
        dbi.setSQLLog(new PrintStreamLog(System.err));

        Map<SomethingField, Object> in = Maps.newHashMap();

        in.put(SomethingField.name, "Brian");
        in.put(SomethingField.age, 25);


        dbi.useHandle((h) -> {
            h.execute("create table something (id int primary key, name varchar, age int)");
            h.execute("insert into something (id, name, age) values (1, 'Tim', 32)");

            Update update = h.createStatement(
                    "update something set <fields:{f| <f> = :<f>};separator=\", \"> where id = :id");

            update.define("fields", in.keySet());
            in.entrySet().forEach((e) -> {
                update.bind(e.getKey().name(), e.getValue());
            });

            update.bind("id", 1);
            int r = update.execute();
            assertThat(r).isEqualTo(1);
            String name = h.createQuery("select name from something where id = 1").mapTo(String.class).first();
            assertThat(name).isEqualTo("Brian");
        });
    }

    @Test
    public void testWithDao() throws Exception {
        DBI dbi = new DBI(h2);
        dbi.setStatementLocator(ST4StatementLocator.fromClasspath("/"));
        dbi.setSQLLog(new PrintStreamLog(System.err));

        dbi.useHandle((h) -> {
            h.execute("create table something (id int primary key, name varchar, age int)");
            h.execute("insert into something (id, name, age) values (1, 'Tim', 32)");
        });

        Dao dao = dbi.onDemand(Dao.class);
        dao.updateFields(1, ImmutableMap.of(SomethingField.age, 29,
                                            SomethingField.name, "Brian"));

        String name = dbi.withHandle(h -> h.createQuery("select name from something where id = 1")
                                           .mapTo(String.class)
                                           .first());
        assertThat(name).isEqualTo("Brian");

        int age = dbi.withHandle(h -> h.createQuery("select age from something where id = 1")
                                       .mapTo(Integer.class)
                                       .first());
        assertThat(age).isEqualTo(29);
    }

    @Test
    public void testStringTemplate() throws Exception {
        ST st = new ST("update something set <fields:{f| <f> = :<f>};separator=\", \"> where id = :id", '<', '>');
        st.add("fields", ImmutableSet.of(SomethingField.age, SomethingField.name));
        String out = st.render();
        assertThat(out).isEqualTo("update something set age = :age, name = :name where id = :id");
    }

    public static enum SomethingField {
        name, id, age
    }

    public interface Dao {
        @SqlUpdate("update something set <fields> where id = :id")
        public int updateFields(@Bind("id") int id, @BindFields("fields") Map<SomethingField, Object> changes);
    }

    @SqlStatementCustomizingAnnotation(BindFields.BindFieldsCustomizerFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface BindFields {

        String value() default "it";

        class BindFieldsCustomizerFactory implements SqlStatementCustomizerFactory {

            @Override
            public SqlStatementCustomizer createForMethod(Annotation annotation, Class sqlObjectType, Method method) {
                throw new UnsupportedOperationException("Not Allowed");
            }

            @Override
            public SqlStatementCustomizer createForType(Annotation annotation, Class sqlObjectType) {
                throw new UnsupportedOperationException("Not Allowed");
            }

            @Override
            public SqlStatementCustomizer createForParameter(Annotation annotation,
                                                             Class sqlObjectType,
                                                             Method method,
                                                             Object arg) {
                return q -> {
                    Preconditions.checkArgument(arg instanceof Map, "@BindFields requires a Map argument");
                    Map<?, ?> fields = (Map<?, ?>) arg;
                    // needs to set value() to name = :name, pairs
                    BindFields bf = (BindFields) annotation;
                    String paramName = bf.value();

                    List<String> statementParts = Lists.newArrayList();
                    fields.entrySet().forEach(e -> {
                        statementParts.add(e.getKey() + " = :" + e.getKey());
                        q.bind(e.getKey().toString(), e.getValue());
                    });
                    q.define(paramName, Joiner.on(", ").join(statementParts));
                };
            }
        }
    }
}
