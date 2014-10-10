package com.example.jeroenlammerts.virtualtherapist;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by jeroenlammerts on 10-10-14.
 */
public interface VirtualTherapistService {

    @POST("/question")
    void addQuestion(@Body String question, Callback<Response> responseCallback);

}
