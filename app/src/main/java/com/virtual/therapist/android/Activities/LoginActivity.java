package com.virtual.therapist.android.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.virtual.therapist.android.Config.HashUtil;
import com.virtual.therapist.android.Network.VirtualTherapistClient;
import com.virtual.therapist.android.Network.VirtualTherapistService;
import com.virtual.therapist.android.Objects.User;
import com.virtual.therapist.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginActivity extends Activity
{
    private VirtualTherapistService apiClient = VirtualTherapistClient.getInstance().getVtService();
    private EditText mEmailField, mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailField     = (EditText) findViewById(R.id.email);
        mPasswordField  = (EditText) findViewById(R.id.password);

        mEmailField.setText("admin@therapist.com");
        mPasswordField.setText("password");

    }

    public void doLogin(View view)
    {
        String emailString      = mEmailField.getText().toString();
        String passwordString   = mPasswordField.getText().toString();

        String authentication   = HashUtil.createHash(emailString, passwordString);
        VirtualTherapistClient.authToken = authentication;

        apiClient.login(authentication, new Callback<User>()
        {
            @Override
            public void success(User user, Response response)
            {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
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
