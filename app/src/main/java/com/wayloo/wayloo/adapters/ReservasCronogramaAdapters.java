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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.wayloo.wayloo.R;
import com.wayloo.wayloo.entidades.ReservasCronograma;

import java.util.List;

public class ReservasCronogramaAdapters extends RecyclerView.Adapter<ReservasCronogramaAdapters.ReservasHolder> {
List<ReservasCronograma> listaReservasCronograma;
    private Context ctx;


public ReservasCronogramaAdapters(List<ReservasCronograma> listaReservasCronogramas, Context context){
    this.listaReservasCronograma = listaReservasCronogramas;
    ctx= context;
}

    @NonNull
    @Override
    public ReservasHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.reservascronograma_list,parent,false);
    RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);
    return new ReservasHolder(vista);
    }

    @Override
    public void onBindViewHolder(ReservasHolder holder, int position){
    holder.FechaReserva.setText(listaReservasCronograma.get(position).getFecha_r());
    holder.HoraIReserva.setText(listaReservasCronograma.get(position).getHI_r());
    holder.HoraFReserva.setText(listaReservasCronograma.get(position).getHF_r());
    holder.nombreBarberoReserva.setText(listaReservasCronograma.get(position).getNombre_cliente());
        Bitmap icon = BitmapFactory.decodeResource(ctx.getResources(),
                R.drawable.cartelcorona);
        Drawable d = new BitmapDrawable(ctx.getResources(), icon);

    }
    @Override
    public int getItemCount(){
    return listaReservasCronograma.size();
    }
public class ReservasHolder extends RecyclerView.ViewHolder{

    TextView FechaReserva, HoraIReserva,HoraFReserva, nombreBarberoReserva;
    public ReservasHolder(View iteView){
        super(iteView);

        FechaReserva = (TextView) iteView.findViewById(R.id.FechaReservaCronograma);
        HoraIReserva= (TextView) iteView.findViewById(R.id.HoraIReservaCronograma);
        HoraFReserva= (TextView) iteView.findViewById(R.id.HoraFReservaCronograma);
        nombreBarberoReserva= (TextView) iteView.findViewById(R.id.NombreClienteReservaCronograma);
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
