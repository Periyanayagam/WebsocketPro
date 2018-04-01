package com.perusudroid.socketpro.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.perusudroid.socketpro.AppController;
import com.perusudroid.socketpro.Constants;
import com.perusudroid.socketpro.R;
import com.perusudroid.socketpro.RecyclerItemClickListener;
import com.perusudroid.socketpro.adapter.CustomAdapter;
import com.perusudroid.socketpro.adapter.IListener;
import com.perusudroid.socketpro.db.Messages;
import com.perusudroid.socketpro.db.MessagesDao;
import com.perusudroid.socketpro.receiver.NetworkReceiver;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener, IListener {

    private static final String TAG = SecondActivity.class.getSimpleName();
    private ListView listView;
    ActionMode mActionMode;
    private Button okBtn;
    private EditText editTxt;
    private CustomAdapter customAdapter;
    private RecyclerView recyclerView;
    private List<Messages> messagesList = new ArrayList<>();
    Menu context_menu;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "broadcastReceiver");
            if (intent != null) {
                if (intent.getAction() != null) {
                    Log.d(TAG, "onReceive: action " + intent.getAction());
                    if (intent.getAction().equals(Constants.broadcasts.DO_REFRESH)) {
                        if (intent.getExtras() != null) {
                            if (intent.getExtras().getBoolean(Constants.bundleKeys.REFERSH_DATA)) {
                                doSendLocalData();
                            }
                        }
                    }
                    if (intent.getAction().equals(Constants.broadcasts.SOCKET_MSG_RECEIVED)) {
                        if (intent.getExtras() != null) {
                            Bundle extras = intent.getExtras();
                            if (extras.getString(Constants.bundleKeys.SOCKET_DATA_STRING) != null) {
                                doSaveData(extras.getString(Constants.bundleKeys.SOCKET_DATA_STRING), Constants.common.RECEIVED);
                            }
                        }
                    }

                    if (intent.getAction().equals(Constants.broadcasts.MSG_SEND_REFRESH)) {
                        if (intent.getExtras() != null) {

                            Bundle ex = intent.getExtras();

                            for (String key :
                                    ex.keySet()) {
                                Log.d(TAG, "onReceive: key " + key + " value " + ex.get(key));
                            }

                            if (intent.getExtras().getBoolean(Constants.bundleKeys.REFERSH_DATA)) {

                                if (intent.getExtras().getParcelableArrayList(Constants.bundleKeys.UPDATED_OFFLINE_MSG_LIST) != null) {

                                    ArrayList<Messages> updateMsg = intent.getExtras().getParcelableArrayList(Constants.bundleKeys.UPDATED_OFFLINE_MSG_LIST);
                                    updateDb(updateMsg);
                                }

                                long msgId = intent.getLongExtra(Constants.bundleKeys.SOCKET_DATA_INTEGER, 0);
                                Log.d(TAG, "onReceive: msgId " + msgId);
                                if (msgId > 0) {
                                    for (int i = 0; i < messagesList.size(); i++) {
                                        if (messagesList.get(i).getId() == msgId) {
                                            messagesList.get(i).setSendStatus(Constants.common.MSG_SEND);
                                            customAdapter.notifyItemChanged(i);
                                            updateDb(messagesList);
                                            Log.d(TAG, "onReceive: notifyItemChanged " + i);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        bindViews();
        setAssets();
        doSendLocalData();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.broadcasts.DO_REFRESH);
        intentFilter.addAction(Constants.broadcasts.SOCKET_MSG_RECEIVED);
        intentFilter.addAction(Constants.broadcasts.MSG_SEND_REFRESH);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void bindViews() {
        editTxt = findViewById(R.id.edittxt);
        okBtn = findViewById(R.id.ok_btn);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setAssets() {

        editTxt.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        recyclerView.smoothScrollToPosition(messagesList.size());
                        return false;
                    }
                }
        );

        editTxt.setOnClickListener(this);
        okBtn.setOnClickListener(this);
        messagesList = ((AppController) getApplication()).getDaoSession().getMessagesDao().loadAll();


        LinearLayoutManager manager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(manager);
        customAdapter = new CustomAdapter(messagesList, this);
        recyclerView.setAdapter(customAdapter);
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect)
                    multi_select(position);
                //else
                //Toast.makeText(getApplicationContext(), "Details Page", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    multiselect_list = new ArrayList<Messages>();
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }

                multi_select(position);

            }
        }));
    }

    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(messagesList.get(position)))
                multiselect_list.remove(messagesList.get(position));
            else
                multiselect_list.add(messagesList.get(position));

            if (multiselect_list.size() > 0)
                mActionMode.setTitle("" + multiselect_list.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear:
                ((AppController) getApplication()).getDaoSession().getMessagesDao().deleteAll();
                List<Messages> msg = getOfflineMessages();
                customAdapter.refresh(msg);
                break;
        }
        return true;
    }

    private boolean isMultiSelect = false;
    private ArrayList<Messages> multiselect_list;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    if (multiselect_list.size() > 0) {

                        List<Messages> listToDelete = new ArrayList<>();
                        for (int i = 0; i < multiselect_list.size(); i++) {
                            listToDelete.add(multiselect_list.get(i));
                            messagesList.remove(multiselect_list.get(i));
                        }

                        ((AppController) getApplication()).getDaoSession().getMessagesDao().deleteInTx(listToDelete);
                        customAdapter.refresh(messagesList);

                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                    }
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list = new ArrayList<Messages>();
            refreshAdapter();
        }
    };

    private void refreshAdapter() {
        customAdapter.setSelectedList(multiselect_list);
        customAdapter.refresh(messagesList);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.edittxt:

                break;

            case R.id.ok_btn:
                if (validated()) {
                    doSaveData(editTxt.getText().toString().trim(), Constants.common.SEND);
                }
                break;
        }
    }


    private void doSaveData(String msg, int who) {

        Log.d(TAG, "doSaveData: msg " + msg + " who " + who);

        switch (who) {

            case Constants.common.SEND:
                if (NetworkReceiver.isNetworkAvailable(this)) {
                    Messages msgCrap = new Messages(null, who, msg, Constants.common.SYNCED, Constants.common.MSG_SENDING);
                    long id = ((AppController) getApplication()).getDaoSession().getMessagesDao().insert(msgCrap);
                    msgCrap.setId(id);
                    Intent intent = new Intent();
                    intent.setAction(Constants.broadcasts.SOCKET);
                    intent.putExtra(Constants.bundleKeys.SOCKET_DATA_OBJECT, msgCrap);
                    sendBroadcast(intent);
                    Log.d(TAG, "onClick: broadcast send");
                } else {
                    ((AppController) getApplication()).getDaoSession().getMessagesDao().insert(new Messages(null, who, msg, Constants.common.NOT_SYNCED, Constants.common.MSG_SENDING));
                }
                editTxt.setText("");
                break;

            case Constants.common.RECEIVED:
                ((AppController) getApplication()).getDaoSession().getMessagesDao().insert(new Messages(null, who, msg, Constants.common.SYNCED, Constants.common.MSG_RECEIVED));
                recyclerView.scrollToPosition(messagesList.size());
                break;
        }


        if (messagesList != null) {
            messagesList.clear();
        }
        messagesList.addAll(((AppController) getApplication()).getDaoSession().getMessagesDao().loadAll());
        customAdapter.refresh(messagesList);
    }

    private void updateDb(List<Messages> listToUpdate) {
        for (int i = 0; i < listToUpdate.size(); i++) {
            Log.d("NEWDATA", "activity: " + listToUpdate.get(i).getMsg() + " id " + listToUpdate.get(i).getSendStatus());
        }
        ((AppController) getApplication()).getDaoSession().getMessagesDao().updateInTx(listToUpdate);
        customAdapter.refresh(listToUpdate);
    }

    private void doSendLocalData() {

        List<Messages> msg = getOfflineMessages();

        Intent intent = new Intent();
        intent.setAction(Constants.broadcasts.SOCKET);

        ArrayList<Messages> msgListToSend = new ArrayList<>();
        msgListToSend.addAll(msg);

        //  intent.putExtra(Constants.bundleKeys.SOCKET_DATA_STRING, editTxt.getText().toString().trim());
        intent.putParcelableArrayListExtra(Constants.bundleKeys.SOCKET_DATA_LIST, msgListToSend);
        sendBroadcast(intent);

        for (int i = 0; i < msg.size(); i++) {
            Log.d(TAG, "doSendLocalData: " + msg.get(i).getMsg());
        }

    }

    private List<Messages> getOfflineMessages() {
        return ((AppController) getApplication())
                .getDaoSession()
                .getMessagesDao()
                .queryBuilder()
                .where(MessagesDao.Properties.Offline.eq(0))
                .list();
    }


    private boolean validated() {
        if (!(editTxt.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, "Enter something to send", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(Messages messages) {

    }

    @Override
    public void onLongClick(Messages messages, int adapterPosition) {

    }

    @Override
    public Context getContext() {
        return this;
    }
}
