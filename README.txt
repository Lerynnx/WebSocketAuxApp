------------------------------------------------------------------------------------------------------------------------------------------
Opción 1 petición REST simple síncrona
------------------------------------------------------------------------------------------------------------------------------------------

Enviar la transferencia por HTTP (Postman)
- Método: POST
- URL: http://localhost:8080/api/transfer
- Headers:
  - Content-Type: application/json
- Body (raw, tipo JSON), ejemplo:
{
  "cantidad": 50.00,
  "numero_cuenta_emisor": 11111,
  "numero_cuenta_receptor": 22222
}
- Respuesta esperada (200 OK):
{
  "content": "transferencia recibida"
}
- Si hay problemas de validación, 400 con un JSON similar, p. ej. { "content": "campo cantidad con formato inválido" }.

Esto permite “mandar un JSON y recibir un mensaje” con Postman de forma síncrona (la respuesta del servidor).


------------------------------------------------------------------------------------------------------------------------------------------
Opción 2 con notificaciones push (WebSocket + STOMP) asíncrona
------------------------------------------------------------------------------------------------------------------------------------------
Recibir NOTIFICACIONES en tiempo real desde Postman (WebSocket + STOMP)
Postman puede abrir una conexión WebSocket; para recibir los mensajes STOMP. Pasos:

A) Abre Postman → New → WebSocket Request.
B) Conéctate a:
ws://localhost:8080/gs-guide-websocket

C) Envía un frame STOMP CONNECT (texto). Un frame STOMP tiene la forma:
CONNECT
accept-version:1.2
host:localhost

\0

D) Envía un SUBSCRIBE al topic:
SUBSCRIBE
id:sub-0
destination:/topic/transfers

\0

SEND
destination:/app/transfer
content-type:application/json

{"cantidad":100.0,"numero_cuenta_emisor":1,"numero_cuenta_receptor":2}\0

DISCONNECT

\0


Si todo va bien, en WebSocket de Postman saldrá un STOMP tipo MESSAGE con el payload JSON enviado desde el servidor.

-------------------------------------------------------------------------------------------------------------------------------------------
IMPORTANTE
- Los frames STOMP deben terminar con el caracter nulo (\0). En algunos clientes WebSocket esto hay que enviarlo explícitamente;
en Postman, para representar la terminación nula intenta enviar el carácter unicode \u0000 al final del mensaje o copia/pega un
terminador nulo si tu versión de Postman lo acepta.

Actualmente, Postman no soporta enviar el carácter nulo directamente. Por lo que NO SE PUEDE USAR STOMP con POSTMAN.