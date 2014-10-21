package com.example.jeroenlammerts.virtualtherapist;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by jeroenlammerts on 10-10-14.
 */
public class VirtualTherapistClient {

//    private static final String API_URL = "http://private-b929f-virtualtherapist.apiary-mock.com/";
    private static final String API_URL = "http://145.37.89.223:9000/api/";
    private static VirtualTherapistClient instance;
    public static String authToken;
    private VirtualTherapistService vtService;
    private RestAdapter restAdapter;

    public VirtualTherapistClient(){

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                if(!authToken.isEmpty()){
                    request.addHeader("Authorization", VirtualTherapistClient.authToken);
                }
            }
        };

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(requestInterceptor)
                .build();

        vtService = restAdapter.create(VirtualTherapistService.class);

//        vtService.addQuestion("Ben ik te dik?", new Callback<Response>() {
//            @Override
//            public void success(Response response, Response response2) {
//                System.out.println("response....");
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                System.out.println("failure...");
//            }
//        });

    }

    public static VirtualTherapistClient getInstance(){
        if(instance == null){
            instance = new VirtualTherapistClient();
        }
        return instance;
    }

    public RestAdapter getRestAdapter(){
        return this.restAdapter;
    }

    public VirtualTherapistService getVtService(){
        return vtService;
    }

}
