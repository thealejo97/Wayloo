package com.wayloo.wayloo;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class MyRebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("En reinicio","reinicio onrecive");
        Intent serviceIntent = new Intent(context, RebootServiceClass.class);
        serviceIntent.putExtra("caller", "RebootReceiver");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}