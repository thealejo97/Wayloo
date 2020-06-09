package com.wayloo.wayloo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;

public class MainActivityPrincipal extends AppCompatActivity {
    private VideoView mVideoView;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnRegistrarme, btnIniciar;
    private String id_firebase;

    //Creamos la BD
    UsuariosSQLiteHelper usdbh =
            new UsuariosSQLiteHelper(MainActivityPrincipal.this, "dbUsuarios", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       // super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        if(verificarIniciadoSesion()){//Verifica que ya no haya iniciado sesion
            Intent intent = new Intent(MainActivityPrincipal.this, MainActivity.class);
            startActivity(intent);
            finish();

        }else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_main_principal);
            mVideoView = (VideoView) findViewById(R.id.videoView);
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bgmain);
            mVideoView.setVideoURI(uri);
            mVideoView.start();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                }

            });

            btnIniciar = findViewById(R.id.loginBtnInicio);
            btnIniciar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(MainActivityPrincipal.this, MainActivityRegistarUser.class));

                }
            });
            btnRegistrarme = findViewById(R.id.SingitUPBtnInicio);
            btnRegistrarme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(MainActivityPrincipal.this, MainLogginActivity.class));


                }
            });
        }
    }


    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        public MyViewPagerAdapter() {
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }
        @Override
        public int getCount() {
            return layouts.length;
        }
        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
    private boolean verificarIniciadoSesion(){
        boolean iniciado= false;
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT id_firebase FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                id_firebase= c.getString(0);
                iniciado = true;
            } while(c.moveToNext());
        }
        if(id_firebase != null){
            iniciado = true;
        }
        return iniciado;
    }
}