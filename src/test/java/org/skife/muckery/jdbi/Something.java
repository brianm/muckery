package org.skife.muckery.jdbi;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Something {
    private int id;
    private String name;

    @JsonCreator
    public Something(@JsonProperty("id") final int id, @JsonProperty("name") final String name) {
        this.id = id;
        this.name = name;
    }

    public Something() {}

    public void setId(final int id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Something something = (Something) o;
        return this.id == something.id &&
                Objects.equals(this.name, something.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("id", this.id)
                          .add("name", this.name)
                          .toString();
    }
}
