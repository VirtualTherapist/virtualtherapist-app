package com.example.jeroenlammerts.virtualtherapist;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by jeroenlammerts on 10-10-14.
 */
public class VirtualTherapistClient {

    private static final String API_URL = "http://private-b929f-virtualtherapist.apiary-mock.com/";

    public VirtualTherapistClient(){

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Authorization", "yourToken");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setRequestInterceptor(requestInterceptor)
                .build();

        VirtualTherapistService vtService = restAdapter.create(VirtualTherapistService.class);

        vtService.addQuestion("Ben ik te dik?", new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                System.out.println("response....");
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("failure...");
            }
        });

    }

    interface VirtualTherapistService {

        @POST("/question")
        void addQuestion(@Body String question, Callback<Response> responseCallback);

    }

}
