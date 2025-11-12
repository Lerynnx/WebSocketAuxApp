package com.example.messagingstompwebsocket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Controller;

import com.example.messagingstompwebsocket.model.MensajeRespuesta;
import com.example.messagingstompwebsocket.model.TransaccionMensajeRespuesta;
import com.example.messagingstompwebsocket.model.Transaccion;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class TransaccionController {

    private static final Logger logger = LoggerFactory.getLogger(TransaccionController.class);

    private final SimpMessagingTemplate messagingTemplate;

    public TransaccionController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Maneja mensajes entrantes en el endpoint /app/transfer. Valida el payload
     * JSON y devuelve respuestas.
     * 
     * @param node el payload JSON recibido
     */
    @MessageMapping("/transfer")
    // Recibimos la cabecera simpSessionId para identificar la sesión STOMP
    public void transaccion(JsonNode node, @Header("simpSessionId") String sessionId) throws Exception {
        logger.info("Payload entrante (raw): {}", node == null ? "<null>" : node.toString());
        if (node == null || node.isNull() || node.toString().trim().isEmpty()) {
            // Enviar mensaje de error al propio usuario
            MensajeRespuesta respErr = new MensajeRespuesta("Payload vacío o nulo");
            if (sessionId != null) {
                messagingTemplate.convertAndSend("/user/" + sessionId + "/queue/transfers", respErr);
            }
            return;
        }

        JsonNode cantidadNode = node.get("cantidad");
        JsonNode emisorNode = node.get("numero_cuenta_emisor");
        JsonNode receptorNode = node.get("numero_cuenta_receptor");
        if (cantidadNode == null || emisorNode == null || receptorNode == null) {
            MensajeRespuesta respErr = new MensajeRespuesta(
                    "Payload debe contener cantidad, numero_cuenta_emisor y numero_cuenta_receptor");
            logger.info("Validación fallida: faltan campos en payload: cantidad={}, emisor={}, receptor={}", cantidadNode, emisorNode, receptorNode);
            if (sessionId != null) {
                messagingTemplate.convertAndSend("/user/" + sessionId + "/queue/transfers", respErr);
            }
            return;
        }

        // Validaciones
        String cantidadText = cantidadNode.asText();
        String emisorText = emisorNode.asText();
        String receptorText = receptorNode.asText();
        if (!cantidadText.matches("^\\d+(?:\\.\\d+)?$")) { // números positivos con decimales opcionales
            MensajeRespuesta respErr = new MensajeRespuesta("campo cantidad con formato inválido");
            logger.info("Validación fallida: cantidad con formato inválido: {}", cantidadText);
            if (sessionId != null) {
                messagingTemplate.convertAndSend("/user/" + sessionId + "/queue/transfers", respErr);
            }
            return;
        }
        if (!emisorText.matches("^[a-zA-Z0-9]+$") || !receptorText.matches("^[a-zA-Z0-9]+$")) { // solo letras y números
            MensajeRespuesta respErr = new MensajeRespuesta(
                    "El campo numero_cuenta_emisor o numero_cuenta_receptor con caracteres inválidos");
            logger.info("Validación fallida: emisor/receptor con caracteres inválidos: emisor={}, receptor={}", emisorText, receptorText);
            if (sessionId != null) {
                messagingTemplate.convertAndSend("/user/" + sessionId + "/queue/transfers", respErr);
            }
            return;
        }

        float cantidadVal = cantidadNode.floatValue();
        if (cantidadVal < 0.0f) {
            MensajeRespuesta respErr = new MensajeRespuesta("La cantidad no puede ser negativa");
            logger.info("Validación fallida: cantidad negativa: {}", cantidadVal);
            if (sessionId != null) {
                messagingTemplate.convertAndSend("/user/" + sessionId + "/queue/transfers", respErr);
            }
            return;
        }

        // AQUI IRÍA LA LÓGICA DE NEGOCIO PARA PROCESAR LA TRANSACCIÓN
        // SE EXTRAERIAN LOS IDS DE CUENTA

        // Crear la transacción
        Integer id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
        Integer idEmisor = 1;
        Integer idReceptor = 2;
        Float cantidad = (float) cantidadVal;
        LocalDateTime now = LocalDateTime.now();
        short estado = 1;

        Transaccion t = new Transaccion(id, idEmisor, idReceptor, cantidad, now, now, estado);

        System.out.println("Recibida transaccion: id=" + t.getId() + ", emisor=" + t.getCuenta_emisor_id()
                + ", receptor=" + t.getCuenta_receptor_id() + ", cantidad=" + t.getCantidad());

        TransaccionMensajeRespuesta response = new TransaccionMensajeRespuesta(t.getId(), emisorText, receptorText,
                t.getCantidad(), t.getFecha_creacion(), t.getEstado_id());

        // Si el cliente envió un clientId en el payload, preferimos enviar a la
        // cola dedicada /queue/transfers-{clientId} (suscrita por el cliente en
        // app.js). Si no, intentamos usar la sessionId para publicar a
        // /user/{sessionId}/queue/transfers. Si nada está disponible hacemos
        // broadcast a /topic/transfers.
        String clientId = null;
        try {
            com.fasterxml.jackson.databind.JsonNode clientIdNode = node.get("clientId");
            if (clientIdNode != null && !clientIdNode.isNull()) {
                String tmp = clientIdNode.asText();
                if (tmp != null && !tmp.trim().isEmpty()) {
                    clientId = tmp.trim();
                }
            }
        } catch (Exception e) {
            // ignorar si node no tiene clientId o similar
        }

        logger.info("clientId detectado en payload: {} ; sessionId header: {}", clientId, sessionId);

        if (clientId != null) {
            String dest = "/queue/transfers-" + clientId;
            logger.info("Enviando respuesta a clientId={} destino={}", clientId, dest);
            messagingTemplate.convertAndSend(dest, response);
            return;
        }

        if (sessionId != null) {
            String dest = "/user/" + sessionId + "/queue/transfers";
            logger.info("Enviando respuesta a sessionId={} destino={}", sessionId, dest);
            messagingTemplate.convertAndSend(dest, response);
        } else {
            logger.info("No sessionId ni clientId disponibles, haciendo broadcast a /topic/transfers");
            messagingTemplate.convertAndSend("/topic/transfers", response);
        }
    }

}