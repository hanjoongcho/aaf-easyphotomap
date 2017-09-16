package me.blog.korn123.easyphotomap.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import me.blog.korn123.easyphotomap.R;

/**
 * Created by CHO HANJOONG on 2016-12-31.
 */

public class IntroActivity extends Activity implements Handler.Callback {

    private final int START_MAIN_ACTIVITY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        new Handler(this).sendEmptyMessageDelayed(START_MAIN_ACTIVITY, 1000);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case START_MAIN_ACTIVITY:
                startActivity(new Intent(this, MapsActivity.class));
                finish();
                break;
            default:
                break;
        }
        return false;
    }

}
