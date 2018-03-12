package com.perusudroid.socketpro;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.perusudroid.socketpro.db.DaoMaster;
import com.perusudroid.socketpro.db.DaoSession;
import com.perusudroid.socketpro.db.DbOpenHelper;
import com.perusudroid.socketpro.service.SocketService;

/**
 * Created by Perusudroid on 3/12/2018.
 */

public class AppController extends Application {

    private static String TAG = AppController.class.getSimpleName();
    private DaoSession mDaoSession;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mDaoSession = new DaoMaster(
                new DbOpenHelper(this, "socket_data.db").getWritableDb()).newSession();
        if(!isMyServiceRunning(SocketService.class)){
            Intent i = new Intent(this, SocketService.class);
            startService(i);
        }
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
