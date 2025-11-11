package com.example.messagingstompwebsocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

  
  /**
   * Configura el broker de mensajes para manejar la mensajería entrante y saliente.
   * @param config el registro del broker de mensajes
   */
  @Override
  public void configureMessageBroker(org.springframework.messaging.simp.config.MessageBrokerRegistry config) {
    // Broker simple en memoria al que los clientes pueden suscribirse
    config.enableSimpleBroker("/topic");
    // Prefijo para destinos de mensajes enviados desde clientes a métodos @MessageMapping
    config.setApplicationDestinationPrefixes("/app");
  }

  
  /**
   * Registra los endpoints STOMP que los clientes utilizarán para conectarse al servidor WebSocket.
   * @param registry el registro de endpoints STOMP
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/gs-guide-websocket");
  }

  
  /**
   * Configura el canal de mensajes entrantes para inspeccionar y manejar comandos STOMP.
   * @param registration el registro del canal de mensajes
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
      @SuppressWarnings("unchecked")// para evitar advertencias al castear genéricos (linea 67)
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
          StompCommand command = accessor.getCommand();
          
          if (command != null) {
            if (StompCommand.CONNECT.equals(command)) {
              // Extraer cabeceras nativas para mayor información en el log
              Object nativeObj = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);
              
              Map<String, List<String>> nativeHeaders = null;
              if (nativeObj instanceof Map) {
                nativeHeaders = (Map<String, List<String>>) nativeObj;
              }
              
              logger.info("STOMP CONNECT recibido: sessionId={}, nativeHeaders={}", accessor.getSessionId(), nativeHeaders);
            } else if (StompCommand.SUBSCRIBE.equals(command)) {
              logger.info("STOMP SUBSCRIBE recibido: sessionId={}, subscriptionId={}, destination={}", accessor.getSessionId(), accessor.getSubscriptionId(), accessor.getDestination());
            } else if (StompCommand.DISCONNECT.equals(command)) {
              logger.info("STOMP DISCONNECT recibido: sessionId={}", accessor.getSessionId());
            }
          }
        }
        return message;
      }
    });
  }

}