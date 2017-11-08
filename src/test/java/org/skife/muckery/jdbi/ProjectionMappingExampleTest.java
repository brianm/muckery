package org.skife.muckery.jdbi;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.ResultSetMapperFactory;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectionMappingExampleTest {
    @Rule
    public H2Rule h2 = new H2Rule();
    private DBI dbi;
    private AccessDao dao;

    @Before
    public void setUp() throws Exception {
        dbi = new DBI(h2);
        dbi.useHandle((h) -> {
            h.execute("create table users (" +
                              "username varchar not null, " +
                              "role varchar not null," +
                              "primary key (username))");

            h.execute("create table permissions (" +
                              "role varchar not null, " +
                              "permission varchar not null, " +
                              "primary key (role, permission))");

            h.execute("create index on users(role)");

            h.prepareBatch("insert into users (username, role) values (?, ?)")
             .add("brianm", "programmer")
             .add("eric", "manager")
             .add("keith", "professor")
             .execute();

            h.prepareBatch("insert into permissions (role, permission) values (?, ?)")
             .add("programmer", "hackery")
             .add("programmer", "meetings")
             .add("manager", "meetings")
             .add("professor", "camping")
             .execute();
        });
        dao = dbi.onDemand(AccessDao.class);
    }


    @Test
    public void testGetPermissions() throws Exception {
        Set<String> perms = dao.findPermissionsFor("brianm");
        assertThat(perms).containsExactlyInAnyOrder("hackery", "meetings");
    }

    @Test
    public void testGetUser() throws Exception {
        User brianm = dao.findUserByName("brianm");
        assertThat(brianm.getRole()).isEqualTo("programmer");
    }

    @Test
    public void testReplacePermissions() throws Exception {
        dao.assignPermissions("programmer", "hackery");
        assertThat(dao.findPermissionsFor("brianm")).containsExactlyInAnyOrder("hackery");
    }

    @Test
    public void testFetchUserWithPermissions() throws Exception {
        Optional<UserWithPermissions> o = dao.findUserAndPermissions("brianm");
        assertThat(o.isPresent());
        o.ifPresent((u) -> {
            assertThat(u.getPermissions()).containsExactlyInAnyOrder("hackery", "meetings");
            assertThat(u.getRole()).isEqualTo("programmer");
        });
    }

    @Test
    public void testFetchUserWithPermissionsNoPermissions() throws Exception {
        dao.assignRole("brianm", "writer");
        Optional<UserWithPermissions> o = dao.findUserAndPermissions("brianm");
        assertThat(o.isPresent());
        o.ifPresent((u) -> {
            assertThat(u.getPermissions()).isEmpty();
        });
    }


    @RegisterMapperFactory(TrivialMapperFactory.class)
    public abstract static class AccessDao {

        @SqlQuery("select permission from permissions p inner join users u on (u.role = p.role) where username = :0")
        public abstract Set<String> findPermissionsFor(String name);

        @SqlQuery("select username, role from users where username = :0")
        public abstract User findUserByName(String name);

        @SqlUpdate("update users set role = :1 where username = :0")
        public abstract void assignRole(String username, String role);

        @SqlBatch("insert into permissions (role, permission) values (:0, :1)")
        abstract void addPermissions(String role, String... permission);

        @SqlUpdate("delete from permissions where role = :0")
        abstract void removeAllPermissionsForRole(String username);

        @Transaction
        public void assignPermissions(String role, String... permissions) {
            removeAllPermissionsForRole(role);
            addPermissions(role, permissions);
        }

        @SqlQuery("select u.username, u.role, p.permission " +
                "from users u " +
                "  left join permissions p " +
                "  on (u.role = p.role) " +
                "where u.username = :0")
        abstract List<UserPermissionRow> findUserPermissionRows(String username);

        public Optional<UserWithPermissions> findUserAndPermissions(String username) {
            Set<String> permissions = Sets.newHashSet();
            String job = null;
            for (UserPermissionRow row : findUserPermissionRows(username)) {
                if (row.permission != null) {
                    permissions.add(row.permission);
                }
                job = row.job;
            }
            if (job == null /* no rows returned */) {
                return Optional.empty();
            }
            return Optional.of(new UserWithPermissions(username, job, permissions));
        }

        private static class UserPermissionRow {
            String username;
            String job;
            String permission;

            UserPermissionRow(String username, String job, String permission) {
                this.username = username;
                this.job = job;
                this.permission = permission;
            }
        }
    }

    public static class User {
        private final String role;
        private final String name;

        public User(String name, String job) {
            this.name = name;
            this.role = job;
        }

        public String getRole() {
            return role;
        }

        public String getName() {
            return name;
        }
    }

    public static class UserWithPermissions extends User {

        private final Set<String> permissions;

        UserWithPermissions(String name, String job, Collection<String> permissions) {
            super(name, job);
            this.permissions = ImmutableSet.copyOf(permissions);
        }

        public Set<String> getPermissions() {
            return permissions;
        }
    }

    /**
     * Quick hack to map strings to ctors, which is all we need for these examples,
     * data holder classes are carefully arranged to only have a single ctor, which takes a
     * series of strings :-)
     */
    public static class TrivialMapperFactory implements ResultSetMapperFactory {

        private static final Set<Class> supported = ImmutableSet.of(User.class, AccessDao.UserPermissionRow.class);

        @Override
        public boolean accepts(Class type, StatementContext ctx) {
            return supported.contains(type);
        }

        @Override
        public ResultSetMapper mapperFor(Class type, StatementContext ctx) {
            return (index, r, _ctx) -> {
                Constructor ctor = type.getDeclaredConstructors()[0];
                int cnt = ctor.getParameterCount();

                List<String> args = Lists.newArrayList();
                for (int i = 1; i <= cnt; i++) {
                    args.add(r.getString(i));
                }

                try {
                    return ctor.newInstance(args.toArray());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            };
        }
    }
}
