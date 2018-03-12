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
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            if (intent.getBooleanExtra("isDefault", false)) {
                Log.d("SocketService", "onReceive: ");
                if (isNetworkAvailable(context)) {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Constants.broadcasts.DO_REFRESH);
                    broadcastIntent.putExtra(Constants.bundleKeys.REFERSH_DATA, true);
                    context.sendBroadcast(broadcastIntent);
                    Log.d("SocketService", "onReceive: bc send");
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
