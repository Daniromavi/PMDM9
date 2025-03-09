package com.example.pmdm9;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;

public class TiendaDialogHelper {

    public static void showNuevaTiendaDialog(Context context, TiendaManager tiendaManager, Runnable actualizarTiendas) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_nueva_tienda, null);
        EditText editTextNombreTienda = dialogView.findViewById(R.id.editTextNombreTienda);

        new AlertDialog.Builder(context)
                .setTitle("Nueva Tienda")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombreTienda = editTextNombreTienda.getText().toString().trim();
                    if (nombreTienda.isEmpty()) {
                        Toast.makeText(context, "Ingrese el nombre de la tienda", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Llamamos directamente al metodo de `TiendaManager`
                    tiendaManager.crearTienda(nombreTienda, new TiendaManager.TiendaManagerCallback() {
                        @Override
                        public void onSuccess(JSONArray resultado) {
                            ((TiendasActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "Tienda creada correctamente", Toast.LENGTH_SHORT).show();
                                actualizarTiendas.run(); // Recargar lista
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            ((TiendasActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Error al crear la tienda: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    public static void showConfirmEliminarTiendaDialog(Context context, Tienda tienda, TiendaManager tiendaManager, Runnable actualizarTiendas) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Tienda")
                .setMessage("¿Está seguro de que desea eliminar la tienda \"" + tienda.getNombre() + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    tiendaManager.eliminarTienda(tienda.getId(), new TiendaManager.TiendaManagerCallback() {
                        @Override
                        public void onSuccess(JSONArray resultado) {
                            ((TiendasActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "Tienda eliminada", Toast.LENGTH_SHORT).show();
                                actualizarTiendas.run(); // Recargar lista
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            ((TiendasActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Error al eliminar la tienda: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public static void showEditarTiendaDialog(Context context, Tienda tienda, TiendaManager tiendaManager, Runnable actualizarTiendas) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_editar_tienda, null);
        EditText editTextNombreTienda = dialogView.findViewById(R.id.editTextNombreTienda);
        editTextNombreTienda.setText(tienda.getNombre());

        new AlertDialog.Builder(context)
                .setTitle("Editar Tienda")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = editTextNombreTienda.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Llamamos a TiendaManager para actualizar la tienda
                    tiendaManager.actualizarTienda(tienda.getId(), nuevoNombre, new TiendaManager.TiendaManagerCallback() {
                        @Override
                        public void onSuccess(JSONArray resultado) {
                            ((TiendasActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "Tienda actualizada correctamente", Toast.LENGTH_SHORT).show();
                                actualizarTiendas.run(); // Recargar lista después de actualizar
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            ((TiendasActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "Error al actualizar la tienda: " + error, Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


}
