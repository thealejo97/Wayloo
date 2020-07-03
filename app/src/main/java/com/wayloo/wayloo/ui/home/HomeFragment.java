package com.wayloo.wayloo.ui.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.adapters.UsuariosAdapters;
import com.wayloo.wayloo.entidades.Usuario;
import com.wayloo.wayloo.zoomBarberia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements com.android.volley.Response.Listener<JSONObject>, Response.ErrorListener  {

    RecyclerView recyclerUsuarios;
    ArrayList<Usuario> listaUsuarios;

    ProgressDialog progress;

    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


//////////////////////////////////////////////////////////////////////////////////////////
        listaUsuarios=new ArrayList<>();

        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_home,container,false);


        recyclerUsuarios=(RecyclerView) vista.findViewById(R.id.idRecycle);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerUsuarios.setHasFixedSize(true);
        request= Volley.newRequestQueue(getContext());



        cargarWebServices();



        return vista;
    }

    private void cargarWebServices() {
        progress=new ProgressDialog(getContext());
        progress.setMessage("Consultando Peluquerias");
        progress.show();
        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/consultarListaUsuarios.php";

        jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }



    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("Error peluquerias",error.toString());
        progress.hide();
    }

    @Override
    public void onResponse(JSONObject response) {
        Usuario usuario=null;

        JSONArray json=response.optJSONArray("peluquerias");

        int cantidadUsuarios =json.length();
        try {

            for (int i=0;i < cantidadUsuarios;i++){
                usuario=new Usuario();
                JSONObject jsonObject=null;
                jsonObject=json.getJSONObject(i);
                usuario.setNit(jsonObject.optString("nit_peluqueria"));
                usuario.setTelefono(jsonObject.optString("telefono_peluqueria"));
                usuario.setNombre(jsonObject.optString("nombre_peluqueria"));
                usuario.setDireccion(jsonObject.optString("direccion_peluqueria"));
                usuario.setCiudad(jsonObject.optString("ciudad_peluqueria"));
                usuario.setCalificacion(jsonObject.optString("calificacion_peluqueria"));
                listaUsuarios.add(usuario);
                Log.e("Error", json.getJSONObject(i) +"");
            }
            progress.hide();


            UsuariosAdapters adapter=new UsuariosAdapters(listaUsuarios, getContext());
            recyclerUsuarios.setAdapter(adapter);


            /////////////////////////////////
            final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });

                recyclerUsuarios.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean b) {

                }

                @Override
                public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                    try {
                        View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                        if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {

                            int position = recyclerView.getChildAdapterPosition(child);
                            Intent intent = new Intent(getContext(), zoomBarberia.class);
                            intent.putExtra("NIT",listaUsuarios.get(position).getNIT());
                            intent.putExtra("telefono",listaUsuarios.get(position).getTelefono());
                            intent.putExtra("nombre",listaUsuarios.get(position).getNombre());
                            intent.putExtra("calificacion",listaUsuarios.get(position).getCalificacion());
                            intent.putExtra("ciudad",listaUsuarios.get(position).getCiudad());
                            intent.putExtra("direccion",listaUsuarios.get(position).getDireccion());

                            startActivity(intent);


                            return true;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    return false;
                }

                @Override
                public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

                }      });


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "No se ha podido establecer conexión con el servidor" +
                    " "+response, Toast.LENGTH_LONG).show();
            progress.hide();
        }}
    }