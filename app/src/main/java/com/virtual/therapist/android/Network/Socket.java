package com.virtual.therapist.android.Network;

import android.util.Log;
import com.virtual.therapist.android.Config.Variables;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akatchi on 22-10-2014.
 */
public class Socket
{
    private String uri;
    private final WebSocketConnection mConnection = new WebSocketConnection();
    private static Socket instance                = null;
    private String response                       = null;
    private List<String> messages;

    private Socket()
    {
        this.uri = Variables.WEBSOCKET_URL;
        messages = new ArrayList<String>();
    }

    public static Socket getInstance()
    {
        if(instance == null){ instance = new Socket(); }
        return instance;
    }

    public void connect()
    {
        try
        {
            Log.d("Socket", "Attempting to connect to: " + uri);
            mConnection.connect(uri, new WebSocketHandler()
            {
                @Override
                public void onOpen() { Log.d("Socket", "Connected"); }

                @Override
                public void onTextMessage(String message)
                {
                    Log.d("Socket", "Retrieved message: " + message);
                    messages.add(message);
                }

                @Override
                public void onClose(int code, String reason){ Log.d("Socket", "Connection lost | Code: " + code + " Reason: " + reason); }
            });

        }
        catch( Exception e ) { Log.d("Socket", "Error: " + e.getMessage()); }
    }

    public void sendMessage(String message)
    {
        Log.d("Socket", "Sending text message: " + message);
        mConnection.sendTextMessage(message);
    }

    public void sendQuestion(String question)
    {
        Log.d("Socket", "Sending question: [question]" + question );
        mConnection.sendTextMessage("[question]" + question);
    }

    public void sendContext(String context)
    {
        Log.d("Socket", "Sending context");
        mConnection.sendTextMessage("[context]" + context);
    }

    public void disconnect()
    {
        Log.d("Socket", "Disconnecting");
        mConnection.disconnect();
    }

    public boolean isConnected()
    {
        Log.d("Socket", "Checking for connection state");
        return mConnection.isConnected();
    }

    public List<String> getMessages()
    {
        List<String> returnList = new ArrayList<String>(messages);
        messages.clear();
        return returnList;
    }
}
