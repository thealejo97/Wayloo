package com.wayloo.wayloo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.adapters.BarberosAdapters;
import com.wayloo.wayloo.entidades.BarberosU;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class zoomBarberia extends AppCompatActivity implements com.android.volley.Response.Listener<JSONObject>, Response.ErrorListener {

    TextView txnombre;
    TextView txtelefono;
    TextView txciudad;
    TextView txdireccion;
    TextView txcalificacion;

    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;

    String telefono;
    String nombre;
    String ciudad;
    String direccion;
    String calificacion;
    String NIT;

    RecyclerView recyclerBarberos;
    ArrayList<BarberosU> listaBarberos;
    ProgressDialog progress;
    RatingBar rBar = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_barberia);
        telefono = getIntent().getStringExtra("telefono");
        nombre = getIntent().getStringExtra("nombre");
        ciudad = getIntent().getStringExtra("ciudad");
        direccion = getIntent().getStringExtra("direccion");
        calificacion = getIntent().getStringExtra("calificacion");
        NIT = getIntent().getStringExtra("NIT");

        txnombre = findViewById(R.id.textViewNombre);
        txtelefono = findViewById(R.id.textViewTel);
        txciudad = findViewById(R.id.textViewCiudadP);
        txdireccion = findViewById(R.id.textViewDireccion);
        txcalificacion = findViewById(R.id.textViewEstrellas);
        rBar = findViewById(R.id.ratingBarBarberia);
        rBar.setRating(Integer.parseInt(calificacion));

        txtelefono.setText(telefono);
        txnombre.setText(nombre);
        txciudad.setText(ciudad);
        txdireccion.setText(direccion);
        txcalificacion.setText(calificacion);

        listaBarberos = new ArrayList<>();

        recyclerBarberos = findViewById(R.id.idRecycleBarber);
        recyclerBarberos.setLayoutManager(new LinearLayoutManager(this));
        recyclerBarberos.setHasFixedSize(true);
        request = Volley.newRequestQueue(this);
        cargarWebServices();
    }

    private void cargarWebServices() {
        progress = new ProgressDialog(this);
        progress.setMessage("Consultando Barberos disponibles");
        progress.show();
        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/consultarListaBarberiaBarberos.php?NIT=" + NIT;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, this, this);
        request.add(jsonObjectRequest);
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        progress.hide();
        TextView txTituloBarber = findViewById(R.id.tituBarberosDisponibles);
        txTituloBarber.setVisibility(View.GONE);
        Toast.makeText(zoomBarberia.this, "La peluqueria no tiene barberos disponibles", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onResponse(JSONObject response) {//1er onresponse lista de todos barberos 1,2,3,4
        BarberosU usuario=null;

        JSONArray json=response.optJSONArray("peluquerias");

        try {

            for (int i=0;i<json.length();i++){
                usuario=new BarberosU();
                JSONObject jsonObject=null;
                jsonObject=json.getJSONObject(i);


                usuario.setNombre(jsonObject.optString("nombre_p"));
                usuario.setApellido_P(jsonObject.optString("apellido_p"));
                usuario.setTelefono(jsonObject.optString("tel_p"));
                usuario.setNIT_pertenese(jsonObject.optString("nit_peluqueria_pertenese"));
                usuario.seth_inicio(jsonObject.optString("h_inicio"));
                usuario.seth_fin(jsonObject.optString("h_fin"));
                usuario.setCalificacion(jsonObject.optString("calificacion_peluquero"));
                usuario.setFireB(jsonObject.optString("fire_b"));

                listaBarberos.add(usuario);



                final GestureDetector mGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
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
                                Intent intent = new Intent(getApplicationContext(), MainActivityZoomPeluquero.class);
                                intent.putExtra("nombre",listaBarberos.get(position).getNombre());
                                intent.putExtra("apellido",listaBarberos.get(position).getApellido_P());
                                intent.putExtra("telefono",listaBarberos.get(position).getTelefono());
                                intent.putExtra("NITPERT",listaBarberos.get(position).getNIT_pertenese());
                                intent.putExtra("h_inicio",listaBarberos.get(position).geth_inicio());
                                intent.putExtra("h_fin",listaBarberos.get(position).geth_fin());
                                intent.putExtra("calificacion",listaBarberos.get(position).getCalificacion());
                                intent.putExtra("fireB",listaBarberos.get(position).getFireB());
                                intent.putExtra("barberiaDelBarbero", NIT);

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

                    }});

            }
            progress.hide();
            BarberosAdapters adapter = new BarberosAdapters(listaBarberos,getApplicationContext());
            recyclerBarberos.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "No se ha podido establecer conexión con el servidor" +
                    " "+response, Toast.LENGTH_LONG).show();
            progress.hide();
        }
    }



}




