package com.virtual.therapist.android.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.virtual.therapist.android.Config.ChatArrayAdapter;
import com.virtual.therapist.android.Config.LocationUtil;
import com.virtual.therapist.android.Config.SessionManager;
import com.virtual.therapist.android.Network.Socket;
import com.virtual.therapist.android.Network.VirtualTherapistClient;
import com.virtual.therapist.android.Objects.ChatContext;
import com.virtual.therapist.android.Objects.ChatMessage;
import com.virtual.therapist.android.R;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatBubbleActivity extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener
{
    private final String[] MOODS = {"Blij", "Verdrietig", "Boos", "Depressief", "Anders.."};
    private int chatId = -1;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;

    private boolean side = true;
    private Socket socket;
    private static int TTS_DATA_CHECK = 1;
    private TextToSpeech mTextToSpeech;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        showContextDialog();
    }

    private void showContextDialog() {
        final
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("In wat voor stemming ben je?")
                .setItems(MOODS, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == MOODS.length-1) {
                            showOtherDialog();
                        }else{
                            setMoodAndLocation(MOODS[which]);
                        }

                    }
                });
        builder.create().show();
    }

    private void showOtherDialog() {
        final EditText text = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Anders, namelijk:")
                .setView(text)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable input = text.getText();
                        setMoodAndLocation(input.toString());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }

    private void setMoodAndLocation(String mood) {
        ChatContext chatContext = new ChatContext();
        chatContext.setMood(mood);
        Location location = LocationUtil.getInstance(getApplicationContext()).getLastLocation();
        if(location != null) {
            chatContext.setLat(location.getLatitude());
            chatContext.setLng(location.getLongitude());
        }
        System.out.println(chatContext.toString());
        sendContextToServerAndStartChat(chatContext);
    }

    private void sendContextToServerAndStartChat(ChatContext context) {
        VirtualTherapistClient.getInstance().getVtService().context(context.getMood(), context.getLat(), context.getLng(), new Callback<Integer>() {
            @Override
            public void success(Integer integer, Response response) {
                chatId = integer;
                initChat();
            }

            @Override
            public void failure(RetrofitError error) {

                if(error.getResponse().getStatus() == 401) {
                    Toast.makeText(getApplicationContext(), "Niet ingelogd", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Er is iets misgegaan, probeer het opnieuw", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initChat() {
        session = new SessionManager(getApplicationContext());
        //Controller of text to speech aanwezig is
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_DATA_CHECK);

        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        socket              = Socket.getInstance();
        socket.connect();

        buttonSend          = (Button) findViewById(R.id.buttonSend);
        chatText            = (EditText) findViewById(R.id.chatText);

        listView            = (ListView) findViewById(R.id.listView1);
        chatArrayAdapter    = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        listView.setAdapter(chatArrayAdapter);

        chatText.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    Log.d("ChatBubbleActivity", "enter pressed");
                    return parseQuestion(chatText.getText().toString());
                }
                return false;
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Log.d("ChatBubbleActivity", "Parsing chattext question");
                parseQuestion(chatText.getText().toString());
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private boolean sendChatMessage(String msg)
    {
        chatArrayAdapter.add(new ChatMessage(side, msg));
        chatText.setText("");
        side = !side;
        return true;
    }

    private boolean parseQuestion(String question)
    {
        hideSoftKeyboard();

        //Show question into listview
        this.sendChatMessage(question);

        //Send question to the server and get the reply
        sendQuestion(question);

        //Show answer into listview
//        this.sendChatMessage(answer);

        //Lees het antwoord voor
        return true;
    }

    private void sendQuestion(String question)
    {
        try
        {
            socket.sendQuestion(question);

            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        List<String> messages = new ArrayList<String>(socket.getMessages());
                        while (messages.size() == 0)
                        {
                            messages = new ArrayList<String>(socket.getMessages());
                            Thread.sleep(1000);
                        }

                        Log.d("ChatBubbleActivity", "Answer: " + messages.get(0));
                        final String answerMessage = messages.get(0);
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //Spreek het antwoord uit
//                                mTextToSpeech.speak(answerMessage, TextToSpeech.QUEUE_FLUSH, null);
                                speak(answerMessage);

                                //Voeg het antwoord toe aan de chat list
                                sendChatMessage(answerMessage);
                            }
                        });

                    }
                    catch( Exception e ){ e.printStackTrace(); }
                }
            };
            t.start();
        }
        catch(Exception e){e. printStackTrace();}
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == TTS_DATA_CHECK)
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                // success, create the TTS instance
                mTextToSpeech = new TextToSpeech(this, this);

                Locale locale = this.getResources().getConfiguration().locale;
                mTextToSpeech.setLanguage(locale);
            } else
            {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    private void speak(String text)
    {
        if(text != null)
        {
            HashMap<String, String> myHashAlarm = new HashMap<String, String>();
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Executing text to speech");
//            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
        }
    }
    // Fired after TTS initialization
    public void onInit(int status)
    {
        if(status == TextToSpeech.SUCCESS)
        {
            mTextToSpeech.setOnUtteranceCompletedListener(this);

            //Eerste bericht weergeven in de listview en dan die text ook uitspreken
            String firstMessage = "Hallo " + session.getFirstName() + ", waar kan ik je mee helpen?";
            this.sendChatMessage(firstMessage);
//            mTextToSpeech.speak(firstMessage, TextToSpeech.QUEUE_FLUSH, null);
            speak(firstMessage);
        }
    }

    // It's callback
    public void onUtteranceCompleted(String utteranceId)
    {
        Log.i("ChatBubbleActivity", utteranceId); //utteranceId == "SOME MESSAGE"
    }

    private void hideSoftKeyboard()
    {
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(chatText.getWindowToken(), 0);
        }
    }

}