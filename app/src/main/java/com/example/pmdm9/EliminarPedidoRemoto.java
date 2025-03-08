package com.example.pmdm9;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EliminarPedidoRemoto {
    OkHttpClient client = new OkHttpClient();

    public EliminarPedidoRemoto() {}

    // Modificación para aceptar un callback
    public void eliminarPedido(int idPedido, EliminarPedidoCallback callback) {
        Request request = new Request.Builder()
                .url("http://192.168.1.139/gestion_pedidos/pedidos.php?idPedido=" + idPedido)
                .delete()
                .build();

        Call call = client.newCall(request);
        // Ejecutar la solicitud de forma asíncrona
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Llamar al callback con el resultado
                boolean exito = response.isSuccessful();
                callback.onResult(exito);  // Deberíamos llamar al callback aquí con el éxito
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // En caso de fallo, notificar al callback
                Log.e("EliminarPedidoRemoto", "Error: " + e.getMessage());
                callback.onResult(false);
            }
        });
    }


    // Interfaz de callback
    public interface EliminarPedidoCallback {
        void onResult(boolean exito);
    }
}
