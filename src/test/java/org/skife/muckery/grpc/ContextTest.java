package org.skife.muckery.grpc;

import io.grpc.Context;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextTest {
    @Test
    public void testFoo() throws Exception {
        Context ctx = Context.current();
        assertThat(ctx).isNotNull();

        Context.Key<Integer> k1 = Context.keyWithDefault("waffle", 7);

        Context ctx2 = ctx.withValue(k1, 42).attach();

        assertThat(k1.get()).isEqualTo(42);

        Context.CancellableContext cancel = ctx.withCancellation();
        cancel.attach();
        cancel.withValue(k1, 37).attach();

    }
}
