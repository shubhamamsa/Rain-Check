package com.example.rain_check;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LauncherActivity extends AppCompatActivity {

    private static final long TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().setStatusBarColor(Color.parseColor("#aa5bb0f3"));
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(Color.parseColor("#aa5bb0f3"));

        ImageView launcherIcon = findViewById(R.id.launcher_icon);

        Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_icon);

        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_icon);

        int []imageArray={R.drawable.ic_cloudy_moon, R.drawable.ic_cloud,R.drawable.ic_ice};


        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i=0;
            public void run() {
                animFadeOut.reset();
                launcherIcon.clearAnimation();
                launcherIcon.startAnimation(animFadeOut);

                launcherIcon.setImageResource(imageArray[i]);

                animFadeIn.reset();
                launcherIcon.clearAnimation();
                launcherIcon.startAnimation(animFadeIn);
                i++;
                if(i>imageArray.length-1)
                {
                    return;
                }
                handler.postDelayed(this, 500);  //for interval...
            }
        };
        handler.postDelayed(runnable, 700); //for initial delay..

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent selectCity = new Intent(LauncherActivity.this, SelectCity.class);
                startActivity(selectCity);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }, TIME_OUT);
    }
}