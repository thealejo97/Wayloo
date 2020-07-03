package com.wayloo.wayloo.ui.mispeluquerias;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.MainLogginActivity;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.adapters.UsuariosAdapters;
import com.wayloo.wayloo.entidades.Usuario;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.anadirpeluqueria.AnadirPeluqueriaFragment;
import com.wayloo.wayloo.ui.editarbarber.EditarBarberiaFragment;
import com.wayloo.wayloo.ui.mispeluqueros.mispeluquerosfragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MisPeluqueriasFragment extends Fragment implements com.android.volley.Response.Listener<JSONObject>, Response.ErrorListener  {

    RecyclerView recyclerMisPeluquerias;
    ArrayList<Usuario> listaPeluquerias;

    ProgressDialog progress;

    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    private Context mContext;
    private Button btnAnadirPeluqueria;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mispeluquerias, container, false);


        listaPeluquerias=new ArrayList<>();
        recyclerMisPeluquerias=(RecyclerView) root.findViewById(R.id.idRecycleMisPeluquerias);
        btnAnadirPeluqueria = (Button) root.findViewById(R.id.anadirButonBarber);
        recyclerMisPeluquerias.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerMisPeluquerias.setHasFixedSize(true);
        request= Volley.newRequestQueue(getContext());

        cargarWebServices();
        btnAnadirPeluqueria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment miFragment = null;
                miFragment = new AnadirPeluqueriaFragment();
                miFragment.setArguments(null);
                getFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();

            }
        });



        return root;
    }


    private void cargarWebServices() {
        progress=new ProgressDialog(getContext());
        progress.setMessage("Consultando Peluquerias");
        progress.show();
        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/consultarListaUsuariosAdm.php?idA="+traerTELSQLITE();
        Log.e("req url",url);
        jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }

    private String traerTELSQLITE(){
        UsuariosSQLiteHelper usdbh =
                new UsuariosSQLiteHelper(mContext, "dbUsuarios", null, 1);
        String name= "UnKnow User";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT id_usu FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                name= c.getString(0);

            } while(c.moveToNext());
        }
        return name;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e("Error peluquerias",error.toString());
        progress.hide();
    }

    @Override
    public void onResponse(JSONObject response) {
        Usuario usuario=null;
        Log.e("URL RESPO", response.toString());

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
                listaPeluquerias.add(usuario);
                Log.e("Error", json.getJSONObject(i) +"");
            }
            progress.hide();


            UsuariosAdapters adapter=new UsuariosAdapters(listaPeluquerias, getContext());
            recyclerMisPeluquerias.setAdapter(adapter);


            /////////////////////////////////
            final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });

            recyclerMisPeluquerias.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
                            data.putString("NIT",listaPeluquerias.get(position).getNIT());
                            data.putString("telefono",listaPeluquerias.get(position).getTelefono());
                            data.putString("nombre",listaPeluquerias.get(position).getNombre());
                            data.putString("calificacion",listaPeluquerias.get(position).getCalificacion());
                            data.putString("ciudad",listaPeluquerias.get(position).getCiudad());
                            data.putString("direccion",listaPeluquerias.get(position).getDireccion());
                            //Editando aquiii
                            Fragment miFragment = null;
                            miFragment = new mispeluquerosfragment();
                            miFragment.setArguments(data);
                            getFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(getContext(), titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}
}