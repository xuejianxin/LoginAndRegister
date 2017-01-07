package com.example.administrator.loginandregister.splash;

/**
 * Created by JimCharles on 2016/12/22.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.administrator.loginandregister.R;
import com.example.administrator.loginandregister.activity.LoginActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private WowSplashView mWowSplashView;
    private WowView mWowView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashs);
        initsView();

        final Intent it = new Intent(SplashActivity.this, LoginActivity.class);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(it); //执行
            }
        };
        timer.schedule(task, 1000*6); //6秒后
    }

    private void initsView(){
        mWowSplashView = (WowSplashView) findViewById(R.id.wowSplash);
        mWowView = (WowView) findViewById(R.id.wowView);

        mWowSplashView.startAnimate();

        mWowSplashView.setOnEndListener(new WowSplashView.OnEndListener() {
            @Override public void onEnd(WowSplashView wowSplashView) {
                mWowSplashView.setVisibility(View.GONE);
                mWowView.setVisibility(View.VISIBLE);
                mWowView.startAnimate(wowSplashView.getDrawingCache());

            }
        });
    }

}
