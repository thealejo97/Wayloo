package com.wayloo.wayloo;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.Utilidades;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class MainActivityRegistarUser extends AppCompatActivity {

    ////Edits que ingresa el usuario
    private TextView cellPhoneNumber;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etLastname;
    private EditText etHora_inicio;
    private EditText etHora_fin;
    private TextView regresarLogin;
    private EditText etCity;
    private TextView titulo;
    private EditText NitPeluqueria;
    private EditText direccionET;
    private EditText nombre_peluqueria;
    private EditText telefono_peluqueria;
    private EditText NITalQuePerteneceBarbero;
    private Button ButonVerificarNIT, ButtonPickImagen;
    private ImageView imgPerfil;


    StringRequest stringRequestS;
    private static final String CARPETA_PRINCIPAL = "misImagenesApp/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    private String path;//almacena la ruta de la imagen
    File fileImagen;
    Bitmap bitmap;

    FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    private final int MIS_PERMISOS = 100;

    private RequestQueue request;
    private RequestQueue request2;
    private RequestQueue request3;
    private RequestQueue request4;
    JsonObjectRequest jsonObjectRequest;

    private ImageButton ibObtenerHora;
    private ImageButton ibObtenerHora_fin;
    private Button btnRegistrar;

    private Spinner spinerRol;
    private Spinner spinner;

    String cellphone;
    String name;
    String id_firebase;
    String email;
    String Password;
    String lastname;
    String NitString;
    String nombrePeluqueria;
    String telPeluqueria;
    String direccion;
    String NITBarberoPertenese;
    int rol = 0;
    String hora_inicio;
    String hora_fin;
    String miCiudad;
    private Boolean NITVerificado = false;

    //Fotos
    private ImageView mPhotoImageView;
    public static final int REQUEST_CODE_TAKE_PHOTO = 0 /*1*/;
    private String mCurrentPhotoPath;
    private Uri photoURI;


    //Progress
    ProgressDialog progress;
    //Creamos la BD interna
    UsuariosSQLiteHelper usdbh =
            new UsuariosSQLiteHelper(MainActivityRegistarUser.this, "dbUsuarios", null, 1);
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    //////// Picker Fecha
    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";

    //Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    //Variables para obtener la hora hora
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        ///////////////////////////////////////////////////////////////// Views ///////////////////////////////////////////////////////////////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_registar_user);

/*
        spinner = findViewById(R.id.spinnerRol);
        ArrayAdapter adapterA = ArrayAdapter.createFromResource(this,
                R.array.roles, R.layout.color_spinner_layout);
        adapterA.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapterA);*/

        //Referenciamos los objetos
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ////////////////////////////////////////////////////////////// inicializo los elementos base del usuario EditTexts ///////////////////////////////////////////////////////////////
        imgPerfil = findViewById(R.id.imageViewProfileFoto);
        ButtonPickImagen = findViewById(R.id.buttonImgperfil);
        mPhotoImageView = findViewById(R.id.imageViewProfileFoto);
        cellPhoneNumber = findViewById(R.id.etxt_cellPhone);
        etName = findViewById(R.id.etxt_name);
        etLastname = findViewById(R.id.etxt_lastname);
        etEmail = findViewById(R.id.etxt_email);
        etPassword = findViewById(R.id.etxt_password);
        etCity = findViewById(R.id.etxt_CIUDAD);
        NITalQuePerteneceBarbero = findViewById(R.id.EtxNitPeluqueriaPerteneceBarbero);
        spinner = (Spinner) findViewById(R.id.ciudad_spinner);
/*
        ArrayList<String> ArraylistCiudadesFRstr = new ArrayList<>();

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setConfigSettings(new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build());
        HashMap<String, Object> defaultData = new HashMap<>();
        defaultData.put("ciudadesAc","Cali,Jamundi");
        defaultData.put("datosPruebaPapu","nadaa");
        defaultData.put("playurl","Cali,Jamundi");
        defaultData.put("versionname","1.0");



//        firebaseRemoteConfig.setDefaults(defaultData);
        Task<Void> fetch = firebaseRemoteConfig.fetch(0);
        fetch.addOnSuccessListener(MainActivityRegistarUser.this, aVoid ->{
            firebaseRemoteConfig.activateFetched();
            Log.e("Datos en fb",  "-"+firebaseRemoteConfig.getString("ciudadesAc") +"-");
        }).addOnFailureListener(MainActivityRegistarUser.this, aVoid -> {
            Log.e("Datos en fb",  "Fetch faild");
            Toast.makeText(MainActivityRegistarUser.this, "Fetch failed",
                    Toast.LENGTH_SHORT).show();
          //  Log.e("Task result", getException().getMessage());
        });
/*
        Toast.makeText(this, strCiudPR[0], Toast.LENGTH_SHORT).show();
       Log.e("Ciudades Traidas", strCiud[0]+"Esto es");

        String[] ciudades = null;
        //strCiud[0].split(",");

        for(int i = 0; i<ciudades.length; i++) {
        ArraylistCiudadesFRstr.add(ciudades[i]);
            Log.e("Ciudad" + i, ciudades[i]);
        }
        ArrayAdapter ArraylistCiudadesFRFire = new ArrayAdapter(this,R.layout.color_spinner_layout, ArraylistCiudadesFRstr);

   /*    // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.citys_array, R.layout.color_spinner_layout);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        // Apply the adapter to the spinner
        spinner.setAdapter(ArraylistCiudadesFRFire);*/
        //otros items inicializan
        regresarLogin = findViewById(R.id.txt_backlogin);
        titulo = findViewById(R.id.tituloSeparador);

        /////////////////////////////////////////////////  PARA SELECCIONAR LA IMAGEN /////////////////////////////////////////////////

        ButtonPickImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solicitaPermisosVersionesSuperiores()) {
                    mostrarDialogOpciones();
                }
            }
        }); // versiones


        ///////////////////////////////////////////////// PARA VERIFICAR EL NIT (EN CASO DE QUE SEA BARBERO) /////////////////////////////////////////////////
        ButonVerificarNIT = findViewById(R.id.ButtonVerificarNIT);
        // Si presiona verificar
        ButonVerificarNIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Verificando NIT", "Espere mientras se contacta con el servidor");
                //Realiza y verifica que el nit ingresado ya este registrado
                if (NITalQuePerteneceBarbero.getText().toString().isEmpty()) {
                    hideProgressDialog();
                    Toast.makeText(MainActivityRegistarUser.this, "El campo NIT a verificar no debe de estar vacio.", Toast.LENGTH_SHORT).show();
                } else {
                    consultarPeluqueriaExiste(NITalQuePerteneceBarbero.getText().toString());
                }
            }
        });

        NITalQuePerteneceBarbero.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                NITalQuePerteneceBarbero.setTextColor(Color.RED);
                btnRegistrar.setClickable(false);
                btnRegistrar.setText("Debe verificar el NIT para registrarse como barbero");
                NITVerificado = false;// El nit que se quiere comprobar a sido aprobado

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ///////////////////////////////////////////////// PARA REGISTRARSE /////////////////////////////////////////////////
        btnRegistrar = findViewById(R.id.btn_register);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inicializo los parametros del usuario Strings
                cellphone = cellPhoneNumber.getText().toString();
                name = etName.getText().toString();
                lastname = etLastname.getText().toString();
                email = etEmail.getText().toString();
                Password = etPassword.getText().toString();
                miCiudad = spinner.getSelectedItem().toString();
                //No inicializo la ciudad ya que se selecciona dependiendo de la ubicacion

                if (name.isEmpty() && lastname.isEmpty() && cellphone.isEmpty() && email.isEmpty() && Password.isEmpty()) {
                    Toast.makeText(MainActivityRegistarUser.this, "Error los campos no deben de estar vacios.", Toast.LENGTH_SHORT).show();
                } else {
                    if (Password.length() > 6) {
                        if (validarEmail()) {
                            //Inicializa todos y registra en firebase
                            RegistrarUsuarioFirebaseDataBase();
                        } else {
                            Toast.makeText(MainActivityRegistarUser.this, "Error Email invalido, verificar.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivityRegistarUser.this, "Error el password debe tener minimo 6 caracteres.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        /////////////////////////////////////////////////////////////// Cuando se cambia el ROL ///////////////////////////////////////////////////////////////
        ArrayList<String> stringsRoles = new ArrayList<>();
        stringsRoles.add("Seleccione su rol");
        stringsRoles.add("Administrador");
        stringsRoles.add("Barbero");
        stringsRoles.add("Cliente");
        spinerRol = findViewById(R.id.spinnerRol);
        ArrayAdapter adapterRol = new ArrayAdapter(this,R.layout.color_spinner_layout, stringsRoles) ;
        // Apply the adapter to the spinner*/
        adapterRol.setDropDownViewResource(R.layout.spinner_dropdown);
        spinerRol.setAdapter(adapterRol);
        spinerRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String currentItem = spinerRol.getSelectedItem().toString();

                if (currentItem.equals("Seleccione su rol")) {
                    rol = 3;
                    View v = findViewById(R.id.linealBarbetoCompleto);
                    v.setVisibility(View.GONE);
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.GONE);

                    titulo.setVisibility(View.GONE);

                }
                if (currentItem.equals("Administrador")) {
                    rol = 1;
                    btnRegistrar.setClickable(true);
                    btnRegistrar.setText("REGISTRAR");
                    View v = findViewById(R.id.linealBarbetoCompleto);
                    v.setVisibility(View.GONE);
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.VISIBLE);
                    titulo.setVisibility(View.VISIBLE);
                    titulo.setText("INGRESE LOS DATOS DE SU PELUQUERIA");
                }
                if (currentItem.equals("Barbero")) {
                    rol = 2;
                    btnRegistrar.setClickable(false);
                    btnRegistrar.setText("Debe verificar el NIT para registrarse como barbero");
                    View v = findViewById(R.id.linealBarbetoCompleto);
                    v.setVisibility(View.VISIBLE);
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.GONE);

                    titulo.setVisibility(View.VISIBLE);
                    titulo.setText("INGRESE SU HORARIO DE TRABAJO");
                }
                if (currentItem.equals("Cliente")) {
                    btnRegistrar.setText("REGISTRAR");
                    btnRegistrar.setClickable(true);
                    rol = 3;
                    View v = findViewById(R.id.linealBarbetoCompleto);
                    v.setVisibility(View.GONE);
                    View v3 = findViewById(R.id.LinealAdministrador);
                    v3.setVisibility(View.GONE);

                    titulo.setVisibility(View.GONE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                rol = 0;
                btnRegistrar.setClickable(false);
                btnRegistrar.setText("REGISTRAR");
                View v = findViewById(R.id.linealBarbetoCompleto);
                v.setVisibility(View.GONE);
                View v3 = findViewById(R.id.LinealAdministrador);
                v3.setVisibility(View.GONE);

                titulo.setVisibility(View.GONE);
            }

        });

        //////////////////////////////////////////////////////////////////// Para fechas ////////////////////////////////////////////////////////////////////////////////////////////////////////
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
        ///////////////////////////////////////////////////////////////// Para el Edit text vuelva  al loggin ///////////////////////////////////////////////////////////////
        regresarLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityRegistarUser.this, MainLogginActivity.class));
                finish();
            }
        });

        /////////////////////////////////////////////////////////////// fin ///////////////////////////////////////////////////////////////
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
        fetch.addOnSuccessListener(MainActivityRegistarUser.this, aVoid ->{
            firebaseRemoteConfig.activateFetched();
            Log.e("Datos en fb",  "-"+firebaseRemoteConfig.getString("ciudadesAc") +"-");
        }).addOnFailureListener(MainActivityRegistarUser.this, aVoid -> {
            Log.e("Datos en fb",  "Fetch faild" );
            Toast.makeText(MainActivityRegistarUser.this, "Fetch failed",
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

        ArrayAdapter ArraylistCiudadesFRFire = new ArrayAdapter(this,R.layout.color_spinner_layout, ArraylistCiudadesFRstr) ;
        // Apply the adapter to the spinner*/
        ArraylistCiudadesFRFire.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(ArraylistCiudadesFRFire);
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
                String authorities = MainActivityRegistarUser.this.getPackageName() + ".provider";
                Uri imageUri = FileProvider.getUriForFile(MainActivityRegistarUser.this, authorities, fileImagen);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityRegistarUser.this);
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

        if(resultCode != RESULT_CANCELED){
            switch (requestCode) {
                case COD_SELECCIONA:
                    Uri miPath = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(MainActivityRegistarUser.this.getContentResolver(), miPath);
                        imgPerfil.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 300, 300, false));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case COD_FOTO:
                    MediaScannerConnection.scanFile(MainActivityRegistarUser.this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Path", "" + path);
                                }
                            });

                    bitmap = BitmapFactory.decodeFile(path);
                    imgPerfil.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 200, 200, false));

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
    // Obtener las horas
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
                etHora_inicio.setText(horaFormateada + DOS_PUNTOS + minutoFormateado );
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
                etHora_fin.setText(horaFormateada + DOS_PUNTOS + minutoFormateado );
            }
            //Estos valores deben ir en ese orden
            //Al colocar en false se muestra en formato 12 horas y true en formato 24 horas
            //Pero el sistema devuelve la hora en formato 24 horas
        }, hora, minuto, false);

        recogerHora.show();

    }

    //Registrar usuario en Firebase
    private void RegistrarUsuarioFirebaseDataBase() {
        try {



    //        Toast.makeText(MainActivityRegistarUser.this, rol + "Registrando en firebase" + email + Password, Toast.LENGTH_LONG).show();
            mAuth.createUserWithEmailAndPassword(email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        id_firebase = mAuth.getCurrentUser().getUid();
                        Log.e("usuario","Entro al roll"+ rol);
                        if (rol == 0) {
                            Toast.makeText(MainActivityRegistarUser.this, "Error, debe seleccionar su rol", Toast.LENGTH_LONG).show();
                        } else {
                            if (rol == 1) {
                                showProgressDialog("Registrando", "Espere mientras se conecta con el servidor. AD");
                                RegistrarUsuarioAdmin();

                            } else {
                                if (rol == 2) {
                                    showProgressDialog("Registrando", "Espere mientras se conecta con el servidor. BR");
                                    RegistrarUsuarioBarbero();

                                } else {
                                    if (rol == 3) {
                                        Log.e("Topo de usuario","Entro al rol cliente "+ rol);
                                        showProgressDialog("Registrando", "Espere mientras se conecta con el servidor. CL");
                                        RegistrarUsuarioClienteWebBD();

                                    }
                                }
                            }

                        }
                    } else {


                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(MainActivityRegistarUser.this, "Error usuario ya registrado.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivityRegistarUser.this, "Error, de registro verifique su conexión a internet", Toast.LENGTH_LONG).show();
                            Log.e("Error de registro,", task.getResult().toString());
                        }
                    }
                }
            });
        } catch (RuntimeExecutionException e) {
            Toast.makeText(this, "ERROR, EMAIL YA REGITRADO", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarEmail() {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private void RegistrarUsuarioAdmin() {
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();

        request2 = Volley.newRequestQueue(getApplicationContext());
        cellphone = cellPhoneNumber.getText().toString();
        name = etName.getText().toString();
        lastname = etLastname.getText().toString();
        email = etEmail.getText().toString();
        Password = etPassword.getText().toString();

        //inicializo et de peluquerias
        direccionET = findViewById(R.id.etxt_peluqueriaDireccion);
        NitPeluqueria = findViewById(R.id.etxt_PeluqueriaNit);
        nombre_peluqueria = findViewById(R.id.etxt_PeluqueriaNombre);
        telefono_peluqueria = findViewById(R.id.etxt_PeluqueriaTelefono);

        //Lleno los demas datos relacionados con el barbero
        hora_inicio = etHora_inicio.getText().toString();
        NitString = NitPeluqueria.getText().toString();
        hora_fin = etHora_fin.getText().toString();
        nombrePeluqueria = nombre_peluqueria.getText().toString();
        telPeluqueria = telefono_peluqueria.getText().toString();
        direccion = direccionET.getText().toString();

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/registrar_UsuarioIMG.php?";
        Log.e("URL DEL POST", url);

        stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.i("RESPUESTA adm: ", "" + response);
                if (response.trim().equalsIgnoreCase("registra")) {
                    salvarPermanente();
                    Toast.makeText(MainActivityRegistarUser.this, "Se ha registrado con exito", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        deleteUserFirebase(email,Password);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivityRegistarUser.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivityRegistarUser.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
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

        request2.add(stringRequestS);
    }

    private void RegistrarUsuarioBarbero() {

        //Inicio los datos de Usuario
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();

        request4 = Volley.newRequestQueue(getApplicationContext());
        cellphone = cellPhoneNumber.getText().toString();
        name = etName.getText().toString();
        lastname = etLastname.getText().toString();
        email = etEmail.getText().toString();
        Password = etPassword.getText().toString();
        CheckBox chl,chm,chmi,chj,chv,chs,chd;
        chl= findViewById(R.id.chLunes);
        chm= findViewById(R.id.chMartes);
        chmi= findViewById(R.id.chMiercoles);
        chj= findViewById(R.id.chJueves);
        chv= findViewById(R.id.chViernes);
        chs= findViewById(R.id.chSabado);
        chd= findViewById(R.id.chDomingo);

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


        Log.e("String diasl", diasLaborales);
        //inicializo et de peluquerias
        direccionET = findViewById(R.id.etxt_peluqueriaDireccion);
        NitPeluqueria = findViewById(R.id.etxt_PeluqueriaNit);
        nombre_peluqueria = findViewById(R.id.etxt_PeluqueriaNombre);
        telefono_peluqueria = findViewById(R.id.etxt_PeluqueriaTelefono);

        //Lleno los demas datos relacionados con el barbero
        hora_inicio = etHora_inicio.getText().toString();
        NitString = NitPeluqueria.getText().toString();
        hora_fin = etHora_fin.getText().toString();
        nombrePeluqueria = nombre_peluqueria.getText().toString();
        telPeluqueria = telefono_peluqueria.getText().toString();
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
                    Log.e("RESPUESTA buena: ", "" + response);
                    if (response.trim().equalsIgnoreCase("registra")) {
                        salvarPermanente();
                        Toast.makeText(MainActivityRegistarUser.this, "Se ha registrado con exito", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            deleteUserFirebase(email,Password);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivityRegistarUser.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("RESPUESTA: mala", "" + error);
                    Toast.makeText(MainActivityRegistarUser.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
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
                    //Datos de la peluqueria del administrador
                    NITBarberoPertenese = NITalQuePerteneceBarbero.getText().toString();
                    Log.e("Peluqueria pertenese", NITBarberoPertenese);
                    parametros.put("nit_peluqueria_pertenese", NITBarberoPertenese);
                    parametros.put("h_inicio", hora_inicio);
                    parametros.put("h_fin", hora_fin);
                    parametros.put("diasl", finalDiasLaborales);




                    return parametros;
                }
            };
            Log.e("Horas a conver", hora_inicio + hora_fin);
            Date horaINICIOBARBEROTURNO = convertirHoraADate(hora_inicio);
            Date horaFINBARBEROTURNO = convertirHoraADate(hora_fin);
            Log.e("Horas ", horaINICIOBARBEROTURNO.toString() + "   "+ horaFINBARBEROTURNO.toString());
            Log.e("Comparacion Horas ", horaINICIOBARBEROTURNO.compareTo(horaFINBARBEROTURNO) + "");

            if(horaINICIOBARBEROTURNO.compareTo(horaFINBARBEROTURNO) > 0){
                try {
                    deleteUserFirebase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hideProgressDialog();
                Toast.makeText(this, "Error horas no validas, verificar.", Toast.LENGTH_SHORT).show();
            }else {
                request4.add(stringRequestS);
            }
        } else {
            Toast.makeText(MainActivityRegistarUser.this, "Error, debe verificar el NIT de su peluqueria.", Toast.LENGTH_LONG).show();

        }
    }

    private void RegistrarUsuarioClienteWebBD() {


        //Inicio los datos de Usuario
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap();

        request4 = Volley.newRequestQueue(getApplicationContext());
        cellphone = cellPhoneNumber.getText().toString();
        name = etName.getText().toString();
        lastname = etLastname.getText().toString();
        email = etEmail.getText().toString();
        Password = etPassword.getText().toString();

        //inicializo et de peluquerias
        direccionET = findViewById(R.id.etxt_peluqueriaDireccion);
        NitPeluqueria = findViewById(R.id.etxt_PeluqueriaNit);
        nombre_peluqueria = findViewById(R.id.etxt_PeluqueriaNombre);
        telefono_peluqueria = findViewById(R.id.etxt_PeluqueriaTelefono);

        //Lleno los demas datos relacionados con el barbero
        hora_inicio = etHora_inicio.getText().toString();
        NitString = NitPeluqueria.getText().toString();
        hora_fin = etHora_fin.getText().toString();
        nombrePeluqueria = nombre_peluqueria.getText().toString();
        telPeluqueria = telefono_peluqueria.getText().toString();
        direccion = direccionET.getText().toString();
        //if (NITVerificado) {

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
                    Toast.makeText(MainActivityRegistarUser.this, "Se ha registrado con exito", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        deleteUserFirebase(email,Password);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivityRegistarUser.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RESPUESTA: ", "" + error);
                Toast.makeText(MainActivityRegistarUser.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String imagen = convertirImgString(bitmap);
                //imagen="estoEsUnaImagen";
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
Log.e("Datos usu Reg ", "cel " + cellphone+"idf " +id_firebase+"nom " + name+lastname+utilidad.Encriptar(Password)+email+miCiudad+rol +" img "+imagen);

                return parametros;
            }
        };

        request4.add(stringRequestS);

    }

    private void deleteUserFirebase(final String emailU, String pwU) throws Exception {
        Log.e("Delete", "ingreso a deleteAccount");


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(emailU, emailU);

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("Usuario eliminar", "User account deleted.");
                                        } else {
                                            Log.d("Usuario eliminar", "error eliminanod." + task.getResult());
                                        }
                                    }
                                });

                    }
                });

    }

    private String convertirImgString(Bitmap bitmap) {

//Calidad Imagen convertida
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);

        return imagenString;
    }

    private void consultarPeluqueriaExiste(final String NITAVerificar) {
        request3 = Volley.newRequestQueue(getApplicationContext());
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
                    Toast.makeText(MainActivityRegistarUser.this, "Barberia no encontrada", Toast.LENGTH_SHORT).show();
                    NITVerificado = false;

                } else {
                    Toast.makeText(MainActivityRegistarUser.this, "Barberia Encontrada", Toast.LENGTH_SHORT).show();
                    btnRegistrar.setClickable(true);
                    NITVerificado = true;// El nit que se quiere comprobar a sido aprobado
                    //Coloco el texto en verde
                    NITalQuePerteneceBarbero.setTextColor(Color.GREEN);
                    btnRegistrar.setText("REGISTRAR");
                    Toast.makeText(MainActivityRegistarUser.this, "NIT Verificado, ya puede terminar el registro.", Toast.LENGTH_LONG).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivityRegistarUser.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
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

        request3.add(stringRequestS);
    }

    private void salvarPermanente(){
        //Save registro permanente
        SQLiteDatabase db = usdbh.getWritableDatabase();
        db.execSQL("INSERT INTO CurrentUsuario VALUES ('"+id_firebase+"','"+name+"','"+email+"','true',"+ rol+",'"+cellphone+"')");
        Intent intent = new Intent(MainActivityRegistarUser.this, MainActivity.class);
        hideProgressDialog();
        startActivity(intent);
        finish();
    }
//permisos
    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        if((MainActivityRegistarUser.this.checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&MainActivityRegistarUser.this.checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
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
        AlertDialog.Builder dialogo=new AlertDialog.Builder(MainActivityRegistarUser.this);
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

    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(MainActivityRegistarUser.this, titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}

    private Date convertirHoraADate(String stringFecha) {
        Date date = null;
        String pattern = "HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        try {
            date = simpleDateFormat.parse(stringFecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainActivityRegistarUser.this, MainLogginActivity.class);
        startActivity(intent);
        finish();
    }
}



