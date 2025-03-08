package com.example.pmdm9;

public class Pedido {

    private int idPedido;
    private String fechaPedido;
    private String fechaEstimadaPedido;
    private String descripcionPedido;
    private double importePedido;
    private int estadoPedido;
    private int idTiendaFK;
    private String nombreTienda;

    // Constructor
    public Pedido(int idPedido, String fechaPedido, String fechaEstimadaPedido,
                  String descripcionPedido, double importePedido, int estadoPedido, int idTiendaFK, String nombreTienda) {
        this.idPedido = idPedido;
        this.fechaPedido = fechaPedido;
        this.fechaEstimadaPedido = fechaEstimadaPedido;
        this.descripcionPedido = descripcionPedido;
        this.importePedido = importePedido;
        this.estadoPedido = estadoPedido;
        this.idTiendaFK = idTiendaFK;
        this.nombreTienda = nombreTienda;
    }

    // Getters y setters
    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(String fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getFechaEstimadaPedido() {
        return fechaEstimadaPedido;
    }

    public void setFechaEstimadaPedido(String fechaEstimadaPedido) {
        this.fechaEstimadaPedido = fechaEstimadaPedido;
    }

    public String getDescripcionPedido() {
        return descripcionPedido;
    }

    public void setDescripcionPedido(String descripcionPedido) {
        this.descripcionPedido = descripcionPedido;
    }

    public double getImportePedido() {
        return importePedido;
    }

    public void setImportePedido(double importePedido) {
        this.importePedido = importePedido;
    }

    public int getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(int estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public int getIdTiendaFK() {
        return idTiendaFK;
    }

    public void setIdTiendaFK(int idTiendaFK) {
        this.idTiendaFK = idTiendaFK;
    }
    public String getNombreTienda(){
        return nombreTienda;
    }
    public void setNombreTienda(String nombreTienda){
        this.nombreTienda = nombreTienda;
    }
}
