package org.skife.muckery.eventsource;

import org.immutables.value.Value;

@Value.Style(
        // Detect names starting with underscore
        typeAbstract = "_*",
        // Generate without any suffix, just raw detected name
        typeImmutable = "*",
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        defaults = @Value.Immutable(copy = false))
public @interface Event {
}
