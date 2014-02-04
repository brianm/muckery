package org.skife.muckery.retrofit;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.Test;
import retrofit.RestAdapter;
import retrofit.http.GET;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RetrofitTest
{
    @Test
    public void testFoo() throws Exception
    {
        MockWebServer mws = new MockWebServer();
        mws.enqueue(new MockResponse().setResponseCode(200)
                                      .setBody("{\"hello\":\"world\"}".getBytes(Charsets.UTF_8)));
        mws.play();

        RestAdapter ra = new RestAdapter.Builder()
            .setEndpoint(mws.getUrl("/").toString())
            .build();

        Client c = ra.create(Client.class);

        assertThat(c.getRoot()).isEqualTo(ImmutableMap.of("hello", "world"));

        mws.shutdown();
    }

    public static interface Client
    {
        @GET("/")
        public Map<String, String> getRoot();
    }
}
