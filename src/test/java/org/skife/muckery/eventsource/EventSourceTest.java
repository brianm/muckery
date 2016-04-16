package org.skife.muckery.eventsource;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.Test;

import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.assertj.core.api.Assertions.assertThat;

public class EventSourceTest {

    @Test
    public void testEventInterop() throws Exception {
        EventBus bus = new EventBus();

        UserInterface ui = new UserInterface(bus);
        Catalog catalog = new Catalog(bus);

        bus.register(ui);
        bus.register(catalog);

        ui.addProduct(new Product("/a"));
        ui.addProduct(new Product("/b"));
        ui.addProduct(new Product("/c"));

        CompletableFuture<SortedSet<Product>> products = ui.listItems();
        SortedSet<Product> ps = products.get();
        assertThat(ps).hasSize(3);
    }

    static class Product implements Comparable<Product> {
        private String id;

        Product(String id) {
            this.id = id;
        }

        @Override
        public int compareTo(Product o) {
            return this.id.compareTo(o.id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Product product = (Product) o;
            return Objects.equal(id, product.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    private static class Catalog {

        private EventBus bus;
        private SortedSet<Product> products = new ConcurrentSkipListSet<>();

        Catalog(EventBus bus) {
            this.bus = bus;
        }

        @Subscribe
        public void listProducts(Events.ListProductsRequest req) {
            bus.post(new Events.ListProductResponse(req.queryId, ImmutableSortedSet.copyOf(products)));
        }

        @Subscribe
        public void addProduct(Events.AddProduct req) {
            this.products.add(req.getProduct());
        }
    }

    private static class UserInterface {
        private EventBus bus;

        UserInterface(EventBus bus) {
            this.bus = bus;
        }


        void addProduct(Product p) {
            bus.post(new Events.AddProduct(p));
        }

        CompletableFuture<SortedSet<Product>> listItems() {
            final UUID id = UUID.randomUUID();
            CompletableFuture<SortedSet<Product>> result = new CompletableFuture<>();

            bus.register(new Object() {

                @Subscribe()
                public void receive(Events.ListProductResponse r) {
                    if (id.equals(r.getQueryId())) {
                        bus.unregister(this);
                        result.complete(r.getProducts());
                    }
                }
            });

            bus.post(new Events.ListProductsRequest(id));

            return result;
        }
    }

    private interface Events {

        class ListProductsRequest {
            private UUID queryId;

            ListProductsRequest(UUID queryId) {
                this.queryId = queryId;
            }
        }

        class ListProductResponse {
            private final UUID queryId;
            private final SortedSet<Product> products;

            ListProductResponse(UUID queryId,
                                SortedSet<Product> products) {
                this.queryId = queryId;
                this.products = products;
            }

            UUID getQueryId() {
                return queryId;
            }

            SortedSet<Product> getProducts() {
                return products;
            }
        }

        class AddProduct {

            private Product product;

            AddProduct(Product product) {

                this.product = product;
            }

            Product getProduct() {
                return product;
            }
        }
    }
}
