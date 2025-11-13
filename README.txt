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

Mandar estos mensajes STOMP en este orden (CODIFICANDO EN HEXADECIMAL)

CONNECT
accept-version:1.2,1.1,1.0
heart-beat:10000,10000

NULL


SUBSCRIBE
id:sub-0
destination:/queue/transfers-d88bc080-e3f4-f179-68a6-a713cba3892e  --> El id se recoge de la traza que sale por consola en el Connect anterior

NULL


SEND
destination:/app/transfer
content-type:application/json
content-length:130                 --> MUY IMPORTANTE: el content-length debe coincidir con el tamaño del JSON que se envía!!

{"cantidad":341,"numero_cuenta_emisor":"1rd","numero_cuenta_receptor":"325rdfa","clientId":"d88bc080-e3f4-f179-68a6-a713cba3892e"}NULL   --> El id igual