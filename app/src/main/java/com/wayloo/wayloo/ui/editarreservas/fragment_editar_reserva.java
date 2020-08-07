package com.wayloo.wayloo.ui.editarreservas;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.Utils;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.Utilidades;
import com.wayloo.wayloo.ui.engine.engine;
import com.wayloo.wayloo.ui.misReservas.MisReservasFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class fragment_editar_reserva extends Fragment {
    private TextView txtHora;
    private TextView txtNombreBR;
    private TextView txtFechaR;
    private RequestQueue request;
    private ImageView imageViewLogomini;
    ProgressDialog progress;
    private Button btnEliminarR;

    public fragment_editar_reserva() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_editar_reserva, container, false);


        txtHora = root.findViewById(R.id.horaReservaEditar);
        txtNombreBR = root.findViewById(R.id.NombreBarReservaEditar);
        txtFechaR = root.findViewById(R.id.FechaReservaEditar);
        imageViewLogomini =root.findViewById(R.id.ImageReservaBarberoEditar);
        btnEliminarR = root.findViewById(R.id.btnEliminarReserva);
        final String fechaR = getArguments().getString("fechaR");
        final String HI = getArguments().getString("HI");
        final String HF = getArguments().getString("HF");
        String nbr = getArguments().getString("nbr");
        final String idBR = getArguments().getString("idBR");

        txtHora.setText(HI +" - "+ HF);
        txtNombreBR.setText(nbr);
        txtFechaR.setText(fechaR);
        cargarWebImagen(idBR);
        // Inflate the layout for this fragment

        btnEliminarR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarReserva(fechaR,HI, new engine().getInternoTelSQLITE(getContext()), HF, idBR);
            }
        });
        return root;
    }


    public void cargarWebImagen(final String id){
        showProgressDialog("Cargando", "Porfavor espere.");
        String url ="https://wayloo.000webhostapp.com/consultas/imagenes/"+id+".jpg";
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
                        Log.e("Respondio", "respondio");
                        response=redimensionarImagen(response,150,150);
                        imageViewLogomini.setImageBitmap(redondearBitmap(response));
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

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    ///Dialog Progress
    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(getContext(), titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}

    private String traerTelSQLITE(){

        //Creamos la BD
        UsuariosSQLiteHelper usdbh =
                new UsuariosSQLiteHelper(getContext(), "dbUsuarios", null, 1);
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

    private void eliminarReserva(final String f_res,final String HI_re,final String telC_re, final String HF_re, final String idB_re ) {

        final String[] idReservaG = {null};
        if (f_res.equalsIgnoreCase("")) {
            Toast.makeText(getContext(), "ERROR ELIMINANDO, POR FAVOR VERIFIQUE", Toast.LENGTH_LONG).show();
        } else {


            final Utilidades utl = new Utilidades();
            //Si no son iguales
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirmación eliminación del Barberia!");
            builder.setMessage("Va a eliminar los datos del sistema, ¿Desea continuar?");
            builder.setCancelable(false);
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showProgressDialog("Eliminando","Por favor espere");
                    request = Volley.newRequestQueue(getContext());

                    String ip =getString(R.string.ip_way);

                    String url = ip + "/consultas/DeleteReserva.php?";

                    Log.e("URL DEL POST", url);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("Response Delete go ", response);
                            hideProgressDialog();
                            try {

                                JSONObject jsonObject = null;
                                    jsonObject = new JSONObject(response.toString());

                            JSONArray json = jsonObject.optJSONArray("reservas");
                            JSONObject jsonObject2 = null;
                            jsonObject2 = json.getJSONObject(0);
                            idReservaG[0] = jsonObject2.optString("id_reserva");

                                Log.e("URL RESPO", response.toString());

                            String tiempo =f_res + " "+ HI_re;
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss");
                            Date parsedDate = null;
                                parsedDate = dateFormat.parse(tiempo);

                            Log.e("Tiempo Cancelar", tiempo);
                            Calendar fechaAlarma= Calendar.getInstance();
                            fechaAlarma.setTime(parsedDate);
                            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        
                            Utils utl = new Utils();
                            utl.cancelAlarm(Integer.parseInt(idReservaG[0]),getContext());

                            Fragment miFragment = null;
                            miFragment = new MisReservasFragment();
                            miFragment.setArguments(null);
                            getFragmentManager().beginTransaction().addToBackStack(getClass().getName()).replace(R.id.content_main, miFragment).commit();

                            } catch (ParseException | JSONException e) {
                                e.printStackTrace();
                            }
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
                            parameters.put("freserva", f_res);
                            parameters.put("hireserva", HI_re);
                            parameters.put("hfreserva", HF_re);
                            parameters.put("telClReserva", telC_re); //firebase
                            parameters.put("fireBarbero", idB_re); //firebase


                            return parameters;
                        }

                    };
                    Log.e("Datos a eliminar R", f_res +" "+ HI_re +" "+ telC_re+ " "+ idB_re);

                    Date dateHReserva = convertirHoraADate(HI_re);
                    Date dateHActual = convertirHoraADate(getHora());
                    //Si al sumarle 1 hora mas a la hora actual se pasa de la hora de la reserva ya no es valido cancelarla
                    Log.e("hora valida?", dateHReserva +"  "+ dateHActual+"  " +  restarHorasFecha(dateHActual,1) +"  Compart " +
                            dateHReserva.compareTo(restarHorasFecha(dateHActual,1)));

                    // Si es hoy
                    if(f_res.equalsIgnoreCase(getCurrentDay())) {
                        if (reservaCancelable(dateHActual,dateHReserva,f_res)) {
                            request.add(stringRequest);
                        }else{
                            hideProgressDialog();
                            Toast.makeText(getContext(), "Error, las reservas solo se pueden eliminar con 1 hora de anticipación.", Toast.LENGTH_LONG).show();
                        }
                    } else{
                        request.add(stringRequest);
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();

        }

    }

    private boolean reservaCancelable(Date dateHActual, Date dateHReserva, String f_res){

        Date dateObj1 = dateHActual;
        Date dateObj2 = dateHReserva;
        System.out.println(dateObj1);
        System.out.println(dateObj2 + "\n");

        DecimalFormat crunchifyFormatter = new DecimalFormat("###,###");

        long diff = dateObj2.getTime() - dateObj1.getTime();

        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
        Log.e("Days", diffDays + "");
        System.out.println("difference between days: " + diffDays);

        int diffhours = (int) (diff / (60 * 60 * 1000));
        Log.e("Hours", diffhours+"");
        System.out.println("difference between hours: " + crunchifyFormatter.format(diffhours));

        int diffmin = (int) (diff / (60 * 1000));
        Log.e("min", diffmin+"");
        System.out.println("difference between minutues: " + crunchifyFormatter.format(diffmin));

        // Si es hoy
            if (diffmin >= 60) {
                return true;
            }else{
                return false;
            }
    }


    private Date convertirHoraADate(String stringFecha) {
        Date date = null;
        String pattern = "HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        try {
            date = simpleDateFormat.parse(stringFecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
    private String getCurrentDay() {
// Obtiene la fecha actual en string
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime ;
    }

    private String getHora() {
        int hora, minutos, segundos;
        //Calendar calendario = Calendar.getInstance();
        Calendar calendario = new GregorianCalendar();
        hora =calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND);

        String horaS= hora + ":" +minutos;

        return horaS;
    }
    public Date restarHorasFecha(Date fecha, int horas){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha); // Configuramos la fecha que se recibe
        calendar.add(Calendar.HOUR, horas);  // numero de horas a añadir, o restar en caso de horas<0

        return calendar.getTime(); // Devuelve el objeto Date con las nuevas horas añadidas

    }
}
