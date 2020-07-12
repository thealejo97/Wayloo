package com.wayloo.wayloo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.Utilidades;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.wayloo.wayloo.ui.anadirpeluqueria.AnadirPeluqueriaFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivityProfile extends AppCompatActivity implements cuadroDialogo.objDialog{
    private static String OLD_PWTV = "";
    private RequestQueue request;

    private String tel_usuario;
    private String id_firebase;
    private String nombre_usuario;
    private String apellido_usuario;
    private String email_usuario;
    private String ciudad_usuario;
    private String rol_usuario;
    ProgressDialog progress;
    ImageView imgPerfil;
    TextView texCambiar;
    CheckBox chModobarber;


    //Firebase user
    FirebaseAuth mAuth;

    private static final String CARPETA_PRINCIPAL = "misImagenesApp/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    private String path;//almacena la ruta de la imagen
    File fileImagen;
    Bitmap bitmap;
    Bitmap bitmapSINREDONDEAR;
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    private final int MIS_PERMISOS = 100;    EditText tel_u_TV, nombTV, apllTV, emailTV, ciudadTV;


    private Button editarButton;
    private TextView btnEliminarPerfil;


    String id_FirebaseCurrentUser = "";
    //Creamos la BD
    UsuariosSQLiteHelper usdbh =
            new UsuariosSQLiteHelper(MainActivityProfile.this, "dbUsuarios", null, 1);
    private TextView btnCambiarClave;
    private FirebaseUser mFirebaseUser;
    private Switch togleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_profile);

        Toolbar toolbare = findViewById(R.id.toolbarPerfil);// Inicializo el toolbar
        toolbare.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbare);// Inicializo el toolbar
        getSupportActionBar().setTitle("Editar Perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Relaciono los elemntos graficos
        editarButton = findViewById(R.id.editarButonPerfil);
        togleBtn = findViewById(R.id.toggleAdministrar);
        chModobarber = findViewById(R.id.checkBoxModoBarbero);
        imgPerfil = findViewById(R.id.imageViewPrincipalFotoPerfil);
        tel_u_TV = findViewById(R.id.TelBarSPerfil);
        nombTV = findViewById(R.id.NomBarSPerfil);
        apllTV = findViewById(R.id.ApellBarSPerfil);
        emailTV = findViewById(R.id.emailPerfil);
        ciudadTV = findViewById(R.id.CiudadPerfil);
        texCambiar = findViewById(R.id.textViewTituCambiar);
        btnCambiarClave = findViewById(R.id.textViewCambiarContra);
        showProgressDialog("Cargando Información del perfil.", "Por favor espere");

        id_FirebaseCurrentUser = ConsultaCurrentUser();

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        rol_usuario = ConsultaCurrentUserROLSQLITE();
        Log.e("Rol Interno SQLI", ConsultaCurrentUserROLSQLITE()+ "---------Ajua-------------------");

        consultaPerfil(id_FirebaseCurrentUser);

        //Actualiza los datos en la BD Externa

        editarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_email = emailTV.getText().toString();
                passwordPromp(new_email);

                /*
                String new_tel, new_nomb, new_apell, new_email, new_ciudad, new_rol;
                new_tel = tel_u_TV.getText().toString();
                new_nomb = nombTV.getText().toString();
                new_apell = apllTV.getText().toString();
                new_email = emailTV.getText().toString();
                new_ciudad = ciudadTV.getText().toString();

                updateBDRemota(new_tel, new_nomb, new_apell, " ", new_email, new_ciudad);*/

            }
        });

        if(rol_usuario.equals("1")){
            LinearLayout lnHabilitarModoBarbero = findViewById(R.id.linearHabilitarmodoBarbero);
            lnHabilitarModoBarbero.setVisibility(View.VISIBLE);
            View viewSeparador = findViewById(R.id.viewMODOB);
            viewSeparador.setVisibility(View.VISIBLE);
            togleBtn.setChecked(true);
        }else {
            togleBtn.setChecked(false);
        }
        togleBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Switch btn = (Switch) v;
                final boolean switchChecked = btn.isChecked();

                if (btn.isChecked()) {
                    btn.setChecked(false);
                } else {
                    btn.setChecked(true);
                }

                String message = "Esta a punto de dehabilitar el modo administrador, Esto elimminara todas las peluquerias.";
                if (!btn.isChecked()) {
                    message = "Esta a punto de convertirse en un administrador, Esto habilitará nuevas funciones para el manejo de su peluquería," +
                            " a continuación debe ingresar los datos de su local. ¿Desea continuar?";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityProfile.this); // Change "this" to `getActivity()` if you're using this on a fragment
                builder.setMessage(message)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                // "Yes" button was clicked
                                if (switchChecked) {
                                    administradorAntiguo(ConsultaCurrentUserTELSQLITE(), btn);

                                           /* //Cosa
                                    if(administradorAntiguo(ConsultaCurrentUserTELSQLITE()) {
                                        Fragment miFragment = new AnadirPeluqueriaFragment();
                                        String tag = "anadirpeluqueria";
                                        setContentView(R.layout.activity_main);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, miFragment, tag).commit();
                                        btn.setChecked(true);
                                    }*/
                                } else {
                                    deshabilitarAdmin();
                                    btn.setChecked(false);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });



        imgPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivityProfile.this, ImagenMainActivity.class);
                intent.putExtra("imagen",id_firebase);
                startActivity(intent);
            }
        });


        texCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solicitaPermisosVersionesSuperiores()) {
                    mostrarDialogOpciones();
                }
            }
        });
        btnEliminarPerfil = findViewById(R.id.editarButonEliminarPerfil);
        btnEliminarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarPerfilBDRemota(ConsultaCurrentUserTELSQLITE());//se elimina con el firebase
            }
        });

        btnCambiarClave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivityProfile.this, MainActivityCambiarClave.class);
                hideProgressDialog();
                startActivity(intent);
            }
        });
        Log.e("El rol es ", ConsultaCurrentUserRol());
        if(ConsultaCurrentUserRol().equalsIgnoreCase("1")){
            chModobarber.setChecked(false);
        }
         if(ConsultaCurrentUserRol().equalsIgnoreCase("4")){
            chModobarber.setChecked(true);}

        if((ConsultaCurrentUserRol().equalsIgnoreCase("1")) || (ConsultaCurrentUserRol().equalsIgnoreCase("4"))){

            chModobarber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                        @Override
                                                        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                                                            if(isChecked){
                                                                completarSeleccionHoraBarbero();
                                                                //   showProgressDialog("Habilitando Modo Barbero", "Espere ... ");
                                                                //    actualizarRemotoBarASAAdmin("4");
                                                            }else{
                                                                // showProgressDialog("Habilitando Modo Barbero", "Espere ... ");
                                                                //  actualizarRemotoBarASAAdmin("1");
                                                                showProgressDialog("Eliminando Modo Barbero", "Espere ... ");
                                                                eliminarPerfilBarberoAsAdminBDRemota(tel_usuario);
                                                            }
                                                        }
                                                    }
            );
        } else{

        }



    }

    private void administradorAntiguo(String telConsulta, Switch btn) {

        //Verifico el usuario existe 000Webhost si no existe, pido que complete los datos
        showProgressDialog("Verificando usuario","Conectando con el servidor espere.");
        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);
        String url = ip + "/consultas/verificarEstadoDelAdministrador.php?";
        Log.e("URL DEL POST", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.e("Response ", response);

                if (response.equalsIgnoreCase("deshabilitado")) {
                    //No existe el administrador osea que toca crearlo
                    Fragment miFragment = new AnadirPeluqueriaFragment();
                    String tag = "anadirpeluqueria";
                    setContentView(R.layout.activity_main);
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_main, miFragment, tag).commit();

                } else {
                    //Esta deshabilitado
                    habilitarAdministradorAntiguo(telConsulta);

                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivityProfile.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                Log.e("Error Response ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("telefono", telConsulta);
                return parameters;
            }

        };
        request.add(stringRequest);

    }

    private void habilitarAdministradorAntiguo(String telConsulta) {

        //Verifico el usuario existe 000Webhost si no existe, pido que complete los datos
        showProgressDialog("Verificando usuario","Conectando con el servidor espere.");
        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);
        String url = ip + "/consultas/rehabilitarAdministrador.php?";
        Log.e("URL DEL POST", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.e("Response ", response);
                Switch bttn = findViewById(R.id.toggleAdministrar);
                bttn.setChecked(true);
                updateROLSQLITE("1");
                Toast.makeText(MainActivityProfile.this, "Funciones de administrador habilitadas.", Toast.LENGTH_SHORT).show();
                reiniciarApp();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivityProfile.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                Log.e("Error Response ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("telefono", telConsulta);
                return parameters;
            }

        };
        request.add(stringRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void deshabilitarAdmin() {

        String new_rol = "3";

            request = Volley.newRequestQueue(getApplicationContext());

            String ip = getString(R.string.ip_way);

            String url = ip + "/consultas/deshabilitarAdministrador.php?";

            Log.e("URL DEL POST", url);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("Response Update go ", response);
                    if (response.equalsIgnoreCase("Registra")) {
                        Toast.makeText(MainActivityProfile.this, "Modo administrador desactivado", Toast.LENGTH_SHORT).show();
                        updateROLSQLITE(new_rol);
                        reiniciarApp();
                    } else {
                        Toast.makeText(MainActivityProfile.this, "Error de conexión verifique su red.", Toast.LENGTH_SHORT).show();
                    }
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(MainActivityProfile.this, "Error de conexión verifique su internet.", Toast.LENGTH_SHORT).show();
                    Log.e("Response Update ", error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("id_usuario", tel_usuario);


                    return parameters;
                }

            };
            request.add(stringRequest);

    }

    private void completarSeleccionHoraBarbero() {

        Context con = MainActivityProfile.this;
        new cuadroDialogModoBarber(con, MainActivityProfile.this);
    }

    private void actualizarRemotoBarASAAdmin(final String new_rol, final String nit, final String HI, final String HF, final String diasLaborales) {


        if (new_rol.equalsIgnoreCase("")) {
            Toast.makeText(this, "ERROR LOS CAMPOS NO PUEDEN ESTAR VACIOS", Toast.LENGTH_LONG).show();
        } else {
            request = Volley.newRequestQueue(getApplicationContext());

            String ip =getString(R.string.ip_way);

            String url = ip + "/consultas/UpdateBarAsAdmin.php?";

            Log.e("URL DEL POST", url);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("Response Update go ", response);
                    if(response.equalsIgnoreCase("RegistraregistraUsuario")){
                    Toast.makeText(MainActivityProfile.this, "Modo barbero activado", Toast.LENGTH_SHORT).show();
                    updateROLSQLITE(new_rol);
                    reiniciarApp();}
                    else{
                        Toast.makeText(MainActivityProfile.this, "Error de conexión verifique su red.", Toast.LENGTH_SHORT).show();
                    }
                }

            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(MainActivityProfile.this, "Error de conexión verifique su internet.", Toast.LENGTH_SHORT).show();
                    Log.e("Response Update ", error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("rp", new_rol);
                    parameters.put("id_firebase", id_FirebaseCurrentUser);
                    parameters.put("nit_peluqueria_pertenese",nit);
                    parameters.put("h_inicio", HI);
                    parameters.put("h_fin", HF);
                    parameters.put("diasLaborales",diasLaborales);
                    parameters.put("tp", tel_usuario);

                    return parameters;
                }

            };
            request.add(stringRequest);


        }


    }

    private void actualizarRemotoRol(final String new_rol) {


        if (new_rol.equalsIgnoreCase("")) {
            Toast.makeText(this, "ERROR LOS CAMPOS NO PUEDEN ESTAR VACIOS", Toast.LENGTH_LONG).show();
        } else {
            request = Volley.newRequestQueue(getApplicationContext());

            String ip =getString(R.string.ip_way);

            String url = ip + "/consultas/UpdateRol.php?";

            Log.e("URL DEL POST", url);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("Response Update go ", response);
                    //Toast.makeText(MainActivityProfile.this, "Modo barbero activado", Toast.LENGTH_SHORT).show();
                    updateROLSQLITE("1");
                    reiniciarApp();
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
                    parameters.put("rp", new_rol);
                    parameters.put("id_firebase", id_FirebaseCurrentUser);


                    return parameters;
                }

            };
            request.add(stringRequest);


        }


    }

    private void eliminarPerfilBarberoAsAdminBDRemota(final String telAEliminar) {


        if (telAEliminar.equalsIgnoreCase("")) {
            Toast.makeText(this, "ERROR ELIMINANDO", Toast.LENGTH_LONG).show();
        } else {
            final Utilidades utl = new Utilidades();
            //Si no son iguales
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmación eliminación del perfil!");
            builder.setMessage("Va a eliminar sus datos del sistema, ¿Desea continuar?");
            builder.setCancelable(false);
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    request = Volley.newRequestQueue(getApplicationContext());

                    String ip =getString(R.string.ip_way);

                    String url = ip + "/consultas/DeleteBarberoAsAdmin.php?";

                    Log.e("URL DEL POST", url);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            hideProgressDialog();
                            Log.e("Response Delete go ", response);
                            Toast.makeText(MainActivityProfile.this, "Modo barbero Eliminado", Toast.LENGTH_SHORT).show();

                            actualizarRemotoRol("1");

                        }

                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            Toast.makeText(MainActivityProfile.this, "Error deshabilitando", Toast.LENGTH_SHORT).show();
                            Log.e("Response Update ", error.toString());
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> parameters = new HashMap<String, String>();
                            String imagen = convertirImgString(bitmapSINREDONDEAR);
                            parameters.put("telEliminar", telAEliminar);
                            return parameters;
                        }

                    };
                    request.add(stringRequest);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hideProgressDialog();
                    Toast.makeText(MainActivityProfile.this, "Cancelado", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();

        }

    }

    private void eliminarPerfilBDRemota(final String telefono) {


        if (telefono.equalsIgnoreCase("")) {
            Toast.makeText(this, "ERROR ELIMINANDO", Toast.LENGTH_LONG).show();
        } else {
            final Utilidades utl = new Utilidades();
            //Si no son iguales
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmación eliminación del perfil!");
            builder.setMessage("Va a eliminar sus datos del sistema, ¿Desea continuar?");
            builder.setCancelable(false);
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    request = Volley.newRequestQueue(getApplicationContext());

                    String ip =getString(R.string.ip_way);

                    String url = ip + "/consultas/DeleteProfile.php?";

                    Log.e("URL DEL POST", url);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("Response Delete go ", response);
                            try {
                                LogoutmenuClick();
                                deleteUserFirebase();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(MainActivityProfile.this, "Perfil Eliminado", Toast.LENGTH_SHORT).show();
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
                            parameters.put("telefono", telefono);
                            return parameters;
                        }

                    };
                    request.add(stringRequest);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivityProfile.this, "Cancelado", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();

        }

    }

    private void consultaPerfil(final String fireUsu) {

        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consulta_Profile.php?";

        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response ", response + response.equalsIgnoreCase("[]") + response.isEmpty());
                if (response.equalsIgnoreCase("[]")) {
                    hideProgressDialog();
                    Toast.makeText(MainActivityProfile.this, "Error de consulta de perfil", Toast.LENGTH_SHORT).show();
                } else {

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());

                        //extracting json array from response string
                        JSONArray json = jsonObject.getJSONArray("usuarioC");

                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject2 = null;
                            jsonObject2 = json.getJSONObject(i);
                            tel_usuario = jsonObject2.optString("tel_usuario");
                            id_firebase = jsonObject2.optString("id_firebase");
                            nombre_usuario = jsonObject2.optString("nombre_usuario");
                            apellido_usuario = jsonObject2.optString("apellido_usuario");
                            email_usuario = jsonObject2.optString("email_usuario");
                            ciudad_usuario = jsonObject2.optString("ciudad_usuario");
                            rol_usuario = jsonObject2.optString("rol_usuario");
                        }

                        updateIU(tel_usuario, id_firebase, nombre_usuario, apellido_usuario, email_usuario, ciudad_usuario, rol_usuario);

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
                parameters.put("idFire", fireUsu);
                return parameters;
            }

        };
        Log.e("Usuario a consultar", fireUsu);
        request.add(stringRequest);
    }

    private void updateIU(String tel_usuario, String id_firebase, String nombre_usuario, String apellido_usuario, String email_usuario, String ciudad_usuario, String rol_usuario) {


        tel_u_TV = findViewById(R.id.TelBarSPerfil);
        nombTV = findViewById(R.id.NomBarSPerfil);
        emailTV = findViewById(R.id.emailPerfil);
        ciudadTV = findViewById(R.id.CiudadPerfil);

        cargarWebImagen(id_firebase);
        tel_u_TV.setText(tel_usuario);
        nombTV.setText(nombre_usuario);
        apllTV.setText(apellido_usuario);
        ciudadTV.setText(ciudad_usuario);


        emailTV.setText(email_usuario);
        ciudadTV.setText(ciudad_usuario);


    }

    private String ConsultaCurrentUser() {
        String result = "null";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id_firebase FROM CurrentUsuario", null);
        if (c != null) {
            c.moveToFirst();
            do {
                //Asignamos el valor en nuestras variables para usarlos en lo que necesitemos
                result = c.getString(c.getColumnIndex("id_firebase"));
            } while (c.moveToNext());
        }

        //Cerramos el cursor y la conexion con la base de datos
        c.close();
        db.close();
        return result;
    }

    private String ConsultaCurrentUserRol() {
        String result = "null";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT rol FROM CurrentUsuario", null);
        if (c != null) {
            c.moveToFirst();
            do {
                //Asignamos el valor en nuestras variables para usarlos en lo que necesitemos
                result = c.getString(c.getColumnIndex("rol"));
            } while (c.moveToNext());
        }

        //Cerramos el cursor y la conexion con la base de datos
        c.close();
        db.close();
        return result;
    }

    public void cargarWebImagen(String id) {

        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/imagenes/" + id + ".jpg";
        url = url.replace(" ", "%20");
        url = url.replace("ñ", "n");
        url = url.replace("á", "a");
        url = url.replace("é", "e");
        url = url.replace("í", "i");
        url = url.replace("ó", "o");
        url = url.replace("ú", "u");
        Log.e("IMG URL", url);
        request = Volley.newRequestQueue(MainActivityProfile.this);
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Log.e("Respondio", "respondio");
                        bitmapSINREDONDEAR = response;
                        imgPerfil.setImageBitmap(redondearBitmap(redimensionarImagen(response, 250, 250)));
                        hideProgressDialog();
                    }
                }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivityProfile.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        });
        request.add(imageRequest);
    }

    private Bitmap redondearBitmap(Bitmap bitAconvertir) {
        Bitmap imageBitmap = bitAconvertir;
        RoundedBitmapDrawable roundedBitmapDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
        roundedBitmapDrawable.setCornerRadius(175.0f);
        roundedBitmapDrawable.setAntiAlias(true);

        Bitmap imageBitmapConBlanco = addWhiteBorder(drawableToBitmap(roundedBitmapDrawable), 2);
        RoundedBitmapDrawable roundedBitmapDrawableBlanco =
                RoundedBitmapDrawableFactory.create(getResources(), imageBitmapConBlanco);
        roundedBitmapDrawableBlanco.setCornerRadius(180.0f);
        roundedBitmapDrawableBlanco.setAntiAlias(true);


        return drawableToBitmap(roundedBitmapDrawableBlanco);
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void showProgressDialog(String titulo, String mensaje) {
        progress = ProgressDialog.show(MainActivityProfile.this, titulo,
                mensaje, true);
    }

    private void hideProgressDialog() {
        progress.dismiss();
    }

    private void updateBDRemota(final String new_tel, final String new_nomb, final String new_apell, final String new_PWTV, final String new_email, final String new_ciudad) {


        if (new_tel.equalsIgnoreCase("") || new_PWTV.equalsIgnoreCase("") || new_email.equalsIgnoreCase("") || new_ciudad.equalsIgnoreCase("")) {
            Toast.makeText(this, "ERROR LOS CAMPOS NO PUEDEN ESTAR VACIOS", Toast.LENGTH_LONG).show();
        } else {
                            request = Volley.newRequestQueue(getApplicationContext());

                            String ip =getString(R.string.ip_way);

                            String url = ip + "/consultas/UpdateProfile.php?";

                            Log.e("URL DEL POST", url);
                            final String finalNew_apell = new_apell;
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    hideProgressDialog();
                                    Log.e("Response Update go ", response);
                                    updateSQLITE(new_tel, (new_nomb + " " + finalNew_apell), new_email, new_ciudad, traerROLSQLITE());
                                }

                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    hideProgressDialog();
                                    Log.e("Response Update ", error.toString());
                                    changeEmailFirebase(traerEmailSQLITE(), "error",null);
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> parameters = new HashMap<String, String>();
                                    String imagen = convertirImgString(bitmapSINREDONDEAR);
                                    parameters.put("tp", new_tel);
                                    parameters.put("np", new_nomb);
                                    parameters.put("ap", new_apell);
                                    parameters.put("ep", new_email);
                                    parameters.put("cp", new_ciudad);
                                    parameters.put("imagen", imagen);
                                    parameters.put("id_firebase", id_FirebaseCurrentUser);

                                    return parameters;
                                }

                            };
                            request.add(stringRequest);


                        }


    }

    private String ConsultaCurrentUserTELSQLITE() {
        String result = "null";
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

    private String ConsultaCurrentUserROLSQLITE() {
        String result = "null";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT rol FROM CurrentUsuario", null);
        if (c != null) {
            c.moveToFirst();
            do {
                //Asignamos el valor en nuestras variables para usarlos en lo que necesitemos
                result = c.getString(c.getColumnIndex("rol"));
            } while (c.moveToNext());
        }

        //Cerramos el cursor y la conexion con la base de datos
        c.close();
        db.close();
        return result;
    }

    public void updateROLSQLITE(final String new_rol) {
        SQLiteDatabase db = usdbh.getReadableDatabase();
        db.execSQL("UPDATE CurrentUsuario SET  rol = '" + new_rol + "'  WHERE id_firebase = '"+ id_FirebaseCurrentUser+"'");
        progress.dismiss();
    }

    public void updateSQLITE(final String new_tel, final String new_nomb, final String new_email, final String new_ciudad, final String new_rol) {


        SQLiteDatabase db = usdbh.getReadableDatabase();
        db.execSQL("UPDATE CurrentUsuario SET nombre = " + "'" + new_nomb + "', email = '" + new_email + "' , rol = '" + new_rol + "' " +
                ", id_usu = '" + new_tel + "' ");
        progress = ProgressDialog.show(MainActivityProfile.this, "Actualizando información",
                "Espere ... ... ", true);
        progress.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                progress.dismiss();
                reiniciarApp();

            }

        }, 2000);

    }

    private void reiniciarApp() {
        Intent mStartActivity = new Intent(MainActivityProfile.this, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivityProfile.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) MainActivityProfile.this.getSystemService(MainActivityProfile.this.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);

        return imagenString;
    }

    ///Para picker de imagen
    private void abrirCamara() {
        File miFile = new File(Environment.getExternalStorageDirectory(), DIRECTORIO_IMAGEN);
        boolean isCreada = miFile.exists();

        if (isCreada == false) {
            isCreada = miFile.mkdirs();
        }
        if (isCreada == true) {
            Long consecutivo = System.currentTimeMillis() / 1000;
            String nombre = consecutivo.toString() + ".jpg";

            path = Environment.getExternalStorageDirectory() + File.separator + DIRECTORIO_IMAGEN
                    + File.separator + nombre;//indicamos la ruta de almacenamiento

            fileImagen = new File(path);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));

            ////
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String authorities = MainActivityProfile.this.getPackageName() + ".provider";
                Uri imageUri = FileProvider.getUriForFile(MainActivityProfile.this, authorities, fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            startActivityForResult(intent, COD_FOTO);

            ////

        }
    }

    private void mostrarDialogOpciones() {
        final CharSequence[] opciones = {"Tomar Foto", "Elegir de la Galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityProfile.this);
        builder.setTitle("Selecciona una opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (opciones[i].equals("Tomar Foto")) {

                    abrirCamara();
                    //Tomar foto
                } else {
                    if (opciones[i].equals("Elegir de la Galeria")) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent, "Selecione"), COD_SELECCIONA);
                    } else {
                        if (opciones[i].equals("Cancelar")) {

                        } else {
                            dialog.dismiss();
                        }
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.e("Respuesta ", "ON ACTIVITY" + requestCode);
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case COD_SELECCIONA:
                    //if(data.get)
                    Uri miPath = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(MainActivityProfile.this.getContentResolver(), miPath);
                        bitmapSINREDONDEAR = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                        imgPerfil.setImageBitmap(redimensionarImagen(Bitmap.createScaledBitmap(bitmap, 200, 200, false), 250, 250));
                        //imgPerfil.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case COD_FOTO:
                    MediaScannerConnection.scanFile(MainActivityProfile.this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Path", "" + path);
                                }
                            });

                    bitmap = BitmapFactory.decodeFile(path);
                    bitmapSINREDONDEAR = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
                    imgPerfil.setImageBitmap(redimensionarImagen(Bitmap.createScaledBitmap(bitmap, 200, 200, false), 250, 250));

                    break;
            }
            bitmap = redimensionarImagen(bitmap, 200, 200);
        }
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

    //permisos
    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        if ((MainActivityProfile.this.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && MainActivityProfile.this.checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }


        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE) || (shouldShowRequestPermissionRationale(CAMERA)))) {
            cargarDialogoRecomendacion();
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(MainActivityProfile.this);
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                }
            }
        });
        dialogo.show();
    }


    private void deleteUserFirebase() throws Exception {
        Log.e("Delete", "ingreso a deleteAccount");


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.e("En el main", "Current User Fir " +user +" aut " + mAuth);
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            //Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
            Log.e("En el main", uid+ " " + email);
        }
        user.delete();

    }

    private void LogoutmenuClick(){
        //Save registro permanente
        SQLiteDatabase db = usdbh.getWritableDatabase();
        db.execSQL("delete from  CurrentUsuario;");

        Intent intent = new Intent(MainActivityProfile.this, MainLogginActivity.class);
        startActivity(intent);
        finish();
    }


    private void changeEmailFirebase(String email, String Tag, String password) {


        showProgressDialog("Actualizando perfil", "Espere mientras se contacta con el servidor");

            AuthCredential cr7 = EmailAuthProvider.getCredential(traerEmailSQLITE(), password);
            mFirebaseUser.reauthenticate(cr7).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (!Tag.equals("error")) {
                            mFirebaseUser.updateEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {


                                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        ///////////

                                                        String new_tel, new_nomb, new_apell, new_email, new_ciudad, new_rol;
                                                        new_tel = tel_u_TV.getText().toString();
                                                        new_nomb = nombTV.getText().toString();
                                                        new_apell = apllTV.getText().toString();
                                                        new_email = emailTV.getText().toString();
                                                        new_ciudad = ciudadTV.getText().toString();

                                                        updateBDRemota(new_tel, new_nomb, new_apell, " ", new_email, new_ciudad);
                                                        return;
                                                    }
                                                });
                                            }
                                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                                hideProgressDialog();
                                                Log.e("Error cambiando FR", task.getException().toString());
                                                Toast.makeText(MainActivityProfile.this, "Error actualizando perfil", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            mFirebaseUser.updateEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivityProfile.this, "Error BD SQL Caida", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                                Log.e("Error cambiando FR", task.getException().toString());
                                                Toast.makeText(MainActivityProfile.this, "Error actualizando perfil", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }


                    } else {
                        hideProgressDialog();
                        Toast.makeText(MainActivityProfile.this, "Error contraseña erronea.", Toast.LENGTH_SHORT).show();
                        Log.e("Clave mala", "Error auth failed");
                    }
                }
            });


        }


    @Override
    public void ResultadoDialogo(String nit, String HI, String HF, String diasLaborales) {
        Log.e("Resultado dialogo modo", nit + "  " + HI +"  "+ HF);
        if(nit.equalsIgnoreCase("") || HI.equalsIgnoreCase("") || HF.equalsIgnoreCase("") )
        {
            Intent intent = new Intent(MainActivityProfile.this, MainActivityProfile.class);
            startActivity(intent);
            finish();
        }else {
            showProgressDialog("Habilitando Modo Barbero", "Espere ... ");
            actualizarRemotoBarASAAdmin("4", nit, HI, HF, diasLaborales);
        }
    }


    //Trae el rol del usuario que inicio sesion en la BDSQLITE
    private String traerROLSQLITE(){
        String rolUsuInterno= "UnKnow User";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT rol FROM CurrentUsuario;", null); // Creamos la consulta
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                rolUsuInterno= c.getString(0);// Obtenemos la respuesta, debe tener una sola fila

            } while(c.moveToNext());
        }
        return rolUsuInterno;
    }

    private String traerEmailSQLITE(){
        String name= "UnKnow User";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT email FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                name= c.getString(0);

            } while(c.moveToNext());
        }
        return name;
    }

    public void passwordPromp(String new_email)
    {
        Context context = MainActivityProfile.this;
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.promp_layout, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);


        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton("Continuar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                changeEmailFirebase(new_email,"actualizacion", userInput.getText().toString());

                            }

                        }

                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}






