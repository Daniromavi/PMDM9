package com.example.pmdm9;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<Pedido> listaPedidos;

    // Interfaces para click corto y largo
    private OnPedidoClickListener clickListener;
    private OnPedidoLongClickListener longClickListener;

    // Interfaz para manejar la pulsación corta
    public interface OnPedidoClickListener {
        void onPedidoClick(Pedido pedido);
    }

    // Interfaz para manejar la pulsación larga
    public interface OnPedidoLongClickListener {
        void onPedidoLongClick(Pedido pedido);
    }

    // Constructor recibiendo ambos listeners
    public PedidoAdapter(List<Pedido> listaPedidos,
                         OnPedidoClickListener clickListener,
                         OnPedidoLongClickListener longClickListener) {
        this.listaPedidos = listaPedidos;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    // Método para actualizar la lista de pedidos
    public void setPedidos(List<Pedido> listaPedidos) {
        this.listaPedidos = listaPedidos;
        notifyDataSetChanged();
    }

    // Método para eliminar un pedido de la lista y notificar el cambio
    public void removePedido(Pedido pedido) {
        int pos = listaPedidos.indexOf(pedido);
        if (pos != -1) {
            listaPedidos.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    // Crear el ViewHolder
    @Override
    public PedidoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    // Asignar los valores a cada ítem
    @Override
    public void onBindViewHolder(PedidoViewHolder holder, int position) {
        Pedido pedido = listaPedidos.get(position);

        holder.tiendaPedido.setText(pedido.getNombreTienda());
        holder.fechaPedido.setText(pedido.getFechaEstimadaPedido());
        holder.descripcionPedido.setText(pedido.getDescripcionPedido());

        // Manejar la pulsación corta
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPedidoClick(pedido);
            }
        });

        // Manejar la pulsación larga
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onPedidoLongClick(pedido);
            }
            return true; // Consumimos el evento
        });
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    // Clase ViewHolder
    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tiendaPedido, fechaPedido, descripcionPedido;

        public PedidoViewHolder(View itemView) {
            super(itemView);
            tiendaPedido = itemView.findViewById(R.id.TiendaPedido);
            fechaPedido = itemView.findViewById(R.id.fechaPedido);
            descripcionPedido = itemView.findViewById(R.id.descripcionPedido);
        }
    }
}
