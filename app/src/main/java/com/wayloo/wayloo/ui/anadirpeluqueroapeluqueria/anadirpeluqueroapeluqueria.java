package com.wayloo.wayloo.ui.anadirpeluqueroapeluqueria;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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
import com.google.firebase.auth.FirebaseAuth;
import com.wayloo.wayloo.MainActivity;
import com.wayloo.wayloo.MainActivityProfile;
import com.wayloo.wayloo.MainActivityZoomPeluquero;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.adapters.BarberosAdapters;
import com.wayloo.wayloo.adapters.clienteAdapters;
import com.wayloo.wayloo.entidades.BarberosU;
import com.wayloo.wayloo.entidades.cliente;
import com.wayloo.wayloo.ui.home.HomeViewModel;
import com.wayloo.wayloo.zoomBarberia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class anadirpeluqueroapeluqueria extends Fragment implements com.android.volley.Response.Listener<JSONObject>, Response.ErrorListener {


    RecyclerView recyclerBarberos;
    ArrayList<cliente> listaClientes;
    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    ProgressDialog progress;
    private Button btAnadir;
    EditText etCelular;

    public anadirpeluqueroapeluqueria() {
        // Required empty public constructor
    }

    public static anadirpeluqueroapeluqueria newInstance(String param1, String param2) {
        anadirpeluqueroapeluqueria fragment = new anadirpeluqueroapeluqueria();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_anadirpeluqueroapeluqueria, container, false);
        listaClientes = new ArrayList<>();
        etCelular = view.findViewById(R.id.etTelefonoAnadir);
        recyclerBarberos = view.findViewById(R.id.idRecycleBarberosAgregar);
        recyclerBarberos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBarberos.setHasFixedSize(true);
        request = Volley.newRequestQueue(getContext());

        btAnadir= view.findViewById(R.id.butonBuscarBarbero);

        btAnadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etCelular.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Error el campo no puede estar vacio.", Toast.LENGTH_SHORT).show();
                }else {
                    listaClientes.clear();
                    recyclerBarberos.setAdapter(null);
                    recyclerBarberos.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerBarberos.setHasFixedSize(true);
                    cargarWebServices(etCelular.getText().toString());
                }
            }
        });



        return view;
    }



    private void cargarWebServices(String telefono) {
        progress = new ProgressDialog(getContext());
        progress.setMessage("Consultando Barberos disponibles");
        progress.show();
        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/consultarListaUsuariosPorTelefono.php?telefono=" + telefono;
        Log.e("URL",url);
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.add(jsonObjectRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        progress.hide();

        Toast.makeText(getActivity(), "No se encontraron usuarios", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onResponse(JSONObject response) {//1er onresponse lista de todos barberos 1,2,3,4
        cliente usuario=null;

        JSONArray json=response.optJSONArray("usuarios");

        try {

            for (int i=0;i<json.length();i++){
                usuario=new cliente();
                JSONObject jsonObject=null;
                jsonObject=json.getJSONObject(i);


                usuario.setNombre(jsonObject.optString("nombre_usuario"));
                usuario.setApellido(jsonObject.optString("apellido_usuario"));
                usuario.setTelefono(jsonObject.optString("tel_usuario"));
                usuario.setFireB(jsonObject.optString("id_firebase"));

                listaClientes.add(usuario);



                final GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                });
                recyclerBarberos.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override
                    public void onRequestDisallowInterceptTouchEvent(boolean b) {

                    }

                    @Override
                    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                        try {
                            View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                            if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {

                                int position = recyclerView.getChildAdapterPosition(child);

                                //
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Va a añadir a "+ listaClientes.get(position).getNombre() );
                                builder.setMessage("¿Esta seguro?");
                                builder.setCancelable(false);
                                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Bundle datosRecuperados = getArguments();

                                        String NIT = datosRecuperados.getString("NIT");
                                        actualizarRemotoClASBarber(listaClientes.get(position).getTelefono(), NIT);
                                    }
                                });

                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                builder.show();

                                return true;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        return false;
                    }

                    @Override
                    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

                    }});

            }
            progress.hide();
            clienteAdapters adapter = new clienteAdapters(listaClientes,getContext());
            recyclerBarberos.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "No se ha podido establecer conexión con el servidor" +
                    " "+response, Toast.LENGTH_LONG).show();
            progress.hide();
        }
    }


    private void actualizarRemotoClASBarber(String tel, String NitPertece) {


        if (tel.equalsIgnoreCase("")) {
            Toast.makeText(getContext(), "ERROR LOS CAMPOS NO PUEDEN ESTAR VACIOS", Toast.LENGTH_LONG).show();
        } else {
            request = Volley.newRequestQueue(getContext());

            String ip =getString(R.string.ip_way);

            String url = ip + "/consultas/updateClAsBar.php?";

            Log.e("URL DEL POST", url);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("Response Update go ", response);
                    if(response.equalsIgnoreCase("registrado")) {
                        Toast.makeText(getContext(), "Barbero añadido", Toast.LENGTH_SHORT).show();
                        listaClientes.clear();
                        recyclerBarberos.setAdapter(null);
                        cargarWebServices(etCelular.getText().toString());
                    }else{
                        Toast.makeText(getContext(), "Error de conexión verifique su red.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(getContext(), "Error de conexión verifique su internet.", Toast.LENGTH_SHORT).show();
                    Log.e("Response Update ", error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("telefono", tel);
                    parameters.put("NIT", NitPertece);

                    return parameters;
                }

            };
            request.add(stringRequest);


        }


    }


}