package com.perusudroid.socketpro.temp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.perusudroid.socketpro.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class NewActivity extends AppCompatActivity {

    private static final String TAG = NewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void handleRealTimeMessage(RealTimeEvent event) {
        Log.d(TAG, "handleRealTimeMessage: "+ event.getMsg());
        // processing of all real-time events
    }
}
