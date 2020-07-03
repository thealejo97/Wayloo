package com.wayloo.wayloo.ui.mispeluqueros;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.adapters.BarberosAdapters;
import com.wayloo.wayloo.adapters.UsuariosAdapters;
import com.wayloo.wayloo.entidades.BarberosU;
import com.wayloo.wayloo.entidades.Usuario;
import com.wayloo.wayloo.ui.anadirpeluqueroapeluqueria.anadirpeluqueroapeluqueria;
import com.wayloo.wayloo.ui.home.HomeViewModel;
import com.wayloo.wayloo.zoomBarberia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class mispeluquerosfragment extends Fragment implements com.android.volley.Response.Listener<JSONObject>, Response.ErrorListener  {

    private HomeViewModel homeViewModel;
    RecyclerView recyclerPeluquero;
    ArrayList<BarberosU> listaBarberos;

    ProgressDialog progress;
    String NIT;

    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    TextView tvMispq,tvDir;


    public mispeluquerosfragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_mispeluquerosfragment,container,false);
//////////////////////////////////////////////////////////////////////////////////////////
        listaBarberos=new ArrayList<>();
        Bundle datosRecuperados = getArguments();


        // Y ahora puedes recuperar usando get en lugar de put
        NIT = datosRecuperados.getString("NIT");
        tvMispq = vista.findViewById(R.id.tvTituloMisPeluqueros);
        tvDir = vista.findViewById(R.id.tvTituDir);
        tvMispq.setText((datosRecuperados.getString("nombre") +"  ").toUpperCase());
        tvDir.setText(datosRecuperados.getString("direccion") + ", "+datosRecuperados.getString("ciudad")  );
        recyclerPeluquero=(RecyclerView) vista.findViewById(R.id.idRecycleMisPeluqueros);
        recyclerPeluquero.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerPeluquero.setHasFixedSize(true);
        request= Volley.newRequestQueue(getContext());


        cargarWebServices();


        ImageView btnAn = vista.findViewById(R.id.anadirPeluqueroaPeluqueriaBTN);
        btnAn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();

                data.putString("NIT",NIT);
                Fragment miFragment = new anadirpeluqueroapeluqueria();
                miFragment.setArguments(data);
                getFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();
                // Fragment que va a remplazar
            }
        });


        return vista;
    }

    private void cargarWebServices() {
        progress=new ProgressDialog(getContext());
        progress.setMessage("Consultando Peluquerias");
        progress.show();
        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/consultarListaBarberiaBarberos.php?NIT=" + NIT;

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
        BarberosU barberosU=null;

        JSONArray json=response.optJSONArray("peluquerias");

        int cantidadUsuarios =json.length();
        try {

            for (int i=0;i < cantidadUsuarios;i++){
                barberosU=new BarberosU();
                JSONObject jsonObject=null;
                jsonObject=json.getJSONObject(i);
                barberosU.setNombre(jsonObject.optString("nombre_p"));
                barberosU.setTelefono(jsonObject.optString("tel_p"));
                barberosU.setApellido_P(jsonObject.optString("apellido_p"));
                barberosU.seth_inicio(jsonObject.optString("h_inicio"));
                barberosU.seth_fin(jsonObject.optString("h_fin"));
                barberosU.setNIT_pertenese(jsonObject.optString("nit_peluqueria_pertenese"));
                barberosU.setFireB(jsonObject.optString("fire_b"));
                barberosU.setCalificacion(jsonObject.optString("calificacion_peluquero"));
                listaBarberos.add(barberosU);
                Log.e("Error", json.getJSONObject(i) +"");
            }
            progress.hide();


            BarberosAdapters adapter=new BarberosAdapters(listaBarberos, getContext());
            recyclerPeluquero.setAdapter(adapter);


            /////////////////////////////////
            final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });

            recyclerPeluquero.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean b) {

                }

                @Override
                public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                    try {
                        View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                        if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {

                            int position = recyclerView.getChildAdapterPosition(child);

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