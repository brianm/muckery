package org.skife.muckery

import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.sqlobject.SqlQuery
import org.skife.jdbi.v2.sqlobject.SqlUpdate
import org.skife.muckery.jdbi.H2Rule

class KotlinTest {

    @Rule
    @JvmField
    val h2 = H2Rule()

    @Test
    fun testDatabasery() {
        val dbi = DBI(h2)
        val dao = dbi.onDemand(Dao::class.java)
        dao.createTable()
        dao.insert(1, "Gil")
        val gil = dao.findSomething(1)
        assertThat(gil).isEqualTo("Gil")

    }

    interface Dao {

        @SqlUpdate("create table something (id int primary key, name varchar)")
        fun createTable()

        @SqlUpdate("insert into something (id, name) values (:0, :1)")
        fun insert(id: Int, name: String)

        @SqlQuery("select name from something where id = :0")
        fun findSomething(id: Int): String?
    }


    @Test
    fun testFun() {
        val (hello, world) = pair();
        assertThat(hello).isEqualTo("hello")
        assertThat(world).isEqualTo("world")

        val (one, two, three) = triple();

    }

    fun pair(): Pair<String, String> {
        return Pair("hello", "world")
    }

    fun triple(): Triple<Int, Int, Int> {
        return Triple(1,2,3);
    }
}

