package com.wayloo.wayloo.ui.anadirpeluqueria;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wayloo.wayloo.MainActivityRegistarUser;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.mispeluquerias.MisPeluqueriasFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnadirPeluqueriaFragment extends Fragment {

    String Nit,Nombre,Dir,Tel,TelDueno,Ciud;
    EditText etNit,etNombre,EtDir,etTel;
    Spinner spCiud;
    Button btnRegistrarP;
    //Progress
    ProgressDialog progress;
    StringRequest stringRequestS;
    private RequestQueue request;
    private AnadirPeluqueriaModel anadirPeluqueriaModel;
    FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        anadirPeluqueriaModel =
                ViewModelProviders.of(this).get(AnadirPeluqueriaModel.class);
        View root = inflater.inflate(R.layout.fragment_anadirpeluqueria, container, false);

        etNit = root.findViewById(R.id.etxt_PeluqueriaNitAnadir);
        etNombre = root.findViewById(R.id.etxt_PeluqueriaNombreAnadir);
        EtDir = root.findViewById(R.id.etxt_peluqueriaDireccionAnadir);
        etTel = root.findViewById(R.id.etxt_PeluqueriaTelefonoAnadir);
        spCiud = root.findViewById(R.id.ciudad_spinnerAnadir);
        ArrayAdapter adapterAL = ArrayAdapter.createFromResource(getContext(),
                R.array.ciudades, R.layout.color_spinner_layout);
        adapterAL.setDropDownViewResource(R.layout.spinner_dropdown);
        spCiud.setAdapter(adapterAL);
        btnRegistrarP = root.findViewById(R.id.btnAnadirPeluqueria);
        btnRegistrarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Añadiendo Peluqueria","Por favor espere....");
                crearPeluqueria();
            }
        });

        return root;
    }

    private void crearPeluqueria() {
        //Inicio los datos de Pluqueria
        Nit = etNit.getText().toString();
        Nombre = etNombre.getText().toString();
        Dir = EtDir.getText().toString();
        Tel = etTel.getText().toString();
        Ciud = spCiud.getSelectedItem().toString();
        TelDueno = traerTelSQLITE();

        if( Nit.isEmpty() || Nombre.isEmpty() || Dir.isEmpty() || Tel.isEmpty() || TelDueno.isEmpty() || Ciud.isEmpty()  || Ciud.equals("Seleccione su ciudad")){
            Toast.makeText(getContext(), "Error, los campos no deben de estar vacios", Toast.LENGTH_SHORT).show();
        }else {


            request = Volley.newRequestQueue(getContext());
            //Lleno los demas datos relacionados con el barbero
            String ip =getString(R.string.ip_way);
            String url = ip + "/consultas/registrar_Barberia.php?";
            Log.e("URL DEL POST", url);

            stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    hideProgressDialog();
                    Log.e("RESPUESTA: ", "" + response);
                    if (response.trim().equalsIgnoreCase("registra")) {
                        Toast.makeText(getContext(), "Se ha la registrado la barberia con exito", Toast.LENGTH_SHORT).show();
                        Fragment miFragment = null;
                        miFragment = new MisPeluqueriasFragment();
                        getFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();
                    } else {
                        Toast.makeText(getContext(), "No se ha registrado la barberia", Toast.LENGTH_SHORT).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("RESPUESTA: ", "" + error);
                    Toast.makeText(getContext(), "No se ha podido conectar", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parametros = new HashMap<>();
                    parametros.put("nit_peluqueria", Nit);
                    parametros.put("nombre_peluqueria", Nombre);
                    parametros.put("direccion_peluqueria", Dir);
                    parametros.put("telefono_peluqueria", Tel);
                    parametros.put("ciudad_peluqueria", Ciud);
                    parametros.put("tel_usuario", TelDueno);
                    return parametros;
                }
            };

            request.add(stringRequestS);
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

    @Override
    public void onStart() {
        super.onStart();

//  Se coloca el array de las ciudades dependiendo de las que esten en el FireBaseRemoteConfig

        ArrayList<String> ArraylistCiudadesFRstr = new ArrayList<>();

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String, Object> defaultData = new HashMap<>();
        defaultData.put("ciudadesAc","Cali,Jamundi");
        defaultData.put("datosPruebaPapu","nadaa");
        defaultData.put("playurl","Cali,Jamundi");
        defaultData.put("versionname","1.0");

        Task<Void> fetch = firebaseRemoteConfig.fetch(0);
        fetch.addOnSuccessListener(getActivity(), aVoid ->{
            firebaseRemoteConfig.activateFetched();
            Log.e("Datos en fb",  "-"+firebaseRemoteConfig.getString("ciudadesAc") +"-");
        }).addOnFailureListener(getActivity(), aVoid -> {
            Log.e("Datos en fb",  "Fetch faild" );
            Toast.makeText(getActivity(), "Fetch failed",
                    Toast.LENGTH_SHORT).show();
        });

        String prueba= (String)firebaseRemoteConfig.getString("ciudadesAc");
        Log.e("Prueba", prueba);
        String[] ciudades = null;
        ciudades=prueba.split(",");

        for(int i = 0; i<ciudades.length; i++) {
            Log.e("CiudadArr", ciudades[i]);
            ArraylistCiudadesFRstr.add(ciudades[i]);
            Log.e("Ciudad" + i, ciudades[i]);
        }

        ArrayAdapter ArraylistCiudadesFRFire = new ArrayAdapter(getContext(),R.layout.color_spinner_layout, ArraylistCiudadesFRstr) ;
        // Apply the adapter to the spinner*/
        ArraylistCiudadesFRFire.setDropDownViewResource(R.layout.spinner_dropdown);
        spCiud.setAdapter(ArraylistCiudadesFRFire);
    }


    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(getContext(), titulo,
                mensaje, true);
    }
    private void  hideProgressDialog(){progress.dismiss();}
}