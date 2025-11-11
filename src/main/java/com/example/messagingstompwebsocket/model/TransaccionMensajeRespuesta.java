package com.example.messagingstompwebsocket.model;

import java.time.LocalDateTime;

public class TransaccionMensajeRespuesta extends MensajeRespuesta {
	private Integer id;
	private String numero_cuenta_emisor;
	private String numero_cuenta_receptor;
	private Float cantidad;
	private LocalDateTime fecha_creacion;
	private short estado_id;
	
	public TransaccionMensajeRespuesta() {
		super("");
	}

	public TransaccionMensajeRespuesta(Integer id, String numero_cuenta_emisor, String numero_cuenta_receptor, Float cantidad,
			LocalDateTime fecha_creacion, short estado_id) {
		super("Transacción creada con id=" + id);
		this.id = id;
		this.numero_cuenta_emisor = numero_cuenta_emisor;
		this.numero_cuenta_receptor = numero_cuenta_receptor;
		this.cantidad = cantidad;
		this.fecha_creacion = fecha_creacion;
		this.estado_id = estado_id;
	}
	
	public String getNumero_cuenta_emisor() {
		return numero_cuenta_emisor;
	}

	public void setNumero_cuenta_emisor(String numero_cuenta_emisor) {
		this.numero_cuenta_emisor = numero_cuenta_emisor;
	}

	public String getNumero_cuenta_receptor() {
		return numero_cuenta_receptor;
	}

	public void setNumero_cuenta_receptor(String numero_cuenta_receptor) {
		this.numero_cuenta_receptor = numero_cuenta_receptor;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Float getCantidad() {
		return cantidad;
	}
	public void setCantidad(Float cantidad) {
		this.cantidad = cantidad;
	}
	public LocalDateTime getFecha_creacion() {
		return fecha_creacion;
	}
	public void setFecha_creacion(LocalDateTime fecha_creacion) {
		this.fecha_creacion = fecha_creacion;
	}
	public short getEstado_id() {
		return estado_id;
	}
	public void setEstado_id(short estado_id) {
		this.estado_id = estado_id;
	}
}