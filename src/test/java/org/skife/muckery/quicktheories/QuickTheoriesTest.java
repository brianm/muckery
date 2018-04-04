package org.skife.muckery.quicktheories;

import org.junit.Test;
import org.quicktheories.impl.Constraint;

import static org.quicktheories.QuickTheory.qt;

public class QuickTheoriesTest {
    @Test
    public void testFoo() throws Exception {
        qt().withExamples(100000)
            .forAll(r -> {
                long l = r.next(Constraint.none());
                return "" + l;
            }).check((s) -> true);
    }
}
