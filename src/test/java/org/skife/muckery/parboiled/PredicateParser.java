package org.skife.muckery.parboiled;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;

@BuildParseTree
public class PredicateParser extends BaseParser<Object>
{
    @SuppressWarnings("InfiniteRecursion")
    public Rule Predicate() {
        return Sequence(Expression(),
                        OptionalWhitespace(),
                        Optional(Sequence(And(),
                                          OptionalWhitespace(),
                                          Predicate())), EOI);
    }

    public Rule Expression()
    {
        return Sequence(Atom(),
                        OptionalWhitespace(),
                        Operator(),
                        OptionalWhitespace(),
                        Atom());
    }

    @SuppressNode
    public Rule OptionalWhitespace()
    {
        return Optional(OneOrMore(AnyOf(" \t")));
    }

    public Rule Atom()
    {
        return FirstOf(VariableName(), Literal());
    }

    @SuppressSubnodes
    public Rule VariableName()
    {
        return OneOrMore(FirstOf(CharRange('a', 'z'),
                                 CharRange('A', 'Z'),
                                 Ch('.'), Ch('_'),
                                 Ch('-')));
    }

    public Rule Literal()
    {
        return FirstOf(QuotedString(), Number());
    }

    public Rule Number()
    {
        return OneOrMore(CharRange('0', '9'));
    }

    public Rule And()
    {
        return String("&&");
    }

    @SuppressSubnodes
    public Rule QuotedString()
    {
        return Sequence(Ch('\''),
                        ZeroOrMore(NoneOf("'")),
                        Ch('\''));
    }

    public Rule Operator()
    {
        return FirstOf(EqualOperator(),
                       NotEqualOperator());
    }

    public Rule EqualOperator()
    {
        return String("==");
    }

    public Rule NotEqualOperator()
    {
        return String("!=");
    }
}