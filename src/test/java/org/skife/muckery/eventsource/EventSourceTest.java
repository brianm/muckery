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
        EventBus bus = new EventBus();

        UserInterface ui = new UserInterface(bus);
        Catalog catalog = new Catalog(bus);

        bus.register(ui);
        bus.register(catalog);

        ui.addProduct(ImmutableProduct.builder().id("/a").build());
        ui.addProduct(ImmutableProduct.builder().id("/b").build());
        ui.addProduct(ImmutableProduct.builder().id("/c").build());

        CompletableFuture<SortedSet<Product>> products = ui.listProducts();
        SortedSet<Product> ps = products.get();
        assertThat(ps).hasSize(3);
    }

    private static class Catalog {

        private EventBus bus;
        private SortedSet<Product> products = new ConcurrentSkipListSet<>();

        Catalog(EventBus bus) {
            this.bus = bus;
        }

        @Subscribe
        public void listProducts(ListProductsRequest req) {

            bus.post(ListProductResponse.builder()
                                        .queryId(req.queryId())
                                        .products(products)
                                        .build());
        }

        @Subscribe
        public void addProduct(AddProduct req) {
            this.products.add(req.product());
        }
    }

    private static class UserInterface {
        private EventBus bus;

        UserInterface(EventBus bus) {
            this.bus = bus;
        }


        void addProduct(Product p) {
            bus.post(AddProduct.builder()
                               .product(p)
                               .build());
        }

        CompletableFuture<SortedSet<Product>> listProducts() {
            final UUID id = UUID.randomUUID();
            CompletableFuture<SortedSet<Product>> result = new CompletableFuture<>();

            bus.register(new Object() {

                @Subscribe()
                public void receive(ListProductResponse r) {
                    if (id.equals(r.queryId())) {
                        bus.unregister(this);
                        result.complete(r.products());
                    }
                }
            });

            bus.post(ListProductsRequest.builder()
                                        .queryId(id)
                                        .build());

            return result;
        }
    }


    @Value.Immutable
    interface Product extends Comparable<Product> {
        String id();

        @Override
        default int compareTo(Product o) {
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
