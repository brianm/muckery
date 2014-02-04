package org.skife.muckery.okhttp;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class OkHttpTest
{
    @Test(expected = java.net.SocketTimeoutException.class)
    public void testFoo() throws Exception
    {
        MockWebServer mws = new MockWebServer();
        mws.enqueue(new MockResponse().setResponseCode(200)
                                      .setBody("hello world".getBytes(Charsets.UTF_8))
                                      .setBodyDelayTimeMs(1000));
        mws.play();
        try {

            OkHttpClient ok = new OkHttpClient();
            HttpURLConnection conn = ok.open(mws.getUrl("/"));
            conn.setConnectTimeout(1);
            conn.setReadTimeout(1);
            InputStream in = conn.getInputStream();

            // shouldn't be reachable, but clean up in case it is
            byte[] _ = ByteStreams.toByteArray(in);
        }
        finally {
            mws.shutdown();
        }
    }
}
