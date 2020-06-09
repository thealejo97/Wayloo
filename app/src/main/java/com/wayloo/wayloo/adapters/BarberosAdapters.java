package com.wayloo.wayloo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wayloo.wayloo.R;
import com.wayloo.wayloo.entidades.BarberosU;

import java.util.List;

public class BarberosAdapters extends RecyclerView.Adapter<BarberosAdapters.UsuariosHolder> {

        List<BarberosU> listaBarberos;
        RequestQueue request;
        Context context;

public BarberosAdapters(List<BarberosU> listaBarberos, Context context){
    this.listaBarberos = listaBarberos;
    this.context = context;
    request= Volley.newRequestQueue(context);
}

    @NonNull
    @Override
    public UsuariosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.barberos_list,parent,false);
    RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);
    return new UsuariosHolder(vista);
    }

    @Override
    public void onBindViewHolder(UsuariosHolder holder, int position){
    //holder.imgBarbero.setImageBitmap(listaBarberos.get(position).getImgBarber());
    holder.txtNombre.setText(listaBarberos.get(position).getNombre()+" "+listaBarberos.get(position).getApellido_P());
    holder.txtTelefono.setText(listaBarberos.get(position).getTelefono());
    holder.txthInicio.setText(listaBarberos.get(position).geth_inicio()+ " a " + listaBarberos.get(position).geth_fin());
    holder.txtHFin.setText(listaBarberos.get(position).geth_fin());
    holder.txtCalifi.setText(listaBarberos.get(position).getCalificacion());

    if(listaBarberos.get(position).getFireB() != null){
        cargarImagenWebService(listaBarberos.get(position).getFireB(),holder);
    }
    else {
        holder.imgBarbero.setImageResource(R.drawable.notfouduser);
    }

    }

    private void cargarImagenWebService(String imgBarber, final UsuariosHolder holder) {
        String ip =context.getString(R.string.ip_way);
        String urlimagen=ip+"/consultas/imagenes/"+imgBarber+".jpg";
        urlimagen=urlimagen.replace(" ","%20");
        Log.e("URL IMG", urlimagen);

        ImageRequest imageRequest = new ImageRequest(urlimagen, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                holder.imgBarbero.setImageBitmap(redondearBitmap(redimensionarImagen(response,100,100)));
            }
        }, 0, 0, ImageView.ScaleType.CENTER, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
            }
        });
    request.add(imageRequest);
}

    @Override
    public int getItemCount(){
    return listaBarberos.size();
    }

    public class UsuariosHolder extends RecyclerView.ViewHolder{

    TextView txtNombre,txtApellido, txtTelefono,txthInicio, txtHFin, txtCalifi;
    ImageView imgBarbero;
    public UsuariosHolder(View iteView){
        super(iteView);
        imgBarbero=(ImageView) iteView.findViewById(R.id.imagenBarberoIndividual);
        txtNombre = (TextView) iteView.findViewById(R.id.ReplaceNameP);
        txtTelefono= (TextView) iteView.findViewById(R.id.ReplaceTelP);
        txthInicio= (TextView) iteView.findViewById(R.id.ReplaceHinicio);
        txtHFin= (TextView) iteView.findViewById(R.id.ReplaceHfin);
        txtCalifi= (TextView) iteView.findViewById(R.id.ReplaceCalificacionP);
    }
}

    private Bitmap redimensionarImagen(Bitmap bitmap, float anchoNuevo, float altoNuevo) {

        int ancho = bitmap.getWidth();
        int alto = bitmap.getHeight();

        if (ancho > anchoNuevo || alto > altoNuevo) {
            float escalaAncho = anchoNuevo / ancho;
            float escalaAlto = altoNuevo / alto;

            Matrix matrix = new Matrix();
            matrix.postScale(escalaAncho, escalaAlto);

            return Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false);

        } else {
            return bitmap;
        }
    }

    private Bitmap redondearBitmap(Bitmap bitAconvertir){
        Bitmap imageBitmap= bitAconvertir;
        RoundedBitmapDrawable roundedBitmapDrawable=
                RoundedBitmapDrawableFactory.create( context.getResources(), imageBitmap);
        roundedBitmapDrawable.setCornerRadius(175.0f);
        roundedBitmapDrawable.setAntiAlias(true);

        Bitmap imageBitmapConBlanco=addWhiteBorder(drawableToBitmap(roundedBitmapDrawable),2);
        RoundedBitmapDrawable roundedBitmapDrawableBlanco=
                RoundedBitmapDrawableFactory.create( context.getResources(), imageBitmapConBlanco);
        roundedBitmapDrawableBlanco.setCornerRadius(180.0f);
        roundedBitmapDrawableBlanco.setAntiAlias(true);


        return  drawableToBitmap(roundedBitmapDrawableBlanco);
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
