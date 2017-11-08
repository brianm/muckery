package org.skife.muckery.protobuf;

import org.junit.Test;
import org.skife.muckery.grpc.hello.Hello;

public class ProtoTest {
    @Test
    public void testFoo() throws Exception {
        Hello.Person p = Hello.Person.newBuilder().setName("Brian").build();

    }
}
