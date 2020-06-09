package com.wayloo.wayloo.ui.micronograma;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.adapters.ReservasCronogramaAdapters;
import com.wayloo.wayloo.entidades.ReservasCronograma;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.editarreservas.fragment_editar_reserva;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MiCronogramaFragment extends Fragment {


    RecyclerView recyclerMiCronograma;
    ArrayList<ReservasCronograma> listaReservasCronograma;

    ProgressDialog progress;
    TextView txtNoSeHanencontrado;

    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;

    private TextView consulFechatxt;
    final Calendar myCalendar = Calendar.getInstance();

    
    public MiCronogramaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_mi_cronograma, container, false);
        recyclerMiCronograma= root.findViewById(R.id.idRecycleMiConograma);
        listaReservasCronograma=new ArrayList<>();
        recyclerMiCronograma=(RecyclerView) root.findViewById(R.id.idRecycleMiConograma);
        recyclerMiCronograma.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMiCronograma.setHasFixedSize(true);
        txtNoSeHanencontrado = root.findViewById(R.id.txtNoSeHanencontradoCronograma);
        request= Volley.newRequestQueue(getContext());
        consulFechatxt = root.findViewById(R.id.textViewSeleccionarFechaReservaCronograma);
        consulFechatxt.setText(getCurrentDay());

        showProgressDialog("Cargando reservas","Porfavor espere");
        consultarReservas(consulFechatxt.getText().toString());

        fechaPicker();


        return root;
    }

    private void consultarReservas(final String fechaConsulta) {
        final String id_barbero_que_consulta  =traerTelSQLITE();
        
        request = Volley.newRequestQueue(getContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consultarListaReservasDiaBarbero.php?";

        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.e("Response ", response + response.equalsIgnoreCase("[]") + response.isEmpty());
                hideProgressDialog();
                recyclerMiCronograma.setAdapter(null);
                listaReservasCronograma.clear();
                if(response.equals("[]")){
                    txtNoSeHanencontrado.setVisibility(View.VISIBLE);
                    //Toast.makeText(getContext(), "No se han encontrado Reservas", Toast.LENGTH_SHORT).show();
                }else {
                    txtNoSeHanencontrado.setVisibility(View.GONE);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ReservasCronograma reservas = null;
                    Log.e("URL RESPO", response.toString());


                    JSONArray json = jsonObject.optJSONArray("reservas");

                    int cantidadUsuarios = json.length();
                    try {

                        for (int i = 0; i < cantidadUsuarios; i++) {
                            reservas = new ReservasCronograma();
                            JSONObject jsonObject2 = null;
                            jsonObject2 = json.getJSONObject(i);
                            reservas.setFecha_r(jsonObject2.optString("fecha_reserva"));
                            reservas.setHI_r(jsonObject2.optString("hora_inicio_reserva"));
                            reservas.setHF_r(jsonObject2.optString("hora_fin_reserva"));
                            reservas.setNombre_cliente(jsonObject2.optString("nomb_cliente"));
                            reservas.setId_cliente(jsonObject2.optString("id_fbCliente"));

                            listaReservasCronograma.add(reservas);
                            Log.e("Error", json.getJSONObject(i) + "");
                        }


                        ReservasCronogramaAdapters adapter = new ReservasCronogramaAdapters(listaReservasCronograma, getContext());
                        recyclerMiCronograma.setAdapter(adapter);


                        /////////////////////////////////
                        final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                return true;
                            }
                        });

                        recyclerMiCronograma.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                            @Override
                            public void onRequestDisallowInterceptTouchEvent(boolean b) {

                            }

                            @Override
                            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                                 try {
                                   View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                                    if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                                        int position = recyclerView.getChildAdapterPosition(child);

                                        Bundle data = new Bundle();
                                        data.putString("fechaR",listaReservasCronograma.get(position).getFecha_r());
                                        data.putString("HI",listaReservasCronograma.get(position).getHI_r());
                                        data.putString("HF",listaReservasCronograma.get(position).getHF_r());
                                        data.putString("nbr", listaReservasCronograma.get(position).getNombre_cliente());
                                        data.putString("idBR", listaReservasCronograma.get(position).getId_cliente());

                                        //data.putString("nombreBarber",listaReservas.get(position).getNombre_barbero_r());
                                        // eliminarReserva(listaReservas.get(position).getFecha_r(),listaReservas.get(position).getHI_r(),traerTelSQLITE());

                                        String fechaReserva= listaReservasCronograma.get(position).getFecha_r();


                                        //Hoy
                                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                                        Date dtHOY = new Date();

                                        //Reserva
                                        Date dt=convertirFechaStrin(fechaReserva);


                                        String strHoye = sdf.format(dtHOY);
                                        String strRv = sdf.format(dt);

                                        Log.e("Estring igual", strHoye  + " " + strRv + " com " + strHoye.equals(strRv));

                                        Log.e("Fecha hoy ", dtHOY.toString());
                                        Log.e("Fecha reserva  ", dt.toString());
                                        Log.e("COOMPARACION ", String.valueOf(dtHOY.compareTo(dt)));
                                        Log.e("iff  ", (dtHOY.compareTo(dt) > 0) + "" );

                                        //Si hoy es mas grande que la fecha de la reserva, quiere decir que ya la reserva paso y no se puede editar
                                        if(dtHOY.compareTo(dt) >= 0 || strHoye.equals(strRv)){
                                            if( strHoye.equals(strRv)){
                                                Fragment miFragment = new fragment_editar_reserva();
                                                miFragment.setArguments(data);
                                                getFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();
                                            }else {
                                                Toast.makeText(getContext(), "La reserva ya vencio", Toast.LENGTH_SHORT).show();
                                            }
                                        }else {
                                            Fragment miFragment = new fragment_editar_reserva();
                                            miFragment.setArguments(data);
                                            getFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();
                                        }
                                        return true;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                return false;
                            }

                            @Override
                            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

                            }
                        });


                    } catch (JSONException e) {
                        hideProgressDialog();
                        e.printStackTrace();
                        Toast.makeText(getContext(), "No se ha podido establecer conexión con el servidor" +
                                " " + response, Toast.LENGTH_LONG).show();
                        progress.hide();
                    }

                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                txtNoSeHanencontrado.setVisibility(View.VISIBLE);
                txtNoSeHanencontrado.setText("Error de conexión");
                hideProgressDialog();
                Toast.makeText(getContext(), "No se ha podido establecer conexión con el servidor" , Toast.LENGTH_LONG).show();
                Log.e("Response ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("idA", id_barbero_que_consulta);
                parameters.put("frv", fechaConsulta);
                return parameters;
            }

        };
        Log.e("Cliente a consultar", id_barbero_que_consulta);
        request.add(stringRequest);
    }

    private String getCurrentDay() {
// Obtiene la fecha actual en string
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime ;
    }

    private Date convertirFechaStrin(String stringFecha) {
        Date date = null;
        String pattern = "MM/dd/yy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        try {
            date = simpleDateFormat.parse(stringFecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

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


    ///Dialog Progress
    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(getContext(), titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}
    private void fechaPicker() {

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
                consulFechatxt.setText(sdf.format(myCalendar.getTime()));

                //Cuando se cambia la fecha
                showProgressDialog("Consultando", "Por favor espere... ... ");
                consultarReservas(consulFechatxt.getText().toString());
            }

        };


        consulFechatxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                consultarReservas(consulFechatxt.getText().toString());
            }
        });
    }

/*    private boolean reservaCancelable(Date dateHActual, Date dateHReserva, String f_res){

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
    }*/

}
