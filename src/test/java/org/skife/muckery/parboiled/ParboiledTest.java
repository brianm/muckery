package org.skife.muckery.parboiled;

import org.junit.Test;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import static org.assertj.core.api.Assertions.assertThat;

public class ParboiledTest
{
    @Test
    public void testPredicate() throws Exception
    {
        PredicateParser p = Parboiled.createParser(PredicateParser.class);
        ParsingResult<Object> out = new ReportingParseRunner<>(
            p.Predicate()).run("event.sleepState == 'tired' " +
                               "&& ian != 'wombat squirrel 7'");

        assertThat(out.parseErrors).isEmpty();
        assertThat(out.matched).isTrue();
    }

    @Test
    public void testExpression() throws Exception
    {
        PredicateParser p = Parboiled.createParser(PredicateParser.class);
        ParsingResult<Object> out = new ReportingParseRunner(p.Expression()).run("brian == 'tired'");

        assertThat(out.parseErrors).isEmpty();
        assertThat(out.matched).isTrue();
        System.out.println(ParseTreeUtils.printNodeTree(out));
    }

    @Test
    public void testLiteral() throws Exception
    {
        PredicateParser p = Parboiled.createParser(PredicateParser.class);
        ParsingResult<Object> out = new ReportingParseRunner<>(p.Literal()).run("'hello'");

        assertThat(out.parseErrors).isEmpty();
        assertThat(out.matched).isTrue();
        System.out.println(ParseTreeUtils.printNodeTree(out));
    }
}
