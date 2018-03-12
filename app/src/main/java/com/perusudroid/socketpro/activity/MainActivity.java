package com.perusudroid.socketpro.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.perusudroid.socketpro.R;
import com.perusudroid.socketpro.service.SocketService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnSend;
    private EditText etTxt;
    private LocalService socketService;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setAssets();
        Intent i = new Intent(this, SocketService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(i);
    }

    private void bindViews() {
        etTxt = findViewById(R.id.etTxt);
        btnSend = findViewById(R.id.btnSend);
    }

    private void setAssets() {
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSend:
                if (validated()) {
                    if (isBound) {
                        socketService.sendMessage(etTxt.getText().toString().trim());
                    }
                }

                break;
        }
    }

    private boolean validated() {
        if (!(etTxt.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, "Enter something to send", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: ");
            LocalService.LocalBinder localBinder = (LocalService.LocalBinder) iBinder;
            socketService = localBinder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
            isBound = false;
        }
    };


}
