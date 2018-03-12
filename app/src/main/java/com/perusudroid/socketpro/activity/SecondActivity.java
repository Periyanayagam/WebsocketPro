package com.perusudroid.socketpro.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.perusudroid.socketpro.AppController;
import com.perusudroid.socketpro.Constants;
import com.perusudroid.socketpro.R;
import com.perusudroid.socketpro.adapter.MessagesAdapter;
import com.perusudroid.socketpro.db.Messages;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SecondActivity.class.getSimpleName();
    private ListView listView;
    private Button okBtn;
    private EditText editTxt;
    private MessagesAdapter adapter;
    private List<Messages> messagesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        bindViews();
        setAssets();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.broadcasts.SOCKET_MSG_RECEIVED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void bindViews() {
        listView = findViewById(R.id.listview);
        editTxt = findViewById(R.id.edittxt);
        okBtn = findViewById(R.id.ok_btn);
    }

    private void setAssets() {
        okBtn.setOnClickListener(this);
        messagesList = ((AppController) getApplication()).getDaoSession().getMessagesDao().loadAll();
        adapter = new MessagesAdapter(this, messagesList);
        listView.setAdapter(adapter);;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok_btn:
                if (validated()) {
                    doSomeCommonstuffs(editTxt.getText().toString().trim(),2);
                    Intent intent = new Intent();
                    intent.setAction(Constants.broadcasts.SOCKET);
                    intent.putExtra(Constants.bundleKeys.SOCKET_DATA, editTxt.getText().toString().trim());
                    sendBroadcast(intent);
                    Log.d(TAG, "onClick: broadcast send");
                    editTxt.setText("");
                }
                break;
        }
    }

    private void doSomeCommonstuffs(String msg, int who) {
        ((AppController) getApplication()).getDaoSession().getMessagesDao().insert(new Messages(null, who,msg));
        if (messagesList != null) {
            messagesList.clear();
        }
        messagesList.addAll(((AppController) getApplication()).getDaoSession().getMessagesDao().loadAll());
        adapter.notifyDataSetChanged();
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "broadcastReceiver");
            if (intent != null) {
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(Constants.broadcasts.SOCKET_MSG_RECEIVED)) {
                        if (intent.getExtras() != null) {
                            Bundle extras = intent.getExtras();
                            if (extras.getString(Constants.bundleKeys.SOCKET_DATA) != null) {
                                doSomeCommonstuffs(extras.getString(Constants.bundleKeys.SOCKET_DATA), 1);
                            }
                        }
                    }
                }

            }
        }
    };

    private boolean validated() {
        if (!(editTxt.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, "Enter something to send", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
