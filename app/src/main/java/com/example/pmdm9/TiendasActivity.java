package com.example.pmdm9;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TiendasActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTiendas;
    private TiendaAdapter tiendaAdapter;
    private List<Tienda> listaTiendas;
    private TiendaManager tiendaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiendas);

        recyclerViewTiendas = findViewById(R.id.recyclerViewTiendas);
        recyclerViewTiendas.setLayoutManager(new LinearLayoutManager(this));

        listaTiendas = new ArrayList<>();
        // Instanciamos el adapter con dos listeners: uno para editar (click corto) y otro para eliminar (long click)
        tiendaAdapter = new TiendaAdapter(
                listaTiendas,
                tienda -> TiendaDialogHelper.showEditarTiendaDialog(this, tienda, nuevoNombre -> actualizarTienda(tienda, nuevoNombre)),
                tienda -> TiendaDialogHelper.showConfirmEliminarTiendaDialog(this, tienda, () -> eliminarTienda(tienda))
        );
        recyclerViewTiendas.setAdapter(tiendaAdapter);

        Button bttnVolver = findViewById(R.id.bttnVolver);
        bttnVolver.setOnClickListener(v -> finish());

        Button bttnNuevaTienda = findViewById(R.id.bttnNuevaTienda);
        bttnNuevaTienda.setOnClickListener(v ->
                TiendaDialogHelper.showNuevaTiendaDialog(this, nombreTienda -> crearTienda(nombreTienda))
        );

        tiendaManager = new TiendaManager();
        obtenerTiendasDeApi();
    }

    private void obtenerTiendasDeApi() {
        tiendaManager.obtenerTiendas(new TiendaManager.TiendaManagerCallback() {
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
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }

            public void onTiendasSuccess(Map<Integer, String> tiendasMap) {
                // No se utiliza en este manager
            }
        });
    }

    private void crearTienda(String nombreTienda) {
        tiendaManager.crearTienda(nombreTienda, new TiendaManager.TiendaManagerCallback() {
            @Override
            public void onSuccess(JSONArray resultado) {
                runOnUiThread(() -> {
                    Toast.makeText(TiendasActivity.this, "Tienda creada correctamente", Toast.LENGTH_SHORT).show();
                    obtenerTiendasDeApi();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error al crear la tienda: " + error, Toast.LENGTH_SHORT).show());
            }

            public void onTiendasSuccess(Map<Integer, String> tiendasMap) { }
        });
    }

    private void actualizarTienda(Tienda tienda, String nuevoNombre) {
        tiendaManager.actualizarTienda(tienda.getId(), nuevoNombre, new TiendaManager.TiendaManagerCallback() {
            @Override
            public void onSuccess(JSONArray resultado) {
                runOnUiThread(() -> {
                    Toast.makeText(TiendasActivity.this, "Tienda actualizada correctamente", Toast.LENGTH_SHORT).show();
                    tienda.setNombre(nuevoNombre);
                    tiendaAdapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error al actualizar la tienda: " + error, Toast.LENGTH_SHORT).show());
            }

            public void onTiendasSuccess(Map<Integer, String> tiendasMap) { }
        });
    }

    private void eliminarTienda(Tienda tienda) {
        tiendaManager.eliminarTienda(tienda.getId(), new TiendaManager.TiendaManagerCallback() {
            @Override
            public void onSuccess(JSONArray resultado) {
                runOnUiThread(() -> {
                    Toast.makeText(TiendasActivity.this, "Tienda eliminada", Toast.LENGTH_SHORT).show();
                    tiendaAdapter.removeTienda(tienda);
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(TiendasActivity.this, "Error al eliminar la tienda: " + error, Toast.LENGTH_SHORT).show());
            }

            public void onTiendasSuccess(Map<Integer, String> tiendasMap) { }
        });
    }
}
