package org.skife.muckery.eventsource;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.immutables.value.Value;
import org.junit.Test;

import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.assertj.core.api.Assertions.assertThat;

public class EventSourceTest {

    @Test
    public void testEventProtocolMuckery() throws Exception {
        final EventBus bus = new EventBus();

        final UserInterface ui = new UserInterface(bus);

        final Catalog catalog = new Catalog(bus);

        bus.register(ui);
        bus.register(catalog);

        ui.addProduct(ImmutableProduct.builder().id("/a").build());
        ui.addProduct(ImmutableProduct.builder().id("/b").build());
        ui.addProduct(ImmutableProduct.builder().id("/c").build());

        final CompletableFuture<SortedSet<Product>> products = ui.listProducts();
        final SortedSet<Product> ps = products.get();
        assertThat(ps).hasSize(3);
    }

    private static class Catalog {

        private final EventBus bus;
        private final SortedSet<Product> products = new ConcurrentSkipListSet<>();

        Catalog(final EventBus bus) {
            this.bus = bus;
        }

        @Subscribe
        public void listProducts(final ListProductsRequest req) {

            this.bus.post(ListProductResponse.builder()
                                             .queryId(req.queryId())
                                             .products(this.products)
                                             .build());
        }

        @Subscribe
        public void addProduct(final AddProduct req) {
            this.products.add(req.product());
        }
    }

    private static class UserInterface {
        private final EventBus bus;

        UserInterface(final EventBus bus) {
            this.bus = bus;
        }


        void addProduct(final Product p) {
            this.bus.post(AddProduct.builder()
                                    .product(p)
                                    .build());
        }

        CompletableFuture<SortedSet<Product>> listProducts() {
            final UUID id = UUID.randomUUID();
            final CompletableFuture<SortedSet<Product>> result = new CompletableFuture<>();

            this.bus.register(new Object() {

                @Subscribe()
                public void receive(final ListProductResponse r) {
                    if (id.equals(r.queryId())) {
                        UserInterface.this.bus.unregister(this);
                        result.complete(r.products());
                    }
                }
            });

            this.bus.post(ListProductsRequest.builder()
                                             .queryId(id)
                                             .build());

            return result;
        }
    }


    @Value.Immutable
    interface Product extends Comparable<Product> {
        String id();

        @Override
        default int compareTo(final Product o) {
            return this.id().compareTo(o.id());
        }
    }

    @Value.Immutable
    @Event
    interface _ListProductsRequest {
        UUID queryId();
    }

    @Value.Immutable
    @Event
    interface _ListProductResponse {
        UUID queryId();

        @Value.NaturalOrder
        SortedSet<Product> products();

    }

    @Value.Immutable
    @Event
    interface _AddProduct {
        Product product();
    }
}
