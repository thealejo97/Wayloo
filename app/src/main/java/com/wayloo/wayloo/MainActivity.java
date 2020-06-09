package com.wayloo.wayloo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wayloo.wayloo.R.id;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.micronograma.MiCronogramaFragment;
import com.wayloo.wayloo.ui.misReservas.MisReservasFragment;
import com.wayloo.wayloo.ui.mispeluquerias.MisPeluqueriasFragment;
import com.wayloo.wayloo.ui.home.HomeFragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "Notification";
    private String fragmentActual = null;
    private AppBarConfiguration mAppBarConfiguration;
    private RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    private ImageView imageViewLogomini;
    NavigationView navigationView =null;
    TextView politicaP;
    private FirebaseAuth mAuth;
    ProgressDialog progress;
    String version;
    FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    private String mCustomToken;
    private TokenBroadcastReceiver mTokenReceiver;

    //Creamos la BD
    UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(MainActivity.this, "dbUsuarios", null, 1);


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
             actualizarRealTime();
            ////     Inicializaciones    ////
            fragmentActual = "home"; //Selecciono el fragment donde estoy
            politicaP = findViewById(id.politicaP); //Inicializo el boton de politica de privacidad
            SQLiteDatabase db = usdbh.getWritableDatabase(); // Inicializo la base de datos
            mAuth = FirebaseAuth.getInstance();  // Obtengo la instancia del usuario de firebase que se logueo
            Toolbar toolbar = findViewById(R.id.toolbar);// Inicializo el toolbar
            setSupportActionBar(toolbar);// Inicializo el toolbar
            final DrawerLayout drawer = findViewById(R.id.drawer_layout); // Inicializo el drawer
            navigationView = findViewById(R.id.nav_view); // inicializo el navegation view

            //Se crea el token de Inicio sesion en Firebase
            mTokenReceiver = new TokenBroadcastReceiver() {
                @Override
                public void onNewToken(String token) {
                    Log.e("New token", "onNewToken:" + token);
                    setCustomToken(token);
                }
            };
            // Determina que elementos se ocultan del menu lateral dependiendo del tipo de usuario que inicie sesion, Admin, Barber, o cliente
            controladorMenuLateral(Integer.parseInt(traerROLSQLITE()));


            // Se creal el menu en el navegation drawer
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                    R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                    .setDrawerLayout(drawer)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);


            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    // Cuando se presiona un item del menu, dependiendo del item se cambia el fragment interno o se inicia una actividad
                    int id = menuItem.getItemId(); // Id del item que se toco
                    Fragment miFragment = null;// Fragment que va a remplazar
                    boolean seleccionado = false;// Quiere decir que se selecciono un  nuevo fragment
                    String tag = "no"; // Es el tag de cada fragment

                    if (id == R.id.nav_home) {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        seleccionado = true;
                        fragmentActual = "home";
                        miFragment = new HomeFragment(); // Se cambia el fragment nulo
                        tag = "home"; // Se agrega un tag de identificador
                    }
                    ;
                    if (id == R.id.nav_mi_cronograma) {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        seleccionado = true;
                        miFragment = new MiCronogramaFragment();
                        tag = "micronograma";
                        fragmentActual = tag;

                    }
                    ;

                    if (id == R.id.nav_mis_peluquerias) {
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        seleccionado = true;
                        miFragment = new MisPeluqueriasFragment();
                        tag = "mispeluquerias";
                        fragmentActual = tag;

                    }
                    ;

                    if (id == R.id.nav_mis_reservas) {
                        //   Log.e("Se presiono un fragment",seleccionado + "home");
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        seleccionado = true;
                        miFragment = new MisReservasFragment();
                        tag = "reservas";
                        fragmentActual = tag;
                    }
                    ;

                    if (id == R.id.cerrar_s) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Confirmación Cierre de sesión!");
                        builder.setMessage("¿Desea cerrar sesión?");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                LogoutmenuClick();
                                Toast.makeText(getApplicationContext(), "Sesión cerrada", Toast.LENGTH_LONG).show();
                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.show();
                    }
                    ;
                    if (id == R.id.nav_perfil) {
                        Intent intent = new Intent(MainActivity.this, MainActivityProfile.class);
                        startActivity(intent);
                        return true;
                    }
                    ;

                    if ((id == R.id.nav_share)) {
                        //Compartir crea un texto de compartir
                        Intent compartir = new Intent(android.content.Intent.ACTION_SEND);
                        compartir.setType("text/plain");
                        String mensaje = "Te recomiendo esta App para pedir tus cortes. Descargala ahora+" +
                                " https://play.google.com/store/apps/details?id=com.wayloo.wayloo";
                        compartir.putExtra(android.content.Intent.EXTRA_SUBJECT, "WayLoo App");
                        compartir.putExtra(android.content.Intent.EXTRA_TEXT, mensaje);
                        startActivity(Intent.createChooser(compartir, "Compartir via"));
                    }

                    if (seleccionado) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, miFragment, tag).commit();
                    }
                    return false;
                }
            });


            /////   Clicks   //////

            //Coloco el listener del toque
            politicaP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Seteo la url
                    String ip = getString(R.string.ip_way);
                    String url = ip + "/webpage/privacidad.php";
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);// Inicia la accion
                    startActivity(intent);
                }
            });
    //    }
    }

    private void actualizarRealTime() {
        SharedPreferences tokensNotificacion
                = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String token = tokensNotificacion.getString("token","Unknow");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("token");
        ref.child(token).setValue(traerId_firebaseQLITE());
    }

    private void setCustomToken(String token) {
        mCustomToken = token;

        String status;
        if (mCustomToken != null) {
            status = "Token:" + mCustomToken;
        } else {
            status = "Token: null";
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }


    @Override
    protected void onPause() {
        super.onPause();
        //Pruebaaa fire error
        //unregisterReceiver(mTokenReceiver);
    }


    private void controladorMenuLateral(int parseInt) {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();

        switch (parseInt) {
            case 1:
                // Administrador
                nav_Menu.findItem(id.nav_mi_cronograma).setVisible(false);
                break;
            case 2:
                //Peluquero
                nav_Menu.findItem(R.id.nav_mis_peluquerias).setVisible(false);
                break;
            case 3:
                //Cliente
                nav_Menu.findItem(id.nav_mis_peluquerias).setVisible(false);
                nav_Menu.findItem(id.nav_mi_cronograma).setVisible(false);
            case 4:


                break;


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        TextView etTitu = findViewById(R.id.navTitulo);
        etTitu.setText(traerNombreSQLITE());
        TextView etEmail = findViewById(R.id.navMail);
        etEmail.setText(traerEmailSQLITE());
        cargarWebImagen(traerId_firebaseQLITE());
        imageViewLogomini = findViewById(R.id.imageViewLogoMININAV);


        imageViewLogomini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ImagenMainActivity.class);
                intent.putExtra("imagen",traerId_firebaseQLITE());
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();


    }


    private String traerNombreSQLITE(){
        String name= "UnKnow User";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT nombre FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                name= c.getString(0);

            } while(c.moveToNext());
        }
        return name;
    }

    //Trae el rol del usuario que inicio sesion en la BDSQLITE
    private String traerROLSQLITE(){
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

    private String traerEmailSQLITE(){
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

    private String traerId_firebaseQLITE(){
        String name= "UnKnow User";
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

    private void LogoutmenuClick(){
        //Save registro permanente
        SQLiteDatabase db = usdbh.getWritableDatabase();
        db.execSQL("delete from  CurrentUsuario;");

        Intent intent = new Intent(MainActivity.this, MainLogginActivity.class);
        startActivity(intent);
        finish();
    }


    public void cargarWebImagen(String id){
        imageViewLogomini = findViewById(R.id.imageViewLogoMININAV);
        String ip =getString(R.string.ip_way);
        String url =ip+"/consultas/imagenes/"+id+".jpg";
        url=url.replace(" ","%20");
        url=url.replace("ñ","n");
        url=url.replace("á","a");
        url=url.replace("é","e");
        url=url.replace("í","i");
        url=url.replace("ó","o");
        url=url.replace("ú","u");
        Log.e("IMG URL", url);
        request = Volley.newRequestQueue(MainActivity.this);
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Log.e("Respondio", "respondio");
                        response=redimensionarImagen(response,100,100);
                        imageViewLogomini.setImageBitmap(redondearBitmap(redimensionarImagen(response, 100, 100)));
                    }
                }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
        request.add(imageRequest);
    }

    private Bitmap redimensionarImagen(Bitmap bitmap, float anchoNuevo, float altoNuevo) {

        int ancho = bitmap.getWidth();
        int alto = bitmap.getHeight();

        if (ancho > anchoNuevo || alto > altoNuevo) {
            float escalaAncho = anchoNuevo / ancho;
            float escalaAlto = altoNuevo / alto;

            Matrix matrix = new Matrix();
            matrix.postScale(escalaAncho, escalaAlto);

            return Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false);

        } else {
            return bitmap;
        }


    }

    @Override
    public void onBackPressed() {
        //android.app.Fragment myFragment = MainActivity.this.getFragmentManager().findFragmentById(id.content_main);
     //   Log.e("Fracment Current", myFragment.toString() + "  " + myFragment.isVisible() + "  " + (myFragment != null));
        if (fragmentActual == "home") {
            finish();
        }else{
            Fragment miFragment = null;
            miFragment = new HomeFragment();
            fragmentActual = "home";
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, miFragment, "home").commit();
        }
    }


    private Bitmap redondearBitmap(Bitmap bitAconvertir) {
        Bitmap imageBitmap = bitAconvertir;
        RoundedBitmapDrawable roundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
        roundedBitmapDrawable.setCornerRadius(175.0f);
        roundedBitmapDrawable.setAntiAlias(true);

        Bitmap imageBitmapConBlanco = addWhiteBorder(drawableToBitmap(roundedBitmapDrawable), 2);
        RoundedBitmapDrawable roundedBitmapDrawableBlanco =
                RoundedBitmapDrawableFactory.create(getResources(), imageBitmapConBlanco);
        roundedBitmapDrawableBlanco.setCornerRadius(180.0f);
        roundedBitmapDrawableBlanco.setAntiAlias(true);


        return drawableToBitmap(roundedBitmapDrawableBlanco);
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public boolean confirmacionBuilder(String titulo, String cuerpo){
        final boolean[] respuesta = {false};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(titulo);
        builder.setMessage(cuerpo);
        builder.setCancelable(false);
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

               respuesta[0] =true;
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                respuesta[0] =false;
            }
        });
        builder.show();
        return respuesta[0];
    }

    @Override
    protected void onResume() {
        super.onResume();

        PackageInfo packageInfo;
        try{
            packageInfo = this.getPackageManager().getPackageInfo(getPackageName(),0);
            version = packageInfo.versionName;
        }catch (Exception e){
            e.printStackTrace();
        }
        firebaseRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String, Object> Actualizacion = new HashMap<>();
        Actualizacion.put("versionname",version);
        Task<Void> fetch = firebaseRemoteConfig.fetch(0);
        fetch.addOnSuccessListener(MainActivity.this, aVoid ->{
            firebaseRemoteConfig.activateFetched();
            Version(version);
        });
    }

    private void Version(String version) {
        String nueva = (String) firebaseRemoteConfig.getString("versionname");
        String playUrl = (String) firebaseRemoteConfig.getString("playurl");
        Log.e("Version en la nube", nueva+" "+playUrl);

        if (nueva.equalsIgnoreCase(version)) {
        }else{
            crearNotificacion(playUrl);
        }
    }

    public String datosDeFr(){
        String nueva = (String) firebaseRemoteConfig.getString("ciudadesA");
        return nueva;
    }

    private void crearNotificacion(String playUrl) {
        NotificationCompat.Builder mBuilder;
        NotificationManager mNotifyMgr =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        int icono = R.mipmap.ic_launcher;
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(playUrl));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);




        mBuilder =new NotificationCompat.Builder(getApplicationContext())
                .setContentIntent(pendingIntent)
                .setSmallIcon(icono)
                .setContentTitle("Hay una nueva actualización para la aplicación.")
                .setContentText("Existe una nueva versión de la aplicación, toca para actualizar.")
                .setVibrate(new long[] {100, 250, 100, 500})
                .setAutoCancel(true);

        mNotifyMgr.notify(1, mBuilder.build());

    }


}
