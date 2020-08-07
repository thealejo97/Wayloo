package com.wayloo.wayloo.ui.engine;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class engine {
    ProgressDialog progress;

    public void reiniciarApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent.makeRestartActivityTask(componentName);
        context.startActivity(intent);
        System.exit(0);
    }

    //Trae datos del usuario que inicio sesion en la BDSQLITE
    public String getInternoTelSQLITE(Context context){

        //Creamos la BD
        UsuariosSQLiteHelper usdbh =
                new UsuariosSQLiteHelper(context, "dbUsuarios", null, 1);
        String name= "UnKnow User";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT id_usu FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                name= c.getString(0);
                Log.e("id en sqlite",name);
            } while(c.moveToNext());
        }
        return name;
    }

    public String getInternoRolSQLITE(Context context){
        UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(context, "dbUsuarios", null, 1);
        String rolUsuInterno= "UnKnow User";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT rol FROM CurrentUsuario;", null); // Creamos la consulta
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                rolUsuInterno= c.getString(0);// Obtenemos la respuesta, debe tener una sola fila

            } while(c.moveToNext());
        }
        return rolUsuInterno;
    }

    public String getInternoEmailSQLITE(Context context){
        //Creamos la BD
        UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(context, "dbUsuarios", null, 1);
        String name= "UnKnow User";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT email FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                name= c.getString(0);

            } while(c.moveToNext());
        }
        return name;
    }

    public String getInternoFireSQLITE(Context context) {
        UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(context, "dbUsuarios", null, 1);
        String result = "null";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id_firebase FROM CurrentUsuario", null);
        if (c != null) {
            c.moveToFirst();
            do {
                //Asignamos el valor en nuestras variables para usarlos en lo que necesitemos
                result = c.getString(c.getColumnIndex("id_firebase"));
            } while (c.moveToNext());
        }

        //Cerramos el cursor y la conexion con la base de datos
        c.close();
        db.close();
        return result;
    }

    //Settear ciudades remotas
    public void ciudadesEnSpinner(Activity activity, Spinner spinnerCiudad){
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        //  Se coloca el array de las ciudades dependiendo de las que esten en el FireBaseRemoteConfig
        ArrayList<String> ArraylistCiudadesFRstr = new ArrayList<>();

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String, Object> defaultData = new HashMap<>();
        defaultData.put("ciudadesAc","Cali,Jamundi");
        defaultData.put("datosPruebaPapu","nadaa");
        defaultData.put("playurl","Cali,Jamundi");
        defaultData.put("versionname","1.0");

        Task<Void> fetch = firebaseRemoteConfig.fetch(0);
        FirebaseRemoteConfig finalFirebaseRemoteConfig = firebaseRemoteConfig;
        fetch.addOnSuccessListener(activity, aVoid ->{
            finalFirebaseRemoteConfig.activateFetched();
            Log.e("Datos en fb",  "-"+ finalFirebaseRemoteConfig.getString("ciudadesAc") +"-");
        }).addOnFailureListener(activity, aVoid -> {
            Log.e("Datos en fb",  "Fetch faild" );
            Toast.makeText(activity, "Fetch failed",
                    Toast.LENGTH_SHORT).show();
        });

        String prueba= (String)firebaseRemoteConfig.getString("ciudadesAc");
        Log.e("Prueba", prueba);
        String[] ciudades = null;
        ciudades=prueba.split(",");

        for(int i = 0; i<ciudades.length; i++) {
            Log.e("CiudadArr", ciudades[i]);
            ArraylistCiudadesFRstr.add(ciudades[i]);
            Log.e("Ciudad" + i, ciudades[i]);
        }

        ArrayAdapter ArraylistCiudadesFRFire = new ArrayAdapter(activity, R.layout.color_spinner_layout, ArraylistCiudadesFRstr) ;
        // Apply the adapter to the spinner*/
        ArraylistCiudadesFRFire.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerCiudad.setAdapter(ArraylistCiudadesFRFire);
    }

    //
    public void showProgressDialog(String titulo, String mensaje, Context context) {
        progress = ProgressDialog.show(context, titulo,
                mensaje, true);
        progress.show();
    }

    public void hideProgressDialog() {
        progress.dismiss();
    }

}
