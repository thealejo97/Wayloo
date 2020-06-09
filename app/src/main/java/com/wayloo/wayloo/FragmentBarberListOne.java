package com.wayloo.wayloo;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.adapters.UsuariosAdapters;
import com.wayloo.wayloo.entidades.Usuario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentBarberListOne.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentBarberListOne#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentBarberListOne extends Fragment implements com.android.volley.Response.Listener<JSONObject>, Response.ErrorListener  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    RecyclerView recyclerUsuarios;
    ArrayList<Usuario> listaUsuarios;

    ProgressDialog progress;

    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;


    private OnFragmentInteractionListener mListener;

    public FragmentBarberListOne() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentBarberListOne.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentBarberListOne newInstance(String param1, String param2) {
        FragmentBarberListOne fragment = new FragmentBarberListOne();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_fragment_barber_list_one,container,false);
        listaUsuarios=new ArrayList<Usuario>();

        recyclerUsuarios=(RecyclerView) vista.findViewById(R.id.idRecycle);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerUsuarios.setHasFixedSize(true);
        request= Volley.newRequestQueue(getContext());

        cargarWebServices();

        return vista;
    }

    private void cargarWebServices() {
        progress=new ProgressDialog(getContext());
        progress.setMessage("Consultando Peluquerias");
        progress.show();
        String ip =getString(R.string.ip_way);
        String url = ip+"/consultas/consultarListaUsuarios.php";

        jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        request.add(jsonObjectRequest);
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("Error peluquerias",error.toString());
        progress.hide();
    }

    @Override
    public void onResponse(JSONObject response) {
        Usuario usuario = null;

        JSONArray json = response.optJSONArray("usuario");
        try {
            for(int i=0;i<json.length();i++) {
                usuario = new Usuario();
                JSONObject jsonObject = null;
                jsonObject = json.getJSONObject(i);
                usuario.setNombre(jsonObject.optString("nombre_peluqueria"));
                usuario.setNombre(jsonObject.optString("telefono_peluqueria"));
                listaUsuarios.add(usuario);
            }
            progress.hide();
            UsuariosAdapters adapter=new UsuariosAdapters(listaUsuarios, getContext());
            recyclerUsuarios.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
