package com.example.pmdm9;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PedidoManager {
    private static final String BASE_URL = "http://192.168.1.139/gestion_pedidos/pedidos.php";
    private static final String TIENDAS_URL = "http://192.168.1.139/gestion_pedidos/tiendas.php";
    private OkHttpClient client;

    public PedidoManager() {
        client = new OkHttpClient();
    }

    // Interfaz para manejar respuestas asíncronas
    public interface PedidoManagerCallback {
        void onSuccess(JSONArray resultado);
        void onFailure(String error);
    }

    // Interfaz para la creación de pedidos
    public interface CrearPedidoCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Interfaz para actualizar pedidos
    public interface ActualizarPedidoCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Interfaz para eliminar pedidos
    public interface EliminarPedidoCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Interfaz para obtener nombres de tiendas
    public interface ObtenerTiendasCallback {
        void onSuccess(Map<Integer, String> tiendasMap);
        void onFailure(String error);
    }

    //  **Obtener Listado de Pedidos (GET)**
    public void obtenerListado(final PedidoManagerCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("PedidoManager", "Error en la conexión: " + e.getMessage());
                callback.onFailure("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray pedidosResultado = new JSONArray(responseBody);
                        callback.onSuccess(pedidosResultado);
                    } catch (JSONException e) {
                        Log.e("PedidoManager", "Error al parsear JSON: " + e.getMessage());
                        callback.onFailure("Error al parsear JSON");
                    }
                } else {
                    callback.onFailure("Error en la respuesta de la API");
                }
            }
        });
    }

    // **Obtener Nombres de Tiendas (GET)**
    public void obtenerNombreTiendas(final ObtenerTiendasCallback callback) {
        Request request = new Request.Builder()
                .url(TIENDAS_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("PedidoManager", "Error al obtener tiendas: " + e.getMessage());
                callback.onFailure("Error de conexión: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray tiendasResultado = new JSONArray(responseBody);
                        Map<Integer, String> tiendasMap = new HashMap<>();
                        for (int i = 0; i < tiendasResultado.length(); i++) {
                            JSONObject tiendaJson = tiendasResultado.getJSONObject(i);
                            int idTienda = tiendaJson.getInt("idTienda");
                            String nombreTienda = tiendaJson.getString("nombreTienda");
                            tiendasMap.put(idTienda, nombreTienda);
                        }
                        callback.onSuccess(tiendasMap);
                    } catch (JSONException e) {
                        Log.e("PedidoManager", "Error al parsear JSON de tiendas: " + e.getMessage());
                        callback.onFailure("Error al parsear JSON de tiendas");
                    }
                } else {
                    callback.onFailure("Error en la respuesta de la API de tiendas");
                }
            }
        });
    }

    // 📌 **Crear Pedido (POST)**
    public void darAlta(String fechaEstimada, String descripcion, double importe, int idTiendaFK, CrearPedidoCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("fechaEstimadaPedido", fechaEstimada)
                .add("descripcionPedido", descripcion)
                .add("importePedido", String.valueOf(importe))
                .add("idTiendaFK", String.valueOf(idTiendaFK))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Error en la petición: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Error en la respuesta: " + response.message());
                }
            }
        });
    }

    // 📌 **Actualizar Pedido (PUT)**
    public void actualizarPedido(int idPedido, String fecha, String descripcion, double importe, int estado, int idTiendaFK, ActualizarPedidoCallback callback) {
        try {
            String url = BASE_URL + "?"
                    + "idPedido=" + idPedido
                    + "&fechaPedido=" + URLEncoder.encode(fecha, "UTF-8")
                    + "&fechaEstimadaPedido=" + URLEncoder.encode(fecha, "UTF-8")
                    + "&descripcionPedido=" + URLEncoder.encode(descripcion, "UTF-8")
                    + "&importePedido=" + importe
                    + "&estadoPedido=" + estado
                    + "&idTiendaFK=" + idTiendaFK;

            RequestBody body = RequestBody.create(null, new byte[0]);
            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Error al actualizar pedido");
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Error en la conexión");
                }
            });
        } catch (UnsupportedEncodingException e) {
            callback.onFailure("Error en la codificación de datos");
        }
    }

    // 📌 **Eliminar Pedido (DELETE)**
    public void eliminarPedido(int idPedido, EliminarPedidoCallback callback) {
        String url = BASE_URL + "?idPedido=" + idPedido;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("Error en la conexión");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Error al eliminar pedido");
                }
            }
        });
    }
    public void obtenerPedidos(final ObtenerPedidosCallback callback) {
        obtenerListado(new PedidoManagerCallback() {
            @Override
            public void onSuccess(JSONArray resultado) {
                List<Pedido> pedidos = new ArrayList<>();
                try {
                    for (int i = 0; i < resultado.length(); i++) {
                        JSONObject jsonObject = resultado.getJSONObject(i);
                        int idPedido = jsonObject.getInt("idPedido");
                        String fechaPedido = jsonObject.getString("fechaPedido");
                        String fechaEstimadaPedido = jsonObject.getString("fechaEstimadaPedido");
                        String descripcionPedido = jsonObject.getString("descripcionPedido");
                        double importePedido = jsonObject.getDouble("importePedido");
                        int estadoPedido = jsonObject.getInt("estadoPedido");
                        int idTiendaFK = jsonObject.getInt("idTiendaFK");

                        Pedido pedido = new Pedido(idPedido, fechaPedido, fechaEstimadaPedido, descripcionPedido, importePedido, estadoPedido, idTiendaFK, null);
                        pedidos.add(pedido);
                    }
                    callback.onSuccess(pedidos);
                } catch (JSONException e) {
                    callback.onFailure("Error al parsear los pedidos: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    // Interfaz para manejar la lista de pedidos
    public interface ObtenerPedidosCallback {
        void onSuccess(List<Pedido> pedidos);
        void onFailure(String error);
    }

}
