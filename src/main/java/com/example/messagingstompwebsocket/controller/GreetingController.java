package com.example.messagingstompwebsocket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.example.messagingstompwebsocket.model.Greeting;
import com.example.messagingstompwebsocket.model.HelloMessage;
import com.example.messagingstompwebsocket.model.TransferMessage;
import com.example.messagingstompwebsocket.model.Transaccion;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.JsonNode;

@Controller
public class GreetingController {

  @MessageMapping("/hello")
  @SendTo("/topic/greetings")
  public Greeting greeting(HelloMessage message) throws Exception {
    Thread.sleep(1000); // simulated delay
    return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
  }

  @MessageMapping("/transfer")
  @SendTo("/topic/transfers")
  public Greeting transfer(JsonNode node) throws Exception {
    // Validate payload is valid JSON and contains the required numeric fields without strange characters
    if (node == null || node.isNull() || node.toString().trim().isEmpty()) {
      return new Greeting("payload vacío o nulo");
    }

    // required fields
    JsonNode cantidadNode = node.get("cantidad");
    JsonNode emisorNode = node.get("id_cuenta_emisor");
    JsonNode receptorNode = node.get("id_cuenta_receptor");

    if (cantidadNode == null || emisorNode == null || receptorNode == null) {
      return new Greeting("payload debe contener cantidad, id_cuenta_emisor y id_cuenta_receptor");
    }

    // validate formats: cantidad -> decimal number, accounts -> integer digits only
    String cantidadText = cantidadNode.asText();
    String emisorText = emisorNode.asText();
    String receptorText = receptorNode.asText();

    if (!cantidadText.matches("^\\d+(?:\\.\\d+)?$")) {
      return new Greeting("campo cantidad con formato inválido");
    }
    if (!emisorText.matches("^\\d+$") || !receptorText.matches("^\\d+$")) {
      return new Greeting("campo id_cuenta_emisor o id_cuenta_receptor con caracteres inválidos");
    }

    // parse values
    double cantidadVal = cantidadNode.asDouble();
    int idEmisor = emisorNode.asInt();
    int idReceptor = receptorNode.asInt();

    // Further simple validation
    if (idEmisor <= 0 || idReceptor <= 0) {
      return new Greeting("los identificadores de cuenta deben ser mayores que 0");
    }

    if (cantidadVal < 0.0) {
      return new Greeting("la cantidad no puede ser negativa");
    }

    // build Transaccion
    Integer id = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    String claveIndempotencia = UUID.randomUUID().toString();
    String idCorrelacion = UUID.randomUUID().toString();
    Float cantidad = (float) cantidadVal;
    LocalDateTime now = LocalDateTime.now();
    String estado = "RECIBIDA";

    Transaccion t = new Transaccion(id, claveIndempotencia, idCorrelacion, idEmisor, idReceptor, cantidad, now, now, estado);

    System.out.println("Recibida transaccion: id=" + t.getId() + ", emisor=" + t.getId_cuenta_emisor() + ", receptor=" + t.getId_cuenta_receptor() + ", cantidad=" + t.getCantidad());

    return new Greeting("transferencia recibida");
  }

}