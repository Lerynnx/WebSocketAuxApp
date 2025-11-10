package com.example.messagingstompwebsocket.model;

public class TransferMessage {

    private Double cantidad;
    private Integer id_cuenta_emisor;
    private Integer id_cuenta_receptor;

    public TransferMessage() {
    }

    public TransferMessage(Double cantidad, Integer id_cuenta_emisor, Integer id_cuenta_receptor) {
        this.cantidad = cantidad;
        this.id_cuenta_emisor = id_cuenta_emisor;
        this.id_cuenta_receptor = id_cuenta_receptor;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getId_cuenta_emisor() {
        return id_cuenta_emisor;
    }

    public void setId_cuenta_emisor(Integer id_cuenta_emisor) {
        this.id_cuenta_emisor = id_cuenta_emisor;
    }

    public Integer getId_cuenta_receptor() {
        return id_cuenta_receptor;
    }

    public void setId_cuenta_receptor(Integer id_cuenta_receptor) {
        this.id_cuenta_receptor = id_cuenta_receptor;
    }
}
