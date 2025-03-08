package com.example.pmdm9;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.net.URLEncoder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PedidoManager {
    private static final String BASE_URL = "http://192.168.1.139/gestion_pedidos/pedidos.php";
    private OkHttpClient client;

    public PedidoManager() {
        client = new OkHttpClient();
    }

    public interface PedidoManagerCallback {
        void onSuccess(JSONArray resultado);
        void onFailure(String error);
    }

    // Obtener pedidos (GET)
    public void obtenerPedidos(PedidoManagerCallback callback) {
        Request request = new Request.Builder().url(BASE_URL).build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    String responseBody = response.body().string();
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        callback.onSuccess(array);
                    } catch (JSONException e) {
                        callback.onFailure(e.getMessage());
                    }
                } else {
                    callback.onFailure("Error en la respuesta de la API");
                }
            }
        });
    }

    // Crear pedido (POST)
    public void crearPedido(String fechaEstimada, String descripcion, double importe, int idTiendaFK, PedidoManagerCallback callback) {
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
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    callback.onSuccess(new JSONArray());
                } else {
                    callback.onFailure("Error al crear pedido");
                }
            }
        });
    }

    // Actualizar pedido (PUT)
    public void actualizarPedido(int idPedido, String fecha, String descripcion, double importe, int estado, int idTiendaFK, PedidoManagerCallback callback) {
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
            client.newCall(request).enqueue(new Callback(){
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()){
                        callback.onSuccess(new JSONArray());
                    } else {
                        callback.onFailure("Error al actualizar pedido");
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    // Eliminar pedido (DELETE)
    public void eliminarPedido(int idPedido, PedidoManagerCallback callback) {
        String url = BASE_URL + "?idPedido=" + idPedido;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    callback.onSuccess(new JSONArray());
                } else {
                    callback.onFailure("Error al eliminar pedido");
                }
            }
        });
    }
}
