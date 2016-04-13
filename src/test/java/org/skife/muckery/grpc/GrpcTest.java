package org.skife.muckery.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import org.skife.muckery.grpc.hello.Greeting;
import org.skife.muckery.grpc.hello.HelloServiceGrpc;
import org.skife.muckery.grpc.hello.Person;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcTest {

    @Test
    public void testBrian() throws Exception {
        int port = 4321;
        Server server = ServerBuilder.forPort(port)
                                     .addService(HelloServiceGrpc.bindService(new HelloService()))
                                     .build()
                                     .start();

        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", port)
                                                      .usePlaintext(true)
                                                      .build();
        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

        try {
            Greeting greeting = stub.greet(Person.newBuilder()
                                                 .setName("Brian")
                                                 .build());

            assertThat(greeting.getMessage()).isEqualTo("Hello, Brian");
        } finally {
            server.shutdown();
            channel.shutdown();
        }
    }

    public static class HelloService implements HelloServiceGrpc.HelloService {

        @Override
        public void greet(final Person request, final StreamObserver<Greeting> responseObserver) {
            String name = request.getName();
            if ("Fred".equals(name)) {
                responseObserver.onError(new IllegalArgumentException("No Freds Allowed"));
            }
            else {
                responseObserver.onNext(Greeting.newBuilder().setMessage("Hello, " + name).build());
                responseObserver.onCompleted();
            }
        }
    }
}
