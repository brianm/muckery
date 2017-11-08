package org.skife.muckery.vavr;

import io.vavr.CheckedFunction0;
import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.control.Try;
import io.vavr.test.Arbitrary;
import io.vavr.test.Gen;
import io.vavr.test.Property;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;
import static org.assertj.core.api.Assertions.assertThat;

public class VavrTest {
    @Test
    public void testSimpleFold() throws Exception {
        String folded = List.of("hello", "world")
                            .fold("", (r, l) -> r + l);
        assertThat(folded).isEqualTo("helloworld");
    }

    @Test
    public void testProperty() throws Exception {
        Arbitrary<Integer> ints = Arbitrary.integer();
        Property.def("-x * x <= 0")
                .forAll(ints)
                .suchThat((x) -> ((x * -1) * x) <= 0)
                .check()
                .assertIsSatisfied();

        Arbitrary<String> strings = Arbitrary.string(Gen.choose('A', 'z'));
        Property.def("ascii chars are one byte")
                .forAll(strings)
                .suchThat((s) -> s.length() == s.getBytes(StandardCharsets.UTF_8).length)
                .check()
                .assertIsSatisfied();
    }

    @Test
    public void testTry() throws Exception {
        Try<String> t = Try.of((CheckedFunction0<String>) () -> {
            throw new Exception("meow");
        }).recover(Throwable::getMessage);

        assertThat(t.get()).isEqualTo("meow");
    }

    @Test
    public void testMatch() throws Exception {
        String s = Match(7).of(
                Case($(is(1)), "one"),
                Case($(is(2)), "two"),
                Case($(is(3)), "many"),
                Case($((i) -> i > 3), "lots"),
                Case($(), "?")
        );

        assertThat(s).isEqualTo("lots");
    }
}
