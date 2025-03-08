package com.example.pmdm9;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TiendaDialogHelper {

    public interface OnNuevaTiendaListener {
        void onNuevaTienda(String nombreTienda);
    }

    public interface OnEditarTiendaListener {
        void onEditarTienda(String nuevoNombre);
    }

    public interface OnConfirmEliminarTiendaListener {
        void onEliminarTienda();
    }

    public static void showNuevaTiendaDialog(Context context, OnNuevaTiendaListener listener) {
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
                    listener.onNuevaTienda(nombreTienda);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public static void showEditarTiendaDialog(Context context, Tienda tienda, OnEditarTiendaListener listener) {
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
                    listener.onEditarTienda(nuevoNombre);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public static void showConfirmEliminarTiendaDialog(Context context, Tienda tienda, OnConfirmEliminarTiendaListener listener) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Tienda")
                .setMessage("¿Está seguro de que desea eliminar la tienda \"" + tienda.getNombre() + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> listener.onEliminarTienda())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
