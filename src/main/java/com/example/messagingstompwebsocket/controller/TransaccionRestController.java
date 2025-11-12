package com.example.messagingstompwebsocket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.messagingstompwebsocket.model.MensajeRespuesta;
import com.example.messagingstompwebsocket.model.Transaccion;
import com.example.messagingstompwebsocket.model.TransaccionMensajeRespuesta;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Controlador REST que expone un endpoint para enviar transferencias mediante
 * HTTP POST. Además publica notificaciones en el topic WebSocket para que
 * clientes conectados reciban el evento.
 * 
 * Es una solución a no poder usar Postman
 */
@RestController
@RequestMapping("/api")
public class TransaccionRestController {

	private final SimpMessagingTemplate messagingTemplate;

	public TransaccionRestController(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	/**
	 * Recibe una transferencia en formato JSON por REST y publica una notificación
	 * en el topic WebSocket. Valida el payload y devuelve mensajes de error.
	 * 
	 * @param node el payload JSON recibido
	 */
	@PostMapping(path = "/transfer", consumes = "application/json", produces = "application/json")
	public ResponseEntity<?> transferViaRest(@RequestBody JsonNode node) {

		if (node == null || node.isNull() || node.toString().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(new MensajeRespuesta("Payload vacío o nulo"));
		}

		JsonNode cantidadNode = node.get("cantidad");
		JsonNode emisorNode = node.get("numero_cuenta_emisor");
		JsonNode receptorNode = node.get("numero_cuenta_receptor");
		if (cantidadNode == null || emisorNode == null || receptorNode == null) {
			return ResponseEntity.badRequest().body(new MensajeRespuesta(
					"Payload debe contener cantidad, numero_cuenta_emisor y numero_cuenta_receptor"));
		}

		// Validaciones
		String cantidadText = cantidadNode.asText();
		String emisorText = emisorNode.asText();
		String receptorText = receptorNode.asText();
		if (!cantidadText.matches("^\\d+(?:\\.\\d+)?$")) {
			return ResponseEntity.badRequest().body(new MensajeRespuesta("Campo cantidad con formato inválido"));
		}
		// Alineado con TransaccionController: permitir identificadores alfanuméricos
		if (!emisorText.matches("^[a-zA-Z0-9]+$") || !receptorText.matches("^[a-zA-Z0-9]+$")) {
			return ResponseEntity.badRequest().body(new MensajeRespuesta(
					"Campo numero_cuenta_emisor o numero_cuenta_receptor con caracteres inválidos (solo letras y números)"));
		}

		double cantidadVal = cantidadNode.asDouble();
		if (cantidadVal < 0.0) {
			return ResponseEntity.badRequest().body(new MensajeRespuesta("La cantidad no puede ser negativa"));
		}

		// AQUI IRÍA LA LÓGICA DE NEGOCIO PARA PROCESAR LA TRANSACCIÓN
		// SE EXTRAERIAN LOS IDS DE CUENTA

		// Crear Transaccion
		Integer id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
		Integer idEmisor = 1;
		Integer idReceptor = 2;
		Float cantidad = (float) cantidadVal;
		LocalDateTime now = LocalDateTime.now();
		short estado = 1;

		Transaccion t = new Transaccion(id, idEmisor, idReceptor, cantidad, now, now, estado);

		System.out.println("[REST] Recibida transaccion: id=" + t.getId() + ", emisor=" + t.getCuenta_emisor_id()
				+ ", receptor=" + t.getCuenta_receptor_id() + ", cantidad=" + t.getCantidad());

		// Publica al topic WebSocket para que los clientes conectados reciban la
		// notificación
		TransaccionMensajeRespuesta notification = new TransaccionMensajeRespuesta(t.getId(), emisorText, receptorText,
				t.getCantidad(), t.getFecha_creacion(), t.getEstado_id());
		messagingTemplate.convertAndSend("/topic/transfers", notification);

		// Devuelve respuesta JSON para clientes REST
		return ResponseEntity.status(HttpStatus.OK).body(notification);
	}
}