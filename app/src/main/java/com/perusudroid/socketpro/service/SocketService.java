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
    private boolean isConnected = false;
    private ArrayList<Messages> offlineMsgList = new ArrayList<>();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            if (message != null) {
                if (message.arg1 == 1) {
                    if (offlineMsgList.size() > 0) {
                        doSendList(offlineMsgList);
                    }
                }
            }
            return true;
        }
    });


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "broadcastReceiver");
            if (intent != null) {
                if (intent.getAction() != null) {
                    Log.d(TAG, "onReceive: " + intent.getAction());
                    if (intent.getAction().equals(Constants.broadcasts.DO_REFRESH)) {
                        if (intent.getExtras() != null) {
                            if (intent.getExtras().getBoolean(Constants.bundleKeys.REFERSH_DATA)) {
                                Log.d(TAG, "onReceive: refreshing socket");
                                doConnectWebSocket();
                            }
                        }
                    } else if (intent.getAction().equals(Constants.broadcasts.SOCKET)) {
                        if (intent.getExtras() != null) {
                            Bundle extras = intent.getExtras();
                            if (extras.get(Constants.bundleKeys.SOCKET_DATA_OBJECT) != null) {

                                Messages msgCrap = (Messages) extras.get(Constants.bundleKeys.SOCKET_DATA_OBJECT);

                                doSendData(msgCrap.getMsg(),
                                        msgCrap.getId());
                            }
                            if (extras.getParcelableArrayList(Constants.bundleKeys.SOCKET_DATA_LIST) != null) {
                                offlineMsgList.clear();
                                offlineMsgList.addAll(extras.getParcelableArrayList(Constants.bundleKeys.SOCKET_DATA_LIST));
                                for (int i = 0; i < offlineMsgList.size(); i++) {
                                    Log.d(TAG, "onReceive: added list"+ offlineMsgList.get(i).getMsg());
                                }
                            } else {
                                Log.e(TAG, "onReceive: doSendList empty");
                            }
                        }
                    }
                }

            }
        }
    };

    public boolean isConnected() {
        return isConnected;
    }


    private void doSendList(final ArrayList<Messages> msg) {

        Log.d(TAG, "doSendList: " + msg.size());

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected) {
                            for (int i = 0; i < msg.size(); i++) {

                                Log.d(TAG, "run: isOpen " + mWebSocketClient.isOpen() + " null " + (mWebSocketClient == null));

                                if (mWebSocketClient.isOpen()) {
                                    mWebSocketClient.send(msg.get(i).getMsg());
                                    offlineMsgList.get(i).setSendStatus(Constants.common.MSG_SEND);
                                   /* Log.d(TAG, "run: removed item "+ msg.get(i).getMsg());
                                    offlineMsgList.remove(i);*/
                                }
                            }

                            Log.d(TAG, "run: "+ offlineMsgList.size());

                            for (int i = 0; i < offlineMsgList.size(); i++) {
                                Log.d("NEWDATA", "service: "+ offlineMsgList.get(i).getMsg() + " status "+ offlineMsgList.get(i).getSendStatus());
                            }

                            Intent x = new Intent();
                            x.setAction(Constants.broadcasts.MSG_SEND_REFRESH);
                            x.putExtra(Constants.bundleKeys.REFERSH_DATA, true);
                            x.putParcelableArrayListExtra(Constants.bundleKeys.UPDATED_OFFLINE_MSG_LIST, offlineMsgList);
                            sendBroadcast(x);
                            offlineMsgList.clear();
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
                        if (isConnected) {
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
                }
        ).start();
    }


    private WebSocketClient getWebSocketClient() {
        if (mWebSocketClient == null || (!mWebSocketClient.isOpen())) {
            doConnectWebSocket();
        }
        return mWebSocketClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.broadcasts.DO_REFRESH);
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
                doConnectWebSocket();
            }
        } else {
            doConnectWebSocket();
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

    private void doConnectWebSocket() {
        Log.d(TAG, "doConnectWebSocket: ");
        URI uri = null;
        try {
            uri = new URI("ws://echo.websocket.org"); //global url for checking
        } catch (URISyntaxException e) {
            Log.e(TAG, "doConnectWebSocket: " + e.getMessage());
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                isConnected = true;
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
                isConnected = false;
                Log.d(TAG, "onClose: code " + code + " reason " + reason);
            }

            @Override
            public void onError(Exception ex) {
                isConnected = false;
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
