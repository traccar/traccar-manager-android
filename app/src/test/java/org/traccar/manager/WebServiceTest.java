package org.traccar.manager;

import com.squareup.okhttp.OkHttpClient;

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

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class WebServiceTest {

    @Test
    public void testWebService() throws Exception {

        OkHttpClient client = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.setCookieHandler(cookieManager);

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://localhost:8082")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WebService service = retrofit.create(WebService.class);

        User user = service.addSession("admin", "admin").execute().body();

        Assert.assertNotNull(user);

        List<Device> devices = service.getDevices().execute().body();

        Assert.assertNotNull(devices);

    }

}
