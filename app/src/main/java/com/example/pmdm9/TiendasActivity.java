package com.example.pmdm9;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;

public class TiendasActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTiendas;
    private TiendaAdapter tiendaAdapter;
    private List<Tienda> listaTiendas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiendas);

        recyclerViewTiendas = findViewById(R.id.recyclerViewTiendas);
        recyclerViewTiendas.setLayoutManager(new LinearLayoutManager(this));

        listaTiendas = new ArrayList<>();
        // Se instancia el adapter con dos listeners: uno para editar (click corto) y otro para eliminar (long click)
        tiendaAdapter = new TiendaAdapter(
                listaTiendas,
                tienda -> showEditarTiendaDialog(tienda),      // Pulsación corta para editar
                tienda -> showEliminarTiendaDialog(tienda)       // Pulsación larga para eliminar
        );
        recyclerViewTiendas.setAdapter(tiendaAdapter);

        Button bttnVolver = findViewById(R.id.bttnVolver);
        bttnVolver.setOnClickListener(v -> finish());

        Button bttnNuevaTienda = findViewById(R.id.bttnNuevaTienda);
        bttnNuevaTienda.setOnClickListener(v -> showNuevaTiendaDialog());

        obtenerTiendasDeApi();
    }

    private void showNuevaTiendaDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_nueva_tienda, null);
        EditText editTextNombreTienda = dialogView.findViewById(R.id.editTextNombreTienda);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nueva Tienda")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombreTienda = editTextNombreTienda.getText().toString().trim();
                    if (nombreTienda.isEmpty()) {
                        Toast.makeText(TiendasActivity.this, "Ingrese el nombre de la tienda", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    crearTienda(nombreTienda);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void crearTienda(String nombreTienda) {
        OkHttpClient client = new OkHttpClient();
        okhttp3.RequestBody formBody = new okhttp3.FormBody.Builder()
                .add("nombreTienda", nombreTienda)
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://192.168.1.139/gestion_pedidos/tiendas.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(TiendasActivity.this, "Tienda creada correctamente", Toast.LENGTH_SHORT).show();
                        obtenerTiendasDeApi();
                    } else {
                        Toast.makeText(TiendasActivity.this, "Error al crear la tienda", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void obtenerTiendasDeApi() {
        AccesoRemoto accesoRemoto = new AccesoRemoto();
        accesoRemoto.obtenerTiendas(new AccesoRemoto.AccesoRemotoCallback() {
            @Override
            public void onSuccess(JSONArray resultado) {
                try {
                    List<Tienda> tiendas = new ArrayList<>();
                    for (int i = 0; i < resultado.length(); i++) {
                        JSONObject jsonObject = resultado.getJSONObject(i);
                        int idTienda = jsonObject.getInt("idTienda");
                        String nombreTienda = jsonObject.getString("nombreTienda");
                        Tienda tienda = new Tienda(idTienda, nombreTienda);
                        tiendas.add(tienda);
                    }
                    runOnUiThread(() -> tiendaAdapter.setTiendas(tiendas));
                } catch (JSONException e) {
                    Log.e("TiendasActivity", "Error al parsear JSON: " + e.getMessage());
                }
            }
            @Override
            public void onTiendasSuccess(Map<Integer, String> tiendasMap) {
                // No es necesario para esta Activity
            }
            @Override
            public void onFailure(String error) {
                Log.e("TiendasActivity", error);
            }
        });
    }

    // Mostrar diálogo para confirmar eliminación de una tienda (pulsación larga)
    private void showEliminarTiendaDialog(Tienda tienda) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Tienda")
                .setMessage("¿Está seguro de que desea eliminar la tienda \"" + tienda.getNombre() + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarTienda(tienda))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void eliminarTienda(Tienda tienda) {
        OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("http://192.168.1.139/gestion_pedidos/tiendas.php?idTienda=" + tienda.getId())
                .delete()
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(TiendasActivity.this, "Tienda eliminada", Toast.LENGTH_SHORT).show();
                        tiendaAdapter.removeTienda(tienda);
                    } else {
                        Toast.makeText(TiendasActivity.this, "Error al eliminar la tienda", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Mostrar diálogo para editar una tienda (pulsación corta)
    private void showEditarTiendaDialog(Tienda tienda) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_editar_tienda, null);
        EditText editTextNombreTienda = dialogView.findViewById(R.id.editTextNombreTienda);
        // Pre-cargar el nombre actual
        editTextNombreTienda.setText(tienda.getNombre());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Tienda")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = editTextNombreTienda.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(TiendasActivity.this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    actualizarTienda(tienda, nuevoNombre);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Actualizar la tienda en el servidor y refrescar la UI
    private void actualizarTienda(Tienda tienda, String nuevoNombre) {
        try {
            String url = "http://192.168.1.139/gestion_pedidos/tiendas.php?"
                    + "idTienda=" + tienda.getId()
                    + "&nombreTienda=" + java.net.URLEncoder.encode(nuevoNombre, "UTF-8");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(null, new byte[0]);
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .put(body)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(TiendasActivity.this, "Tienda actualizada correctamente", Toast.LENGTH_SHORT).show();
                            tienda.setNombre(nuevoNombre);
                            tiendaAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(TiendasActivity.this, "Error al actualizar la tienda", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(TiendasActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
