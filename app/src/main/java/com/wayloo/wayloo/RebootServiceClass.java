package com.wayloo.wayloo;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class RebootServiceClass extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RebootServiceClass(String name) {
        super(name);
        startForeground(1, new Notification());
    }

    public RebootServiceClass() {
        super("RebootServiceClass");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("En reinicio","reinicio);");
        String intentType = intent.getExtras().getString("caller");
        if (intentType == null) return;
        if (intentType.equals("RebootReceiver")) {
            SharedPreferences settings = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
            Log.e("En reinicio", "alarm id "+ settings.getInt("alarmID", 0)+ "  " + settings.getInt("alarmTime", 0));
            Utils.setAlarm(settings.getInt("alarmID", 0), settings.getLong("alarmTime", 0), this);
        }
    }
}