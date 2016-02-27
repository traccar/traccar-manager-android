package org.traccar.manager;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.traccar.manager.model.Device;
import org.traccar.manager.model.User;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class WebServiceTest {

    @Test
    public void testWebService() throws Exception {

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager)).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://localhost:8082")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        WebService service = retrofit.create(WebService.class);

        User user = service.addSession("admin", "admin").execute().body();

        Assert.assertNotNull(user);

        List<Device> devices = service.getDevices().execute().body();

        Assert.assertNotNull(devices);

    }

}
