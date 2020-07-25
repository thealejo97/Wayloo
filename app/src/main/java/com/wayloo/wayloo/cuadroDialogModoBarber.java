package com.wayloo.wayloo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class cuadroDialogModoBarber extends AppCompatActivity {

    final Dialog dialog ;
    private Button bntVerificarNIT = null;
    private cuadroDialogo.objDialog interfaz;
    private EditText editTextViewNIT,HoraINICIO, HoraFIN;
    private Button btnAceptar;

    //////// Picker Fecha
    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";
    //Calendario para obtener fecha & hora
    private Context cn;
    public final Calendar c = Calendar.getInstance();

    //Variables para obtener la hora hora
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);
    private RequestQueue request3;
    StringRequest stringRequestS;
    ProgressDialog progress;
    Spinner spBarberias;
    ArrayList<String> arrBarber= new ArrayList<String>();

    public interface objDialog{
        void ResultadoDialogo(String nit,String HI,String HF, String diasLaborales);


    }

/*
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        spBarberias = dialog.findViewById(R.id.spBarberiasDelAdmin);
        arrBarber.add("Cosa");
        arrBarber.add("mas");
        Log.e("llego al create", String.valueOf(arrBarber));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrBarber);
    //    ArrayAdapter adapter = new ArrayAdapter(this, R.layout.color_spinner_layout, arrBarber);
        spBarberias.setAdapter(adapter);
        return super.onCreateView(name, context, attrs);
    }
*/

    public cuadroDialogModoBarber(final Context contexto, ArrayList<String> stringBarbers, cuadroDialogo.objDialog inter){


        Log.e("llego al dialog", String.valueOf(stringBarbers));
        interfaz=inter;
        dialog = new Dialog(contexto);
        View view = getLayoutInflater().inflate(R.layout.dialogmodobarbero,null);
        arrBarber = stringBarbers;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        dialog.setContentView(R.layout.dialogmodobarbero);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                interfaz.ResultadoDialogo("","", "","");
                dialog.dismiss();
            }
        });
        dialog.show();
        spBarberias = (Spinner)view.findViewById(R.id.spBarberiasDelAdmin);
        Log.e("llego al create", String.valueOf(arrBarber));
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.color_spinner_layout, arrBarber);
        spBarberias.setAdapter(adapter);
    }


    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(cn, titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}
}
