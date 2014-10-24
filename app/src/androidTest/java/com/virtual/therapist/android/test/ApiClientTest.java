package com.virtual.therapist.android.test;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.virtual.therapist.android.Config.HashUtil;
import com.virtual.therapist.android.Network.VirtualTherapistClient;
import com.virtual.therapist.android.Network.VirtualTherapistService;
import com.virtual.therapist.android.Objects.User;

import junit.framework.TestCase;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jeroenlammerts on 24-10-14.
 */
public class ApiClientTest extends InstrumentationTestCase {

    private static boolean called;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        called = false;
    }

    @SmallTest
    public void testLoginCall() throws Throwable{

        final CountDownLatch signal = new CountDownLatch(1);

        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {

                VirtualTherapistService apiClient = VirtualTherapistClient.getInstance().getVtService();

                String emailString      = "admin@therapist.com";
                String passwordString   = "password";

                String authentication   = HashUtil.createHash(emailString, passwordString);
                VirtualTherapistClient.authToken = authentication;

                apiClient.login(authentication, new Callback<User>()
                {
                    @Override
                    public void success(User user, Response response)
                    {
                        int responseCode = response.getStatus();
                        if(responseCode == 200){
                            called = true;
                        } else {
                            called = false;
                        }
                        signal.countDown();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        called = false;
                        signal.countDown();
                    }

                });

            }
        });

        signal.await(5, TimeUnit.SECONDS);
        assertTrue(called);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
