package com.example.messagingstompwebsocket.model;

import java.time.LocalDateTime;

public class Transaccion {
	private Integer id;
	private Integer cuenta_emisor_id;
	private Integer cuenta_receptor_id;
	private Float cantidad;
	private LocalDateTime fecha_creacion;
	private LocalDateTime fecha_actualizacion;
	private short estado_id;
	
	public Transaccion(Integer id, Integer cuenta_emisor_id, Integer cuenta_receptor_id, Float cantidad,
			LocalDateTime fecha_creacion, LocalDateTime fecha_actualizacion, short estado_id) {
		super();
		this.id = id;
		this.cuenta_emisor_id = cuenta_emisor_id;
		this.cuenta_receptor_id = cuenta_receptor_id;
		this.cantidad = cantidad;
		this.fecha_creacion = fecha_creacion;
		this.fecha_actualizacion = fecha_actualizacion;
		this.estado_id = estado_id;
	}

	public Transaccion() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCuenta_emisor_id() {
		return cuenta_emisor_id;
	}

	public void setCuenta_emisor_id(Integer cuenta_emisor_id) {
		this.cuenta_emisor_id = cuenta_emisor_id;
	}

	public Integer getCuenta_receptor_id() {
		return cuenta_receptor_id;
	}

	public void setCuenta_receptor_id(Integer cuenta_receptor_id) {
		this.cuenta_receptor_id = cuenta_receptor_id;
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

	public LocalDateTime getFecha_actualizacion() {
		return fecha_actualizacion;
	}

	public void setFecha_actualizacion(LocalDateTime fecha_actualizacion) {
		this.fecha_actualizacion = fecha_actualizacion;
	}

	public short getEstado_id() {
		return estado_id;
	}

	public void setEstado_id(short estado_id) {
		this.estado_id = estado_id;
	}
}