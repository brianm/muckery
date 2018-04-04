package org.skife.muckery.jdbi3;

import org.immutables.value.Value;

import javax.persistence.Column;
import javax.persistence.Entity;

@Value.Immutable
@Entity
public interface Something {

    @Column(name = "id")
    long id();

    @Column(name = "name")
    String name();
}
