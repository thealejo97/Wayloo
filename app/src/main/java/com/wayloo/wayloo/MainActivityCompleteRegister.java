package com.wayloo.wayloo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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

public class MainActivityCompleteRegister extends AppCompatActivity {
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
        //Inicializamos los componentes graficos
        imgPerfil = findViewById(R.id.imageViewProfileFoto);
        telefono= findViewById(R.id.etxt_telefono);
        btRegistrar=findViewById(R.id.btn_register);
        etPassword=findViewById(R.id.etxt_password);
        etCity = findViewById(R.id.etxt_CIUDAD);
        //Sacamos los datos que nos mando el facebook osea el nombre y el correo
        // Llenamos los datos base
        name=getIntent().getStringExtra("nombre_fb");;
        String [] tempNombr= name.split(" ");
        name=tempNombr[0];
        lastname=getIntent().getStringExtra("nombre_fb");
        String []  apell = lastname. split(" ");
        if(apell[1] != null){lastname=apell[1];}else{lastname="";}
        email=getIntent().getStringExtra("email");
        id_firebase=getIntent().getStringExtra("id_firebase");
        spinerCiudad=findViewById(R.id.ciudad_spinner);
        mAuth= FirebaseAuth.getInstance();
        txtEmailComp = findViewById(R.id.etxt_emailComplete);
        //Inicializo el campo email con el q me trajo de fb
        txtEmailComp.setText(email);

        //Cuando presiona registrar
        btRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cellphone = telefono.getText().toString();
                email = txtEmailComp.getText().toString();
                Password =etPassword.getText().toString();
                miCiudad=spinerCiudad.getSelectedItem().toString();
                if (name.isEmpty() || email.isEmpty() || Password.isEmpty()) {
                    Toast.makeText(MainActivityCompleteRegister.this, "Error los campos no deben de estar vacios.", Toast.LENGTH_SHORT).show();
                } else {
                    if (Password.length() > 6) {
                        if (validarEmail()) {
                            //RegistrarUsuarioFirebase();
                            AuthCredential credential = EmailAuthProvider.getCredential(email, Password);
                            vincularFacebookEmailProvider(credential);

                            /*
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
                       */ }else {
                            Toast.makeText(MainActivityCompleteRegister.this, "Error el email ingresado no es valido", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivityCompleteRegister.this, "Error el password debe tener minimo 6 caracteres y el usuario 3.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
//Si presiono seleccionar imagen
        ButtonPickImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (solicitaPermisosVersionesSuperiores()) {
                    //Pide permisos android
                    mostrarDialogOpciones();
                }
            }
        });

    }



    private void vincularFacebookEmailProvider( AuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Se envio el correo exitosamente
                                    //escribe en 000
                                    registrarUsuario();
                                    Log.d("Vinculacion", "linkWithCredential:success");
                                    //Login
                                    Toast.makeText(MainActivityCompleteRegister.this, "Se ha enviado un correo de verificación", Toast.LENGTH_SHORT).show();
                                    finish();                                }
                            });
                        } else {
                            Log.e("Vinculacion", "linkWithCredential:failure", task.getException());
                            Log.e("Vinculacion", "linkWithCredential:failure--"+task.getException().getMessage() + "--");
                            if(task.getException().getMessage().equals("User has already been linked to the given provider.")){
                                //Se envio el correo exitosamente
                                //escribe en 000
                                registrarUsuario();
                                Log.d("Vinculacion", "linkWithCredential:success");
                                //Login
                                Toast.makeText(MainActivityCompleteRegister.this, "Se ha enviado un correo de verificación", Toast.LENGTH_SHORT).show();
                            }else{
                            Toast.makeText(MainActivityCompleteRegister.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            }
                        }

                        // ...
                    }
                });
    }




    //Registra el usuario en la bd SQL Externa
    private void registrarUsuario() {

        showProgressDialog("Registrando.... ", "Por favor espere... ");// Muestro el progress
        //Inicializo los atributos que voy a enviar a la bd
        bitmap = ((BitmapDrawable) imgPerfil.getDrawable()).getBitmap(); // Saco el bitmap de la imagen que hay en el Imageview
        request2 = Volley.newRequestQueue(getApplicationContext());// Creo el request
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
                if (response.trim().equalsIgnoreCase("registrado")) {

                    Toast.makeText(MainActivityCompleteRegister.this, "Verifique su correo para validar su usuario", Toast.LENGTH_SHORT).show();
                    //Guardo los datos en sqlite
                    //     salvarPermanente();

                    //Ya inicio sesion entonces paso al Mainactivity
                    Intent intent = new Intent(MainActivityCompleteRegister.this, MainLogginActivity.class);
                    startActivity(intent);

                } else {
                    //Si la respuesta del web service es error, no se pudo escribir en la 000webhost, entonces lo borro del fire
                    try {
                        Toast.makeText(MainActivityCompleteRegister.this, "No se ha registrado ", Toast.LENGTH_SHORT).show();
                        LoginManager.getInstance().logOut();
                        deleteUserFirebase(email,Password);
                    } catch (Exception e) {
                        Log.e("Error no se registrado", e.toString());
                        e.printStackTrace();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Error de conexión
                Log.e("Error de conexion", error.toString());
                Toast.makeText(MainActivityCompleteRegister.this, "No se ha podido conectar", Toast.LENGTH_SHORT).show();
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






















    //Valida el email que sea correcto
    private boolean validarEmail() {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
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
        spinerCiudad.setAdapter(ArraylistCiudadesFRFire);
    }


    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);

        return imagenString;
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
        startActivity(intent);
        finish();
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
            @RequiresApi(api = Build.VERSION_CODES.M)
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

}
