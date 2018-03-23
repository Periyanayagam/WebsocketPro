package com.perusudroid.socketpro.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.perusudroid.socketpro.Constants;

import static android.content.ContentValues.TAG;

/**
 * Created by Perusudroid on 3/11/2018.
 */

public class NetworkReceiver extends BroadcastReceiver {

    private static String TAG = NetworkReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null) {

            Log.d(TAG, "onReceive: isDefault " + intent.getExtras().getBoolean("isDefault"));

            if(intent.getExtras().getBoolean("isDefault")){
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                //if there is a network
                if (activeNetwork != null) {
                    //if connected to wifi or mobile data plan
                    boolean isConnected = (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);

                    if (isConnected) {
                        Log.d(TAG, "onReceive: connected");
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(Constants.broadcasts.DO_REFRESH);
                        broadcastIntent.putExtra(Constants.bundleKeys.REFERSH_DATA, true);
                        context.sendBroadcast(broadcastIntent);
                        Log.d(TAG, "onReceive: bc send");
                    } else {
                        Log.d(TAG, "onReceive: disconnected");
                    }
                }
            }
        }
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (NetworkInfo networkInfo : info) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
