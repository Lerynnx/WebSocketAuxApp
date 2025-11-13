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
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	/**
	 * Configura el broker de mensajes para manejar la mensajería entrante y
	 * saliente.
	 * 
	 * @param config el registro del broker de mensajes
	 */
	@Override
	public void configureMessageBroker(org.springframework.messaging.simp.config.MessageBrokerRegistry config) {
		// Broker simple en memoria al que los clientes pueden suscribirse.
		// Añadimos "/queue" para que las colas de usuario (user queues) funcionen
		// correctamente cuando publicamos a "/user/{sessionId}/queue/...".
		config.enableSimpleBroker("/topic", "/queue");
		// Prefijo para destinos de mensajes enviados desde clientes a métodos
		// @MessageMapping
		config.setApplicationDestinationPrefixes("/app");
		// Prefijo usado por Spring para destinos de usuario (por defecto "/user").
		// Lo declaramos explícitamente para mayor claridad.
		config.setUserDestinationPrefix("/user");
	}

	/**
	 * Registra los endpoints STOMP que los clientes utilizarán para conectarse al
	 * servidor WebSocket.
	 * 
	 * @param registry el registro de endpoints STOMP
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/gs-guide-websocket");
	}

	/**
	 * Configura el canal de mensajes entrantes para inspeccionar y manejar comandos
	 * STOMP.
	 * 
	 * @param registration el registro del canal de mensajes
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@SuppressWarnings("unchecked") // para evitar advertencias al castear genéricos (linea 91)
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

				if (accessor != null) {
					StompCommand command = accessor.getCommand();

					if (command != null) {
						if (StompCommand.CONNECT.equals(command)) {
							// Extrae cabeceras nativas (si existen) y trata de decodificar valores hex
							Object nativeObj = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);

							Map<String, List<String>> nativeHeaders = null;
							if (nativeObj instanceof Map) {
								nativeHeaders = (Map<String, List<String>>) nativeObj;
							}

							String decodedHeadersInfo = "";
							if (nativeHeaders != null) {
								StringBuilder sb = new StringBuilder();
								for (Map.Entry<String, List<String>> e : nativeHeaders.entrySet()) {
									for (String val : e.getValue()) {
										// tryDecodeHex devuelve la versión decodificada si es hex, o el original
										String maybeDecoded = tryDecodeHex(val);
										sb.append(e.getKey()).append("=").append(maybeDecoded).append("; ");
									}
								}
								decodedHeadersInfo = sb.toString();
							}

							logger.info("STOMP CONNECT recibido: sessionId={}, nativeHeadersDecoded={}",
								accessor.getSessionId(), decodedHeadersInfo.isEmpty() ? nativeHeaders : decodedHeadersInfo);
						} else if (StompCommand.SUBSCRIBE.equals(command)) {
							logger.info("STOMP SUBSCRIBE recibido: sessionId={}, subscriptionId={}, destination={}",
								accessor.getSessionId(), accessor.getSubscriptionId(), accessor.getDestination());
						} else if (StompCommand.DISCONNECT.equals(command)) {
							logger.info("STOMP DISCONNECT recibido: sessionId={}", accessor.getSessionId());
						}
					}
				}

				// Inspección adicional del payload: si viene en hex, lo decodificamos para logging
				Object payload = message.getPayload();
				if (payload != null) {
					try {
						String payloadStr;
						if (payload instanceof byte[]) {
							payloadStr = new String((byte[]) payload, StandardCharsets.UTF_8);
						} else {
							payloadStr = payload.toString();
						}

						String decoded = tryDecodeHex(payloadStr);
						if (!decoded.equals(payloadStr)) {
							logger.info("STOMP payload hex decodificado: {}", decoded);
						} else {
							logger.debug("STOMP payload (no-hex): {}", payloadStr);
						}
					} catch (Exception ex) {
						logger.debug("Error inspeccionando payload STOMP: {}", ex.getMessage());
					}
				}

				return message;
			}
		});
	}

	// Decora el handler WebSocket para decodificar frames hex (texto) antes de que STOMP los parsee
	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
			@Override
			public WebSocketHandler decorate(final WebSocketHandler handler) {
				return new WebSocketHandlerDecorator(handler) {
					@Override
					public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws java.lang.Exception {
						try {
							if (message instanceof TextMessage) {
								String payload = ((TextMessage) message).getPayload();
								String processed = preprocessPossibleHexFrame(payload);
								if (!processed.equals(payload)) {
									// reenviamos el mensaje con el payload ya decodificado
									TextMessage newMsg = new TextMessage(processed);
									handler.handleMessage(session, newMsg);
									return;
								}
							}
						} catch (Exception ex) {
							logger.debug("Error decodificando frame hex: {}", ex.getMessage());
						}
						handler.handleMessage(session, message);
					}
				};
			}
		});
	}

	/**
	 * Decodifica un payload de texto que puede representar un frame STOMP en hex.
	 * - Decodifica percent-encoding si existe
	 * - Elimina espacios/CRLF que separen el hex
	 * - Si el resultado es hex válido lo convierte a UTF-8
	 * Devuelve el payload original si no es hex.
	 */
	private String preprocessPossibleHexFrame(String payload) {
		if (payload == null) return null;
		String p = payload.trim();
		// Si contiene % puede estar percent-encoded — intentamos decodificar
		if (p.contains("%")) {
			try {
				p = URLDecoder.decode(p, StandardCharsets.UTF_8.name());
			} catch (Exception e) {
				// fallamos silenciosamente y usamos el original
			}
		}
		// eliminar espacios y control chars (CR/LF) que puedan separa hex
		p = p.replaceAll("\\s+", "");
		// algunos clientes añaden prefijos/sufijos; quitar comillas si existen
		if ((p.startsWith("\"") && p.endsWith("\"")) || (p.startsWith("'") && p.endsWith("'"))) {
			p = p.substring(1, p.length() - 1);
		}
		// si ahora es hex decodificable
		if (isHexString(p)) {
			try {
				byte[] bytes = hexStringToByteArray(p);
				return new String(bytes, StandardCharsets.UTF_8);
			} catch (Exception e) {
				return payload; // fallback
			}
		}
		return payload;
	}

	// Intenta decodificar una cadena hex a UTF-8; si no es hex devuelve el original
	private String tryDecodeHex(String s) {
		if (s == null) return null;
		String trimmed = s.trim();
		if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
			trimmed = trimmed.substring(1, trimmed.length() - 1);
		}
		if (isHexString(trimmed)) {
			try {
				byte[] bytes = hexStringToByteArray(trimmed);
				return new String(bytes, StandardCharsets.UTF_8);
			} catch (Exception ex) {
				return bytesToHexViewSafe(trimmed);
			}
		}
		return s;
	}

	// True si la cadena contiene solo caracteres hex y tiene longitud par
	private boolean isHexString(String s) {
		if (s == null) return false;
		String st = s.trim();
		if (st.length() == 0 || (st.length() % 2) != 0) return false;
		for (int i = 0; i < st.length(); i++) {
			char c = st.charAt(i);
			boolean isHex = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
			if (!isHex) return false;
		}
		return true;
	}

	// Convierte pares hex a bytes
	private byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	// Representación segura si no puede decodificar a texto
	private String bytesToHexViewSafe(String hex) {
		try {
			byte[] b = hexStringToByteArray(hex);
			return "[hex decoded: " + b.length + " bytes]";
		} catch (Exception e) {
			return hex;
		}
	}
}