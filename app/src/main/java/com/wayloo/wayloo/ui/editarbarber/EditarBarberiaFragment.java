package com.wayloo.wayloo.ui.editarbarber;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.MainActivity;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.ui.UsuariosSQLiteHelper;
import com.wayloo.wayloo.ui.Utilidades;
import com.wayloo.wayloo.ui.mispeluquerias.MisPeluqueriasFragment;

import java.util.HashMap;
import java.util.Map;

public class EditarBarberiaFragment extends Fragment {
EditText etNombre,etNit,etTel,etDireccion,etCiudad;
TextView tvTitu;
Button btnEditar;

    ProgressDialog progress;

    private RequestQueue request;
    private Button btnEliminar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_editar_barber, container, false);

        etNit = root.findViewWithTag("EtNitBarberEditar");;
        etNombre = root.findViewWithTag("EtNombreEdit");;
        etTel =  root.findViewWithTag("EtTelEdit");;
        etDireccion =  root.findViewWithTag("EtDirEdit");;
        etCiudad =  root.findViewWithTag("EtCiudadEdit");;
        tvTitu= root.findViewById(R.id.textViewTituPerfilBarbCarte);
        btnEditar  =root.findViewById(R.id.editarButonBarber);
        btnEliminar = root.findViewById(R.id.eliminarButonBarber);

        String nameB = getArguments().getString("nombre");
        final String NitB = getArguments().getString("NIT");
        String TelB = getArguments().getString("telefono");
        String DirB = getArguments().getString("direccion");
        String CiuB = getArguments().getString("ciudad");
        String CalB = getArguments().getString("calificacion");




        etNombre.setText(nameB);
        tvTitu.setText(nameB);
        etNit.setText(NitB);
        etTel.setText(TelB);
        etDireccion.setText(DirB);
        etCiudad.setText(CiuB);

        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_nom = etNombre.getText().toString();
                String new_Nit = etNit.getText().toString();
                String new_Tel = etTel.getText().toString();
                String new_Dir = etDireccion.getText().toString();
                String new_Ciu = etCiudad.getText().toString();
                showProgressDialog("Actualizando Barberia","Porfavor espere... ... ... ");
                updateBarberiaBDRemota(new_nom,new_Nit,new_Tel,new_Dir,new_Ciu);
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                consultarReservasAfectadasELIMINACION(NitB);
            }
        });

        return root;
    }


    private void updateBarberiaBDRemota(final String new_nameB, final String new_NitB, final String new_TelB, final String new_DirB, final String new_CiuB) {

        final String tUsuActual = getTelefonoSQLITE();
        if (new_nameB.equalsIgnoreCase("") || new_NitB.equalsIgnoreCase("") || new_TelB.equalsIgnoreCase("") || new_DirB.equalsIgnoreCase("") ||
                new_CiuB.equalsIgnoreCase("")) {
            Toast.makeText(getContext(), "ERROR LOS CAMPOS NO PUEDEN ESTAR VACIOS", Toast.LENGTH_LONG).show();
        } else {
                final Utilidades utl = new Utilidades();

                    request = Volley.newRequestQueue(getContext());

                    String ip =getString(R.string.ip_way);

                    String url = ip + "/consultas/UpdateBarberia.php?";

                    Log.e("URL DEL POST", url);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            hideProgressDialog();
                            Log.e("Response Update go ", response);
                            Toast.makeText(getContext(), "Barberia Actualizada", Toast.LENGTH_SHORT).show();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                            Intent mStartActivity = new Intent(getContext(), MainActivity.class);
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager) getContext().getSystemService(getContext().ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                            System.exit(0);
                        }

                    }, 2000);
                        }

                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            Toast.makeText(getContext(), "Error Actualizando barberia", Toast.LENGTH_SHORT).show();
                            Log.e("Response Update ", error.toString());
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> parameters = new HashMap<String, String>();
                            parameters.put("tus",tUsuActual);
                            parameters.put("nip", new_NitB);
                            parameters.put("nnp", new_nameB);
                            parameters.put("ntb", new_TelB);
                            parameters.put("ndp", new_DirB);
                            parameters.put("ncp", new_CiuB);
                            
                            return parameters;
                        }

                    };
                    request.add(stringRequest);


        }
    }

    private String getTelefonoSQLITE() {
        UsuariosSQLiteHelper usdbh = new UsuariosSQLiteHelper(getContext(), "dbUsuarios", null, 1);
        String result = "null";
        SQLiteDatabase db = usdbh.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id_usu FROM CurrentUsuario", null);
        if (c != null) {
            c.moveToFirst();
            do {
                //Asignamos el valor en nuestras variables para usarlos en lo que necesitemos
                result = c.getString(c.getColumnIndex("id_usu"));
            } while (c.moveToNext());
        }

        //Cerramos el cursor y la conexion con la base de datos
        c.close();
        db.close();
        Log.e("Tel Guardad", result);
        return result;
    }


    private void consultarReservasAfectadasELIMINACION(String NitBEliminar) {

        request = Volley.newRequestQueue(getContext());

        String ip =getString(R.string.ip_way);

        String url = ip + "/consultas/consultarReservasAfectadas.php?";

        Log.e("URL DEL POST", url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("response", response);
                Log.e("response del server", response);

                if (response.equalsIgnoreCase("ok")) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Va a realizar cambios de horario");
                    builder.setMessage("Atención,Esta a punto de elminar esta bareria ¿Desea continuar?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            eliminarBarberiaBDRemota(NitBEliminar);
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();

                } else {
                    if (response.equalsIgnoreCase("reservaProxima")) {
                        Toast.makeText(getContext(), "Error, tiene una reserva en la siguiente hora, debe finalizar el turno y reintentar.",
                                Toast.LENGTH_SHORT).show();
                    } else {

                        try {
                            int numRes = Integer.parseInt(response);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Cambio de horario detectado");
                            builder.setMessage("Atención, aun tiene reservas sin cumplir, al eliminar esta barberia se cancelaran " + numRes + " reservas ¿Desea continuar?");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    eliminarBarberiaBDRemota(NitBEliminar);
                                }
                            });

                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                                }
                            });
                            builder.show();

                        }catch (NumberFormatException  e){
                            Log.e("Error ", e.toString());
                            Toast.makeText(getContext(), "Error "+ response, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response Update ", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("key", "barberia");
                parameters.put("nit", NitBEliminar);

                return parameters;
            }
        };
        request.add(stringRequest);

    }


    private void eliminarBarberiaBDRemota(final String NitBEliminar) {


        if (NitBEliminar.equalsIgnoreCase("")) {
            Toast.makeText(getContext(), "ERROR ELIMINANDO, VERIFIQUE EL CAMPO NIT", Toast.LENGTH_LONG).show();
        } else {
            //Si no son iguales
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirmación eliminación del Barberia!");
            builder.setMessage("Va a eliminar los datos del sistema, ¿Desea continuar?");
            builder.setCancelable(false);
            builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showProgressDialog("Eliminando Barberia", "Por favor espere .. .. ..");

                    request = Volley.newRequestQueue(getContext());

                    String ip =getString(R.string.ip_way);

                    String url = ip + "/consultas/DeleteBarber.php?";

                    Log.e("URL DEL POST", url);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            hideProgressDialog();
                            Log.e("Response Delete go ", response);
                            Fragment miFragment = null;
                            miFragment = new MisPeluqueriasFragment();
                            miFragment.setArguments(null);
                            getFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();
                            Toast.makeText(getContext(), "Barberia Eliminado", Toast.LENGTH_SHORT).show();
                        }

                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            Toast.makeText(getContext(), "Error eliminando verifique su conexión", Toast.LENGTH_SHORT).show();
                            Log.e("Response Update ", error.toString());
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> parameters = new HashMap<String, String>();
                            parameters.put("nitEliminar", NitBEliminar);
                            return parameters;
                        }

                    };
                    Log.e("nitEliminar", NitBEliminar);
                    request.add(stringRequest);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();

        }

    }


    private void showProgressDialog(String titulo,String mensaje){
        progress = ProgressDialog.show(getContext(), titulo,
                mensaje, true);
    }

    private void  hideProgressDialog(){progress.dismiss();}
}