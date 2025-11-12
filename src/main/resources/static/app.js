// Generar un clientId único por instancia de cliente para recibir solo los
// mensajes dirigidos a este cliente (alternativa al uso de sessionId/principal).
const clientId = 'c_' + Date.now().toString(36) + '_' + Math.random().toString(36).slice(2, 9);

// Crear cliente STOMP de forma defensiva según cómo exporte la librería UMD
// (algunas versiones exponen `StompJs`, otras `Stomp`). Si no está presente
// informamos por consola para que el desarrollador lo vea.
let StompCtor = null;
if (typeof window !== 'undefined') {
    if (window.StompJs && window.StompJs.Client) {
        StompCtor = window.StompJs.Client;
    } else if (window.Stomp && window.Stomp.Client) {
        StompCtor = window.Stomp.Client;
    }
}

if (!StompCtor) {
    console.error('STOMP client library not found. Asegúrate de incluir @stomp/stompjs UMD bundle antes de app.js');
}

const stompClient = StompCtor ? new StompCtor({
    // URL del endpoint STOMP expuesto por la aplicación Spring Boot
    brokerURL: 'ws://localhost:8080/gs-guide-websocket'
}) : null;

// Se ejecuta cuando la conexión STOMP se establece correctamente
if (stompClient) {
    stompClient.onConnect = (frame) => {
        setConnected(true);
        //! DEPURACION
        //console.log('Conectado: ' + frame);
        // Suscribirse a una cola dedicada a este cliente: /queue/transfers-{clientId}.
        // El servidor publicará la respuesta a ese destino cuando reciba el
        // campo clientId en el payload.
        const destinoPersonal = '/queue/transfers-' + clientId;
        // Mostrar en consola para facilitar depuración en el navegador
        console.log('STOMP conectado. clientId=', clientId, 'suscribiendo a', destinoPersonal);
        // Actualizar la UI con el clientId y estado
        try {
            $("#clientId").text(clientId);
            $("#wsStatus").text('(conectado)');
        } catch (e) {
            console.warn('No se pudo actualizar UI clientId/wsStatus', e);
        }
        stompClient.subscribe(destinoPersonal, (respuesta) => {
            //! Trama para DEPURACION
            //console.log('Trama STOMP recibida:', respuesta);

            // Mostrar JSON 'crudo' en la sección rawJson
            try {
                const parsed = JSON.parse(respuesta.body);
                const pretty = JSON.stringify(parsed, null, 2);
                appendRawMessage(pretty);
                // Mantener la visualización en la tabla
                // Si llega un objeto con muchos campos, mostrar todos
                showRespuesta(parsed);
            } catch (e) {
                // Si el body no es JSON, mostrar el body tal cual
                appendRawMessage(respuesta.body);
                showRespuesta(respuesta.body);
            }
        });
    };

    // Manejo de errores provistos por el broker (frame ERROR)
    stompClient.onStompError = (frame) => {
        console.error('STOMP broker error:', frame.headers, frame.body);
        appendRawMessage('STOMP ERROR: ' + JSON.stringify(frame.headers || {}) + '\n' + frame.body);
    };

    // Errores de WebSocket nativo
    stompClient.onWebSocketError = (evt) => {
        console.error('WebSocket error', evt);
    };

    // Loguear desconexiones
    stompClient.onDisconnect = (frame) => {
        console.log('STOMP disconnected', frame);
        try {
            $("#wsStatus").text('(desconectado)');
        } catch (e) {}
    };
} else {
    // Si no hay cliente STOMP, deshabilitar botones para evitar intentos
    $(function () {
        $("#connect").prop('disabled', true);
        $("#send").prop('disabled', true);
    });
}

// Helper para escapar HTML y evitar inyección simple
function escapeHtml(unsafe) {
    if (unsafe === null || unsafe === undefined) return '';
    return String(unsafe)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Actualiza la UI (botones y área de conversación) según el estado de conexión
function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#respuesta").html("");
    $("#rawJson").html("");
}

// Inicia la conexión STOMP
function connect() {
    stompClient.activate();
}

// Cierra la conexión STOMP
function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Desconectado");
    try { $("#wsStatus").text('(desconectado)'); } catch(e){}
}

// Envía una transferencia al controlador STOMP (/app/transfer)
function sendTransfer() {
    const cantidad = parseFloat($("#cantidad").val()) || 0.0;
    const NumCuentaEmisor = $("#id_cuenta_emisor").val();
    const NumCuentaReceptor = $("#id_cuenta_receptor").val();

    const payload = {
        cantidad: cantidad,
        numero_cuenta_emisor: NumCuentaEmisor,
        numero_cuenta_receptor: NumCuentaReceptor
        , clientId: clientId
    };

    console.log('Enviando payload STOMP:', payload);
    if (!stompClient) {
        console.error('No STOMP client available, abortando envío');
        return;
    }

    try {
        stompClient.publish({
            destination: "/app/transfer",
            headers: { 'content-type': 'application/json' },
            body: JSON.stringify(payload)
        });
    } catch (e) {
        console.error('Error publicando STOMP:', e);
    }
}

// Muestra la respuesta en la tabla
function showRespuesta(message) {
    // message puede ser: string, objeto MensajeRespuesta, o TransaccionMensajeRespuesta
    if (typeof message === 'string') {
        // Mensaje simple: mostrarlo en una fila que ocupe todas las columnas
        $("#respuesta").append("<tr><td colspan='7'>" + escapeHtml(message) + "</td></tr>");
        return;
    }

    // Si viene un wrapper con campo 'content' (MensajeRespuesta), usar ese texto
    const content = message.content || message.contenido || message.mensaje || '';

    // Intentar extraer campos de transacción (nombres comunes que usamos en el backend)
    const id = message.id || message.ID || '';
    const emisor = message.numero_cuenta_emisor || message.numeroCuentaEmisor || message.emisor || '';
    const receptor = message.numero_cuenta_receptor || message.numeroCuentaReceptor || message.receptor || '';
    const cantidad = (message.cantidad !== undefined && message.cantidad !== null) ? message.cantidad : '';
    let fecha = '';
    if (message.fecha_creacion) {
        if (typeof message.fecha_creacion === 'string') {
            const d = new Date(message.fecha_creacion);
            fecha = isNaN(d.getTime()) ? escapeHtml(message.fecha_creacion) : d.toLocaleString();
        } else {
            // Si es objeto (por ejemplo serializado por Jackson sin el módulo JavaTime), mostrémoslo como JSON
            fecha = escapeHtml(JSON.stringify(message.fecha_creacion));
        }
    }
    const estado = (message.estado_id !== undefined && message.estado_id !== null) ? message.estado_id : '';

    // Si no hay ninguno de los campos anteriores, mostrar simplemente el contenido
    if (!id && !emisor && !receptor && !cantidad && !fecha && !estado) {
        $("#respuesta").append("<tr><td colspan='7'>" + escapeHtml(content) + "</td></tr>");
        return;
    }

    // Añadir una fila con todas las columnas
    const row = "<tr>" +
        "<td>" + escapeHtml(id) + "</td>" +
        "<td>" + escapeHtml(emisor) + "</td>" +
        "<td>" + escapeHtml(receptor) + "</td>" +
        "<td>" + escapeHtml(cantidad) + "</td>" +
        "<td>" + escapeHtml(fecha) + "</td>" +
        "<td>" + escapeHtml(estado) + "</td>" +
        "<td>" + escapeHtml(content) + "</td>" +
        "</tr>";

    $("#respuesta").append(row);
}

// Añade un mensaje crudo (JSON u otro) a la zona de texto con marca temporal
function appendRawMessage(text) {
    const timestamp = new Date().toLocaleTimeString();
    const existing = $("#rawJson").text();
    const newText = existing + "\n[" + timestamp + "]\n" + text + "\n---------------------------\n";
    $("#rawJson").text(newText);
	//! DEPURACION
//    console.log('Mensaje STOMP crudo añadido');
}

// Inicialización de handlers para los botones de la UI
$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendTransfer());
});