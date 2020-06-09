package com.wayloo.wayloo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
    private final ImageButton btnDialogHoraInicio,btnDialogHoraFin;
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

    public interface objDialog{
        void ResultadoDialogo(String nit,String HI,String HF, String diasLaborales);


    }


    public cuadroDialogModoBarber(final Context contexto, cuadroDialogo.objDialog inter){


        interfaz=inter;
        dialog = new Dialog(contexto);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialogmodobarbero);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                interfaz.ResultadoDialogo("","", "","");
                dialog.dismiss();
            }
        });
        editTextViewNIT = dialog.findViewById(R.id.EtxNitPeluqueriaPerteneceBarberoMODO);
        HoraINICIO = dialog.findViewById(R.id.et_mostrar_hora_picker_inicio_MODO);
        HoraFIN = dialog.findViewById(R.id.et_mostrar_hora_picker_fin_MODO);
        btnAceptar = dialog.findViewById(R.id.butonAceptarMODO);
        btnDialogHoraInicio = dialog.findViewById(R.id.ib_obtener_hora_inicioMODO);
        btnDialogHoraFin = dialog.findViewById(R.id.ib_obtener_hora_finMODO);
        cn=contexto;
        bntVerificarNIT= dialog.findViewById(R.id.ButtonVerificarNITmodo);

        bntVerificarNIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Verificando","Espere mientras se verifica el NIT en el sistema");;
                consultarPeluqueriaExiste(editTextViewNIT.getText().toString());
            }
        });

        btnDialogHoraInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerHora_inicio(contexto);
            }
        });

        btnDialogHoraFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerHora_fin(contexto);
            }
        });

        btnAceptar.setEnabled(false);
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nitBarbe = editTextViewNIT.getText().toString();
                String horaI = HoraINICIO.getText().toString();
                String horaF = HoraFIN.getText().toString();

                CheckBox chl,chm,chmi,chj,chv,chs,chd;
                chl= dialog.findViewById(R.id.chLunesMD);
                chm= dialog.findViewById(R.id.chMartesMD);
                chmi= dialog.findViewById(R.id.chMiercolesMD);
                chj= dialog.findViewById(R.id.chJuevesMD);
                chv= dialog.findViewById(R.id.chViernesMD);
                chs= dialog.findViewById(R.id.chSabadoMD);
                chd= dialog.findViewById(R.id.chDomingoMD);

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




                if(nitBarbe.equalsIgnoreCase("") || horaI.equalsIgnoreCase("") || horaF.equalsIgnoreCase("") ){
                    Toast.makeText(contexto, "Error, los campos no deben de estar vacios", Toast.LENGTH_SHORT).show();
                }else{
                    interfaz.ResultadoDialogo(nitBarbe,horaI, horaF,diasLaborales);
                    dialog.dismiss();
                }
            }
        });

        editTextViewNIT.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                editTextViewNIT.setTextColor(Color.RED);
                btnAceptar.setEnabled(false);
                btnAceptar.setText("Debe verificar el NIT para registrarse como barbero");

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        dialog.show();
    }

    // Obtener las horas
    private void obtenerHora_inicio(Context con) {
        TimePickerDialog recogerHora = new TimePickerDialog(con, new TimePickerDialog.OnTimeSetListener() {
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
                HoraINICIO.setText(horaFormateada + DOS_PUNTOS + minutoFormateado );
            }
            //Estos valores deben ir en ese orden
            //Al colocar en false se muestra en formato 12 horas y true en formato 24 horas
            //Pero el sistema devuelve la hora en formato 24 horas
        }, hora, minuto, false);

        recogerHora.show();

    }

    private void obtenerHora_fin(Context con) {
        TimePickerDialog recogerHora = new TimePickerDialog(con, new TimePickerDialog.OnTimeSetListener() {
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
                HoraFIN.setText(horaFormateada + DOS_PUNTOS + minutoFormateado );
            }
            //Estos valores deben ir en ese orden
            //Al colocar en false se muestra en formato 12 horas y true en formato 24 horas
            //Pero el sistema devuelve la hora en formato 24 horas
        }, hora, minuto, false);

        recogerHora.show();

    }

    private void consultarPeluqueriaExiste(final String NITAVerificar) {
        request3 = Volley.newRequestQueue(cn);
        String ip =cn.getString(R.string.ip_way);

        String url = ip + "/consultas/consultarPeluqueriaExistePOST.php?";
        Log.e("URL DEL POST", url);

        stringRequestS = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                Log.i("RESPUESTA: ", "" + response);
                if (response.trim().equalsIgnoreCase("404")) {
                    editTextViewNIT.setTextColor(Color.RED);
                    btnAceptar.setEnabled(false);
                    Toast.makeText(cn, "Barberia no encontrada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(cn, "Barberia Encontrada", Toast.LENGTH_SHORT).show();
                    btnAceptar.setEnabled(true);
                    //Coloco el texto en verde
                    editTextViewNIT.setTextColor(Color.GREEN);
                    btnAceptar.setText("REGISTRAR");
                    Toast.makeText(cn, "NIT Verificado, ya puede terminar el registro.", Toast.LENGTH_LONG).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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


    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(cn, titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}
}
