package com.virtual.therapist.android.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.view.View;

import com.virtual.therapist.android.R;

import java.io.InputStream;

/**
 */
public class GIFView extends View{

    Movie movie, movie1;
    InputStream is = null, is1 = null;
    long moviestart;
    public GIFView(Context context) {
        super(context);
        is = context.getResources().openRawResource(R.drawable.vt_talking);
        movie = Movie.decodeStream(is);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        long movieStart = 0;
        long now = android.os.SystemClock.uptimeMillis();
        if (movieStart == 0) {
            movieStart = now;
        }
        if (movie != null) {
            int relTime = (int) ((now - movieStart) % movie.duration());
            movie.setTime(relTime);
            movie.draw(canvas, getWidth() - movie.width(), getHeight() - movie.height());
            this.invalidate();
        }
    }
}
