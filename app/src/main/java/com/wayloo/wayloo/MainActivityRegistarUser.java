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
import androidx.annotation.RequiresApi;
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
    private TextView regresarLogin;
    private Button ButtonPickImagen;
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

    private Button btnRegistrar;


    private Spinner spinnerCiudad;

    String cellphone;
    String name;
    String id_firebase;
    String email;
    String Password;
    String lastname;
    String miCiudad;
    private Boolean NITVerificado = false;

    //Fotos
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

        //Referenciamos los objetos
        mAuth = FirebaseAuth.getInstance();// Crea la instancia de Firebase

        ////////////////////////////////////////////////////////////// inicializo los elementos base del usuario EditTexts ///////////////////////////////////////////////////////////////
        imgPerfil = findViewById(R.id.imageViewProfileFoto);// Imagen
        ButtonPickImagen = findViewById(R.id.buttonImgperfil);// Seleccionar imagen de perfil
        cellPhoneNumber = findViewById(R.id.etxt_cellPhone);// Telefono celular
        etName = findViewById(R.id.etxt_name);// Nombre
        etLastname = findViewById(R.id.etxt_lastname);//Apellido
        etEmail = findViewById(R.id.etxt_email);// Email
        etPassword = findViewById(R.id.etxt_password); // Clave
        spinnerCiudad = (Spinner) findViewById(R.id.ciudad_spinner); //Spinner ciudad
        regresarLogin = findViewById(R.id.txt_backlogin);// Texto que al presionar vuelve atras
        btnRegistrar = findViewById(R.id.btn_register);

        /////////////////////////////////////////////////  PARA SELECCIONAR LA IMAGEN /////////////////////////////////////////////////
//Cuando se presiona click en el burronImage picker ejecuto mostrar dialogo
        ButtonPickImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solicitaPermisosVersionesSuperiores()) {
                    //Pide permisos android
                    mostrarDialogOpciones();
                }
            }
        });

        ///////////////////////////////////////////////// CLICK PARA REGISTRARSE /////////////////////////////////////////////////

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Inicializo los parametros del usuario Strings
                cellphone = cellPhoneNumber.getText().toString();
                name = etName.getText().toString();
                lastname = etLastname.getText().toString();
                email = etEmail.getText().toString();
                Password = etPassword.getText().toString();
                miCiudad = spinnerCiudad.getSelectedItem().toString();

                if (name.isEmpty() || lastname.isEmpty() || cellphone.isEmpty() || email.isEmpty() || Password.isEmpty()) {
                    Toast.makeText(MainActivityRegistarUser.this, "Error los campos no deben de estar vacios.", Toast.LENGTH_SHORT).show();
                } else {
                    if (Password.length() > 6) {
                        if (validarEmail()) {
                            //Inicializa todos y registra en firebase
                            RegistrarUsuarioFirebase();
                        } else {
                            Toast.makeText(MainActivityRegistarUser.this, "Error Email invalido, verificar.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivityRegistarUser.this, "Error el password debe tener minimo 6 caracteres.", Toast.LENGTH_SHORT).show();
                    }
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
        spinnerCiudad.setAdapter(ArraylistCiudadesFRFire);
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

    //Registrar usuario en Firebase
    private void RegistrarUsuarioFirebase() {
        try {
            mAuth.createUserWithEmailAndPassword(email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {// Inicio la creación
                // del usuario en Firebase
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        id_firebase = mAuth.getCurrentUser().getUid();
                        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Se envio el correo exitosamente
                                //Escribir en 000
                                registrarUsuario();
                                //Login
                                Toast.makeText(MainActivityRegistarUser.this, "Se ha enviado un correo de verificación", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {

                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Log.e("Error de registro dupl", "Error Usuario duplicado");
                            Toast.makeText(MainActivityRegistarUser.this, "Error usuario ya registrado.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivityRegistarUser.this, "Error, de registro verifique su conexión a internet", Toast.LENGTH_LONG).show();
                            Log.e("Error de registro con", task.getResult().toString());
                        }
                    }
                }
            });
        } catch (RuntimeExecutionException e) {
            Toast.makeText(this, "ERROR, EMAIL YA REGITRADO", Toast.LENGTH_SHORT).show();
        }
    }
//Registra el usuario en la bd SQL Externa
    private void registrarUsuario() {

        showProgressDialog("Registrando.... ", "Por favor espere... ");// Muestro el progress
        //Inicializo los atributos que voy a enviar a la bd
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap(); // Saco el bitmap de la imagen que hay en el Imageview
        request2 = Volley.newRequestQueue(getApplicationContext());// Creo el request
        cellphone = cellPhoneNumber.getText().toString();// Telefono
        name = etName.getText().toString(); //Nombre
        lastname = etLastname.getText().toString();// Apellido
        email = etEmail.getText().toString();// correo
        Password = etPassword.getText().toString(); // Clave
        String imagen = convertirImgString(bitmap);

        String ip =getString(R.string.ip_way);// Saco la ip base del value String
        String url = ip + "/consultas/rUsuario.php?";// Completo  la op
        Log.e("URL DEL POST", url);// Imprimo la ip completa
        //Cargo el string request que es como la peticion
        stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //On response es la respuesta de la consulta
                hideProgressDialog();// Oculto el progress
                Log.e("RESPUESTA SQL: ", "" + response); // imprimo la respuesta
                // Si la respuesta del web service es que guardo
                if (response.trim().equalsIgnoreCase("registradoregistrado")) {

                    Toast.makeText(MainActivityRegistarUser.this, "Verifique su correo para validar su usuario", Toast.LENGTH_SHORT).show();
                    //Guardo los datos en sqlite

                    //Ya inicio sesion entonces paso al Mainactivity
                    Intent intent = new Intent(MainActivityRegistarUser.this, MainLogginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    //Si la respuesta del web service es error, no se pudo escribir en la 000webhost, entonces lo borro del fire
                    try {
                        Toast.makeText(MainActivityRegistarUser.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                        deleteUserFirebase(email,Password);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Error de conexión
                Toast.makeText(MainActivityRegistarUser.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
            }

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //los parametros del POST
                Map<String, String> parametros = new HashMap<>();
                parametros.put("tel_usuario", cellphone);
                parametros.put("id_firebase", id_firebase);
                parametros.put("nombre_usuario", name);
                parametros.put("apellido_usuario", lastname);
                parametros.put("email_usuario", email);
                parametros.put("ciudad_usuario", miCiudad);
                parametros.put("rol_usuario", "3");
                parametros.put("imagen", imagen);
                return parametros;
            }
        };
        //Ejecuto el request
        request2.add(stringRequestS);
    }

    private boolean validarEmail() {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private void deleteUserFirebase(final String emailU, String pwU) throws Exception {
        Log.e("Delete", "ingreso a deleteAccount");

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //Obtengo el usuario
        AuthCredential credential = EmailAuthProvider.getCredential(emailU, emailU); // Re autentico
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Elimino
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.e("Usuario eliminar", "User account deleted.");
                                        } else {
                                            Log.e("Usuario eliminar", "error eliminanod." + task.getResult());
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

//Guarda los datos en la bd Interna
    private void salvarPermanente(){
        //Save registro permanente
        SQLiteDatabase db = usdbh.getWritableDatabase();
        //Crea el SQL OJO EL ROL ORIGINALMENTE ES
        db.execSQL("INSERT INTO CurrentUsuario VALUES ('"+id_firebase+"','"+name+"','"+email+"','true',"+ 3+",'"+cellphone+"')");
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
            @RequiresApi(api = Build.VERSION_CODES.M)
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



