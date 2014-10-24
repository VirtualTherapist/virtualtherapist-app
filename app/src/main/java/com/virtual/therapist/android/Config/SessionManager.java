package com.virtual.therapist.android.Config;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.virtual.therapist.android.Activities.LoginActivity;

/**
 * Created by Akatchi on 24-10-2014.
 */
public class SessionManager
{
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    public Context context;
    private int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "VirtualTherapist";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_FIRST_NAME   = "firstname";
    public static final String KEY_LAST_NAME    = "lastname";
    public static final String KEY_EMAIL        = "email";
    public static final String KEY_AUTH_TOKEN   = "authtoken";

    // Constructor
    public SessionManager(Context context)
    {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String fname, String lname, String email)
    {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_FIRST_NAME, fname);
        editor.putString(KEY_LAST_NAME, lname);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public void checkLogin()
    {
        if(!this.isLoggedIn())
        {
            Intent i = new Intent(context, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public void logoutUser()
    {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        Toast.makeText(context, "U bent uitgelogd!", Toast.LENGTH_SHORT).show();
        
        Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public boolean isLoggedIn()
    {
        return pref.getBoolean(IS_LOGIN, false);
    }

    public String getFirstName()
    {
        return pref.getString(KEY_FIRST_NAME, "firstname");
    }

    public String getLastName()
    {
        return pref.getString(KEY_LAST_NAME,  "lastname");
    }

    public String getToken()
    {
        return pref.getString(KEY_AUTH_TOKEN, "token");
    }

    public void setToken(String token)
    {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.commit();
    }
}