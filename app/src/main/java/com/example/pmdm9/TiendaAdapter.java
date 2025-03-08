package com.example.pmdm9;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TiendaAdapter extends RecyclerView.Adapter<TiendaAdapter.TiendaViewHolder> {

    private List<Tienda> listaTiendas;
    private OnTiendaClickListener clickListener;
    private OnTiendaLongClickListener longClickListener;

    // Interfaz para manejar la pulsación corta
    public interface OnTiendaClickListener {
        void onTiendaClick(Tienda tienda);
    }

    // Interfaz para manejar la pulsación larga
    public interface OnTiendaLongClickListener {
        void onTiendaLongClick(Tienda tienda);
    }

    // Constructor recibiendo ambas interfaces
    public TiendaAdapter(List<Tienda> listaTiendas, OnTiendaClickListener clickListener, OnTiendaLongClickListener longClickListener) {
        this.listaTiendas = listaTiendas;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TiendaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tienda, parent, false);
        return new TiendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TiendaViewHolder holder, int position) {
        Tienda tienda = listaTiendas.get(position);
        holder.textNombreTienda.setText(tienda.getNombre());

        // Pulsación corta: para editar la tienda
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTiendaClick(tienda);
            }
        });

        // Pulsación larga: para eliminar la tienda
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTiendaLongClick(tienda);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaTiendas.size();
    }

    public void setTiendas(List<Tienda> tiendas) {
        this.listaTiendas = tiendas;
        notifyDataSetChanged();
    }

    // Método para eliminar una tienda de la lista y notificar el cambio
    public void removeTienda(Tienda tienda) {
        int pos = listaTiendas.indexOf(tienda);
        if (pos != -1) {
            listaTiendas.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    static class TiendaViewHolder extends RecyclerView.ViewHolder {
        TextView textNombreTienda;

        public TiendaViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombreTienda = itemView.findViewById(R.id.textNombreTienda);
        }
    }
}
