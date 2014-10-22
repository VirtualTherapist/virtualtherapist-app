package com.virtual.therapist.android.Activities;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.virtual.therapist.android.Config.ChatArrayAdapter;
import com.virtual.therapist.android.Objects.ChatMessage;
import com.example.jeroenlammerts.virtualtherapist.R;

public class ChatBubbleActivity extends Activity
{
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;

    private Intent intent;
    private boolean side = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.activity_chat);

        buttonSend          = (Button) findViewById(R.id.buttonSend);
        chatText            = (EditText) findViewById(R.id.chatText);

        listView            = (ListView) findViewById(R.id.listView1);
        chatArrayAdapter    = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        listView.setAdapter(chatArrayAdapter);

        chatText.setOnKeyListener(new OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
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

        this.sendChatMessage("Hallo "+ i.getStringExtra("name") +", waar kan ik je mee helpen?");
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
        this.sendChatMessage(question);
        this.sendChatMessage("Ik begrijp u nog niet helaas...");
        return true;
    }

}