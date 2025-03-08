package com.example.pmdm9;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CrearPedido {

    private final OkHttpClient client = new OkHttpClient();

    public CrearPedido() {}

    // Interfaz para manejar el resultado de la petición
    public interface CrearPedidoCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Metodo para crear un nuevo pedido
    public void darAlta(String fechaEstimada, String descripcion, double importe, int idTiendaFK, CrearPedidoCallback callback) {

        // Montamos la petición POST con los parámetros necesarios
        RequestBody formBody = new FormBody.Builder()
                .add("fechaEstimadaPedido", fechaEstimada)
                .add("descripcionPedido", descripcion)
                .add("importePedido", String.valueOf(importe))
                .add("idTiendaFK", String.valueOf(idTiendaFK))
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.1.139/gestion_pedidos/pedidos.php") // Asegúrate de que la URL sea correcta
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("CrearPedido", "Error en la petición: " + e.getMessage());
                if (callback != null) {
                    callback.onFailure("Error en la petición: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("CrearPedido", "Pedido creado exitosamente");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    Log.e("CrearPedido", "Error en la respuesta: " + response.message());
                    if (callback != null) {
                        callback.onFailure("Error en la respuesta: " + response.message());
                    }
                }
            }
        });
    }
}
