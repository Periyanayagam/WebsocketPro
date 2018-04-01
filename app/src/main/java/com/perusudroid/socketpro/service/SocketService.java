package com.perusudroid.socketpro.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.perusudroid.socketpro.Constants;
import com.perusudroid.socketpro.db.Messages;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Perusudroid on 3/10/2018.
 */

public class SocketService extends Service {


    private static final String TAG = SocketService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    private WebSocketClient mWebSocketClient;
    private ArrayList<Messages> offlineMsgList = new ArrayList<>();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            if (message != null) {
                Log.d(TAG, "handleMessage: " + message.arg1);
                if (message.arg1 == 1) {
                    if (offlineMsgList.size() > 0) {
                        doSendOfflineList(offlineMsgList);
                    }
                }
            }
            return true;
        }
    });


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent != null) {
                if (intent.getAction() != null) {

                    Log.d(TAG, "onReceive: action " + intent.getAction());

                    if (intent.getAction().equals(Constants.broadcasts.SOCKET)) {
                        if (intent.getExtras() != null) {
                            Bundle extras = intent.getExtras();
                            if (extras.get(Constants.bundleKeys.SOCKET_DATA_OBJECT) != null) {

                                Messages msgCrap = (Messages) extras.get(Constants.bundleKeys.SOCKET_DATA_OBJECT);

                                doSendData(msgCrap.getMsg(),
                                        msgCrap.getId());
                            }
                            if (extras.getParcelableArrayList(Constants.bundleKeys.SOCKET_DATA_LIST) != null) {
                                doConnectWebSocket(3);
                                offlineMsgList.clear();
                                offlineMsgList.addAll(extras.getParcelableArrayList(Constants.bundleKeys.SOCKET_DATA_LIST));
                                Log.d(TAG, "onReceive: list addded to offline list");
                            }
                        }
                    }
                }

            }
        }
    };


    private void doSendOfflineList(final ArrayList<Messages> msg) {

        Log.d(TAG, "doSendOfflineList: " + msg.size());

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mWebSocketClient.isOpen()) {

                            for (int i = 0; i < msg.size(); i++) {
                                mWebSocketClient.send(msg.get(i).getMsg());
                                offlineMsgList.get(i).setSendStatus(Constants.common.MSG_SEND);
                                offlineMsgList.get(i).setOffline(Constants.common.SYNCED);
                                Log.d(TAG, "run: send offline msg list " + msg.get(i).getMsg());
                            }

                            Intent x = new Intent();
                            x.setAction(Constants.broadcasts.MSG_SEND_REFRESH);
                            x.putExtra(Constants.bundleKeys.REFERSH_DATA, true);
                            x.putParcelableArrayListExtra(Constants.bundleKeys.UPDATED_OFFLINE_MSG_LIST, offlineMsgList);
                            sendBroadcast(x);
                            offlineMsgList.clear();
                        } else {
                            Log.e(TAG, "run: websocket is not open");
                        }
                    }
                }
        ).start();
    }

    private void doSendData(final String msg, long id) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mWebSocketClient != null && mWebSocketClient.isOpen()) {
                            mWebSocketClient.send(msg);
                            Intent i = new Intent();
                            i.setAction(Constants.broadcasts.MSG_SEND_REFRESH);
                            i.putExtra(Constants.bundleKeys.REFERSH_DATA, true);
                            i.putExtra(Constants.bundleKeys.SOCKET_DATA_INTEGER, id);
                            sendBroadcast(i);
                            Log.d(TAG, "run: id " + id);
                        } else {
                            offlineMsgList.add(new Messages());
                        }
                    }
                }
        ).start();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.broadcasts.SOCKET);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (mWebSocketClient != null) {
            if (!mWebSocketClient.isOpen()) {
                doConnectWebSocket(2);
            }
        } else {
            doConnectWebSocket(2);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void sendMessage(String message) {
        if (mWebSocketClient != null) {
            if (mWebSocketClient.isOpen()) {
                mWebSocketClient.send(message);
            } else {
                Log.e(TAG, "sendMessage: connection is not open");
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void doConnectWebSocket(int who) {

        Log.d(TAG, "doConnectWebSocket: " + who);
        URI uri = null;
        try {
            uri = new URI("ws://echo.websocket.org"); //global url for checking
        } catch (URISyntaxException e) {
            Log.e(TAG, "doConnectWebSocket: " + e.getMessage());
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {

                Log.d(TAG, "onOpen: ");
                Messenger messenger = new Messenger(mHandler);
                Message msg = Message.obtain();
                msg.arg1 = 1;
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "onMessage: " + message);
                Intent intent = new Intent();
                intent.setAction(Constants.broadcasts.SOCKET_MSG_RECEIVED);
                intent.putExtra(Constants.bundleKeys.SOCKET_DATA_STRING, message);
                sendBroadcast(intent);

            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "onClose: code " + code + " reason " + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "onError: " + ex.getLocalizedMessage());
            }
        };
        mWebSocketClient.setConnectionLostTimeout(30);
        mWebSocketClient.connect();
    }

    public class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

}
