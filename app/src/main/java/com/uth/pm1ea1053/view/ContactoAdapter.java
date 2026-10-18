package com.uth.pm1ea1053.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.uth.pm1ea1053.R;
import com.uth.pm1ea1053.model.Contacto;
import com.uth.pm1ea1053.util.ImageUtils;

import java.util.List;

/**
 * Adaptador de la lista: muestra miniatura, nombre, país y teléfono de cada contacto.
 */
public class ContactoAdapter extends ArrayAdapter<Contacto> {

    private final LayoutInflater inflater;
    private final LruCache<String, Bitmap> cacheMiniaturas = new LruCache<>(40);

    public ContactoAdapter(Context context, List<Contacto> contactos) {
        super(context, 0, contactos);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_contacto, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contacto c = getItem(position);
        if (c != null) {
            holder.nombre.setText(c.getNombre());
            holder.detalle.setText(c.getPais() + " · " + c.getTelefono());

            Bitmap miniatura = obtenerMiniatura(c.getImagen());
            if (miniatura != null) {
                holder.foto.setImageBitmap(miniatura);
            } else {
                holder.foto.setImageResource(R.drawable.ic_person);
            }
        }
        return convertView;
    }

    private Bitmap obtenerMiniatura(String ruta) {
        if (ruta == null) {
            return null;
        }
        Bitmap bitmap = cacheMiniaturas.get(ruta);
        if (bitmap == null) {
            bitmap = ImageUtils.cargar(ruta, 160);
            if (bitmap != null) {
                cacheMiniaturas.put(ruta, bitmap);
            }
        }
        return bitmap;
    }

    private static class ViewHolder {
        final ImageView foto;
        final TextView nombre;
        final TextView detalle;

        ViewHolder(View vista) {
            foto = vista.findViewById(R.id.ivItemFoto);
            nombre = vista.findViewById(R.id.tvItemNombre);
            detalle = vista.findViewById(R.id.tvItemDetalle);
        }
    }
}
