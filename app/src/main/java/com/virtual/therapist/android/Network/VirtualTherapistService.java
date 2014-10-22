package com.virtual.therapist.android.Network;

import com.virtual.therapist.android.Objects.User;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

public interface VirtualTherapistService
{
    @POST("/question")
    public void addQuestion(@Body String question, Callback<Response> responseCallback);

    @GET("/login")
    public void login(@Header("authentication") String authentication, Callback<User> responseCallback);

}