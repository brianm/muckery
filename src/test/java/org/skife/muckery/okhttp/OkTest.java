package org.skife.muckery.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OkTest {
    @Rule
    public final MockWebServer server = new MockWebServer();

    @Test
    public void testFoo() throws Exception {
        this.server.enqueue(new MockResponse().setBody("hello world"));

        final OkHttpClient ok = new OkHttpClient.Builder().build();
        final Request req = new Request.Builder().method("GET", null)
                                                 .url(this.server.url("/"))
                                                 .addHeader("Foo", "Bar")
                                                 .build();
        final Response r = ok.newCall(req).execute();
        assertThat(r.body().string()).isEqualTo("hello world");
        final RecordedRequest rr = this.server.takeRequest();
        assertThat(rr.getMethod()).isEqualTo("GET");
        assertThat(rr.getHeader("Foo")).isEqualTo("Bar");
    }
}
