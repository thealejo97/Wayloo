package com.wayloo.wayloo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import java.util.ArrayList;
import java.util.Calendar;

public class cuadroDialogModoBarber extends AppCompatActivity {

    final Dialog dialog ;
    private cuadroDialogo.objDialog interfaz;
    //Calendario para obtener fecha & hora
    private Context cn;
    public final Calendar c = Calendar.getInstance();
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


}
