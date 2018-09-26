package com.onebot.myapplication;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

/**
 * full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    private String url;
    private ImageView fullScreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(com.onebot.myapplication.R.layout.activity_fullscreen);

        url = getIntent().getExtras().getString("URL");

        fullScreenImageView = (ImageView) findViewById(com.onebot.myapplication.R.id.fullscreen_imageview);
        Intent callingActivityIntent = getIntent();

        if (callingActivityIntent != null) {
            if (url != null && fullScreenImageView != null) {
                GlideApp.with(getApplicationContext())
                        .load(url)
                        .into(fullScreenImageView);
            }
        }

        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        Log.d("tag", "config changed");
        super.onConfigurationChanged(newConfig);

        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            Log.d("tag", "Portrait");
        else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("tag", "Landscape");
        }
        else
            Log.w("tag", "other: " + orientation);

    }
}

