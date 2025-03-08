package com.example.pmdm9;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AccesoRemoto {

    private OkHttpClient client;
    private Request request;

    public AccesoRemoto() {
        client = new OkHttpClient();
        request = new Request.Builder()
                .url("http://192.168.1.139/gestion_pedidos/pedidos.php")
                .build();
    }

    public void setUrl(String url) {
        request = new Request.Builder().url(url).build();
    }

    // Método asincrono para obtener el listado
    public void obtenerListado(final AccesoRemotoCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AccesoRemoto", "Error en la conexión: " + e.getMessage());
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray pedidosResultado = new JSONArray(responseBody);  // Procesar como JSONArray
                        callback.onSuccess(pedidosResultado);  // Pasar el JSONArray a onSuccess
                    } catch (JSONException e) {
                        Log.e("AccesoRemoto", "Error al parsear JSON: " + e.getMessage());
                        callback.onFailure("Error al parsear JSON");
                    }
                } else {
                    callback.onFailure("Error en la respuesta de la API");
                }
            }
        });
    }

    public void obtenerTiendas(final AccesoRemotoCallback callback) {
        Request tiendaRequest = new Request.Builder()
                .url("http://192.168.1.139/gestion_pedidos/tiendas.php")  // URL de la API que devuelve las tiendas
                .build();

        client.newCall(tiendaRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AccesoRemoto", "Error al obtener tiendas: " + e.getMessage());
                callback.onFailure("Error al obtener tiendas");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray tiendasResultado = new JSONArray(responseBody);
                        callback.onSuccess(tiendasResultado);  // Llamar a onSuccess para TiendasActivity

                        Map<Integer, String> tiendasMap = new HashMap<>();
                        for (int i = 0; i < tiendasResultado.length(); i++) {
                            JSONObject tiendaJson = tiendasResultado.getJSONObject(i);
                            int idTienda = tiendaJson.getInt("idTienda");
                            String nombreTienda = tiendaJson.getString("nombreTienda");
                            tiendasMap.put(idTienda, nombreTienda);
                        }
                        callback.onTiendasSuccess(tiendasMap);

                    } catch (JSONException e) {
                        Log.e("AccesoRemoto", "Error al parsear JSON de tiendas: " + e.getMessage());
                        callback.onFailure("Error al parsear JSON de tiendas");
                    }
                } else {
                    callback.onFailure("Error en la respuesta de la API de tiendas");
                }
            }
        });
    }



    // Interfaz del callback
    public interface AccesoRemotoCallback {
        void onSuccess(JSONArray pedidosResultado);  // Método para manejar el JSONArray de pedidos
        void onTiendasSuccess(Map<Integer, String> tiendasMap);  // Método para manejar el Map de tiendas
        void onFailure(String error);
    }

    // Dentro de AccesoRemoto
    public void crearPedido(String tienda, String fecha, String descripcion, double importe, final AccesoRemotoCallback callback) {
        // Crear un JSONObject con los parámetros del pedido
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("tienda", tienda);
            jsonBody.put("fecha", fecha);
            jsonBody.put("descripcion", descripcion);
            jsonBody.put("importe", importe);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Convertir el JSONObject en un cuerpo para la petición POST
        okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, jsonBody.toString());

        // Crear la petición POST
        Request request = new Request.Builder()
                .url("http://192.168.1.139/gestion_pedidos/crear_pedido.php") // URL de la API para crear un pedido
                .post(body) // Enviar los datos en el cuerpo de la petición
                .build();

        // Enviar la petición de manera asincrónica
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AccesoRemoto", "Error en la conexión: " + e.getMessage());
                callback.onFailure("Error al crear el pedido: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if (responseBody.equals("success")) {
                        callback.onSuccess(new JSONArray()); // Devolver vacío ya que es una operación de creación
                    } else {
                        callback.onFailure("Error al crear el pedido en la base de datos");
                    }
                } else {
                    callback.onFailure("Error en la respuesta de la API");
                }
            }
        });
    }


}
