package com.wayloo.wayloo;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;

import java.util.Random;


/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private static int NOTIFICATION_ID = 1;
    Context context = this;
    Notification notification;
    UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(context, "dbUsuarios", null, 1);
    String datoRecibidoExtra= null;


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        SharedPreferences tokensNotificacion = PreferenceManager.getDefaultSharedPreferences(context);
        Log.e("Token FCM: " , s);
        SharedPreferences.Editor tokensNotificacionEditable = tokensNotificacion.edit();
        tokensNotificacionEditable.putString("token", s);
        tokensNotificacionEditable.commit();

        cargarToken(s);
    }

    private void cargarToken(String s) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("token");
        ref.child(traerId_firebaseQLITE()).child(s).setValue(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("Notificaicon ", "Se ha recibido una notificacino");
        Log.e("Mensaje ", "msj" + remoteMessage.getNotification().getTitle());
        Log.e("Mensaje ", "msj" + remoteMessage.getData());

        constructorNotificacion(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

        if(remoteMessage.getData().size() > 0){
            Log.e("Mensaje recibido de ", remoteMessage.getData().get("titulo"));
            Log.e("Mensaje recibido de ", remoteMessage.getData().get("body"));
            //Log.e("Mensaje recibido de ", remoteMessage.getData().get("color"));
            datoRecibidoExtra  = remoteMessage.getData().get("extra");
            Log.e("Dato recibido extra ",datoRecibidoExtra);
        }


    }

    private void constructorNotificacion(String titulo, String detalle){
      /*  String id = "mensaje";

        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(id, "nuevo",NotificationManager.IMPORTANCE_HIGH);
            nc.setShowBadge(true);
            nm.createNotificationChannel(nc);
        }
        builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(titulo)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(detalle)
                .setContentInfo("nuevo")
                .setContentIntent(clickNoti());

        Random random = new Random();
        int idNotify = 0;
        idNotify= random.nextInt(8000);
        assert nm != null;

        nm.notify(idNotify,builder.build());


    }

    public PendingIntent clickNoti(){
        Intent nf = new Intent( getApplicationContext(),  MainActivityPrincipal.class);
        nf.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK  | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return  PendingIntent.getActivity(this,0,nf,0);

    }*/

        String NOTIFICATION_CHANNEL_ID = getApplicationContext().getString(R.string.app_name);
        Context context = this.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mIntent = new Intent(this, MainActivity.class);
        Resources res = this.getResources();
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Log.e("Construyendo de MFCM",datoRecibidoExtra );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final int NOTIFY_ID = 0; // ID of notification
            String id = NOTIFICATION_CHANNEL_ID; // default_channel_id
            String title = NOTIFICATION_CHANNEL_ID; // Default Channel
            PendingIntent pendingIntent;
            NotificationCompat.Builder builder;
            NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifManager == null) {
                notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle(getString(R.string.app_name)).setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.drawable.ic_logo)   // required
                    .setContentText(detalle)
                    .setContentTitle(title)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_logor))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(detalle))
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            Notification notification = builder.build();
            notifManager.notify(NOTIFY_ID, notification);

            startForeground(1, notification);

        } else {
            pendingIntent = PendingIntent.getActivity(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(detalle))
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_logor))
                    .setSound(soundUri)

                    .setAutoCancel(true)
                    .setContentTitle(getString(R.string.app_name)).setCategory(Notification.CATEGORY_SERVICE)
                    .setContentTitle(titulo).setContentText(detalle).build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private String traerId_firebaseQLITE(){
        String name= "UnKnow";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT id_firebase FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                name= c.getString(0);
                Log.e("id en sqlite",name);
            } while(c.moveToNext());
        }
        return name;
    }

}
