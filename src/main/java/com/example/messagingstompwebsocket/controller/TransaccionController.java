package com.example.messagingstompwebsocket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
//import org.springframework.web.util.HtmlUtils;

import com.example.messagingstompwebsocket.model.MensajeRespuesta;
import com.example.messagingstompwebsocket.model.TransaccionMensajeRespuesta;
//import com.example.messagingstompwebsocket.model.HelloMessage;
import com.example.messagingstompwebsocket.model.Transaccion;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;

@Controller
public class TransaccionController {

  /**
   * Maneja mensajes entrantes en el endpoint /app/transfer.
   * Valida el payload JSON y devuelve respuestas.
   * @param node el payload JSON recibido
   */
  @MessageMapping("/transfer")
  @SendTo("/topic/transfers")
  public MensajeRespuesta transaccion(JsonNode node) throws Exception {
    if (node == null || node.isNull() || node.toString().trim().isEmpty()) {
      return new MensajeRespuesta("Payload vacío o nulo");
    }

    JsonNode cantidadNode = node.get("cantidad");
    JsonNode emisorNode = node.get("numero_cuenta_emisor");
    JsonNode receptorNode = node.get("numero_cuenta_receptor");
    if (cantidadNode == null || emisorNode == null || receptorNode == null) {
      return new MensajeRespuesta("Payload debe contener cantidad, numero_cuenta_emisor y numero_cuenta_receptor");
    }
    

    //Validaciones
    String cantidadText = cantidadNode.asText();
    String emisorText = emisorNode.asText();
    String receptorText = receptorNode.asText();
    if (!cantidadText.matches("^\\d+(?:\\.\\d+)?$")) { // números positivos con decimales opcionales
      return new MensajeRespuesta("campo cantidad con formato inválido");
    }
    if (!emisorText.matches("^[a-zA-Z0-9]+$") || !receptorText.matches("^[a-zA-Z0-9]+$") ) { // solo letras y números
      return new MensajeRespuesta("El campo numero_cuenta_emisor o numero_cuenta_receptor con caracteres inválidos");
    }

    float cantidadVal = cantidadNode.floatValue();
    if (cantidadVal < 0.0f) {
      return new MensajeRespuesta("La cantidad no puede ser negativa");
    }

    
    //AQUI IRÍA LA LÓGICA DE NEGOCIO PARA PROCESAR LA TRANSACCIÓN
    //SE EXTRAERIAN LOS IDS DE CUENTA
    
    // Crear la transacción
    Integer id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    Integer idEmisor = 1;
    Integer idReceptor = 2;
    Float cantidad = (float) cantidadVal;
    LocalDateTime now = LocalDateTime.now();
    short estado = 1;

    Transaccion t = new Transaccion(id, idEmisor, idReceptor, cantidad, now, now, estado);

    System.out.println("Recibida transaccion: id=" + t.getId() + ", emisor=" + t.getCuenta_emisor_id() + ", receptor=" + t.getCuenta_receptor_id() + ", cantidad=" + t.getCantidad());

    return new TransaccionMensajeRespuesta(t.getId(), emisorText, receptorText, t.getCantidad(), t.getFecha_creacion(), t.getEstado_id());
  }

}