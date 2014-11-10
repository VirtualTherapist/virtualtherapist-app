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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.virtual.therapist.android.Config.*;
import com.virtual.therapist.android.Network.ApiErrorHandler;
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

public class ChatBubbleActivity extends Activity implements TextToSpeech.OnInitListener
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
    private ChatContext chatContext;

    private AnimatedGifImageView gifView;
    private TTSUtteranceProgressListener progressListener;

    @Override
    public void onBackPressed() {

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.rating, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        builder.setTitle(this.getResources().getString(R.string.rating_dialog_title));

        final RatingBar rating = (RatingBar)dialoglayout.findViewById(R.id.ratingBar);
        rating.setNumStars(5);
        rating.setStepSize(1);

        final EditText input = (EditText)dialoglayout.findViewById(R.id.editText);

        builder.setPositiveButton(R.string.dialog_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveRating(rating.getProgress(), input.getText().toString());
                    }
                });
        builder.setNegativeButton(R.string.dialog_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finishRating();
                    }
                });

        builder.show();

//        if(mTextToSpeech.isSpeaking())
//        {
            mTextToSpeech.stop();
//            mTextToSpeech.setPitch((float) 5.0);
            speak("Tot volgende keer!");
//        }

    }

    public void saveRating(int stars, String comment){
        VirtualTherapistClient.getInstance().getVtService().rating(stars, comment, chatId, new Callback<Integer>()
        {
            @Override
            public void success(Integer integer, Response response)
            {
                finishRating();
            }

            @Override
            public void failure(RetrofitError error)
            {
                new ApiErrorHandler(getApplicationContext(), error);
            }
        });
    }

    public void finishRating(){
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //setContentView(new GIFView(this));
        showContextDialog();

        gifView = new AnimatedGifImageView(this);
        gifView.setImageResource(R.drawable.vt_talking);

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_chat);
        relativeLayout.addView(gifView, 1);

        listView            = (ListView) findViewById(R.id.listView1);
//        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
//        p.addRule(RelativeLayout.BELOW, R.id.img_therapist);
//        listView.setLayoutParams(p);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }

    private void showContextDialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.mood_dialog_title)
                .setItems(MOODS, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which == MOODS.length-1)
                        {
                            showOtherDialog();
                        }
                        else
                        {
                            setMoodAndLocation(MOODS[which]);
                        }

                    }
                });
        builder.create().show();
    }

    private void showOtherDialog()
    {
        final EditText text = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.other_mood_dialog_title)
                .setView(text)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Editable input = text.getText();
                        setMoodAndLocation(input.toString());
                    }
                }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            // Do nothing.
                        }
                    }).show();
    }

    private void setMoodAndLocation(String mood)
    {
        chatContext = new ChatContext();
        chatContext.setMood(mood);
        Location location = LocationUtil.getInstance(getApplicationContext()).getLastLocation();
        if(location != null)
        {
            chatContext.setLat(location.getLatitude());
            chatContext.setLng(location.getLongitude());
        }
        System.out.println(chatContext.toString());
        sendContextToServerAndStartChat(chatContext);
    }

    private void sendContextToServerAndStartChat(ChatContext context)
    {
        VirtualTherapistClient.getInstance().getVtService().context(context.getMood(), context.getLat(), context.getLng(), new Callback<Integer>()
        {
            @Override
            public void success(Integer integer, Response response)
            {
                chatId = integer;
                initChat();
            }

            @Override
            public void failure(RetrofitError error)
            {
                new ApiErrorHandler(getApplicationContext(), error);
            }
        });
    }

    private void initChat()
    {
        session = new SessionManager(getApplicationContext());
        // check text to speech available
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_DATA_CHECK);

        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null)
        {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        socket              = Socket.getInstance();
        socket.connect();

        buttonSend          = (Button) findViewById(R.id.buttonSend);
        chatText            = (EditText) findViewById(R.id.chatText);

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

        // to scroll the list view to bottom on data change
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

        // show question into listview
        this.sendChatMessage(question);

        // send question to the server and get the reply
        sendQuestion(question);

        // show answer into listview
//        this.sendChatMessage(answer);

        return true;
    }

    private void sendQuestion(String question)
    {
        try
        {
            socket.sendQuestion(question, chatId, session.getEmail());

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
                                // speak answer
                                // mTextToSpeech.speak(answerMessage, TextToSpeech.QUEUE_FLUSH, null);
                                speak(answerMessage);

                                // add answer to chat list
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
                /*
                int index = 0;
                for(Locale locale1 : Locale.getAvailableLocales()) {
                    System.out.println(locale1.getDisplayName() + " " + index);
                    index ++;
                }*/
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
            if(text.equals("Ik hou van chocolade!")) {
                mTextToSpeech.setPitch(20);
                mTextToSpeech.setSpeechRate(1.2f);
                progressListener.dontResetImage();
                gifView.setAnimatedGif(R.drawable.spongebob, AnimatedGifImageView.TYPE.FIT_CENTER);
            }
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
        }
    }

    // Fired after TTS initialization
    public void onInit(int status)
    {
        if(status == TextToSpeech.SUCCESS)
        {
            //mTextToSpeech.setOnUtteranceCompletedListener(this);
            progressListener = new TTSUtteranceProgressListener(this, gifView, mTextToSpeech);
            mTextToSpeech.setOnUtteranceProgressListener(progressListener);

            mTextToSpeech.setSpeechRate((float) 0.8);
            mTextToSpeech.setPitch((float) 1.3);

            // first message
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