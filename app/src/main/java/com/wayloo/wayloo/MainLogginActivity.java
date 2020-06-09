package com.wayloo.wayloo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainLogginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private TextView txtRegistar;
    private LoginButton logoutButton;
    private LoginButton loginButton;
    private EditText etusu;
    private EditText etpw;
    //Datos para la DB externa
    private RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    private ImageView imageViewLogomini;

    private String id_firebase;
    private String nombre_fb;
    private String email_fb;
    private Button btnIniciarSesion;

    //Creamos la BD
    UsuariosSQLiteHelper usdbh =
            new UsuariosSQLiteHelper(MainLogginActivity.this, "dbUsuarios", null, 1);
    //Firebase user
    FirebaseAuth mAuth;

    ProgressDialog progress;
    private TextView btnRestablecer;

    //SharedPreferences myPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_main_loggin);

        if(verificarIniciadoSesion()){//Verifica que ya no haya iniciado sesion
            Intent intent = new Intent(MainLogginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {

            //Iniciamos la sesion Sin Facebook
            btnIniciarSesion=findViewById(R.id.buttonLogginNomal);
            btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etusu = findViewById(R.id.editUsuarioLoggin);
                    etpw= findViewById(R.id.editTextPasswordLogin);
                    showProgressDialog("Verificando usuario","Conectando con el servidor espere.");
                    //iniciarSesionUsuarioNormal(etusu.getText().toString(),etpw.getText().toString());
                    verificarUsuarioYaEsteRegistrado(etusu.getText().toString());
                    //verificarUsuarioRegistrado(etusu.getText().toString(),etpw.getText().toString());

                }
            });



            //Referenciamos los objetos
            mAuth = FirebaseAuth.getInstance();
            //mDatabase = FirebaseDatabase.getInstance().getReference();
            firebaseAuth = FirebaseAuth.getInstance();
            btnRestablecer = findViewById(R.id.tvRestablecerContra);
            callbackManager = CallbackManager.Factory.create();
            loginButton = findViewById(R.id.buttonLoggin);
            loginButton.setReadPermissions("email", "public_profile");//user_status, publish_actions..
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    mAuth = FirebaseAuth.getInstance();
                    ProgressDialog progress;

                        showProgressDialog("Éxito", "..");
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }



                @Override
                public void onCancel() {
                    Toast.makeText(MainLogginActivity.this, "Inicio cancelado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(MainLogginActivity.this, "Error de inicio con Facebook", Toast.LENGTH_SHORT).show();
                }
            });

            //Cambiar a menu registrar
            txtRegistar = (TextView) findViewById(R.id.textViewRegistrar);

            txtRegistar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainLogginActivity.this, MainActivityRegistarUser.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        btnRestablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainLogginActivity.this, MainActivityCambiarClave.class);

                startActivity(intent);
            }
        });
        }

    private void iniciarSesionUsuarioNormal(final String telIngresado, String pswIngresado, final String emaildeEseTelefon, final String IDDEFIRE,
                                            final String nombre_USU, final String rol_Usu) {

        mAuth = FirebaseAuth.getInstance();
        Log.e("Finalizando Inicio", "Finalizando");

        mAuth.signInWithEmailAndPassword(emaildeEseTelefon, pswIngresado)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            hideProgressDialog();
                            // Sign in success, update UI with the signed-in user's information
                            Log.e("log fireb", "signInWithEmail:success");
                            salvarPermanente(IDDEFIRE, nombre_USU, emaildeEseTelefon, rol_Usu, telIngresado);
                            //updateIU();
                        } else {
                            hideProgressDialog();
                            // If sign in fails, display a message to the user.
                            Log.e("log fireb E", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainLogginActivity.this, "Usuario o contraseña erroneas.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

      /*  Utilidades tl = new Utilidades();
        pswIngresado=tl.Encriptar(pswIngresado);
        final String finalPswIngresado = pswIngresado;

            request = Volley.newRequestQueue(getApplicationContext());

            String ip = getString(R.string.ip);

            String url = ip + "/consultas/verificarUsuarioLoginPOSTa.php?";

            Log.e("URL DEL POST", url);

        final String finalPswIngresado1 = pswIngresado;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("Response ", response );
                    if (response.equalsIgnoreCase("DatosMalos[]")) {
                        hideProgressDialog();
                        Toast.makeText(MainLogginActivity.this, "Usuario o contraseña erroneas", Toast.LENGTH_SHORT).show();
                    } else {
                        hideProgressDialog();
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response.toString());
                            JSONArray json = jsonObject.getJSONArray("usuario");

                            String id_firebase= null;
                            String nombre_usuario= null;
                            String email_usuario= null;
                            String rol_usuario= null;
                            String tel_usuario= null;
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject2 = null;
                                jsonObject2 = json.getJSONObject(i);
                                 id_firebase=(jsonObject2.optString("id_firebase"));
                                 nombre_usuario=(jsonObject2.optString("nombre_usuario"));
                                 email_usuario=(jsonObject2.optString("email_usuario"));
                                 rol_usuario=(jsonObject2.optString("rol_usuario"));
                                 tel_usuario=(jsonObject2.optString("tel_usuario"));
                            }
                            Utilidades ult = new Utilidades();

                            Log.e("Intento en Fire",ult.Desencriptar(finalPswIngresado1));
                            FinalizarInicioS(id_firebase,nombre_usuario,email_usuario,rol_usuario,tel_usuario, ult.Desencriptar(finalPswIngresado1));


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
                    hideProgressDialog();
                    Toast.makeText(MainLogginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    Log.e("Response ", error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("tlI", telIngresado);
                    parameters.put("pswI", finalPswIngresado);
                    return parameters;
                }

            };
            Log.e("Usuario a consultar", telIngresado + " " + finalPswIngresado);
            request.add(stringRequest);*/
    }

    private void FinalizarInicioS(final String id_firebase, final String nombre_usuario, final String email_usuario, final String rol_usuario, final String tel_usuario, String password_usuario) {
        mAuth = FirebaseAuth.getInstance();
        Log.e("Finalizando Inicio", "Finalizando");

            mAuth.signInWithEmailAndPassword(email_usuario, password_usuario)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.e("log fireb", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                salvarPermanente(id_firebase, nombre_usuario, email_usuario, rol_usuario, tel_usuario);
                                //updateIU();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.e("log fireb E", "signInWithEmail:failure", task.getException());
                                Toast.makeText(MainLogginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

    private void verificarUsuarioYaEsteRegistrado(final String telIngresadoVerificar) {
        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/verificarUsuarioYaEstaRegistrado.php?";

        Log.e("URL DEL POST", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response ", response);
                if (response.equalsIgnoreCase("NoEncontrado[]")) {
                    hideProgressDialog();
                    Toast.makeText(MainLogginActivity.this, "Error Usuario No encontrado", Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject jsonObject = null;
                    try {
                        String emaildeEseTelefon=null;
                        String rol_u = null;
                        String nombre_u = null;
                        String IDFIre_u = null;
                        jsonObject = new JSONObject(response.toString());
                        JSONArray json = jsonObject.getJSONArray("already");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject2 = null;
                            jsonObject2 = json.getJSONObject(i);
                            IDFIre_u = jsonObject2.optString("id_firebase");
                            nombre_u = jsonObject2.optString("nombre_usuario");
                            emaildeEseTelefon = jsonObject2.optString("email_usuario");
                            rol_u = jsonObject2.optString("rol_usuario");

                        }

                        iniciarSesionUsuarioNormal(etusu.getText().toString(), etpw.getText().toString(),emaildeEseTelefon,IDFIre_u,nombre_u,rol_u);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainLogginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                hideProgressDialog();
                Log.e("Error Response ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("telVerificar", telIngresadoVerificar);
                return parameters;
            }

        };

        request.add(stringRequest);

    }

    private void iniciarSesionUsuarioFacebook(final String idFirebaseparaFB) {
            request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);

            String url = ip + "/consultas/verificarUsuarioLoginPOSTFB.php?";

            Log.e("URL DEL POST", url);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    hideProgressDialog();
                    Log.e("Response ", response + response.equalsIgnoreCase("[]") + response.isEmpty());
                    if (response.equalsIgnoreCase("DatosMalos[]")) {
                        Toast.makeText(MainLogginActivity.this, "Por favor complete su registro.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainLogginActivity.this, MainActivityCompleteRegister.class);
                        intent.putExtra("nombre_fb",nombre_fb);
                        intent.putExtra("email",  email_fb );
                        intent.putExtra("id_firebase",  id_firebase);
                        hideProgressDialog();
                        startActivity(intent);
                        finish();
                    } else {
                        hideProgressDialog();
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response.toString());
                            JSONArray json = jsonObject.getJSONArray("usuario");

                            String id_firebase= null;
                            String nombre_usuario= null;
                            String email_usuario= null;
                            String rol_usuario= null;
                            String tel_usuario= null;
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject2 = null;
                                jsonObject2 = json.getJSONObject(i);
                                id_firebase=(jsonObject2.optString("id_firebase"));
                                nombre_usuario=(jsonObject2.optString("nombre_usuario"));
                                email_usuario=(jsonObject2.optString("email_usuario"));
                                rol_usuario=(jsonObject2.optString("rol_usuario"));
                                tel_usuario=(jsonObject2.optString("tel_usuario"));
                            }

                            //Verifico que no esta vacio
                            if(id_firebase != null){
                                //hideProgressDialog();
                                salvarPermanente(id_firebase,nombre_usuario,email_usuario,rol_usuario,tel_usuario);
                                Toast.makeText(MainLogginActivity.this, "Login Exitoso! Bienvenido "+nombre_usuario+".", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainLogginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(MainLogginActivity.this, "Debe completar el registro.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainLogginActivity.this, MainActivityCompleteRegister.class);
                                intent.putExtra("nombre_fb",nombre_fb);
                                intent.putExtra("email",  email_fb );
                                intent.putExtra("id_firebase",  id_firebase);
                                hideProgressDialog();
                                startActivity(intent);
                                finish();
                            }



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
                    hideProgressDialog();
                    Log.e("Response ", error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("fID", idFirebaseparaFB);
                    return parameters;
                }

            };
            Log.e("Usuario a consultar", idFirebaseparaFB);
            request.add(stringRequest);

    }





























    //Registra el token de facebook, y inicia el completar la sesion
    private void handleFacebookAccessToken(AccessToken token) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.e("Result login", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            id_firebase = mAuth.getCurrentUser().getUid();
                            nombre_fb =mAuth.getCurrentUser().getDisplayName();
                            email_fb= mAuth.getCurrentUser().getEmail();
                            Log.e("Datos de FB", id_firebase +" " + nombre_fb+" " + email_fb);
                            showProgressDialog("Verificando Información","");
                            iniciarSesionUsuarioFacebook(id_firebase);


                        } else {
                            // If sign in fails, display a message to the user.
                            hideProgressDialog();
                            Log.e("Result login", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainLogginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,
                resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(verificarIniciadoSesion()){}
        else{
            if(LoginManager.getInstance() != null){
                LoginManager.getInstance().logOut();
            }
        }

    }

    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(MainLogginActivity.this, titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}

    private boolean verificarIniciadoSesion(){
        boolean iniciado= false;
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT id_firebase FROM CurrentUsuario;", null);
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                id_firebase= c.getString(0);
                iniciado = true;
            } while(c.moveToNext());
        }
        if(id_firebase != null){
            iniciado = true;
        }
        return iniciado;
    }

    private void salvarPermanente(String Currentid,String name, String email, String rol, String tel_usuario){
        //Save registro permanente
        SQLiteDatabase db = usdbh.getWritableDatabase();
        db.execSQL("INSERT INTO CurrentUsuario VALUES ('"+Currentid+"','"+name+"','"+email+"','true',"+ rol+",'"+tel_usuario+"')");
        Log.e("Salvado permanente",Currentid);
        Intent intent = new Intent(MainLogginActivity.this, MainActivity.class);
        hideProgressDialog();
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainLogginActivity.this, MainActivityPrincipal.class);
        startActivity(intent);
        finish();
    }


}
