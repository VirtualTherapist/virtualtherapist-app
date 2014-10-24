package com.virtual.therapist.android.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.virtual.therapist.android.Config.HashUtil;
import com.virtual.therapist.android.Config.SessionManager;
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
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());

        if ( session.isLoggedIn() )
        {
            VirtualTherapistClient.authToken = session.getToken();

            Intent myIntent = new Intent(this, MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LoginActivity.this.startActivity(myIntent);
        }

        mEmailField     = (EditText) findViewById(R.id.email);
        mPasswordField  = (EditText) findViewById(R.id.password);

        mEmailField.setText("admin@therapist.com");
        mPasswordField.setText("password");

    }

    public void doLogin(View view)
    {
        final String emailString      = mEmailField.getText().toString();
        String passwordString         = mPasswordField.getText().toString();

        final String authentication   = HashUtil.createHash(emailString, passwordString);
        VirtualTherapistClient.authToken = authentication;

        apiClient.login(authentication, new Callback<User>()
        {
            @Override
            public void success(User user, Response response)
            {
                session.createLoginSession(user.first_name, user.last_name, emailString);
                session.setToken(authentication);

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        });

    }

}
