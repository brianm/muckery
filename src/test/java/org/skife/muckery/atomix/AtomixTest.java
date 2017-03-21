package org.skife.muckery.atomix;

import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.concurrent.DistributedLock;
import io.atomix.copycat.server.storage.Storage;
import org.junit.Test;
import org.skife.muckery.NetUtil;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AtomixTest {
    @Test
    public void testFoo() throws Exception {

        final Path tmp = Files.createTempDirectory(this.getClass().getName());

        final int p1 = NetUtil.findUnusedPort();
        final int p2 = NetUtil.findUnusedPort();
        final int p3 = NetUtil.findUnusedPort();
        final int p4 = NetUtil.findUnusedPort();
        final int p5 = NetUtil.findUnusedPort();

        final AtomixReplica a1 = AtomixReplica.builder(new Address("localhost", p1))
                                              .withTransport(NettyTransport.builder()
                                                                           .build())
                                              .withStorage(Storage.builder()
                                                                  .withDirectory(tmp.resolve("1").toFile())
                                                                  .build())
                                              .build()
                                              .bootstrap()
                                              .join();

        final AtomixReplica a2 = AtomixReplica.builder(new Address("localhost", p2))
                                              .withTransport(NettyTransport.builder().build())
                                              .withStorage(Storage.builder()
                                                                  .withDirectory(tmp.resolve("2").toFile())
                                                                  .build())
                                              .build()
                                              .join(new Address("localhost", p1))
                                              .join();

        final AtomixReplica a3 = AtomixReplica.builder(new Address("localhost", p3))
                                              .withTransport(NettyTransport.builder().build())
                                              .withStorage(Storage.builder()
                                                                  .withDirectory(tmp.resolve("3").toFile())
                                                                  .build())
                                              .build()
                                              .join(new Address("localhost", p1))
                                              .join();

        final AtomixReplica a4 = AtomixReplica.builder(new Address("localhost", p4))
                                              .withTransport(NettyTransport.builder().build())
                                              .withStorage(Storage.builder()
                                                                  .withDirectory(tmp.resolve("4").toFile())
                                                                  .build())
                                              .build()
                                              .join(new Address("localhost", p1))
                                              .join();

        final AtomixReplica a5 = AtomixReplica.builder(new Address("localhost", p5))
                                              .withTransport(NettyTransport.builder().build())
                                              .withStorage(Storage.builder()
                                                                  .withDirectory(tmp.resolve("5").toFile())
                                                                  .build())
                                              .build()
                                              .join(new Address("localhost", p1))
                                              .join();

        final DistributedLock lock = a1.getLock("locky").get(1, TimeUnit.SECONDS);
        lock.lock().thenRun(() -> System.out.println("LOCK 1")).join();


        a2.<Integer>getValue("a").join().set(1).join();
        final int a = a3.<Integer>getValue("a").join().get().join();
        assertThat(a).isEqualTo(1);

        a1.leave().join();
        a2.leave().join();
        a3.leave().join();
        a4.leave().join();
        a5.leave().join();

        Files.walkFileTree(tmp, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
