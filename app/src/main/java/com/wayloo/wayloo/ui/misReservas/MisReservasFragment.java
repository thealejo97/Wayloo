package com.wayloo.wayloo.ui.misReservas;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.adapters.ReservasAdapters;
import com.wayloo.wayloo.entidades.Reservas;
import com.wayloo.wayloo.ui.editarreservas.fragment_editar_reserva;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.Utilidades;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MisReservasFragment extends Fragment {

    RecyclerView recyclerMisReservas;
    ArrayList<Reservas> listaReservas;

    ProgressDialog progress;
    TextView txtNoSeHanencontrado;

    RequestQueue request;
    private TextView consulFechatxt;
    final Calendar myCalendar = Calendar.getInstance();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_misreservas, container, false);
        recyclerMisReservas= root.findViewById(R.id.idRecycleMisReservas);
        listaReservas=new ArrayList<>();
        recyclerMisReservas=(RecyclerView) root.findViewById(R.id.idRecycleMisReservas);
        recyclerMisReservas.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMisReservas.setHasFixedSize(true);
        txtNoSeHanencontrado = root.findViewById(R.id.txtNoSeHanencontrado);
        request= Volley.newRequestQueue(getContext());
        consulFechatxt = root.findViewById(R.id.textViewSeleccionarFechaReserva);
        consulFechatxt.setText(getCurrentDay());

        showProgressDialog("Cargando reservas","Porfavor espere");
        consultarReservas(consulFechatxt.getText().toString());

        fechaPicker();


        return root;
    }

    private void consultarReservas(final String fechaConsulta) {
        final String id_cliente_reserva  =traerTelSQLITE();
        request = Volley.newRequestQueue(getContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consultarListaReservas.php?";

        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                Log.e("Response ", response + response.equalsIgnoreCase("[]") + response.isEmpty());
                hideProgressDialog();
                recyclerMisReservas.setAdapter(null);
                listaReservas.clear();
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
                    Reservas reservas = null;
                    Log.e("URL RESPO", response.toString());


                    JSONArray json = jsonObject.optJSONArray("reservas");

                    int cantidadUsuarios = json.length();
                    try {

                        for (int i = 0; i < cantidadUsuarios; i++) {
                            reservas = new Reservas();
                            JSONObject jsonObject2 = null;
                            jsonObject2 = json.getJSONObject(i);
                            reservas.setFecha_r(jsonObject2.optString("fecha_reserva"));
                            reservas.setHI_r(jsonObject2.optString("hora_inicio_reserva"));
                            reservas.setHF_r(jsonObject2.optString("hora_fin_reserva"));
                            reservas.setNombre_barbero_r(jsonObject2.optString("nomb_peluquero"));
                            reservas.setId_barbero(jsonObject2.optString("id_fbBarbero"));

                            listaReservas.add(reservas);
                            Log.e("Error", json.getJSONObject(i) + "");
                        }


                        ReservasAdapters adapter = new ReservasAdapters(listaReservas, getContext());
                        recyclerMisReservas.setAdapter(adapter);


                        /////////////////////////////////
                        final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                return true;
                            }
                        });

                        recyclerMisReservas.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
                                        data.putString("fechaR",listaReservas.get(position).getFecha_r());
                                        data.putString("HI",listaReservas.get(position).getHI_r());
                                        data.putString("HF",listaReservas.get(position).getHF_r());
                                        data.putString("nbr", listaReservas.get(position).getNombre_barbero_r());
                                        data.putString("idBR", listaReservas.get(position).getId_barbero());

                                        //data.putString("nombreBarber",listaReservas.get(position).getNombre_barbero_r());
                                       // eliminarReserva(listaReservas.get(position).getFecha_r(),listaReservas.get(position).getHI_r(),traerTelSQLITE());

                                        String fechaReserva= listaReservas.get(position).getFecha_r();


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
                                                Bundle datosAEnviar = new Bundle();
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
                parameters.put("idA", id_cliente_reserva);
                parameters.put("frv", fechaConsulta);
                return parameters;
            }

        };
        Log.e("Cliente a consultar", id_cliente_reserva + " "+fechaConsulta);
        request.add(stringRequest);
    }

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

   private void eliminarReserva(final String f_res,final String HI_re,final String idC_re ) {


            if (f_res.equalsIgnoreCase("")) {
                Toast.makeText(getContext(), "ERROR ELIMINANDO, POR FAVOR VERIFIQUE", Toast.LENGTH_LONG).show();
            } else {

                final Utilidades utl = new Utilidades();
                //Si no son iguales
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirmación eliminación de reserva!");
                builder.setMessage("Va a eliminar la reserva del sistema, ¿Desea continuar?");
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

                                Fragment miFragment = null;
                                miFragment = new MisReservasFragment();
                                miFragment.setArguments(null);
                                getFragmentManager().beginTransaction().addToBackStack(getClass().getName()).replace(R.id.content_main, miFragment).commit();
                                Toast.makeText(getContext(), "Barberia Eliminado", Toast.LENGTH_SHORT).show();
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
                                parameters.put("idcreserva", idC_re);
                                return parameters;
                            }

                        };
                        Log.e("Datos a eliminar R", f_res +" "+ HI_re +" "+ idC_re);
                        request.add(stringRequest);
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

    private String getCurrentDay() {
// Obtiene la fecha actual en string
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime ;
    }

}