package com.example.messagingstompwebsocket.model;

import java.time.LocalDateTime;

public class Transaccion {
	private Integer id;
	private String clave_indempotencia;
	private String id_correlacion;
	private Integer id_cuenta_emisor;
	private Integer id_cuenta_receptor;
	private Float cantidad;
	private LocalDateTime fecha_creacion;
	private LocalDateTime fecha_actualizacion;
	private String estado_id;

	
	public Transaccion() {
		super();
	}

	public Transaccion(Integer id, String clave_indempotencia, String id_correlacion, Integer id_cuenta_emisor,
			Integer id_cuenta_receptor, Float cantidad, LocalDateTime fecha_creacion, LocalDateTime fecha_actualizacion,
			String estado_id) {
		super();
		this.id = id;
		this.clave_indempotencia = clave_indempotencia;
		this.id_correlacion = id_correlacion;
		this.id_cuenta_emisor = id_cuenta_emisor;
		this.id_cuenta_receptor = id_cuenta_receptor;
		this.cantidad = cantidad;
		this.fecha_creacion = fecha_creacion;
		this.fecha_actualizacion = fecha_actualizacion;
		this.estado_id = estado_id;
	}

	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getClave_indempotencia() {
		return clave_indempotencia;
	}

	public void setClave_indempotencia(String clave_indempotencia) {
		this.clave_indempotencia = clave_indempotencia;
	}

	public String getId_correlacion() {
		return id_correlacion;
	}

	public void setId_correlacion(String id_correlacion) {
		this.id_correlacion = id_correlacion;
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

	public String getEstado_id() {
		return estado_id;
	}

	public void setEstado_id(String estado_id) {
		this.estado_id = estado_id;
	}
	
}