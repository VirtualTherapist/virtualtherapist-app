package com.virtual.therapist.android.Network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.virtual.therapist.android.R;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ApiErrorHandler {

    public ApiErrorHandler(Context context, RetrofitError error){

        Response r = error.getResponse();
        String errorMsg = "";

        if(error.getKind() == RetrofitError.Kind.NETWORK){
            errorMsg = context.getString(R.string.error_network);
        } else if(r != null && r.getStatus() == 401){
            errorMsg = context.getString(R.string.error_autorization);
        } else {
            errorMsg = context.getString(R.string.error_general) + ": " + error.toString();
        }

        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();

    }

}
