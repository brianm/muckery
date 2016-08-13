package org.skife.muckery.grpc;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.muckery.ExecutorServiceRule;
import org.skife.muckery.NetUtil;
import org.skife.muckery.grpc.hello.Hello;
import org.skife.muckery.grpc.hello.HelloServiceGrpc;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GrpcTest {

    @Rule
    public ExecutorServiceRule exec = new ExecutorServiceRule();

    private ManagedChannel channel;
    private Server server;

    @Before
    public void setUp() throws Exception {

        final int port = NetUtil.findUnusedPort();
        this.server = NettyServerBuilder.forPort(port)
                                        .executor(this.exec)
                                        .addService(new HelloService())
                                        .build()
                                        .start();


        this.channel = NettyChannelBuilder.forAddress("127.0.0.1", port)
                                          .executor(this.exec)
                                          .directExecutor()
                                          .usePlaintext(true)
                                          .build();
    }

    @After
    public void tearDown() throws Exception {
        this.server.shutdown();
        this.channel.shutdown();
    }

    @Test
    public void testBlockingStub() throws Exception {

        final HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(this.channel);
        final Hello.Greeting greeting = stub.greet(Hello.Person.newBuilder()
                                                               .setName("Brian")
                                                               .build());

        assertThat(greeting.getMessage()).isEqualTo("Hello, Brian");

    }

    @Test
    public void testHelloFutureStub() throws Exception {
        final HelloServiceGrpc.HelloServiceFutureStub stub = HelloServiceGrpc.newFutureStub(this.channel);


        final ListenableFuture<Hello.Greeting> greeting = stub.greet(Hello.Person.newBuilder()
                                                                                 .setName("Brian")
                                                                                 .build());

        assertThat(greeting.get().getMessage()).isEqualTo("Hello, Brian");

    }

    @Test
    public void testStreamingClient() throws Exception {
        final HelloServiceGrpc.HelloServiceStub stub = HelloServiceGrpc.newStub(this.channel);

        final Set<String> flags = Sets.newConcurrentHashSet();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> msg = new AtomicReference<>();
        final StreamObserver<Hello.Person> obs = stub.greetEveryone(new StreamObserver<Hello.Greeting>() {
            @Override
            public void onNext(final Hello.Greeting greeting) {
                msg.set(greeting.getMessage());
                flags.add("response");
            }

            @Override
            public void onError(final Throwable throwable) {
                flags.add("error");
            }

            @Override
            public void onCompleted() {
                flags.add("complete");
                latch.countDown();
            }
        });
        obs.onNext(Hello.Person.newBuilder().setName("Joy").build());
        obs.onNext(Hello.Person.newBuilder().setName("Brian").build());
        obs.onCompleted();

        latch.await(1, TimeUnit.SECONDS);
        assertThat(msg.get()).isEqualTo("Hello, Joy, Brian!");
        assertThat(flags).containsExactly("response", "complete");
    }

    @Test
    public void testBidirectionalStreamin() throws Exception {
        final HelloServiceGrpc.HelloServiceStub stub = HelloServiceGrpc.newStub(this.channel);
        final List<String> results = Lists.newArrayList();
        final CountDownLatch latch = new CountDownLatch(1);
        final StreamObserver<Hello.Person> caller = stub.greetEveryoneIndividually(new StreamObserver<Hello.Greeting>() {
            @Override
            public void onNext(final Hello.Greeting greeting) {
                results.add(greeting.getMessage());
            }

            @Override
            public void onError(final Throwable throwable) {
                fail(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        caller.onNext(Hello.Person.newBuilder().setName("Brian").build());
        caller.onNext(Hello.Person.newBuilder().setName("Francisco").build());
        caller.onCompleted();
        latch.await();

        assertThat(results).containsExactly("Hello, Brian!", "Hello, Francisco!");
    }

    private static class HelloService extends HelloServiceGrpc.HelloServiceImplBase {

        @Override
        public void greet(final Hello.Person request, final StreamObserver<Hello.Greeting> responseObserver) {
            final String name = request.getName();
            if ("Fred".equals(name)) {
                responseObserver.onError(new IllegalArgumentException("No Freds Allowed"));
            }
            else {
                responseObserver.onNext(Hello.Greeting.newBuilder().setMessage("Hello, " + name).build());
                responseObserver.onCompleted();
            }
        }

        @Override
        public StreamObserver<Hello.Person> greetEveryone(final StreamObserver<Hello.Greeting> ro) {
            return new StreamObserver<Hello.Person>() {

                private final List<String> names = Lists.newArrayList();

                @Override
                public void onNext(final Hello.Person person) {
                    this.names.add(person.getName());
                }

                @Override
                public void onError(final Throwable throwable) {

                }

                @Override
                public void onCompleted() {
                    ro.onNext(Hello.Greeting.newBuilder()
                                            .setMessage("Hello, " + Joiner.on(", ").join(this.names) + "!")
                                            .build());
                    ro.onCompleted();
                }
            };
        }

        @Override
        public StreamObserver<Hello.Person> greetEveryoneIndividually(final StreamObserver<Hello.Greeting> responseObserver) {
            return new StreamObserver<Hello.Person>() {
                @Override
                public void onNext(final Hello.Person person) {
                    responseObserver.onNext(Hello.Greeting.newBuilder()
                                                          .setMessage("Hello, " + person.getName() + "!")
                                                          .build());
                }

                @Override
                public void onError(final Throwable throwable) {

                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
