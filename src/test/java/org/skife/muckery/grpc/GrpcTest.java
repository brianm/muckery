package org.skife.muckery.grpc;

import io.grpc.ChannelImpl;
import io.grpc.ServerImpl;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import org.skife.muckery.grpc.hello.Greeting;
import org.skife.muckery.grpc.hello.HelloGrpc;
import org.skife.muckery.grpc.hello.Person;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcTest
{
    @Test
    public void testFoo() throws Exception
    {

        ServerImpl server = NettyServerBuilder.forPort(7654)
                                              .addService(HelloGrpc.bindService(new HelloService()))
                                              .build()
                                              .start();

        ChannelImpl channel = NettyChannelBuilder.forAddress("127.0.0.1", 7654)
                                                 .negotiationType(NegotiationType.PLAINTEXT)
                                                 .build();

        HelloGrpc.HelloStub stub = HelloGrpc.newStub(channel);

        final AtomicReference<String> result = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        stub.greet(Person.newBuilder().setName("Brian").build(), new StreamObserver<Greeting>()
        {
            @Override
            public void onValue(final Greeting greeting)
            {
                result.set(greeting.getMessage());
            }

            @Override
            public void onError(final Throwable throwable)
            {

            }

            @Override
            public void onCompleted()
            {
                latch.countDown();
            }
        });
        latch.await();
        assertThat(result.get()).isEqualTo("Hello, Brian");


        final CountDownLatch boomlatch = new CountDownLatch(1);
        final AtomicReference<Throwable> bang = new AtomicReference<>();
        stub.greet(Person.newBuilder().setName("Fred").build(), new StreamObserver<Greeting>()
        {
            @Override
            public void onValue(final Greeting greeting)
            {

            }

            @Override
            public void onError(final Throwable throwable)
            {
                bang.set(throwable);
                boomlatch.countDown();
            }

            @Override
            public void onCompleted()
            {

            }
        });
        boomlatch.await();
        System.out.println(bang.get().getMessage());


        HelloGrpc.HelloBlockingStub bstub = HelloGrpc.newBlockingStub(channel);
        Greeting rs = bstub.greet(Person.newBuilder().setName("Greg").build());
        assertThat(rs.getMessage()).isEqualTo("Hello, Greg");

        server.shutdown();

        server.awaitTermination(30, TimeUnit.MINUTES);

    }

    public static class HelloService implements HelloGrpc.Hello
    {

        @Override
        public void greet(final Person request, final StreamObserver<Greeting> responseObserver)
        {
            String name = request.getName();
            if ("Fred".equals(name)) {
                responseObserver.onError(new IllegalArgumentException("No Freds Allowed"));
            }
            else {
                responseObserver.onValue(Greeting.newBuilder().setMessage("Hello, " + name).build());
                responseObserver.onCompleted();
            }
        }
    }
}
