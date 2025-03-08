package com.example.pmdm9;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActualizarPedidoRemoto {
    OkHttpClient client = new OkHttpClient();

    public interface ActualizarPedidoCallback {
        void onResult(boolean exito);
    }

    public void actualizarPedido(int idPedido, String fecha, String descripcion,
                                 double importe, int estado, int idTiendaFK,
                                 ActualizarPedidoCallback callback) {
        try {
            // Construir la URL con los parámetros en el query string
            String url = "http://192.168.1.139/gestion_pedidos/pedidos.php?"
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
                    callback.onResult(response.isSuccessful());
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onResult(false);
                }
            });
        } catch (UnsupportedEncodingException e) {
            callback.onResult(false);
        }
    }
}
