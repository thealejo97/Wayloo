package com.wayloo.wayloo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.ui.engine.engine;

public class ImagenMainActivity extends AppCompatActivity {
    private RequestQueue request;
    TouchImageView tImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_main);




        tImg = (TouchImageView)findViewById(R.id.touchImageView);
        Intent intent = getIntent();

        String fire = intent.getStringExtra("imagen");
        cargarWebImagen(fire);
    }

    public void cargarWebImagen(String id) {

        engine myEngine = new engine();
        myEngine.showProgressDialog("Cargando","Por favor espere", ImagenMainActivity.this);
        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/imagenes/" + id + ".jpg";
        url = url.replace(" ", "%20");
        url = url.replace("ñ", "n");
        url = url.replace("á", "a");
        url = url.replace("é", "e");
        url = url.replace("í", "i");
        url = url.replace("ó", "o");
        url = url.replace("ú", "u");
        Log.e("IMG URL", url);
        request = Volley.newRequestQueue(ImagenMainActivity.this);
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        myEngine.hideProgressDialog();
                        Log.e("Respondio", "respondio");
                        tImg.setImageBitmap(response);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                myEngine.hideProgressDialog();
                Toast.makeText(ImagenMainActivity.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();

            }
        });
        request.add(imageRequest);
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

}
