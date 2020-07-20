package com.wayloo.wayloo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class cuadroDialogo {


    public interface objDialog{
        void ResultadoDialogo(String fecha, String HI, String HF, String diasLaborales);

    }
    private objDialog interfaz;


    //////// Picker Fecha
    private static final String CERO = "0";
    private static final String DOS_PUNTOS = ":";

    //Calendario para obtener fecha & hora
    public final Calendar c = Calendar.getInstance();

    //Variables para obtener la hora hora
    final int hora = c.get(Calendar.HOUR_OF_DAY);
    final int minuto = c.get(Calendar.MINUTE);

    TextView fecha;
    EditText etHora_inicio;
    private String hora_inicio;
    private String hora_fin;
    private String fechaFinal;
    private Spinner spinner;
    final Calendar myCalendar = Calendar.getInstance();
    ArrayList<TextView> txtORIGINAL = null;
    final Dialog dialog ;
    boolean vacio = false;

    public cuadroDialogo(final Context contexto, String fechaInicialDelPicker, objDialog inter, ArrayList<TextView> textViews){

        interfaz=inter;

        dialog = new Dialog(contexto);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogregistrar);

        fecha = (TextView) dialog.findViewById(R.id.editTextFecha_dialog);

        if(textViews.size()==0){
            TextView textotitu = dialog.findViewById(R.id.textTituloDialog);
            TextView textohorar = dialog.findViewById(R.id.textViewHorarioTitu);

            textotitu.setText("El barbero no tiene turnos disponibles en este momento");
            etHora_inicio.setVisibility(View.GONE);
            fecha.setVisibility(View.GONE);
            textohorar.setVisibility(View.GONE);
            vacio = true;
        }else{vacio=false;}

        fecha.setText(fechaInicialDelPicker);
        txtORIGINAL=textViews;


        ArrayList<String> contacts = new ArrayList<>();

        for (int i = 0; i < textViews.size(); i++) {
            contacts.add(textViews.get(i).getText().toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(contexto,  android.R.layout.simple_spinner_dropdown_item, contacts);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) dialog.findViewById(R.id.spinnerDialog);
        spinner.setAdapter(adapter);


        final ImageView aceptar = (ImageView) dialog.findViewById(R.id.imgAceptar);

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vacio){
                    dialog.dismiss();
                }else {
                    spinner = (Spinner) dialog.findViewById(R.id.spinnerDialog);
                    String text = spinner.getSelectedItem().toString();
                    fechaFinal = fecha.getText().toString();
                    if (text.equalsIgnoreCase("Reservas") || text.equalsIgnoreCase(null) || fechaFinal.equalsIgnoreCase("")) {
                        Toast.makeText(contexto, "ERROR DEBE SELECCIONAR UN TURNO Y FECHA VALIDOS.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (fecha.getText().toString().equalsIgnoreCase("Reservas")) {
                            Toast.makeText(contexto, "Error debe seleccionar una fecha valida", Toast.LENGTH_SHORT).show();
                        } else {
                            hora_inicio = text;
                            fechaFinal = fecha.getText().toString();
                            interfaz.ResultadoDialogo(fechaFinal, hora_inicio, "00:00:00", "");
                        }
                        dialog.dismiss();
                    }
                }
            }
        });



        dialog.show();
    }




}

