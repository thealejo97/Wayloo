package com.wayloo.wayloo.ui.perfildebarbero;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.wayloo.wayloo.MainActivity;
import com.wayloo.wayloo.MainActivityZoomPeluquero;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;

import java.util.HashMap;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;

public class barberoperfilFragment extends Fragment {

    private TextView tvnombre, TelBarPer, horarioBar;
    private String tel,nombre,calificion,hora,fireb;
    private RatingBar rakBar;
    ProgressDialog progress;
    private RequestQueue request;
    private ImageView imgPerfil;
    private Button btnEliminar;

    public barberoperfilFragment() {
        // Required empty public constructor
    }

    public static barberoperfilFragment newInstance(String param1, String param2) {
        barberoperfilFragment fragment = new barberoperfilFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_barberoperfil, container, false);
        tvnombre = vista.findViewById(R.id.NombarPerfilBar);
        TelBarPer = vista.findViewById(R.id.TelBarPer);
        horarioBar = vista.findViewById(R.id.horarioBar);
        rakBar = vista.findViewById(R.id.rankBarberPerfil);
        imgPerfil = vista.findViewById(R.id.imagenProfileBarber);
        btnEliminar = vista.findViewById(R.id.desasociarBr);

        tel = getArguments().getString("telefono");
        nombre = getArguments().getString("nombre");
        calificion = getArguments().getString("calificacion");
        hora = getArguments().getString("hora");
        fireb = getArguments().getString("fire");

        cargarWebImagen(fireb);
        tvnombre.setText(nombre);
        TelBarPer.setText(tel);
        horarioBar.setText(hora);
        rakBar.setNumStars(Integer.parseInt(calificion));

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                consultarReservasAfectadasELIMINACION(tel);
            }
        });
        return vista;
    }

    private void consultarReservasAfectadasELIMINACION(String tel) {

        request = Volley.newRequestQueue(getContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consultarReservasAfectadas.php?";

        Log.e("URL DEL POST", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("response", response);

                if (response.equalsIgnoreCase("ok")) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Va a realizar cambios de horario");
                    builder.setMessage("Atención,Esta a punto de realizar un cambio en su perfil publico de barbero ¿Desea continuar?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ejecutarELIMINAR(tel);
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();

                } else {
                    if (response.equalsIgnoreCase("reservaProxima")) {
                        Toast.makeText(getContext(), "Error, tiene una reserva en la siguiente hora, debe finalizar el turno y reintentar.",
                                Toast.LENGTH_SHORT).show();
                    } else {

                        try {
                            int numRes = Integer.parseInt(response);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Cambio de horario detectado");
                            builder.setMessage("Atención, aun tiene reservas sin cumplir, al eliminar el perfil se cancelaran " + numRes + " reservas ¿Desea continuar?");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ejecutarELIMINAR(tel);
                                }
                            });

                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                                }
                            });
                            builder.show();

                        }catch (NumberFormatException  e){
                            Log.e("Error ", e.toString());
                            Toast.makeText(getContext(), "Error "+ response, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response Update ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("telBarbero", tel);
                parameters.put("key", "barbero");

                return parameters;
            }
        };
        request.add(stringRequest);

    }

    private void ejecutarELIMINAR(String tel){

        request = Volley.newRequestQueue(getContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/DeleteBarbero.php?";

        Log.e("URL DEL POST", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response Update go ", response);
                if(response.equalsIgnoreCase("ok")) {
                    Toast.makeText(getContext(), "Actualizado", Toast.LENGTH_SHORT).show();
                    reiniciarApp();
                }else{
                    Toast.makeText(getContext(), "Error Actualizando", Toast.LENGTH_SHORT).show();
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response Update ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("telBarbero", tel);

                return parameters;
            }

        };
        request.add(stringRequest);

    }


    /*

    private void desasociarBarbero(String telefono) {
        showProgressDialog("Desvinculando el barbero", "Conectando con el servidor.");
        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/DeleteBarbero.php?";
        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Se ha desvinculado el barbero", Toast.LENGTH_SHORT).show();
                Log.e("Response Update go ", response);
                reiniciarApp();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Log.e("Response Update ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("telBarbero", telefono);
                return parameters;
            }

        };
        request.add(stringRequest);

    }
*/
    private void reiniciarApp() {
        Intent mStartActivity = new Intent(getContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getContext().getSystemService(getContext().ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public void cargarWebImagen(final String id){
        showProgressDialog("Cargando ..","Por favor espere....");
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
        request = Volley.newRequestQueue(getContext());
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Log.e("Respondio", "respondio imagen");
                        response=redimensionarImagen(response,200,200);
                        imgPerfil.setImageBitmap(redondearBitmap(response));
                        hideProgressDialog();
                    }
                }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
        request.add(imageRequest);
    }
    ///Dialog Progress
    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(getContext(), titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}

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

    private Bitmap redondearBitmap(Bitmap bitAconvertir){
        Bitmap imageBitmap= bitAconvertir;
        RoundedBitmapDrawable roundedBitmapDrawable=
                RoundedBitmapDrawableFactory.create( getResources(), imageBitmap);
        roundedBitmapDrawable.setCornerRadius(175.0f);
        roundedBitmapDrawable.setAntiAlias(true);

        Bitmap imageBitmapConBlanco=addWhiteBorder(drawableToBitmap(roundedBitmapDrawable),2);
        RoundedBitmapDrawable roundedBitmapDrawableBlanco=
                RoundedBitmapDrawableFactory.create( getResources(), imageBitmapConBlanco);
        roundedBitmapDrawableBlanco.setCornerRadius(180.0f);
        roundedBitmapDrawableBlanco.setAntiAlias(true);


        return  drawableToBitmap(roundedBitmapDrawableBlanco);
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
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