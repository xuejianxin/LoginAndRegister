package com.example.administrator.loginandregister.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.administrator.loginandregister.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by JimCharles on 2016/12/12.
 */

public class WelcomActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcom);

        final Intent it = new Intent(this, UserActivity.class); //你要转向的Activity
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(it); //执行
            }
        };
        timer.schedule(task, 1000*2); //2秒后
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            case R.id.iv_cancel:
                finish();
                break;
        }
    }
}
