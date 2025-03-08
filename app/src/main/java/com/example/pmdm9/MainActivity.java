package com.example.pmdm9;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button bttnNuevoPedido;
    private Button bttnTiendas;
    private RecyclerView recyclerView;
    private PedidoAdapter pedidoAdapter;
    private List<Pedido> listaPedidos;
    private Toolbar BarraHerramientas;
    private Map<Integer, String> tiendasMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BarraHerramientas = findViewById(R.id.toolbar);
        setSupportActionBar(BarraHerramientas);
        getSupportActionBar().setTitle("Pedidos Pendientes");

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewPedidos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crear la lista de pedidos y configurar el adapter
        listaPedidos = new ArrayList<>();
        pedidoAdapter = new PedidoAdapter(
                listaPedidos,
                // Pulsación corta: muestra el diálogo de edición
                pedido -> showEditarPedidoDialog(pedido),
                // Pulsación larga: muestra el diálogo para eliminar
                pedido -> mostrarDialogoEliminar(pedido)
        );
        recyclerView.setAdapter(pedidoAdapter);

        // Inicializar botones
        bttnNuevoPedido = findViewById(R.id.button5);
        bttnTiendas = findViewById(R.id.bttnTiendas);

        // Botón para ir a TiendasActivity
        bttnTiendas.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TiendasActivity.class)));

        // Obtener datos de la API
        obtenerPedidosDeApi();
        obtenerTiendasDeApi();

        bttnNuevoPedido.setOnClickListener(v -> showNuevoPedidoDialog());
    }

    private void obtenerPedidosDeApi() {
        AccesoRemoto accesoRemoto = new AccesoRemoto();
        accesoRemoto.obtenerListado(new AccesoRemoto.AccesoRemotoCallback() {
            @Override
            public void onSuccess(JSONArray resultado) {
                runOnUiThread(() -> {
                    try {
                        ArrayList<Pedido> pedidos = new ArrayList<>();
                        for (int i = 0; i < resultado.length(); i++) {
                            JSONObject jsonObject = resultado.getJSONObject(i);
                            int idPedido = jsonObject.getInt("idPedido");
                            String fechaPedido = jsonObject.getString("fechaPedido");
                            String fechaEstimadaPedido = jsonObject.getString("fechaEstimadaPedido");
                            String descripcionPedido = jsonObject.getString("descripcionPedido");
                            double importePedido = jsonObject.getDouble("importePedido");
                            int estadoPedido = jsonObject.getInt("estadoPedido");
                            int idTiendaFK = jsonObject.getInt("idTiendaFK");

                            String nombreTienda = tiendasMap.get(idTiendaFK);
                            Pedido pedido = new Pedido(idPedido, fechaPedido, fechaEstimadaPedido, descripcionPedido, importePedido, estadoPedido, idTiendaFK, nombreTienda);
                            pedidos.add(pedido);
                        }
                        pedidoAdapter.setPedidos(pedidos);
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error al parsear JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onTiendasSuccess(Map<Integer, String> tiendasMap) {
                MainActivity.this.tiendasMap = tiendasMap;
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al obtener los pedidos", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void obtenerTiendasDeApi() {
        AccesoRemoto accesoRemoto = new AccesoRemoto();
        accesoRemoto.obtenerTiendas(new AccesoRemoto.AccesoRemotoCallback() {
            @Override
            public void onSuccess(JSONArray resultado) { }
            @Override
            public void onTiendasSuccess(Map<Integer, String> tiendasMap) {
                MainActivity.this.tiendasMap = tiendasMap;
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNuevoPedidoDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_nuevo_pedido, null);

        EditText editTextFechaPedido = dialogView.findViewById(R.id.editTextFechaPedido);
        EditText editTextDescripcionPedido = dialogView.findViewById(R.id.editTextDescripcionPedido);
        EditText editTextImportePedido = dialogView.findViewById(R.id.editTextImportePedido);
        Spinner spinnerTiendas = dialogView.findViewById(R.id.spinnerTiendas);

        List<String> tiendasList = new ArrayList<>(tiendasMap.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiendasList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTiendas.setAdapter(adapter);

        editTextFechaPedido.setOnClickListener(v -> showDatePickerDialog(editTextFechaPedido));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Pedido")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String fechaEstimada = editTextFechaPedido.getText().toString().trim();
                    String descripcionPedido = editTextDescripcionPedido.getText().toString().trim();
                    String importePedidoString = editTextImportePedido.getText().toString().trim();
                    String tiendaSeleccionada = (String) spinnerTiendas.getSelectedItem();

                    if (fechaEstimada.isEmpty() || descripcionPedido.isEmpty() || importePedidoString.isEmpty() || tiendaSeleccionada == null) {
                        Toast.makeText(MainActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double importePedido;
                    try {
                        importePedido = Double.parseDouble(importePedidoString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Por favor, ingrese un importe válido.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Error al obtener la tienda seleccionada.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Crear el pedido llamando a la API
                    CrearPedido crearPedido = new CrearPedido();
                    crearPedido.darAlta(fechaEstimada, descripcionPedido, importePedido, idTiendaFK, new CrearPedido.CrearPedidoCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Pedido guardado correctamente", Toast.LENGTH_SHORT).show();
                                obtenerPedidosDeApi();
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Error al guardar el pedido: " + error, Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showDatePickerDialog(final EditText editTextFechaPedido) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth1) -> {
            String fechaSeleccionada = year1 + "-" + (month1 + 1) + "-" + dayOfMonth1;
            editTextFechaPedido.setText(fechaSeleccionada);
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void mostrarDialogoEliminar(Pedido pedido) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Pedido")
                .setMessage("¿Estás seguro de que quieres eliminar este pedido?")
                .setPositiveButton("Sí", (dialog, which) -> new EliminarPedidoTask(pedido).execute())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class EliminarPedidoTask extends AsyncTask<Void, Void, Boolean> {
        private Pedido pedido;

        public EliminarPedidoTask(Pedido pedido) {
            this.pedido = pedido;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://192.168.1.139/gestion_pedidos/pedidos.php?idPedido=" + pedido.getIdPedido())
                        .delete()
                        .build();
                Response response = client.newCall(request).execute();
                return response.isSuccessful();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean exito) {
            if (exito) {
                pedidoAdapter.removePedido(pedido);
                Toast.makeText(MainActivity.this, "Pedido eliminado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error al eliminar pedido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEditarPedidoDialog(Pedido pedido) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_editar_pedido, null);

        Spinner spinnerTiendasEditar = dialogView.findViewById(R.id.spinnerTiendasEditar);
        EditText editTextFechaEditar = dialogView.findViewById(R.id.editTextFechaEditar);
        EditText editTextDescripcionEditar = dialogView.findViewById(R.id.editTextDescripcionEditar);
        EditText editTextImporteEditar = dialogView.findViewById(R.id.editTextImporteEditar);
        CheckBox checkBoxRecibido = dialogView.findViewById(R.id.checkBoxRecibido);

        // Cargar la lista de tiendas en el spinner
        List<String> tiendasList = new ArrayList<>(tiendasMap.values());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiendasList);
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
        editTextFechaEditar.setOnClickListener(v -> showDatePickerDialog(editTextFechaEditar));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pedido Nº " + pedido.getIdPedido())
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String fechaNueva = editTextFechaEditar.getText().toString().trim();
                    String descNueva = editTextDescripcionEditar.getText().toString().trim();
                    String importeString = editTextImporteEditar.getText().toString().trim();

                    // Aquí se obtiene el estado según el CheckBox: 1 si está marcado, 0 si no.
                    boolean recibido = checkBoxRecibido.isChecked();
                    int nuevoEstado = recibido ? 1 : 0;

                    if (fechaNueva.isEmpty() || descNueva.isEmpty() || importeString.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double importeNuevo;
                    try {
                        importeNuevo = Double.parseDouble(importeString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Importe inválido", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Error al obtener la tienda seleccionada.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Llamar al método remoto para actualizar el pedido, enviando también el nuevo estado
                    actualizarPedidoRemoto(
                            pedido.getIdPedido(),
                            fechaNueva,
                            descNueva,
                            importeNuevo,
                            nuevoEstado,
                            idTiendaFK
                    );
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    // Metodo para actualizar el pedido mediante ActualizarPedidoRemoto
    private void actualizarPedidoRemoto(int idPedido, String fecha, String descripcion,
                                        double importe, int estado, int idTiendaFK) {
        ActualizarPedidoRemoto actualizarPedidoRemoto = new ActualizarPedidoRemoto();
        actualizarPedidoRemoto.actualizarPedido(
                idPedido,
                fecha,
                descripcion,
                importe,
                estado,
                idTiendaFK,
                exito -> runOnUiThread(() -> {
                    if (exito) {
                        Toast.makeText(MainActivity.this, "Pedido actualizado correctamente", Toast.LENGTH_SHORT).show();
                        obtenerPedidosDeApi();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al actualizar pedido", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }
}
