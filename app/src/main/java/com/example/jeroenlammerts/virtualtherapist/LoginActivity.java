package com.example.jeroenlammerts.virtualtherapist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends Activity {

    private VirtualTherapistService apiClient = VirtualTherapistClient.getInstance().getVtService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void doLogin(View view) {

        EditText emailField = (EditText)findViewById(R.id.email);
        EditText passwordField = (EditText)findViewById(R.id.password);

        String emailString = emailField.getText().toString();
        String passwordString = passwordField.getText().toString();

        String authentication = HashUtil.createHash(emailString, passwordString);
        VirtualTherapistClient.authToken = authentication;

        apiClient.login(authentication, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                Intent i = new Intent(getApplicationContext(), ChatBubbleActivity.class);
                i.putExtra("name", user.first_name);
                startActivity(i);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        });

    }

}
