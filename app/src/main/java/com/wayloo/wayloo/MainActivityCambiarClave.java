package com.wayloo.wayloo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class MainActivityCambiarClave extends AppCompatActivity {
    EditText etCorreo;
    Button cambiarPS;
    private RequestQueue request;
    String CurrentFire;
    FirebaseAuth mAuth;
    ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cambiar_clave);
        mAuth = FirebaseAuth.getInstance();
        etCorreo = findViewById(R.id.editTextCorreo);
        cambiarPS =findViewById(R.id.buttonCambiarClave);
        cambiarPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMailPassword(etCorreo.getText().toString(),CurrentFire);

            }
        });


    }

    private void sendMailPassword(String email, String currentFire) {
        showProgressDialog("Enviando Correo","Espere mientras se envia conrreo de verificación");
        if(validarEmail(email)){
            mAuth.setLanguageCode("es");
/*
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideProgressDialog();
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivityCambiarClave.this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                    }else{
                        Log.e("Email error sending",task.toString());
                        Toast.makeText(MainActivityCambiarClave.this, "No se pudo enviar el correo de restablecimiento", Toast.LENGTH_SHORT).show();
                    }

                }
            });
*/
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            hideProgressDialog();
                            Toast.makeText(MainActivityCambiarClave.this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception e) {
hideProgressDialog();
                    Log.e("Email error sending",e.toString());
                    Toast.makeText(MainActivityCambiarClave.this, "No se pudo enviar el correo de restablecimiento, revise el correo electronico.", Toast.LENGTH_SHORT).show();
                }

        });
        }else{

            Toast.makeText(this, "Error, debe de ingresar un correo valido.", Toast.LENGTH_LONG).show();
        }
    }

    //Valida el email que sea correcto
    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
    ///Dialog Progress
    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(MainActivityCambiarClave.this, titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}

}
