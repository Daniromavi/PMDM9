package com.example.pmdm9;

import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TiendaManager {
    private static final String BASE_URL = "http://192.168.1.139/gestion_pedidos/tiendas.php";
    private OkHttpClient client;

    public TiendaManager() {
        client = new OkHttpClient();
    }

    public interface TiendaManagerCallback {
        void onSuccess(JSONArray resultado);
        void onFailure(String error);
    }

    public void obtenerTiendas(final TiendaManagerCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();
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
    // Metodo para crear una tienda (POST)
    public void crearTienda(String nombreTienda, TiendaManagerCallback callback) {
        RequestBody formBody = new FormBody.Builder()
                .add("nombreTienda", nombreTienda)
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
                    callback.onSuccess(new JSONArray()); // No devolvemos datos
                } else {
                    callback.onFailure("Error al crear tienda");
                }
            }
        });
    }

    // Metodo para actualizar una tienda (PUT)
    public void actualizarTienda(int idTienda, String nuevoNombre, TiendaManagerCallback callback) {
        try {
            String url = BASE_URL + "?idTienda=" + idTienda + "&nombreTienda=" + URLEncoder.encode(nuevoNombre, "UTF-8");
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
                        callback.onFailure("Error al actualizar tienda");
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    // Metodo para eliminar una tienda (DELETE)
    public void eliminarTienda(int idTienda, TiendaManagerCallback callback) {
        String url = BASE_URL + "?idTienda=" + idTienda;
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
                    callback.onFailure("Error al eliminar tienda");
                }
            }
        });
    }
}

