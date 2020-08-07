package com.wayloo.wayloo.ui.editarbarbero;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.wayloo.wayloo.MainActivity;
import com.wayloo.wayloo.MainActivityProfile;
import com.wayloo.wayloo.MainActivityZoomPeluquero;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.engine.engine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;


public class EditarBarberoFragment extends Fragment {

    private RequestQueue request;
    Spinner snHoraInicio;
    Spinner snHoraFin;
    Button btnActualizar;
    TextView tvL,tvM,tvMi,tvJ,tvV,tvS,tvD, desvinvularBR;

    public void pintar_dia(TextView txCambio){
        txCambio.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {

                if(txCambio.getCurrentTextColor() == Color.parseColor("#00FF00") ){
                    txCambio.setTextColor(Color.parseColor("#EC2222"));
                }else {
                    txCambio.setTextColor(Color.parseColor("#00FF00"));
                }
            }
        });
    }

    public void pintar_por_dia(String diaPintar){

        tvL.setTextColor(Color.parseColor("#00FF00"));
        tvM.setTextColor(Color.parseColor("#00FF00"));
        tvMi.setTextColor(Color.parseColor("#00FF00"));
        tvJ.setTextColor(Color.parseColor("#00FF00"));
        tvV.setTextColor(Color.parseColor("#00FF00"));
        tvS.setTextColor(Color.parseColor("#00FF00"));
        tvD.setTextColor(Color.parseColor("#00FF00"));

        String[] dia = diaPintar.split(",");
        for(int i=0;i <= dia.length;i++) {
            Log.e("Dia a pintar de blanco", dia[i]);
            if (dia[i].equalsIgnoreCase("l")) {
                tvL.setTextColor(Color.parseColor("#EC2222"));
            }
            if (dia[i].equalsIgnoreCase("m")) {
                tvM.setTextColor(Color.parseColor("#EC2222"));
            }
            if (dia[i].equalsIgnoreCase("mi")) {
                tvMi.setTextColor(Color.parseColor("#EC2222"));
            }
            if (dia[i].equalsIgnoreCase("j")) {
                tvJ.setTextColor(Color.parseColor("#EC2222"));
            }
            if (dia[i].equalsIgnoreCase("v")) {
                tvV.setTextColor(Color.parseColor("#EC2222"));
            }
            if (dia[i].equalsIgnoreCase("s")) {
                tvS.setTextColor(Color.parseColor("#EC2222"));
            }
            if (dia[i].equalsIgnoreCase("d")) {
                tvD.setTextColor(Color.parseColor("#EC2222"));
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_editar_barbero, container, false);

        tvL = root.findViewById(R.id.tvL);
        tvM = root.findViewById(R.id.tvM);
        tvMi = root.findViewById(R.id.tvMi);
        tvJ = root.findViewById(R.id.tvJ);
        tvV = root.findViewById(R.id.tvV);
        tvS = root.findViewById(R.id.tvS);
        tvD = root.findViewById(R.id.tvD);
        desvinvularBR = root.findViewById(R.id.desvincularBarbero);

        pintar_dia(tvL);
        pintar_dia(tvM);
        pintar_dia(tvMi);
        pintar_dia(tvJ);
        pintar_dia(tvV);
        pintar_dia(tvS);
        pintar_dia(tvD);

       consultaDiasLaboralesBarbero(new engine().getInternoTelSQLITE(getContext()));
        snHoraInicio = root.findViewById(R.id.horaI_spinner);
        snHoraFin = root.findViewById(R.id.horaF_spinner);

        ArrayList<String> ArrHoras = new ArrayList<>();
        for(int i = 0 ; i<24; i++){
            ArrHoras.add(i+":00");
            ArrHoras.add(i+":30");
        }

        ArrayAdapter adaptadorHoras = new ArrayAdapter(getContext(),R.layout.color_spinner_layout, ArrHoras) ;

        adaptadorHoras.setDropDownViewResource(R.layout.spinner_dropdown);
        snHoraInicio.setAdapter(adaptadorHoras);
        snHoraFin.setAdapter(adaptadorHoras);

        btnActualizar = root.findViewById(R.id.editarBarbero);

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                consultarReservasAfectadas();
            }
        });
        desvinvularBR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consultarReservasAfectadasELIMINACION();
            }
        });

        return root;
    }

    private String obtenerDiasNoLaborales() {
        String diasN = "";
        if(tvL.getCurrentTextColor() == Color.parseColor("#EC2222") ) {
            diasN += "l,";
        }
            if(tvM.getCurrentTextColor() == Color.parseColor("#EC2222") ) {
                diasN += "m,";
            }
                if(tvMi.getCurrentTextColor() == Color.parseColor("#EC2222") ){
                    diasN += "mi,";
                }
                    if(tvJ.getCurrentTextColor() == Color.parseColor("#EC2222") ) {
                        diasN += "j,";
                    }
                        if(tvV.getCurrentTextColor() == Color.parseColor("#EC2222") ) {
                            diasN += "v,";
                        }
                        if(tvS.getCurrentTextColor() == Color.parseColor("#EC2222") ) {
                            diasN += "s,";
                        }
                        if(tvD.getCurrentTextColor() == Color.parseColor("#EC2222") ){
                                    diasN += "d";
                                }

        return diasN;
    }

    private void consultarReservasAfectadasELIMINACION() {

        String horaI = snHoraInicio.getSelectedItem().toString();
        String horaF = snHoraFin.getSelectedItem().toString();
        String dias_no = obtenerDiasNoLaborales();
        Log.e("DIASNO ", dias_no+" "+horaI+" "+horaF);

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
                            ejecutarELIMINAR();
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
                                    ejecutarELIMINAR();
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
                parameters.put("telBarbero", new engine().getInternoTelSQLITE(getContext()));
                parameters.put("key", "barbero");

                return parameters;
            }
        };
        request.add(stringRequest);

    }

    private void consultarReservasAfectadas() {
        String horaI = snHoraInicio.getSelectedItem().toString();
        String horaF = snHoraFin.getSelectedItem().toString();
        String dias_no = obtenerDiasNoLaborales();

        Log.e("DIASNO ", dias_no+" "+horaI+" "+horaF);

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
                            ejecutarUpdate(horaI, horaF, dias_no);
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
                            builder.setMessage("Atención, aun tiene reservas sin cumplir, al cambiar el horario se cancelaran " + numRes + " reservas ¿Desea continuar?");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ejecutarUpdate(horaI, horaF, dias_no);
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
                parameters.put("telBarbero", new engine().getInternoTelSQLITE(getContext()));
                parameters.put("HI", horaI);
                parameters.put("HF", horaF);
                parameters.put("DIASN", dias_no);
                parameters.put("key", "barbero");

                return parameters;
            }
        };
        request.add(stringRequest);

    }

    private void ejecutarELIMINAR(){

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
                    new engine().reiniciarApp(getContext());
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
                parameters.put("telBarbero", new engine().getInternoTelSQLITE(getContext()));

                return parameters;
            }

        };
        request.add(stringRequest);

    }

    private void ejecutarUpdate(String horaI, String horaF, String dias_no) {

            request = Volley.newRequestQueue(getContext());

            String ip =getString(R.string.ip_way);

            String url = ip + "/consultas/updateBarbero.php?";

            Log.e("URL DEL POST", url);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("Response Update go ", response);
                    if(response.equalsIgnoreCase("Registra")) {
                        Toast.makeText(getContext(), "Actualizado", Toast.LENGTH_SHORT).show();
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
                    parameters.put("telBarbero", new engine().getInternoTelSQLITE(getContext()));
                    parameters.put("HI", horaI);
                    parameters.put("HF", horaF);
                    parameters.put("dias_no", dias_no);
                    
                    return parameters;
                }

            };
            request.add(stringRequest);

    }

    private void consultaDiasLaboralesBarbero(final String telBarberoDiaLab) {

        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consultarDiasLaborales.php?";

        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            private String diasLaborales;

            @Override
            public void onResponse(String response) {
                Log.e("Response ", response +"   "+ response.equalsIgnoreCase("[]") + response.isEmpty());
                if (response.equalsIgnoreCase("[]")) {
                    Toast.makeText(getContext(), "Error de consulta de dias", Toast.LENGTH_SHORT).show();
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
Log.e(" Cositas", snHoraInicio.getAdapter().getCount()+ " "+ jsonObject2.optString("h_inicio")+ " "+jsonObject2.optString("h_fin")
+"  "+ diasLaborales);
                            for(int j = 0; j<snHoraInicio.getAdapter().getCount(); j++) {
                                Log.e(" Cosita s 2" , snHoraInicio.getItemAtPosition(j) +"  "+jsonObject2.optString("h_inicio") +"  "+
                                        snHoraInicio.getItemAtPosition(j).equals(jsonObject2.optString("h_inicio")));
                                if(snHoraInicio.getItemAtPosition(j).equals(jsonObject2.optString("h_inicio"))){
                                    snHoraInicio.setSelection(j);
                                    break;
                                }
                            }
                            for(int j = 0; j<snHoraFin.getAdapter().getCount(); j++) {
                                Log.e(" Cosita s 3 2" , snHoraFin.getItemAtPosition(j) +"  "+jsonObject2.optString("h_fin") +"  "+
                                        snHoraFin.getItemAtPosition(j).equals(jsonObject2.optString("h_fin")));
                                if(snHoraFin.getItemAtPosition(j).equals(jsonObject2.optString("h_fin"))){
                                    snHoraFin.setSelection(j);
                                    break;
                                }
                            }

                            Log.e("dia Laboral ", diasLaborales);
                        }
                        pintar_por_dia(diasLaborales);

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