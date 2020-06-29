package com.wayloo.wayloo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.api.AuthProvider;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.SignInMethodQueryResult;
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
    private LoginButton loginButton;
    private EditText etusu;
    private EditText etpw;

    String emaildeEseTelefon=null;
    String rol_u = null;
    String nombre_u = null;
    String IDFIre_u = null;
    String estadoUsu = null;
    private String id_firebase;
    private String nombre_fb; // Es el nombre del usuario de facebook
    private String email_fb;// Es el email del usuario de facebook
    private Button btnIniciarSesion;

    //Creamos la BD
    UsuariosSQLiteHelper usdbh =
            new UsuariosSQLiteHelper(MainLogginActivity.this, "dbUsuarios", null, 1);
    //Firebase user
    FirebaseAuth mAuth;
    private RequestQueue request;

    ProgressDialog progress;
    private TextView btnRestablecer;

    //SharedPreferences myPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main_loggin);
        //Referenciamos los objetos
        mAuth = FirebaseAuth.getInstance();

        if(verificarIniciadoSesion()){//Verifica que ya no haya iniciado sesion
            //Si ya existe el usuario interno
            //Vaya directamente a MainActivity
            Intent intent = new Intent(MainLogginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }else {

            btnIniciarSesion=findViewById(R.id.buttonLogginNomal);
            btnRestablecer = findViewById(R.id.tvRestablecerContra);
            loginButton = findViewById(R.id.buttonLoggin);
            etusu = findViewById(R.id.editUsuarioLoggin);
            etpw= findViewById(R.id.editTextPasswordLogin);
            txtRegistar = (TextView) findViewById(R.id.textViewRegistrar);
            /////////////////////////////////////////////// cuando doy click en iniciar sesion ///////////////////////////////////////////////
            btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Va y busca el email con el telefono proporcionado
                    verificarUsuarioYaEsteRegistrado(etusu.getText().toString());

                }
            });


            callbackManager = CallbackManager.Factory.create();

            loginButton.setReadPermissions("email", "public_profile");//user_status, publish_actions..
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    //Getting the user information
                    GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            // Application code
                            Log.e("usuario obtenido", "onCompleted: response: " + response.toString());
                            try {
                                email_fb = object.getString("email");
                                nombre_fb = object.getString("name");

                                Log.e("El usuario obtenido es", "onCompleted: Email: " + email_fb);
                                Log.e("El usuario obtenido es", "onCompleted: Birthday: " + nombre_fb);


                                handleFacebookAccessToken(loginResult.getAccessToken());

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("El usuario obtenido es", "onCompleted: JSON exception");
                            }
                        }
                    });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender,birthday");
                    request.setParameters(parameters);
                    request.executeAsync();




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
        //Inicio la sesion con el correo que obtuvimos
//        mAuth = FirebaseAuth.getInstance();// Creo el usuario firebase
        //Inicio sesion con el correo y la clave
        mAuth.signInWithEmailAndPassword(emaildeEseTelefon, pswIngresado)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                            Log.e("Choco error", task.getException()+" "+ task.toString());
                            if (task.isSuccessful()) {
                                Log.e("Mauth desde arriba2", "-"+mAuth.getCurrentUser().toString()+"-");
                                //Si el correo y la clave estan ok, entonces verifico que la cuenta este verifica
                                if(!mAuth.getCurrentUser().isEmailVerified()){
                                    //Verificar que el usuario ya haya verificado su email
                                    Toast.makeText(MainLogginActivity.this, "Error debe de verificar su usuario revise su email", Toast.LENGTH_LONG).show();
                                    mAuth.getInstance().signOut();
                                }else {
                                    //Si es validao
                                    Log.e("login fireb", "signInWithEmail:success");
                                    salvarPermanente(IDDEFIRE, nombre_USU, emaildeEseTelefon, rol_Usu, telIngresado);
                                }
                            } else {
                                hideProgressDialog();
                                // If sign in fails, display a message to the user.
                                Log.e("log fireb E", "signInWithEmail:failure", task.getException());
                                Toast.makeText(MainLogginActivity.this, "Usuario o contraseña erroneas.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                });
        
    }

    private void verificarUsuarioYaEsteRegistrado(final String telIngresadoVerificar) {
        //Verifico el usuario existe 000Webhost
        //Relaciono el email con el telefono que ingrese y miro si existe
        showProgressDialog("Verificando usuario","Conectando con el servidor espere.");
        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);
        String url = ip + "/consultas/verificarUsuarioYaEstaRegistrado.php?";
        Log.e("URL DEL POST", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Response ", response);
                if (response.equalsIgnoreCase("NoEncontrado[]")) {
                    hideProgressDialog();// Oculto el dialog
                    Toast.makeText(MainLogginActivity.this, "Error Usuario No encontrado", Toast.LENGTH_SHORT).show();// Usuario Telefono no encontrado
                } else {
                    try {
                        //Recibo los datos del usuario que tiene ese telefono

                        JSONObject jsonObject = new JSONObject(response.toString());
                        JSONArray json = jsonObject.getJSONArray("already");
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject2 = json.getJSONObject(i); // Creo un objeto con cada uno de los usuarios que trae
                            IDFIre_u = jsonObject2.optString("id_firebase");// Saco el dato de la fila
                            nombre_u = jsonObject2.optString("nombre_usuario");// Saco el dato de la fila
                            emaildeEseTelefon = jsonObject2.optString("email_usuario");// Saco el dato de la fila
                            rol_u = jsonObject2.optString("rol_usuario");// Saco el dato de la fila
                            estadoUsu = jsonObject2.optString("estado");// Saco el dato de la fila
                        }
                        if(estadoUsu.equals("0")){
                            hideProgressDialog();
                            Toast.makeText(MainLogginActivity.this, "Error, el usuario ha sido deshabilitado, Comuniquese con soporte.", Toast.LENGTH_LONG).show();
                        }else {
                            //ahora si inicio la sesion con la contraseña
                            iniciarSesionUsuarioNormal(etusu.getText().toString(), etpw.getText().toString(), emaildeEseTelefon, IDFIre_u, nombre_u, rol_u);
                        }
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

    private void generarInicioDeSesionFB(final String IDEnviadoU) {
        //Verifico el usuario existe 000Webhost si no existe, pido que complete los datos
        showProgressDialog("Verificando usuario","Conectando con el servidor espere.");
        request = Volley.newRequestQueue(getApplicationContext());

        String ip =getString(R.string.ip_way);
        String url = ip + "/consultas/verificarUsuarioYaEstaRegistradoconFirebase.php?";
        Log.e("URL DEL POST", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.e("Response ", response);

                if (response.equalsIgnoreCase("NoEncontrado[]")) {
                    Intent intent = new Intent(MainLogginActivity.this, MainActivityCompleteRegister.class);
                    intent.putExtra("nombre_fb", nombre_fb);
                    intent.putExtra("email", email_fb);
                    id_firebase = mAuth.getUid();
                    intent.putExtra("id_firebase", id_firebase);
                    startActivity(intent);

                } else {
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        String tel_usuario = null;
                        try {
                            //Creo un jsonobject con el string
                            JSONObject jsonObject = new JSONObject(response.toString());
                            //extracting json array from response string
                            JSONArray json = jsonObject.getJSONArray("already");

                            for (int i = 0; i < json.length(); i++) {
                                JSONObject jsonObject2 = json.getJSONObject(i);
                                tel_usuario = jsonObject2.optString("tel_usuario");
                                id_firebase = jsonObject2.optString("id_firebase");
                                estadoUsu = jsonObject2.optString("estado");
                                rol_u = jsonObject2.optString("rol_usuario");
                            }
                            hideProgressDialog();
                            Log.e("Estado estado usu " ,estadoUsu+ "55");
                            if(estadoUsu.equals("0")){
                                FirebaseAuth.getInstance().signOut();
                                LoginManager.getInstance().logOut();
                                Toast.makeText(MainLogginActivity.this, "Error, el usuario ha sido deshabilitado, Comuniquese con soporte.", Toast.LENGTH_LONG).show();
                                hideProgressDialog();
                            }else {
                                hideProgressDialog();
                                salvarPermanente(id_firebase, nombre_fb, emaildeEseTelefon, rol_u, tel_usuario);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        LoginManager.getInstance().logOut();
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(MainLogginActivity.this, "Error debe validar su usuario verifique su correo electronico", Toast.LENGTH_SHORT).show();
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
                parameters.put("IDEnviadoU", IDEnviadoU);
                return parameters;
            }

        };
        request.add(stringRequest);

    }








    //Registra el token de facebook, y inicia el completar la sesion
    private void handleFacebookAccessToken(AccessToken token) {
        showProgressDialog("Verificando Información", "....");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        // Account exists with different credential. Assume the developer wants to
                        // continue and link new credential to existing account.
                        //Verificar que el usuario no exista con el correo y OTRA CREDENCIAL
                        if (!task.isSuccessful() ){
                            if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                            FirebaseAuthUserCollisionException exception =
                                    (FirebaseAuthUserCollisionException) task.getException();
                            Log.e("Colisiono", exception.getErrorCode());
                            if (exception.getErrorCode().equals(
                                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") ){
                                // Lookup existing account’s provider ID.
                                Log.e("Dandole email", email_fb);
                                mAuth.fetchSignInMethodsForEmail(email_fb).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().getSignInMethods().contains(
                                                    EmailAuthProvider.PROVIDER_ID)) {
                                                // Password account already exists with the same email.
                                                // Ask user to provide password associated with that account.
                                                // Sign in with email and the provided password.
                                                // If this was a Google account, call signInWithCredential instead.

                                                //ERROR AQUI HAY UN ERROR

                                                vincularEmailFacebookProvider(credential);
                                            }
                                        }
                                    }
                                });

                            }}
                            else{
                                Log.e("Error Usu Fb",task.toString());
                                Toast.makeText(MainLogginActivity.this, "Error Creando Usuario.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                            {
                                //Verificar, que el usuario no este en 000
                                //Si el usuario no esta en 000 tiene que ir a completar el registro
                                generarInicioDeSesionFB(mAuth.getUid());
                                //Si si esta en 000 ya tiene que iniciar en el main
                            hideProgressDialog();
                        }
                    }
                });
    }

    private void vincularEmailFacebookProvider(AuthCredential credential  ) {
        Log.e("Credenciales",  " -"+credential + "- " + mAuth.getCurrentUser());

        final String[] psw = new String[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ingrese su contraseña de Wayloo para vincular a Facebook");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                psw[0] = input.getText().toString();
                mAuth.signInWithEmailAndPassword(email_fb,psw[0])
                        .addOnCompleteListener(MainLogginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.e("Mauth ", "-"+mAuth.getCurrentUser().toString()+"-");
                                    //Si el correo y la clave estan ok, entonces verifico que la cuenta este verifica
                                    mAuth.getCurrentUser().linkWithCredential(credential)
                                            .addOnCompleteListener(MainLogginActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("Vinculacion", "linkWithCredential:success");
                                                        generarInicioDeSesionFB(mAuth.getUid());
                                                    } else {
                                                        Log.e("Vinculacion", "linkWithCredential:failure", task.getException());
                                                        Toast.makeText(MainLogginActivity.this, "Authentication failed.",
                                                                Toast.LENGTH_SHORT).show();

                                                    }

                                                    // ...
                                                }
                                            });


                                } else {
                                    LoginManager.getInstance().logOut();
                                    // If sign in fails, display a message to the user.
                                    Log.e("log fireb E", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainLogginActivity.this, "Contraseña de Wayloo erronea no se pudo vincular.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }


                        });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginManager.getInstance().logOut();
                dialog.cancel();

            }
        });

        builder.show();

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
        boolean iniciado= false;// Si ya esta iniciado
        //SQLITE
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery(" SELECT id_firebase FROM CurrentUsuario;", null);// Si ya existe almenos una fila en la bdsqlite
        if (c.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                id_firebase= c.getString(0);
                iniciado = true;// Iniciado vale true
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
        db.execSQL("INSERT INTO CurrentUsuario VALUES ('"+Currentid+"','"+name+"','"+email+"',"+ rol+",'"+tel_usuario+"')");
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
