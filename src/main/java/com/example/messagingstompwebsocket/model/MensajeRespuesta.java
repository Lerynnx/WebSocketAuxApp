package com.example.messagingstompwebsocket.model;

public class MensajeRespuesta {

  private String contenido;

  public MensajeRespuesta() {
  }

  public MensajeRespuesta(String content) {
    this.contenido = content;
  }

  public String getContent() {
    return contenido;
  }

  public void setContent(String content) {
    this.contenido = content;
  }

}