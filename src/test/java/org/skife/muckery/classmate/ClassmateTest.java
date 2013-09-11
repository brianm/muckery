package org.skife.muckery.classmate;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedMethod;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassmateTest
{
    @Test
    public void testFoo() throws Exception
    {
        TypeResolver tr = new TypeResolver();
        MemberResolver mr = new MemberResolver(tr);

        ResolvedType rt = tr.resolve(Foo.class);
        ResolvedTypeWithMembers rtm = mr.resolve(rt, null, null);

        Class<?> param_type = null;
        for (ResolvedMethod member : rtm.getMemberMethods()) {
            if ("bar".equals(member.getName())) {
                ResolvedType arg_type = member.getReturnType();
                param_type = arg_type.getTypeParameters()
                                     .get(0)
                                     .getErasedType();
            }
        }

        assertThat(param_type).isEqualTo(String.class);
    }

    public static interface Foo
    {
        public List<String> bar();
    }

}
