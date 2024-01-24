package com.guisei.bluearchiveuncensored.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.io.InputStream;

/**
 * https://stackoverflow.com/questions/6533942/adding-gif-image-in-an-imageview-in-android
 */
public class GifView extends View {

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long mMovieStart;

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Context context, int drawableId) {
        setFocusable(true);
        gifInputStream = context.getResources().openRawResource(drawableId);

        gifMovie = Movie.decodeStream(gifInputStream);
        movieWidth = gifMovie.width();
        movieHeight = gifMovie.height();
        movieDuration = gifMovie.duration();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,
                             int heightMeasureSpec) {
        setMeasuredDimension(movieWidth, movieHeight);
    }

    public int getMovieWidth() {
        return movieWidth;
    }

    public int getMovieHeight() {
        return movieHeight;
    }

    public long getMovieDuration() {
        return movieDuration;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {   // first time
            mMovieStart = now;
        }

        if (gifMovie != null) {

            int dur = gifMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }

            int relTime = (int) ((now - mMovieStart) % dur);

            gifMovie.setTime(relTime);

            gifMovie.draw(canvas, 0, 0);
            invalidate();

        }

    }

}