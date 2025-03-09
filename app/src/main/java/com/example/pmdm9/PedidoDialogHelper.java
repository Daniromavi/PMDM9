package com.example.pmdm9;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class PedidoDialogHelper {

    public static void mostrarNuevoPedidoDialog(Context context, Map<Integer, String> tiendasMap, PedidoManager pedidoManager, Runnable actualizarPedidos) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_nuevo_pedido, null);

        EditText editTextFechaPedido = dialogView.findViewById(R.id.editTextFechaPedido);
        EditText editTextDescripcionPedido = dialogView.findViewById(R.id.editTextDescripcionPedido);
        EditText editTextImportePedido = dialogView.findViewById(R.id.editTextImportePedido);
        Spinner spinnerTiendas = dialogView.findViewById(R.id.spinnerTiendas);

        List<String> tiendasList = new ArrayList<>(tiendasMap.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, tiendasList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTiendas.setAdapter(adapter);

        editTextFechaPedido.setOnClickListener(v -> showDatePickerDialog(context, editTextFechaPedido));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Nuevo Pedido")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String fechaEstimada = editTextFechaPedido.getText().toString().trim();
                    String descripcionPedido = editTextDescripcionPedido.getText().toString().trim();
                    String importePedidoString = editTextImportePedido.getText().toString().trim();
                    String tiendaSeleccionada = (String) spinnerTiendas.getSelectedItem();

                    if (fechaEstimada.isEmpty() || descripcionPedido.isEmpty() || importePedidoString.isEmpty() || tiendaSeleccionada == null) {
                        Toast.makeText(context, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double importePedido;
                    try {
                        importePedido = Double.parseDouble(importePedidoString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Por favor, ingrese un importe válido.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int idTiendaFK = -1;
                    for (Map.Entry<Integer, String> entry : tiendasMap.entrySet()) {
                        if (entry.getValue().equals(tiendaSeleccionada)) {
                            idTiendaFK = entry.getKey();
                            break;
                        }
                    }

                    if (idTiendaFK == -1) {
                        Toast.makeText(context, "Error al obtener la tienda seleccionada.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Crear el pedido llamando a la API
                    pedidoManager.darAlta(fechaEstimada, descripcionPedido, importePedido, idTiendaFK, new PedidoManager.CrearPedidoCallback() {
                        @Override
                        public void onSuccess() {
                            ((MainActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "Pedido guardado correctamente", Toast.LENGTH_SHORT).show();
                                actualizarPedidos.run();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            ((MainActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "Error al guardar el pedido: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    public static void mostrarDialogoEliminar(Context context, Pedido pedido, PedidoManager pedidoManager, PedidoAdapter pedidoAdapter) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Pedido")
                .setMessage("¿Estás seguro de que quieres eliminar este pedido?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    pedidoManager.eliminarPedido(pedido.getIdPedido(), new PedidoManager.EliminarPedidoCallback() {
                        @Override
                        public void onSuccess() {
                            ((MainActivity) context).runOnUiThread(() -> {
                                pedidoAdapter.removePedido(pedido);
                                Toast.makeText(context, "Pedido eliminado correctamente", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            ((MainActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Error al eliminar pedido: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }


    public static void mostrarEditarPedidoDialog(Context context, Pedido pedido, Map<Integer, String> tiendasMap, PedidoManager pedidoManager, Runnable actualizarPedidos) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_editar_pedido, null);

        Spinner spinnerTiendasEditar = dialogView.findViewById(R.id.spinnerTiendasEditar);
        EditText editTextFechaEditar = dialogView.findViewById(R.id.editTextFechaEditar);
        EditText editTextDescripcionEditar = dialogView.findViewById(R.id.editTextDescripcionEditar);
        EditText editTextImporteEditar = dialogView.findViewById(R.id.editTextImporteEditar);
        CheckBox checkBoxRecibido = dialogView.findViewById(R.id.checkBoxRecibido);

        // Cargar la lista de tiendas en el spinner
        List<String> tiendasList = new ArrayList<>(tiendasMap.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, tiendasList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTiendasEditar.setAdapter(adapter);

        // Pre-cargar los datos del pedido en el diálogo
        editTextFechaEditar.setText(pedido.getFechaEstimadaPedido());
        editTextDescripcionEditar.setText(pedido.getDescripcionPedido());
        editTextImporteEditar.setText(String.valueOf(pedido.getImportePedido()));

        // Lógica del CheckBox: si estadoPedido es 1, significa que el pedido está entregado
        checkBoxRecibido.setChecked(pedido.getEstadoPedido() == 1);

        // Seleccionar la tienda actual en el spinner
        String nombreTiendaActual = pedido.getNombreTienda();
        if (nombreTiendaActual != null) {
            int index = tiendasList.indexOf(nombreTiendaActual);
            if (index != -1) {
                spinnerTiendasEditar.setSelection(index);
            }
        }

        // Configurar el DatePicker para la fecha
        editTextFechaEditar.setOnClickListener(v -> showDatePickerDialog(context, editTextFechaEditar));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pedido Nº " + pedido.getIdPedido())
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String fechaNueva = editTextFechaEditar.getText().toString().trim();
                    String descNueva = editTextDescripcionEditar.getText().toString().trim();
                    String importeString = editTextImporteEditar.getText().toString().trim();

                    boolean recibido = checkBoxRecibido.isChecked();
                    int nuevoEstado = recibido ? 1 : 0;

                    if (fechaNueva.isEmpty() || descNueva.isEmpty() || importeString.isEmpty()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double importeNuevo;
                    try {
                        importeNuevo = Double.parseDouble(importeString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Importe inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String tiendaSeleccionada = (String) spinnerTiendasEditar.getSelectedItem();
                    int idTiendaFK = -1;
                    for (Map.Entry<Integer, String> entry : tiendasMap.entrySet()) {
                        if (entry.getValue().equals(tiendaSeleccionada)) {
                            idTiendaFK = entry.getKey();
                            break;
                        }
                    }

                    if (idTiendaFK == -1) {
                        Toast.makeText(context, "Error al obtener la tienda seleccionada.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Llamar al metodo remoto para actualizar el pedido
                    pedidoManager.actualizarPedido(pedido.getIdPedido(), fechaNueva, descNueva, importeNuevo, nuevoEstado, idTiendaFK, new PedidoManager.ActualizarPedidoCallback() {
                        @Override
                        public void onSuccess() {
                            ((MainActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "Pedido actualizado correctamente", Toast.LENGTH_SHORT).show();
                                actualizarPedidos.run(); // Recargar la lista después de actualizar
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            ((MainActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Error al actualizar: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private static void showDatePickerDialog(Context context, final EditText editTextFechaPedido) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, month1, dayOfMonth1) -> {
            String fechaSeleccionada = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1;
            editTextFechaPedido.setText(fechaSeleccionada);
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }


}
