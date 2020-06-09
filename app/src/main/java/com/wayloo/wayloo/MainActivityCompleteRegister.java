package com.wayloo.wayloo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wayloo.wayloo.entidades.nitp;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.Utilidades;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivityCompleteRegister extends AppCompatActivity implements Response.Listener<JSONObject>,Response.ErrorListener  {
    //Creamos la BD interna
    UsuariosSQLiteHelper usdbh =
            new UsuariosSQLiteHelper(MainActivityCompleteRegister.this, "dbUsuarios", null, 1);
    //Datos para la DB externa
    private RequestQueue request;
    FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    private final int MIS_PERMISOS = 100;
    private static final String CARPETA_PRINCIPAL = "misImagenesApp/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    private String path;//almacena la ruta de la imagen



    private EditText etPassword;
    private EditText etHora_inicio;
    private EditText etHora_fin;
    private EditText etCity;
    private TextView titulo;
    private EditText NitPeluqueriaET;
    private EditText direccionET;
    private EditText nombre_peluqueriaET;
    private EditText telefono;
    private EditText NITalQuePerteneceBarbero;
    private Button ButonVerificarNIT;
    private Boolean NITVerificado= false;
    private EditText telefono_peluqueriaET;

    StringRequest stringRequestS;
    private RequestQueue request2;
    private Spinner spinerRol;
    private Spinner spinerCiudad;
    int rol=0;
    File fileImagen;
    Bitmap bitmap;
    private ImageView imgPerfil;
    Button btRegistrar,ButtonPickImagen;;
    //Usuario
    String cellphone;
    String name;
    String id_firebase;
    String email;
    String Password;
    String lastname;


    //Peluqueria
    String NitString;
    String nombrePeluqueria;
    String telPeluqueria;
    String direccion;
    String miCiudad;

    //Barbero
    String hora_inicio;
    String hora_fin;
    String NITBarberoPertenese;

    FirebaseAuth mAuth;
    //Progress
    ProgressDialog progress;

    //////// Picker Fecha
    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";


    //Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    //Variables para obtener la hora hora
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);
    private ImageButton ibObtenerHora;
    private ImageButton ibObtenerHora_fin;
    private TextView txtEmailComp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_complete_register);

        ButtonPickImagen = findViewById(R.id.buttonImgperfil);
        request= Volley.newRequestQueue(getApplicationContext());

//Primero queremos obtener la informacion general del usuario la comun
        imgPerfil = findViewById(R.id.imageViewProfileFoto);
      //  redondearCanvas(imgPerfil);
        BitmapDrawable drawable = (BitmapDrawable) imgPerfil.getDrawable();
        Bitmap mbitmap = drawable.getBitmap();
        mbitmap = redimensionarImagen(mbitmap, 5, 10);
//Inicializamos los componentes graficos
        telefono= findViewById(R.id.etxt_telefono);
        titulo=findViewById(R.id.tituloSeparador);
        spinerRol= findViewById(R.id.spinnerRol);
        ArrayAdapter adapterA = ArrayAdapter.createFromResource(this,
                R.array.roles, R.layout.color_spinner_layout);
        adapterA.setDropDownViewResource(R.layout.spinner_dropdown);
        spinerRol.setAdapter(adapterA);
        btRegistrar=findViewById(R.id.btn_register);
        etPassword=findViewById(R.id.etxt_password);
        etCity = findViewById(R.id.etxt_CIUDAD);
//Llenamos los datos base
        name=getIntent().getStringExtra("nombre_fb");;
        String [] nombr= name.split(" ");
        name=nombr[0];
        lastname=getIntent().getStringExtra("nombre_fb");
        String []  apell = lastname. split(" ");
        lastname=apell[1];
        email=getIntent().getStringExtra("email");
        id_firebase=getIntent().getStringExtra("id_firebase");
        ButonVerificarNIT = findViewById(R.id.ButtonVerificarNIT);
        NITalQuePerteneceBarbero = findViewById(R.id.EtxNitPeluqueriaPerteneceBarbero);

        spinerCiudad=findViewById(R.id.ciudad_spinner);
/*
        ArrayAdapter adapterAL = ArrayAdapter.createFromResource(this,
                R.array.ciudades, R.layout.color_spinner_layout);
        adapterAL.setDropDownViewResource(R.layout.spinner_dropdown);
        spinerCiudad.setAdapter(adapterAL);*/
        txtEmailComp = findViewById(R.id.etxt_emailComplete);
        txtEmailComp.setText(email);


        //Verifica que si se edita el NIT ya no fue verificado
        btRegistrar.setClickable(false);
        NITalQuePerteneceBarbero.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                btRegistrar.setClickable(false);
                NITVerificado = false;
                NITalQuePerteneceBarbero.setTextColor(Color.RED);
            }
        });
        // Si presiona verificar
        ButonVerificarNIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Verificando NIT", "Espere mientras se contacta con el servidor");
                //Realiza y verifica que el nit ingresado ya este registrado
                if (NITalQuePerteneceBarbero.getText().toString().isEmpty()) {
                    hideProgressDialog();
                    Toast.makeText(MainActivityCompleteRegister.this, "El campo NIT a verificar no debe de estar vacio.", Toast.LENGTH_SHORT).show();
                } else {
                    consultarPeluqueriaExiste(NITalQuePerteneceBarbero.getText().toString());
                }
            }
        });


//Cuando presiona registrar
        btRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //No inicializo la ciudad ya que se selecciona dependiendo de la ubicacion
                cellphone = telefono.getText().toString();
                email = txtEmailComp.getText().toString();
                email=getIntent().getStringExtra("email");
                Password =etPassword.getText().toString();
                if (name.isEmpty() && email.isEmpty() && Password.isEmpty()) {
                    Toast.makeText(MainActivityCompleteRegister.this, "Error los campos no deben de estar vacios.", Toast.LENGTH_SHORT).show();
                } else {
                    if (Password.length() > 6 && name.length() > 3) {
                        if (validarEmail()) {

                            if (rol != 0) {
                                showProgressDialog("Registrando Usuario", "Espere mientras se contacta con el servidor");
                                //Inicializa todos y registra en firebase
                                if (rol == 0) {
                                    hideProgressDialog();
                                    Toast.makeText(MainActivityCompleteRegister.this, "Error, debe seleccionar su rol", Toast.LENGTH_LONG).show();
                                } else {
                                    if (rol == 1) {

                                        RegistrarUsuarioAdminWebBD();
                                        //Save registro permanente
//                                    salvarPermanente();

                                    } else {
                                        if (rol == 2) {
                                            RegistrarUsuarioBarbero();
                                            //Save registro permanente
                                            //    salvarPermanente();
                                        } else {
                                            if (rol == 3) {
                                                RegistrarUsuarioClienteWebBD();
                                                //Save registro permanente
                                                //      salvarPermanente();
                                            }

                                        }
                                    }

                                }
                            }
                        }else {
                            Toast.makeText(MainActivityCompleteRegister.this, "Error el email ingresado no es valido", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivityCompleteRegister.this, "Error el password debe tener minimo 6 caracteres y el usuario 3.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        //Spinner cuando cambia
        spinerRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String currentItem = spinerRol.getSelectedItem().toString();
                if (currentItem.equals("Administrador")) {btRegistrar.setClickable(true);
                    rol = 1;
                    titulo.setVisibility(View.VISIBLE);
                    titulo.setText("INGRESE INFORMACIÓN DE SU PELUQUERIA");
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.VISIBLE);
                    View v55 = findViewById(R.id.linealBarbetoCompleto);
                    v55.setVisibility(View.GONE);
                }
                if (currentItem.equals("Barbero")) {
                    if(NITVerificado){btRegistrar.setClickable(true);}else{btRegistrar.setClickable(false);}
                    rol = 2;
                    View v55 = findViewById(R.id.linealBarbetoCompleto);
                    v55.setVisibility(View.VISIBLE);
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.GONE);
                    titulo.setVisibility(View.VISIBLE);
                    titulo.setText("Recuerde que debe de estar registrado en una peluqueria para ingresar.");
                }
                if (currentItem.equals("Cliente")) {btRegistrar.setClickable(true);
                    rol = 3;
                    View v55 = findViewById(R.id.linealBarbetoCompleto);
                    v55.setVisibility(View.GONE);
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.GONE);
                    titulo.setVisibility(View.INVISIBLE);

                }
                if (currentItem.equals("Seleccione su rol")) {btRegistrar.setClickable(false);
                    rol = 3;
                    View v55 = findViewById(R.id.linealBarbetoCompleto);
                    v55.setVisibility(View.GONE);
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.GONE);
                    titulo.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                rol=0;
                View v55 = findViewById(R.id.linealBarbetoCompleto);
                v55.setVisibility(View.GONE);
                View v3 = findViewById(R.id.LinealAdministrador);
                v3.setVisibility(View.GONE);
                titulo.setVisibility(View.GONE);
            }
        });

        ///// Para fechas
        //Widget EditText donde se mostrara la hora obtenida
        etHora_inicio = (EditText) findViewById(R.id.et_mostrar_hora_picker_inicio);
        etHora_fin = (EditText) findViewById(R.id.et_mostrar_hora_picker_fin);
        //Widget ImageButton del cual usaremos el evento clic para obtener la hora
        ibObtenerHora = (ImageButton) findViewById(R.id.ib_obtener_hora_inicio);
        ibObtenerHora_fin = (ImageButton) findViewById(R.id.ib_obtener_hora_fin);
        //Evento setOnClickListener - clic
        ibObtenerHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ib_obtener_hora_inicio:
                        obtenerHora_inicio();
                        break;
                }
            }
        });
        ibObtenerHora_fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ib_obtener_hora_fin:
                        obtenerHora_fin();
                        break;
                }
            }
        });
        ButtonPickImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solicitaPermisosVersionesSuperiores()) {
                    mostrarDialogOpciones();
                }
            }
        });


    }

    //Valida el email que sea correcto
    private boolean validarEmail() {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private void RegistrarUsuarioAdminWebBD(){
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();
        try {
            bitmap = getFacebookProfilePicture(id_firebase);
        } catch (Exception e) {
            e.printStackTrace();
        }
        request2 = Volley.newRequestQueue(getApplicationContext());
        cellphone = telefono.getText().toString();
        Password = etPassword.getText().toString();
        miCiudad= spinerCiudad.getSelectedItem().toString();


    if(miCiudad.equalsIgnoreCase("Seleccione su ciudad")){
        Toast.makeText(this, "Error, debe seleccionar una ciudad Valida", Toast.LENGTH_SHORT).show();
    }else {
        direccionET = findViewById(R.id.etxt_peluqueriaDireccion);
        NitPeluqueriaET = findViewById(R.id.etxt_PeluqueriaNit);
        nombre_peluqueriaET = findViewById(R.id.etxt_PeluqueriaNombre);
        telefono_peluqueriaET = findViewById(R.id.etxt_telefono_peluqueria);

        direccion = direccionET.getText().toString();
        NitString = NitPeluqueriaET.getText().toString();
        nombrePeluqueria = nombre_peluqueriaET.getText().toString();
        telPeluqueria = telefono_peluqueriaET.getText().toString();

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/registrar_UsuarioIMG.php?";
        Log.e("URL DEL POST", url);

        stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.i("RESPUESTA: ", "" + response);
                if (response.trim().equalsIgnoreCase("registra")) {
                    salvarPermanente();
                    Toast.makeText(MainActivityCompleteRegister.this, "Se ha registrado con exito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivityCompleteRegister.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error conexion", error.toString());
                Toast.makeText(MainActivityCompleteRegister.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                String imagen = convertirImgString(bitmap);
                Utilidades utilidad = new Utilidades();
                Map<String, String> parametros = new HashMap<>();
                parametros.put("tel_usuario", cellphone);
                parametros.put("id_firebase", id_firebase);
                parametros.put("nombre_usuario", name);
                parametros.put("apellido_usuario", lastname);
                parametros.put("password_usuario", utilidad.Encriptar(Password));
                parametros.put("email_usuario", email);
                parametros.put("ciudad_usuario", miCiudad);
                parametros.put("rol_usuario", rol + "");

                //Datos de la peluqueria del administrador

                parametros.put("nit_peluqueria", NitString);
                parametros.put("nombre_peluqueria", nombrePeluqueria);
                parametros.put("telefono_peluqueria", telPeluqueria);
                parametros.put("direccion_peluqueria", direccion);
                parametros.put("ciudad_peluqueria", miCiudad);


                parametros.put("imagen", imagen);
                return parametros;
            }
        };
        Log.e("Usuario reg", cellphone+id_firebase+name+lastname+Password+email+miCiudad);
        request2.add(stringRequestS);
    }
    }

    @Override
    protected void onStart() {
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
        fetch.addOnSuccessListener(MainActivityCompleteRegister.this, aVoid ->{
            firebaseRemoteConfig.activateFetched();
            Log.e("Datos en fb",  "-"+firebaseRemoteConfig.getString("ciudadesAc") +"-");
        }).addOnFailureListener(MainActivityCompleteRegister.this, aVoid -> {
            Log.e("Datos en fb",  "Fetch faild" );
            Toast.makeText(MainActivityCompleteRegister.this, "Fetch failed",
                    Toast.LENGTH_SHORT).show();
        }); //Versionador

        String prueba= (String)firebaseRemoteConfig.getString("ciudadesAc");
        Log.e("Prueba", prueba);
        String[] ciudades = null;
        ciudades=prueba.split(",");

        for(int i = 0; i<ciudades.length; i++) {
            Log.e("CiudadArr", ciudades[i]);
            ArraylistCiudadesFRstr.add(ciudades[i]);
            Log.e("Ciudad" + i, ciudades[i]);
        }

        ArrayAdapter ArraylistCiudadesFRFire = new ArrayAdapter(this,R.layout.color_spinner_layout, ArraylistCiudadesFRstr) ;
        // Apply the adapter to the spinner*/
        ArraylistCiudadesFRFire.setDropDownViewResource(R.layout.spinner_dropdown);
        spinerCiudad.setAdapter(ArraylistCiudadesFRFire);
    }


    public static Bitmap getFacebookProfilePicture(String userID) throws SocketException, SocketTimeoutException, MalformedURLException, IOException, Exception
    {
        String imageURL;

        Bitmap bitmap = null;
        imageURL = "http://graph.facebook.com/"+userID+"/picture?type=large";
        InputStream in = (InputStream) new URL(imageURL).getContent();
        bitmap = BitmapFactory.decodeStream(in);

        return bitmap;
    }

    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);

        return imagenString;
    }

    //Registra el usuario si es barbero
    private void RegistrarUsuarioBarbero() {

        //Inicio los datos de Usuario
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();

        request = Volley.newRequestQueue(getApplicationContext());
        cellphone = telefono.getText().toString();

        Password = etPassword.getText().toString();
        miCiudad= spinerCiudad.getSelectedItem().toString();
        //inicializo et de peluquerias
        direccionET = findViewById(R.id.etxt_peluqueriaDireccion);
        NITBarberoPertenese=NitPeluqueriaET.getText().toString();
        CheckBox chl,chm,chmi,chj,chv,chs,chd;
        chl= findViewById(R.id.chLunesCom);
        chm= findViewById(R.id.chMartesCom);
        chmi= findViewById(R.id.chMiercolesCom);
        chj= findViewById(R.id.chJuevesCom);
        chv= findViewById(R.id.chViernesCom);
        chs= findViewById(R.id.chSabadoCom);
        chd= findViewById(R.id.chDomingoCom);

        String diasLaborales= "";
        if(chl.isChecked()) {
            diasLaborales = diasLaborales + "l,";
        }
        if(chm.isChecked()) {
            diasLaborales = diasLaborales + "m,";
        }
        if(chmi.isChecked()) {
            diasLaborales = diasLaborales + "mi,";
        }
        if(chj.isChecked()) {
            diasLaborales = diasLaborales + "j,";
        }
        if(chv.isChecked()) {
            diasLaborales = diasLaborales + "v,";
        }
        if(chs.isChecked()) {
            diasLaborales = diasLaborales + "s,";
        }
        if(chd.isChecked()){
            diasLaborales = diasLaborales+"d,";
        }

        //Lleno los demas datos relacionados con el barbero
        hora_inicio = etHora_inicio.getText().toString();
        NitString = NITBarberoPertenese;
        hora_fin = etHora_fin.getText().toString();
        nombrePeluqueria = nombre_peluqueriaET.getText().toString();
        telPeluqueria = telefono_peluqueriaET.getText().toString();
        direccion = direccionET.getText().toString();

        if (NITVerificado) {

            String ip =getString(R.string.ip_way);

            String url = ip + "/consultas/registrar_UsuarioIMG.php?";
            Log.e("URL DEL POST", url);

            String finalDiasLaborales = diasLaborales;
            stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    hideProgressDialog();
                    Log.e("RESPUESTA: ", "" + response);
                    if (response.trim().equalsIgnoreCase("registra")) {
                        salvarPermanente();
                        Toast.makeText(MainActivityCompleteRegister.this, "Se ha registrado con exito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivityCompleteRegister.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("RESPUESTA: ", "" + error);
                    Toast.makeText(MainActivityCompleteRegister.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    String imagen = convertirImgString(bitmap);
                    Utilidades utilidad = new Utilidades();
                    Map<String, String> parametros = new HashMap<>();
                    parametros.put("tel_usuario", cellphone);
                    parametros.put("id_firebase", id_firebase);
                    parametros.put("nombre_usuario", name);
                    parametros.put("apellido_usuario", lastname);
                    parametros.put("password_usuario", utilidad.Encriptar(Password));
                    parametros.put("email_usuario", email);
                    parametros.put("ciudad_usuario", miCiudad);
                    parametros.put("rol_usuario", rol + "");
                    parametros.put("diasl", finalDiasLaborales);
                    //Datos de la peluqueria del administrador
                    NITBarberoPertenese = NITalQuePerteneceBarbero.getText().toString();
                    Log.e("Peluqueria pertenese", NITBarberoPertenese);
                    parametros.put("nit_peluqueria_pertenese", NITBarberoPertenese);
                    parametros.put("h_inicio", hora_inicio);
                    parametros.put("h_fin", hora_fin);


                    parametros.put("imagen", imagen);
                    return parametros;
                }
            };

            request.add(stringRequestS);

        } else {
            Toast.makeText(MainActivityCompleteRegister.this, "Error, debe verificar el NIT de su peluqueria.", Toast.LENGTH_LONG).show();

        }
    }

    //Registra el usuario si es Cliente
    private void RegistrarUsuarioClienteWebBD() {


        //Inicio los datos de Usuario
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();

        request = Volley.newRequestQueue(getApplicationContext());
        cellphone = telefono.getText().toString();
        Password = etPassword.getText().toString();

        //inicializo et de peluquerias
        direccionET = findViewById(R.id.etxt_peluqueriaDireccion);
miCiudad=spinerCiudad.getSelectedItem().toString();

        //Lleno los demas datos relacionados con el barbero
        hora_inicio = etHora_inicio.getText().toString();
       // NitString = NitPeluqueria.getText().toString();
        hora_fin = etHora_fin.getText().toString();
        //nombrePeluqueria = nombre_peluqueria.getText().toString();
        //telPeluqueria = telefono_peluqueria.getText().toString();
        direccion = direccionET.getText().toString();

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/registrar_UsuarioIMG.php?";
        Log.e("URL DEL POST", url);

        stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.e("RESPUESTA: ", "" + response);
                if (response.trim().equalsIgnoreCase("registra")) {
                    salvarPermanente();
                    Toast.makeText(MainActivityCompleteRegister.this, "Se ha registrado con exito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivityCompleteRegister.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RESPUESTA: ", "" + error);
                Toast.makeText(MainActivityCompleteRegister.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String imagen = convertirImgString(bitmap);
                Utilidades utilidad = new Utilidades();
                Map<String, String> parametros = new HashMap<>();
                parametros.put("tel_usuario", cellphone);
                parametros.put("id_firebase", id_firebase);
                parametros.put("nombre_usuario", name);
                parametros.put("apellido_usuario", lastname);
                parametros.put("password_usuario", utilidad.Encriptar(Password));
                parametros.put("email_usuario", email);
                parametros.put("ciudad_usuario", miCiudad);
                parametros.put("rol_usuario", rol + "");


                parametros.put("imagen", imagen);
                return parametros;
            }
        };

        request.add(stringRequestS);

    }



    //Onresponses de la consulta de nit
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i("URL REGISTRED ERROR","RESPUESTA "+ error.toString());

        //El nit que se quiere comprobar fallo el registr

        if(error.getMessage().toString().equals("java.net.UnknownHostException: Unable to resolve host \"wayloo.000webhostapp.com\": No address associated with hostname")){
            Toast.makeText(MainActivityCompleteRegister.this, "Error, No se pudo conectar al servidor verifique la conexión",Toast.LENGTH_LONG).show();
        }else{
            //Si el error value insert es por que se esta ingresando un dato
            if(rol==1 || rol ==3 || error.getMessage().toString().equals("org.json.JSONException: Value INSERT of type java.lang.String cannot be converted to JSONObject")){
                salvarPermanente();
            }else{
                NITVerificado=false;
                NITalQuePerteneceBarbero.setTextColor(Color.RED);
                Toast.makeText(MainActivityCompleteRegister.this, "Error, NIT no encontrado, la peluqueria debe de estar registrada",Toast.LENGTH_LONG).show();
            }
        }
        hideProgressDialog();
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.i("VOLLEY ANSWER","VOLEY EXITOSO");
         if(rol==2){
            //Creo el objeto nit
            nitp miNIT = new nitp();
            //Busco el arreglo en el json llamado peluquera
            JSONArray json = response.optJSONArray("peluqueria");
            JSONObject jsonObject = null;

            try {
                //saco el objeto json
                jsonObject = json.getJSONObject(0);
                //Grabo el nit del json en el objeto clase minIT
                miNIT.setNoNIT(jsonObject.optString("nit_peluqueria"));
    //Verifico que no esta vacio
                if(miNIT.getNoNIT() != null){
                    hideProgressDialog();
                    NITVerificado=true;// El nit que se quiere comprobar a sido aprobado
                    //Coloco el texto en verde
                    NITalQuePerteneceBarbero.setTextColor(Color.GREEN);
                    Toast.makeText(MainActivityCompleteRegister.this, "NIT Verificado, ya puede terminar el registro.", Toast.LENGTH_LONG).show();

                }else{
                    //El nit que se quiere comprobar fallo el registro
                    NITVerificado=false;
                    NITalQuePerteneceBarbero.setTextColor(Color.RED);
                    Toast.makeText(MainActivityCompleteRegister.this, "Error esta barberia aun no ha sido registrada."+NITVerificado, Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            } catch (JSONException e) {
                e.printStackTrace();
         }}
     }


    private void consultarPeluqueriaExiste(final String NITAVerificar) {
        request = Volley.newRequestQueue(getApplicationContext());
        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consultarPeluqueriaExistePOST.php?";
        Log.e("URL DEL POST", url);

        stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.i("RESPUESTA: ", "" + response);
                if (response.trim().equalsIgnoreCase("404")) {
                    NITalQuePerteneceBarbero.setTextColor(Color.RED);
                    Toast.makeText(MainActivityCompleteRegister.this, "Barberia no encontrada", Toast.LENGTH_SHORT).show();
                    NITVerificado = false;

                } else {
                    Toast.makeText(MainActivityCompleteRegister.this, "Barberia Encontrada", Toast.LENGTH_SHORT).show();
                    btRegistrar.setClickable(true);
                    NITVerificado = true;// El nit que se quiere comprobar a sido aprobado
                    //Coloco el texto en verde
                    NITalQuePerteneceBarbero.setTextColor(Color.GREEN);
                    btRegistrar.setText("REGISTRAR");
                    Toast.makeText(MainActivityCompleteRegister.this, "NIT Verificado, ya puede terminar el registro.", Toast.LENGTH_LONG).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivityCompleteRegister.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> parametros = new HashMap<>();
                parametros.put("NITID", NITAVerificar);

                return parametros;
            }
        };

        request.add(stringRequestS);
    }



    private void obtenerHora_inicio() {
                  TimePickerDialog recogerHora = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //Formateo el hora obtenido: antepone el 0 si son menores de 10
                String horaFormateada = (hourOfDay < 10) ? String.valueOf(CERO + hourOfDay) : String.valueOf(hourOfDay);
                //Formateo el minuto obtenido: antepone el 0 si son menores de 10
                String minutoFormateado = (minute < 10) ? String.valueOf(CERO + minute) : String.valueOf(minute);
                //Obtengo el valor a.m. o p.m., dependiendo de la selección del usuario
                String AM_PM;
                if (hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }
                //Muestro la hora con el formato deseado
                etHora_inicio.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
                hora_inicio=etHora_inicio.getText().toString();
            }
            //Estos valores deben ir en ese orden
            //Al colocar en false se muestra en formato 12 horas y true en formato 24 horas
            //Pero el sistema devuelve la hora en formato 24 horas
        }, hora, minuto, false);

        recogerHora.show();

    }

    private void obtenerHora_fin() {
        TimePickerDialog recogerHora = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //Formateo el hora obtenido: antepone el 0 si son menores de 10
                String horaFormateada = (hourOfDay < 10) ? String.valueOf(CERO + hourOfDay) : String.valueOf(hourOfDay);
                //Formateo el minuto obtenido: antepone el 0 si son menores de 10
                String minutoFormateado = (minute < 10) ? String.valueOf(CERO + minute) : String.valueOf(minute);
                //Obtengo el valor a.m. o p.m., dependiendo de la selección del usuario
                String AM_PM;
                if (hourOfDay < 12) {
                    AM_PM = "a.m.";
                } else {
                    AM_PM = "p.m.";
                }
                //Muestro la hora con el formato deseado
                etHora_fin.setText(horaFormateada + DOS_PUNTOS + minutoFormateado + " " + AM_PM);
                hora_fin=etHora_fin.getText().toString();
            }
            //Estos valores deben ir en ese orden
            //Al colocar en false se muestra en formato 12 horas y true en formato 24 horas
            //Pero el sistema devuelve la hora en formato 24 horas
        }, hora, minuto, false);

        recogerHora.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // por defecto si escribes aquí el super el botón hará lo que debía hacer si lo quitas ya no hará lo que debía de hacer y puedes programar otros comportamientos.
        Intent intent = new Intent(MainActivityCompleteRegister.this, MainLogginActivity.class);
        Toast.makeText(MainActivityCompleteRegister.this, "Registro no completo!",Toast.LENGTH_LONG).show();
        startActivity(intent);
        finish();
    }

    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(MainActivityCompleteRegister.this, titulo,
                mensaje, true);
    }
    private void  hideProgressDialog(){progress.dismiss();}

    private void salvarPermanente(){
        //Save registro permanente
        SQLiteDatabase db = usdbh.getWritableDatabase();
        db.execSQL("INSERT INTO CurrentUsuario VALUES ('"+id_firebase+"','"+name+"','"+email+"','true',"+rol+",'"+telefono.getText()+"')");
        Intent intent = new Intent(MainActivityCompleteRegister.this, MainActivity.class);
        hideProgressDialog();
        cambiarClaveFirebase(Password);
        startActivity(intent);
        finish();
    }

    public void redondearCanvas(ImageView img){
        ImageView mimageView = img;

        BitmapDrawable drawable = (BitmapDrawable) mimageView.getDrawable();
        Bitmap mbitmap = drawable.getBitmap();

        //B//itmap mbitmap=((BitmapDrawable) getResources().getDrawable(R.drawable.cat)).getBitmap();
        Bitmap imageRounded=Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
        Canvas canvas=new Canvas(imageRounded);
        Paint mpaint=new Paint();
        mpaint.setAntiAlias(true);
        mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 360, 360, mpaint); // Round Image Corner 100 100 100 100
        mimageView.setImageBitmap(imageRounded);
    }

    //permisos
    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        if((MainActivityCompleteRegister.this.checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&MainActivityCompleteRegister.this.checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
            return true;
        }


        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||(shouldShowRequestPermissionRationale(CAMERA)))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo=new AlertDialog.Builder(MainActivityCompleteRegister.this);
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
            }
        });
        dialogo.show();
    }
    private void mostrarDialogOpciones() {
        final CharSequence[] opciones = {"Tomar Foto", "Elegir de la Galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityCompleteRegister.this);
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
                String authorities = MainActivityCompleteRegister.this.getPackageName() + ".provider";
                Uri imageUri = FileProvider.getUriForFile(MainActivityCompleteRegister.this, authorities, fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            startActivityForResult(intent, COD_FOTO);

            ////

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            Log.e("Respuesta ", "ON ACTIVITY" + requestCode + " data " + data + " is null? " + data.toString().equals(null));
            super.onActivityResult(requestCode, resultCode, data);


            switch (requestCode) {
                case COD_SELECCIONA:
                    if (data.toString().equals("")) {

                    } else {
                        Uri miPath = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(MainActivityCompleteRegister.this.getContentResolver(), miPath);
                            imgPerfil.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 200, 200, false));
                            //imgPerfil.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case COD_FOTO:
                    MediaScannerConnection.scanFile(MainActivityCompleteRegister.this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Path", "" + path);
                                }
                            });

                    bitmap = BitmapFactory.decodeFile(path);
                    imgPerfil.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 200, 200, false));
                    //  redondearCanvas(imgPerfil);
                    break;
            }
            bitmap = redimensionarImagen(bitmap, 5, 10);
        }catch (NullPointerException e){
            Toast.makeText(this, "Foto, vacia", Toast.LENGTH_SHORT).show();
        }catch (Exception ex){
            Toast.makeText(this, "Error, vacio", Toast.LENGTH_SHORT).show();
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

    private void cambiarClaveFirebase(String pssw){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String newPassword = pssw;

        user.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.e("Clave cambiada", "User password updated.");
                        }
                    }
                });
    }
}
