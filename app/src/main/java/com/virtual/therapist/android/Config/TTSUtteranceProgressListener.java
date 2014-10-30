package com.virtual.therapist.android.Config;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.UtteranceProgressListener;

import com.virtual.therapist.android.R;

import java.util.Random;

/**
 * Created by bas on 30-10-14.
 */
public class TTSUtteranceProgressListener extends UtteranceProgressListener{

    private Context context;
    private AnimatedGifImageView gifView;
    private Handler mainHandler;

    public TTSUtteranceProgressListener(Context context, AnimatedGifImageView gifView) {
        this.context = context;
        this.gifView = gifView;
        mainHandler = new Handler(context.getMainLooper());
    }


    @Override
    public void onStart(String s) {
        int gifId;
        Random random = new Random();
        int randomInt = random.nextInt(2);
        switch (randomInt) {
            case 0: gifId = R.drawable.vt_talking;
                break;
            case 1: gifId = R.drawable.vt_talking1;
                break;
            case 2: gifId = R.drawable.vt_talking2;
                break;
            default: gifId = R.drawable.vt_talking;
                break;
        }
        final int finalInt = gifId;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                gifView.setAnimatedGif(finalInt, AnimatedGifImageView.TYPE.FIT_CENTER);
            }
        });

    }

    @Override
    public void onDone(String s) {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                gifView.setImageResource(R.drawable.vt_talking);
            }
        });
    }

    @Override
    public void onError(String s) {

    }
}
