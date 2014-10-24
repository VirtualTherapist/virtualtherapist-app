package com.virtual.therapist.android.Config;

import android.util.Log;

import java.security.MessageDigest;

/**
 * Created by bas on 20-10-14.
 */
public class HashUtil
{

    public static String createHash(String email, String password)
    {
        byte[] hash = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(email.getBytes("UTF-8"));
                md.update(password.getBytes("UTF-8"));

            hash = md.digest();
        }
        catch( Exception e ) { Log.d("HashUtil", "Error: " + e.getMessage()); }

        return bytesToHex(hash);
    }

    public static String bytesToHex(byte[] bytes)
    {
        final char[] hexArray   = "0123456789abcdef".toCharArray();
        char[] hexChars         = new char[bytes.length * 2];

        for ( int j = 0; j < bytes.length; j++ )
        {
            int v               = bytes[j] & 0xFF;
            hexChars[j * 2]     = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }
}