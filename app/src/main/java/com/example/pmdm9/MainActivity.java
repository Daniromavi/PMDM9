package com.example.pmdm9;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                pedido -> PedidoDialogHelper.mostrarEditarPedidoDialog(this, pedido, tiendasMap, new PedidoManager(), this::obtenerPedidosDeApi),

                // Pulsación larga: muestra el diálogo para eliminar
                pedido -> PedidoDialogHelper.mostrarDialogoEliminar(this, pedido, new PedidoManager(), pedidoAdapter)

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

        bttnNuevoPedido.setOnClickListener(v ->
                PedidoDialogHelper.mostrarNuevoPedidoDialog(
                        this,
                        tiendasMap,
                        new PedidoManager(),
                        this::obtenerPedidosDeApi
                )
        );

    }

    public void obtenerPedidosDeApi() {
        PedidoManager pedidoManager = new PedidoManager();
        pedidoManager.obtenerPedidos(new PedidoManager.ObtenerPedidosCallback() {
            @Override
            public void onSuccess(List<Pedido> pedidos) {
                runOnUiThread(() -> {
                    for (Pedido pedido : pedidos) {
                        if (tiendasMap != null) {
                            pedido.setNombreTienda(tiendasMap.get(pedido.getIdTiendaFK()));
                        }
                    }
                    pedidoAdapter.setPedidos(pedidos);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al obtener los pedidos: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void obtenerTiendasDeApi() {
        PedidoManager pedidoManager = new PedidoManager();
        pedidoManager.obtenerNombreTiendas(new PedidoManager.ObtenerTiendasCallback() {
            @Override
            public void onSuccess(Map<Integer, String> todasLasTiendas) {
                tiendasMap = todasLasTiendas;  // Guardamos todas las tiendas, no solo las con pedidos
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error al obtener tiendas", Toast.LENGTH_SHORT).show());
            }
        });
    }

}
