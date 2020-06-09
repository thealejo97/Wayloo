package com.wayloo.wayloo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.wayloo.wayloo.R;
import com.wayloo.wayloo.entidades.Usuario;

import java.util.List;

public class UsuariosAdapters extends RecyclerView.Adapter<UsuariosAdapters.UsuariosHolder> {
List<Usuario> listaUsuarios;
    private Context ctx;


public  UsuariosAdapters(List<Usuario> listaUsuarios, Context context){
    this.listaUsuarios = listaUsuarios;
    ctx= context;
}

    @NonNull
    @Override
    public UsuariosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.usuarios_list,parent,false);
    RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);
    return new UsuariosHolder(vista);
    }

    @Override
    public void onBindViewHolder(UsuariosHolder holder, int position){
    holder.txtNombre.setText(listaUsuarios.get(position).getNombre());
    holder.txtTelefono.setText(listaUsuarios.get(position).getTelefono());
    holder.txtDir.setText(listaUsuarios.get(position).getDireccion());
    holder.txtCiu.setText(listaUsuarios.get(position).getCiudad());
        Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(),
                R.drawable.cartelcorona);
       // Drawable d = new BitmapDrawable(ctx.getResources(), icon);
  //  holder.RLC.setBackground(d);

    }
    @Override
    public int getItemCount(){
    return listaUsuarios.size();
    }
public class UsuariosHolder extends RecyclerView.ViewHolder{

    TextView txtNombre, txtTelefono,txtDir, txtCiu, txtCalifi;
  //  RelativeLayout RLC;
    public UsuariosHolder(View iteView){
        super(iteView);

        txtNombre = (TextView) iteView.findViewById(R.id.NameReplaceBarber);
        txtTelefono= (TextView) iteView.findViewById(R.id.TelReplaceBarber);
        txtDir= (TextView) iteView.findViewById(R.id.DirReplaceBarber);
        txtCiu= (TextView) iteView.findViewById(R.id.CiuReplaceBarber);
        txtCalifi= (TextView) iteView.findViewById(R.id.CalReplaceBarber);
   //     RLC = (RelativeLayout) iteView.findViewById(R.id.RLCartel);
    }
}
    private Bitmap redondearBitmap(Bitmap bitAconvertir){
        Bitmap imageBitmap= bitAconvertir;
        RoundedBitmapDrawable roundedBitmapDrawable=
                RoundedBitmapDrawableFactory.create( ctx.getResources(), imageBitmap);
        roundedBitmapDrawable.setCornerRadius(175.0f);
        roundedBitmapDrawable.setAntiAlias(true);

        Bitmap imageBitmapConBlanco=addWhiteBorder(drawableToBitmap(roundedBitmapDrawable),2);
        RoundedBitmapDrawable roundedBitmapDrawableBlanco=
                RoundedBitmapDrawableFactory.create( ctx.getResources(), imageBitmapConBlanco);
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
