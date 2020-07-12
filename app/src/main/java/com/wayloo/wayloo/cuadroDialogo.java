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

    private ImageButton ibObtenerHora;
    private ImageButton ibObtenerHora_fin;
    private ImageButton fechaB;
    TextView fecha;
    EditText etHora_inicio;
    EditText etHora_fin;
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
        //dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialogregistrar);


        etHora_inicio= (EditText) dialog.findViewById(R.id.editTextHInicio_dialog);
        etHora_fin= (EditText) dialog.findViewById(R.id.editTextHInicioFin_dialog);
        fecha = (TextView) dialog.findViewById(R.id.editTextFecha_dialog);
        fechaB = (ImageButton) dialog.findViewById(R.id.buttonFecha_dialgo);

        if(textViews.size()==0){
            TextView textotitu = dialog.findViewById(R.id.textTituloDialog);
            TextView textohorar = dialog.findViewById(R.id.textViewHorarioTitu);

            textotitu.setText("El barbero no tiene turnos disponibles en este momento");
            etHora_inicio.setVisibility(View.GONE);
            etHora_fin.setVisibility(View.GONE);
            fecha.setVisibility(View.GONE);
            fechaB.setVisibility(View.GONE);
            textohorar.setVisibility(View.GONE);
            vacio = true;
        }else{vacio=false;}

        fecha.setText(fechaInicialDelPicker );
        txtORIGINAL=textViews;
        //Widget ImageButton del cual usaremos el evento clic para obtener la hora
        ibObtenerHora = (ImageButton) dialog.findViewById(R.id.ib_obtener_hora_inicio_dialog);
        ibObtenerHora_fin = (ImageButton) dialog.findViewById(R.id.ib_obtener_hora_Fin_dialog);
        //Evento setOnClickListener - clic
        ibObtenerHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ib_obtener_hora_inicio_dialog:
                        obtenerHora_inicio(contexto);
                        break;
                }
            }
        });
        ibObtenerHora_fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ib_obtener_hora_Fin_dialog:
                        obtenerHora_fin(contexto);
                        break;
                }
            }
        });



        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateLabel();
            }

        };

        fechaB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Calendar calendar = Calendar.getInstance();
                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);



                DatePickerDialog dpDialog = new DatePickerDialog(contexto, date, mYear, mMonth,mDay);
                dpDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                dpDialog.show();
            }
        });


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

        ArrayList<String> contacts = new ArrayList<>();
        Log.e("Cantidad en el combo",textViews.size() + "");
        for (int i = 0; i < textViews.size(); i++) {
            contacts.add(textViews.get(i).getText().toString());
            Log.e("Miembros sp",textViews.get(i).getText().toString());
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(contexto,  android.R.layout.simple_spinner_dropdown_item, contacts);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) dialog.findViewById(R.id.spinnerDialog);
        spinner.setAdapter(adapter);


        dialog.show();
    }



    private void obtenerHora_inicio(Context ctx) {

        String text = spinner.getSelectedItem().toString();

        if(text.equalsIgnoreCase("Reservas")){
            Toast.makeText(ctx, "ERROR DEBE SELECCIONAR UN TURNO VALIDO", Toast.LENGTH_SHORT).show();
        }

    }

    private void obtenerHora_fin(final Context ctx) {
        TimePickerDialog recogerHora = new TimePickerDialog(ctx, new TimePickerDialog.OnTimeSetListener() {
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




    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        fecha.setText(sdf.format(myCalendar.getTime()));
        fechaFinal=fecha.getText().toString();

    }


}

