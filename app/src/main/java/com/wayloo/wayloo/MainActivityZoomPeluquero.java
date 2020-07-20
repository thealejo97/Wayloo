package com.wayloo.wayloo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivityZoomPeluquero extends AppCompatActivity implements cuadroDialogo.objDialog{

    String HORAINICIOPERMANENTE;
    UsuariosSQLiteHelper usdbh =
            new UsuariosSQLiteHelper(MainActivityZoomPeluquero.this, "dbUsuarios", null, 1);
    private SharedPreferences settings;
    private String nombre;
    private String apellido;
    private String telefono;
    private String NITPertenece;
    private String hInicio;
    private String hFin;
    private String calificacion;
    private String fireB;
    private String nitDelBarbeto;
    private ImageView imageViewLogomini;
    private RequestQueue request;
    private TextView nombreTv;
    private TextView telefTV;
    private TextView hInicioTV;


    private Button botonRegistrar;
    StringRequest stringRequestS;
    private Time fecFormatoTime;
    final Calendar myCalendar = Calendar.getInstance();
    TextView fechaB;
    RatingBar rBar = null;
    //Progress
    ProgressDialog progress;
    ArrayList<TextView> textViews = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_zoom_peluquero);

        //Obtengo los datos del activity anterior
        settings = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        rBar= findViewById(R.id.ratingBar);
        nombre = getIntent().getStringExtra("nombre");
        apellido = getIntent().getStringExtra("apellido");
        telefono = getIntent().getStringExtra("telefono");
        NITPertenece = getIntent().getStringExtra("NITPERT");
        hInicio = getIntent().getStringExtra("h_inicio");
        HORAINICIOPERMANENTE =  getIntent().getStringExtra("h_inicio");
        hFin = getIntent().getStringExtra("h_fin");
        calificacion = getIntent().getStringExtra("calificacion");
        fireB = getIntent().getStringExtra("fireB");
        nitDelBarbeto = getIntent().getStringExtra("barberiaDelBarbero");

        //Inicializo los campos
        rBar.setRating(Integer.parseInt(calificacion));
        nombreTv = findViewById(R.id.NomBarS);
        telefTV= findViewById(R.id.TelBarS);
        hInicioTV = findViewById(R.id.hInicioBarS);
        botonRegistrar = findViewById(R.id.buttonReservarCita);

        //Coloco los datos en los campos
        nombreTv.setText(nombre+" "+apellido);
        telefTV.setText(telefono);
        hInicioTV.setText("Turno: "+hInicio + " a "+ hFin);
        imageViewLogomini = findViewById(R.id.imageViewPrincipalFotoB);
        cargarWebImagen(fireB);

        //Cuando doy clic en registrar reserva
        botonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context con = MainActivityZoomPeluquero.this;
                new cuadroDialogo(con, fechaB.getText().toString(),MainActivityZoomPeluquero.this, textViews);
            }
        });
        //Llama al picker y dice que hacer cuando le da ok
        //Cuando le da click a la fecha para buscar
        fechaB = findViewById(R.id.textViewSeleccionarFecha);
        fechaPicker();
        String fActual = getCurrentDay();//Obtengo la fecha actual
        fechaB.setText(fActual);// La coloco

        if(fechaB.equals(getCurrentDay())) {// Si la fecha es la de hoy
            hInicio = getHora();// La hora de inicio es igual a la hora actual
        }
        Calendar now = Calendar.getInstance();
        String diaSem = getLetraDiaSemHoy(now);
        //Consulta si el peluquer trabaja el dia
        consultaDiasLaboralesBarbero(telefono,fActual,diaSem);



    }

    private String getLetraDiaSemHoy(Calendar now) {
        String currentDayLetter;
        // Creamos una instancia del calendario

        // Array con los dias de la semana
        String[] strDays = new String[]{
                "Domingo",
                "Lunes",
                "Martes",
                "Miercoles",
                "Jueves",
                "Viernes",
                "Sabado"};
        // El dia de la semana inicia en el 1 mientras que el array empieza en el 0
        currentDayLetter= strDays[now.get(Calendar.DAY_OF_WEEK) - 1];
        Log.e("Current day", currentDayLetter+" Dia hoy");
        switch (currentDayLetter){
            case "Domingo":
                currentDayLetter="D";
                break;
            case "Lunes":
                currentDayLetter="L";
                break;
            case "Martes":
                currentDayLetter="M";
                break;
            case "Miercoles":
                currentDayLetter="MI";
                break;
            case "Jueves":
                currentDayLetter="J";
                break;
            case "Viernes":
                currentDayLetter="V";
                break;
            case "Sabado":
                currentDayLetter="S";
                break;
        }
        return currentDayLetter;
    }

    private void rellenarTablaTurnos(Date horaOriginal, Date hFinalOriginal){
        //Rellena la tabla original con los turnos y los estados en blanco
        //Recibo las foras inicial y finales del peluquero
        TableLayout tbLayout = findViewById(R.id.tbLayout);
        tbLayout.removeAllViews(); //Reinicio la tabla
        tbLayout.setStretchAllColumns(true);// Ajusto los tamaños
        TableRow row1 = new TableRow(MainActivityZoomPeluquero.this); //Creo una fila
        TextView tv1 = new TextView(MainActivityZoomPeluquero.this);// TextView1 el titulo "reservas"
        TextView tvE1 = new TextView(MainActivityZoomPeluquero.this);// TextViewE1 el titulo "Estado"
        //Inicializo el titulo
        tv1.setText("Reservas");
        tv1.setTextSize(18);
        tv1.setTypeface(null, Typeface.BOLD_ITALIC);
        tv1.setTextColor(Color.WHITE);
        tv1.setGravity(Gravity.CENTER);
        //Inicializo el titulo estado
        tvE1.setText("Estado");
        tvE1.setTextSize(18);
        tvE1.setTextColor(Color.WHITE);
        tvE1.setGravity(Gravity.CENTER);
        tvE1.setTypeface(null, Typeface.BOLD_ITALIC);
        //Acomodo la primera fila y le añado ambos textviews
        row1.setGravity(Gravity.CENTER);
        row1.addView(tv1);
        row1.addView(tvE1);
        tbLayout.addView(row1);

        //Inicializo un date sumandole 0 horas y 0 minutos
        Date hSumada = sumaHora(horaOriginal,0,0);
        //Total de cantidad de cupos miro cuantas reservas de 45 minutos se pueden poner entre la hinicial y la hfinal
        int total=cantidadDeCupos(horaOriginal,hFinalOriginal);
        //For que crea las filas
        for (int i = 0; i<total;i++){
            TableRow row = new TableRow(MainActivityZoomPeluquero.this);// Fila
            TextView tv = new TextView(MainActivityZoomPeluquero.this);// Texto de la hora
            TextView tvE = new TextView(MainActivityZoomPeluquero.this);// Texto del estado

            tv.setText(hSumada.toString() +" - "+ sumaHora(hSumada,0,45));// Coloco el texto de la hora y 45 min mas
            //Especifico atributos
            tv.setTextSize(18);
            //Todos los estados en verde
            tvE.setText("■");
            tvE.setTextSize(18);
            tvE.setTypeface(null, Typeface.BOLD);

            hSumada= sumaHora(hSumada,0,45);

            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(Color.WHITE);

            tvE.setGravity(Gravity.CENTER);
            tvE.setTextColor(Color.GREEN);

            row.addView(tv);
            row.addView(tvE);
            row.setGravity(Gravity.CENTER);
            tbLayout.addView(row);
        }
    }

    private int cantidadDeCupos(Date horaOriginal, Date hFinalOriginal){
        int contador=0;
        // 1 si la primera es mas grande que la segunda
        // 0 si son iguales
        //-1 si la primera es mas pequeña que la segunda
        while(horaOriginal.compareTo(hFinalOriginal) <0){
            contador++;
            horaOriginal=sumaHora(horaOriginal,0,45);
            //Log.e("Primera coparacion",horaOriginal.toString() + "-"+ hFinalOriginal.toString());
        }
        return contador;
    }

    private Date convertirHoraStringDate(String stringHora) {
        DateFormat hora = new SimpleDateFormat("HH:mm");
        Date convertido = null;
        try {
            convertido = hora.parse(stringHora);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertido;
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

    private void fechaPicker() {
        final TextView fechaB = findViewById(R.id.textViewSeleccionarFecha);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                fechaB.setText(sdf.format(myCalendar.getTime()));

                //Restriccion d hora el mismo dia
                if(fechaB.equals(getCurrentDay())) {
                    hInicio = getHora();
                }else{
                    hInicio =HORAINICIOPERMANENTE;
                }
                //Cuando se cambia la fecha
                //Restriccion de dias laborales
                //Consulto el dia
                String diaSem = getLetraDiaSemHoy(myCalendar);
                consultaDiasLaboralesBarbero(telefono, fechaB.getText().toString(),diaSem);
            }

        };


        fechaB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                textViews.clear();
                Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpDialog = new DatePickerDialog(MainActivityZoomPeluquero.this,
                          date, mYear, mMonth,mDay);
                dpDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                dpDialog.show();
                borrarTabla();
                rellenarTablaTurnos(convertirHoraStringDate(hInicio),convertirHoraStringDate(hFin));
                String diaSem = getLetraDiaSemHoy(calendar);
                consultaDiasLaboralesBarbero(telefono, fechaB.getText().toString(),diaSem);
            }
        });
    }

    private void borrarTabla(){
        final TableLayout tbLayout = findViewById(R.id.tbLayout);
        //Rebuild tabla
        int count = tbLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = tbLayout.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

    }

    private Date sumaHora(Date tuFechaBase, int horasASumar,int minutosASumar){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(tuFechaBase); //tuFechaBase es un Date;
        calendar.add(Calendar.MINUTE, minutosASumar); //minutosASumar es int.
        calendar.add(Calendar.HOUR,   horasASumar); //horasASumar es int.
        //lo que más quieras sumar
        Date fechaSalida = calendar.getTime();
        //DateFormat format = new SimpleDateFormat("HHmm");

        String fechaSt= fechaSalida.getHours()+":"+fechaSalida.getMinutes();
        return conversor(fechaSt);
    }

    private Time conversor(String horaConver){
        try {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", new Locale("es", "ES"));
            fecFormatoTime = new java.sql.Time(sdf.parse(horaConver).getTime());
         //   Log.e("Hora date",fecFormatoTime.toString());
            System.out.println("Fecha con el formato java.sql.Time: " + fecFormatoTime);
            return fecFormatoTime;
        } catch (Exception ex) {
        //    Log.e("Error de parse",ex.getMessage());
            System.out.println("Error al obtener el formato de la fecha/hora: " + ex.getMessage());
            return null;
        }
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
        request = Volley.newRequestQueue(MainActivityZoomPeluquero.this);
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        hideProgressDialog();
                        Log.e("Respondio", "respondio imagen");
                        response=redimensionarImagen(response,150,150);
                        imageViewLogomini = findViewById(R.id.imageViewPrincipalFotoB);
                        imageViewLogomini.setImageBitmap(redondearBitmap(response));

                    }
                }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Toast.makeText(MainActivityZoomPeluquero.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
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
    public void ResultadoDialogo(String fecha, String HI, String HF, String diasLaborales) {
        //Toast.makeText(MainActivityZoomPeluquero.this, "Hora seleccionada" +fecha+ "Ho "+HI+" a "+HF, Toast.LENGTH_SHORT).show();
        if(fecha != null && HI != null ){
        RegistrarReserva(fecha,HI,HF);
        }else {
            Toast.makeText(MainActivityZoomPeluquero.this, "Error debe seleccionar los datos para completar el registro", Toast.LENGTH_SHORT).show();
        }


    }


    private void RegistrarReserva(final String fecha, String hi, String hf) {
        showProgressDialog("Registrando reserva","Espere mientras se contacta con el servidor");
        String partes[] = hi.split(" - ");
        hi= partes[0];
        hf= partes[1];


        Log.e("Rserva en", fecha + " "+ hi + " "+hf);
        final String tel_cliente = consultarClienteSQLITE();
        //Inicio los datos de Usuario

        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/registrar_Reserva.php?";
        Log.e("URL DEL POST", url);

        final String finalHi = hi;
        final String finalHf = hf;
        final String[] idReservaG = {null};
        stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.e("RESPUESTA  regis5tro: ", "" + response);
                if(response.trim().equalsIgnoreCase("TimeReserverd")) {
                    Toast toast = Toast.makeText(MainActivityZoomPeluquero.this, "Ya existe una reserva en este horario", Toast.LENGTH_LONG);
                    View view = toast.getView();

                    //To change the Background of Toast
                    view.setBackgroundColor(Color.RED);
                    TextView text = (TextView) view.findViewById(android.R.id.message);

                    //Shadow of the Of the Text Color
                    text.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                    text.setTextColor(Color.BLACK);
                    text.setTextSize(16);
                    toast.show();
                }else{
                try {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e("URL RESPO", response.toString());


                     JSONArray json = jsonObject.optJSONArray("reservas");
                     JSONObject jsonObject2 = null;
                     jsonObject2 = json.getJSONObject(0);
                     idReservaG[0] = jsonObject2.optString("id_reserva");

                    Log.e("JSON ARRAY RESPON",json.toString());

                    Log.e("ST ARR RESPON",jsonObject2.getString("id_reserva"));
                } catch (JSONException e) {
                    Log.e("Error, creandoJSOn", e.toString());
                    e.printStackTrace();
                }

                if (!(response.trim().equalsIgnoreCase("ErrorReserva"))) {
                    Toast.makeText(MainActivityZoomPeluquero.this, "Se ha registrado su reserva con exito", Toast.LENGTH_SHORT).show();

                    try {
                        String tiempo =fecha + " "+ finalHi;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss");
                        Date parsedDate = dateFormat.parse(tiempo);
                        Calendar fechaAlarma= Calendar.getInstance();
                        fechaAlarma.setTime(parsedDate);
                        alarmador(Integer.parseInt(idReservaG[0]),(fechaAlarma.getTimeInMillis() - 1800000 ), finalHi);
                    } catch(Exception e) { //this generic but you can control another types of exception
                        // look the origin of excption
                        e.printStackTrace();
                        Log.e("Excepcion tiempo", e.toString());
                    }


                } else {
                    Toast.makeText(MainActivityZoomPeluquero.this, "No se ha registrado su reserva", Toast.LENGTH_SHORT).show();
                }


                }
                ConsultaReservas(fecha);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                Log.e("RESPUESTA: ", "" + error);
                Toast.makeText(MainActivityZoomPeluquero.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> parametros = new HashMap<>();


                parametros.put("id_reserva", "1");
                parametros.put("fecha_r", fecha);
                parametros.put("h_i_r", finalHi);
                parametros.put("h_f_r", finalHf);
                parametros.put("id_b_r", telefono);
                parametros.put("id_c_r", tel_cliente);
                parametros.put("id_barberia", nitDelBarbeto );
                return parametros;
            }
        };

        request.add(stringRequestS);
    }

    private void alarmador(int i, Long timestamp, String hi) {
        Context ctx= getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
        ctx.getSystemService(ALARM_SERVICE);

        SharedPreferences.Editor edit = settings.edit();
        edit.putString("hour", hi);
        //SAVE ALARM TIME TO USE IT IN CASE OF REBOOT
        edit.putLong("alarmID", timestamp);
        edit.putLong("alarmTime", timestamp);
        edit.commit();

        Log.e("     ALARMADOR " , i+ " "+timestamp+ " "+hi);
        Utils.setAlarm(i, timestamp, MainActivityZoomPeluquero.this);

    }

    private String consultarClienteSQLITE() {
        String result="null";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id_usu FROM CurrentUsuario", null);
        if (c != null) {
            c.moveToFirst();
            do {
                //Asignamos el valor en nuestras variables para usarlos en lo que necesitemos
                result = c.getString(c.getColumnIndex("id_usu"));
            } while (c.moveToNext());
        }

        //Cerramos el cursor y la conexion con la base de datos
        c.close();
        db.close();
        return result;
    }


    private void ConsultaReservas(final String fechaConsulta){

        showProgressDialog("Consultando", "Consultando fecha "+ fechaConsulta);
        final TableLayout tbLayout = findViewById(R.id.tbLayout);
        tbLayout.setStretchAllColumns(true);

        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);
        //Trae todas las reservas en esa fecha
        String url = ip + "/consultas/consulta_RESERVA.php?";

        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Trae todas las reservas en esa fechay de ese barbero
                hideProgressDialog();
                Log.e("Response ", response + response.equalsIgnoreCase("[]") +response.isEmpty());
                if(response.equalsIgnoreCase("[]")){
                    // SI NO DEVUELVE NADA no hay reservas ese dia entonces dejo la tabla libre todo el dia
                    //Borro todas las filas de la tabla
                    int count = tbLayout.getChildCount();
                    if(count != 0) {
                        for (int i = 1; i < count; i++) {
                            View child = tbLayout.getChildAt(i);
                            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
                        }
                    }
                    ///Ejecuto rellenar tabla turnos para llenar en blanco
                    rellenarTablaTurnos(convertirHoraStringDate(hInicio),convertirHoraStringDate(hFin));
                    //Crea la lista de reservas con la lista de turnos que creo rellenar tabla
                    creadorDeComboReservas();
                }else {
                    try {
                        ///Ejecuto rellenar tabla turnos para llenar en blanco
                        rellenarTablaTurnos(convertirHoraStringDate(hInicio),convertirHoraStringDate(hFin));
                        //Crea la lista de reservas con la lista de turnos que creo rellenar tabla
                        creadorDeComboReservas();
                        Log.e("Response ", "Rellenando");
                        //Creo un json con las reservas creadas que existen
                        JSONObject jsonObject = new JSONObject(response.toString());
                        //Extraigo el objeto
                        JSONArray json = jsonObject.getJSONArray("reservas");
                        //Recorrer el objeto
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject2 = null;
                            jsonObject2 = json.getJSONObject(i); // traigo el primer objeto(Fila del JSON)
                            //ubico los ocupados para pintarlos
                            selectorDeOcupados(sumaHora(convertirHoraStringDate(jsonObject2.optString("hora_inicio_reserva")),0,0).toString() );
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressDialog();
                int count = tbLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = tbLayout.getChildAt(i);
                    if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
                }

                TableRow row = new TableRow(MainActivityZoomPeluquero.this);
                TableRow rowNR = new TableRow(MainActivityZoomPeluquero.this);
                TextView tv = new TextView(MainActivityZoomPeluquero.this);
                TextView tvNR = new TextView(MainActivityZoomPeluquero.this);
                tv.setText("RESERVAS");
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(Color.WHITE);
                tvNR.setText("Error de consulta, reintente.");
                tvNR.setGravity(Gravity.CENTER);
                tvNR.setTextColor(Color.WHITE);
                final int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    row.setBackgroundDrawable(ContextCompat.getDrawable(MainActivityZoomPeluquero.this, R.drawable.border) );
                    rowNR.setBackgroundDrawable(ContextCompat.getDrawable(MainActivityZoomPeluquero.this, R.drawable.border) );
                } else {
                    row.setBackground(ContextCompat.getDrawable(MainActivityZoomPeluquero.this, R.drawable.border));
                    rowNR.setBackground(ContextCompat.getDrawable(MainActivityZoomPeluquero.this, R.drawable.border));
                }
                row.addView(tv);
                row.setGravity(Gravity.CENTER);
                rowNR.addView(tvNR);
                rowNR.setGravity(Gravity.CENTER);
                tbLayout.addView(row);
                tbLayout.addView(rowNR);
                Log.e("Response Error", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parameters = new HashMap<String,String>();
                parameters.put("fBuscada",fechaConsulta);
                parameters.put("idbarbero",telefono);
                return parameters;
            }

        };
        Log.e("Datos reservas a bus", fechaConsulta + "  "+ telefono);
        request.add(stringRequest);
    }
    ///Dialog Progress
    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(MainActivityZoomPeluquero.this, titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}

    private  void selectorDeOcupados(String hUnaReserva){
        //Recibe, recrea el combo y compara si la hora que recibe esta en la tabla
        //Recibo la hora que se debe pintar
        Log.e("Hora a pintar ",hUnaReserva);
        final TableLayout tableLayout = findViewById(R.id.tbLayout);
        textViews.clear();
        //Recorro las filas de la tabla
        for(int i = 0; i < tableLayout.getChildCount(); i++){
                TableRow tableRow = (TableRow) tableLayout.getChildAt(i); // Una fila
                View tempView = tableRow.getChildAt(0); // Saco el textview de la hora
                if(tempView instanceof TextView){
                    String horaInicioCombo =((TextView) tempView).getText().toString(); // Saco el estring del textview que tiene la hora
                    //Si la hora es valida, osea es despues de la hora actual, la añado al combo
                    if(fechaB.getText().toString().equalsIgnoreCase(getCurrentDay())){
                        if(ocultarYValidadHoras(horaInicioCombo)) {
                            textViews.add((TextView) tempView);
                        }
                    }else{
                        textViews.add((TextView) tempView);
                    }
                    ////Corto la hora del textview para sacar la hi y la comparo con la hora que tiene la reserva
                    if(splitHora((TextView) tempView).getText().toString().equalsIgnoreCase(hUnaReserva)){
                        //Si si tiene una reserva en esa hora
                        //Saco el textView de la columna estado
                        View tempViewLibre = tableRow.getChildAt(1);
                        //Seteo el texto otra vez
                        ((TextView) tempViewLibre).setText("■");
                        ((TextView) tempViewLibre).setTypeface(null, Typeface.BOLD);
                        Log.e("Pintado___",((TextView) tempView).getText().toString());
                        //Lo pinto de rojo ocupado
                        ((TextView) tempViewLibre).setTextColor(Color.RED);
                    }
            }
        }
    }

    private TextView splitHora(TextView tempText){
        //Corta la hora

        if( tempText.getText().toString().equalsIgnoreCase("Reservas")){return tempText;}else{
        TextView txVcortado = new TextView(this);
        String textoDelViejo = tempText.getText().toString(); // Creo el string
        String[] separated = textoDelViejo.split(" ");
        String part1 = separated[0];
        txVcortado.setText(part1); // devuelvo la hora cortada
        return txVcortado;}
    }

    private void creadorDeComboReservas() {
        //Crea la lista de reservas con la lista de turnos que creo rellenar tabla
        //Limpia el listado que haya
        textViews.clear();
        final TableLayout tableLayout = findViewById(R.id.tbLayout);
        //For que recorre las filas de la tabla
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            // Saco la primera fila
            TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
            View tempView = tableRow.getChildAt(0); // Saco el textView 1 de las horas
            View tempView2 = tableRow.getChildAt(1); // Saco el textView 2 de los estados
            if (tempView instanceof TextView && tempView2 instanceof TextView) {

                    //Saco el texto de cada textview
                    String horaInicioCombo =((TextView) tempView).getText().toString();
                    Log.e("Comparacion dia",fechaB.getText().toString() + "   " +getCurrentDay()+"    "+
                            fechaB.getText().toString().equalsIgnoreCase(getCurrentDay()));
                    //Si la fecha puesta en el selector de fecha es igual al a hoy debo verificar que el combo solo reciba horas\
                    // posteriores a la actual
                    //ya que no se puede reservar en el pasado
                    if(fechaB.getText().toString().equalsIgnoreCase(getCurrentDay())){
                        Log.e("Hora valida",horaInicioCombo + "  ");
                        //Si es hoy, verifico que la hora de la fila sea despues
                        if(ocultarYValidadHoras(horaInicioCombo)) {
                            //Si la hora es valida, osea es despues de la hora actual, la añado al combo
                            textViews.add((TextView) tempView);
                        }
                    }else{
                        //Si no es hoy, quiere decir que tiene todas las horas disponibles
                        //las añado todas
                        textViews.add((TextView) tempView);
                    }

            }
        }
    }

    private boolean ocultarYValidadHoras(String horaValidar) {
        //Verifico que la hora del text recibido sea posterior a la actual
        //Si no es el titulo
        if(horaValidar.equalsIgnoreCase("Reservas")){return false;}else {
            String hi = null;
            String partes[] = horaValidar.split(" - ");
            hi = partes[0];// Divido y asi saco la hora de inicio
            Date dateHI = convertirHoraADate(hi); // Creo el date de la reserva
            Date dateHActual = convertirHoraADate(getHora()); // Creo el date de la hora actual

            if (dateHI.compareTo(dateHActual) > 0) {
                return true;
            } else {
                return false;
            }
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
    

    private void consultaDiasLaboralesBarbero(final String telBarberoDiaLab, final String fActual, final String diaSemana) {

        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consultarDiasLaborales.php?";

        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            private String diasLaborales;

            @Override
            public void onResponse(String response) {
                Log.e("Response ", response + response.equalsIgnoreCase("[]") + response.isEmpty());
                if (response.equalsIgnoreCase("[]")) {
                    Toast.makeText(MainActivityZoomPeluquero.this, "Error de consulta de dias", Toast.LENGTH_SHORT).show();
                } else {

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());

                        //extracting json array from response string
                        JSONArray json = jsonObject.getJSONArray("usuarioC");

                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject2 = null;
                            jsonObject2 = json.getJSONObject(i);
                            diasLaborales = jsonObject2.optString("diaslaborales");

                        }
                        String[] dia = diasLaborales.split(",");
                        Log.e("Dia comparacion blan"," dias que no trabaja "+ dia[0]);


                        Boolean blanquear=false;

                        for(int i=0; i <dia.length;i++){
                            Log.e("Forrrr rr", dia[i]);
                            if(diaSemana.equalsIgnoreCase(dia[i]) ){
                                blanquear=true;
                            }
                        }

                        if(blanquear){
                            TableLayout tbLayout = findViewById(R.id.tbLayout);
                            tbLayout.removeAllViews(); //Reinicio la tabla
                            Button btnAceptar = findViewById(R.id.buttonReservarCita);
                            btnAceptar.setEnabled(false);
                            btnAceptar.setText("EL BARBERO NO TIENE TURNOS ESTE DIA");
                         //   Toast.makeText(MainActivityZoomPeluquero.this, "El barbero no tiene turnos para este dia", Toast.LENGTH_SHORT).show();
                        }else{
                            ConsultaReservas(fActual);//Consulta las reservas del barbero
                            Button btnAceptar = findViewById(R.id.buttonReservarCita);
                            btnAceptar.setEnabled(true);
                            btnAceptar.setText("RESERVAR");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("IDPEL", telBarberoDiaLab);
                return parameters;
            }

        };
        Log.e("Usuario a consultar", telBarberoDiaLab);
        request.add(stringRequest);
    }

}
