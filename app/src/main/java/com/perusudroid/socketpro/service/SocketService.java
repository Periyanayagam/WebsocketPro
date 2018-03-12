package com.perusudroid.socketpro.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.perusudroid.socketpro.Constants;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Perusudroid on 3/10/2018.
 */

public class SocketService extends Service {


    private static final String TAG = SocketService.class.getSimpleName();
    private WebSocketClient mWebSocketClient;
    private boolean isConnected = false;
    private final IBinder mBinder = new LocalBinder();


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "broadcastReceiver");
            if (intent != null) {
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(Constants.broadcasts.DO_REFRESH)) {
                        if (intent.getExtras() != null) {
                            if (intent.getExtras().getBoolean(Constants.bundleKeys.REFERSH_DATA)) {
                                doConnectWebSocket();
                            }
                        }
                    } else if (intent.getAction().equals(Constants.broadcasts.SOCKET)) {
                        if (intent.getExtras() != null) {
                            Bundle extras = intent.getExtras();
                            if (extras.getString(Constants.bundleKeys.SOCKET_DATA) != null) {
                                doSendData(extras.getString(Constants.bundleKeys.SOCKET_DATA));
                            }
                        }
                    }
                }

            }
        }
    };

    private void doSendData(final String msg) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        getWebSocketClient().send(msg);
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

    public class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
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
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "onMessage: " + message);
                Intent intent = new Intent();
                intent.setAction(Constants.broadcasts.SOCKET_MSG_RECEIVED);
                intent.putExtra(Constants.bundleKeys.SOCKET_DATA, message);
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

}
