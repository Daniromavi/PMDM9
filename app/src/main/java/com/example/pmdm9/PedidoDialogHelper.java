package com.example.pmdm9;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PedidoDialogHelper {

    public interface OnEditarPedidoListener {
        void onEditarPedido(String fecha, String descripcion, double importe, int estado, String tiendaSeleccionada);
    }

    public static void showEditarPedidoDialog(Context context, Pedido pedido, OnEditarPedidoListener listener, Map<Integer, String> tiendasMap) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_editar_pedido, null);

        Spinner spinnerTiendas = dialogView.findViewById(R.id.spinnerTiendasEditar);
        EditText editTextFecha = dialogView.findViewById(R.id.editTextFechaEditar);
        EditText editTextDescripcion = dialogView.findViewById(R.id.editTextDescripcionEditar);
        EditText editTextImporte = dialogView.findViewById(R.id.editTextImporteEditar);
        CheckBox checkBoxRecibido = dialogView.findViewById(R.id.checkBoxRecibido);

        // Pre-cargar datos del pedido
        editTextFecha.setText(pedido.getFechaEstimadaPedido());
        editTextDescripcion.setText(pedido.getDescripcionPedido());
        editTextImporte.setText(String.valueOf(pedido.getImportePedido()));
        checkBoxRecibido.setChecked(pedido.getEstadoPedido() == 1);

        // Cargar spinner con nombres de tiendas
        List<String> listaTiendas = new ArrayList<>(tiendasMap.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, listaTiendas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTiendas.setAdapter(adapter);
        // Seleccionar la tienda actual
        int index = listaTiendas.indexOf(pedido.getNombreTienda());
        if(index != -1){
            spinnerTiendas.setSelection(index);
        }

        new AlertDialog.Builder(context)
                .setTitle("Editar Pedido Nº " + pedido.getIdPedido())
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String fecha = editTextFecha.getText().toString().trim();
                    String descripcion = editTextDescripcion.getText().toString().trim();
                    String importeStr = editTextImporte.getText().toString().trim();
                    boolean recibido = checkBoxRecibido.isChecked();
                    int estado = recibido ? 1 : 0;
                    String tiendaSeleccionada = (String) spinnerTiendas.getSelectedItem();

                    if(fecha.isEmpty() || descripcion.isEmpty() || importeStr.isEmpty()){
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double importe;
                    try {
                        importe = Double.parseDouble(importeStr);
                    } catch(NumberFormatException e){
                        Toast.makeText(context, "Importe inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listener.onEditarPedido(fecha, descripcion, importe, estado, tiendaSeleccionada);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
