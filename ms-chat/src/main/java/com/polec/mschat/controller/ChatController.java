package com.polec.mschat.controller;

import com.polec.mschat.client.UsuarioClient;
import com.polec.mschat.client.UsuarioDTO;
import com.polec.mschat.dto.*;
import com.polec.mschat.entity.Conversacion;
import com.polec.mschat.entity.Mensaje;
import com.polec.mschat.repository.ConversacionRepository;
import com.polec.mschat.repository.MensajeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@Tag(name = "Chat", description = "Conversaciones y mensajes entre usuarios; los mensajes no se pueden editar")
public class ChatController {

    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioClient usuarioClient;

    public ChatController(ConversacionRepository conversacionRepository,
                          MensajeRepository mensajeRepository,
                          UsuarioClient usuarioClient) {
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.usuarioClient = usuarioClient;
    }

    // Crea (o reutiliza) una conversación entre dos usuarios sobre un item opcional
    @PostMapping
    @Operation(summary = "Crear o reutilizar conversacion", description = "Crea una conversacion entre dos usuarios. Si ya existe una conversacion entre ellos (y el item, si se indica), devuelve la existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conversacion creada correctamente"),
            @ApiResponse(responseCode = "200", description = "Ya existia una conversacion equivalente; se devuelve la existente"),
            @ApiResponse(responseCode = "400", description = "Participantes iguales, usuario inexistente o datos invalidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody CrearConversacionRequest request) {
        if (request.usuario1Id().equals(request.usuario2Id())) {
            return ResponseEntity.badRequest()
                    .body("Los participantes deben ser usuarios distintos");
        }
        if (!usuarioClient.existe(request.usuario1Id())) {
            return ResponseEntity.badRequest()
                    .body("El usuario " + request.usuario1Id() + " no existe");
        }
        if (!usuarioClient.existe(request.usuario2Id())) {
            return ResponseEntity.badRequest()
                    .body("El usuario " + request.usuario2Id() + " no existe");
        }

        List<Conversacion> existentes = request.itemId() == null
                ? conversacionRepository.buscarEntreUsuarios(request.usuario1Id(), request.usuario2Id())
                : conversacionRepository.buscarEntreUsuariosPorItem(
                        request.usuario1Id(), request.usuario2Id(), request.itemId());

        if (!existentes.isEmpty()) {
            return ResponseEntity.ok(toResponse(existentes.get(0)));
        }

        Conversacion conversacion = new Conversacion();
        conversacion.setUsuario1Id(request.usuario1Id());
        conversacion.setUsuario2Id(request.usuario2Id());
        conversacion.setItemId(request.itemId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(conversacionRepository.save(conversacion)));
    }

    @GetMapping
    @Operation(summary = "Listar conversaciones", description = "Devuelve todas las conversaciones registradas. Si no hay conversaciones, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de conversaciones obtenida correctamente")
    })
    public List<ConversacionResponse> listar() {
        return conversacionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar conversaciones de un usuario", description = "Devuelve las conversaciones en las que participa el usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de conversaciones obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "El usuario indicado no existe")
    })
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long usuarioId) {
        if (!usuarioClient.existe(usuarioId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El usuario " + usuarioId + " no existe");
        }
        return ResponseEntity.ok(conversacionRepository.listarPorUsuario(usuarioId).stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener conversacion por id", description = "Busca una conversacion por su identificador e incluye los participantes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversacion encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una conversacion con el id indicado")
    })
    public ResponseEntity<ConversacionResponse> buscarPorId(@PathVariable Long id) {
        return conversacionRepository.findById(id)
                .map(conversacion -> ResponseEntity.ok(toResponse(conversacion)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar conversacion", description = "Elimina la conversacion y sus mensajes asociados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conversacion eliminada; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe una conversacion con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!conversacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        conversacionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/mensajes")
    @Operation(summary = "Enviar mensaje", description = "Envia un mensaje a la conversacion. Los mensajes son inmutables: una vez creados no se pueden editar.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje enviado correctamente"),
            @ApiResponse(responseCode = "400", description = "Contenido vacio, emisor inexistente o emisor que no participa en la conversacion"),
            @ApiResponse(responseCode = "404", description = "No existe una conversacion con el id indicado")
    })
    public ResponseEntity<?> enviarMensaje(@PathVariable Long id,
                                           @Valid @RequestBody EnviarMensajeRequest request) {
        Conversacion conversacion = conversacionRepository.findById(id).orElse(null);
        if (conversacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("La conversación " + id + " no existe");
        }
        if (!esParticipante(conversacion, request.emisorId())) {
            return ResponseEntity.badRequest()
                    .body("El usuario " + request.emisorId() + " no participa en esta conversación");
        }
        if (!usuarioClient.existe(request.emisorId())) {
            return ResponseEntity.badRequest()
                    .body("El usuario " + request.emisorId() + " no existe");
        }

        Mensaje mensaje = new Mensaje(conversacion, request.emisorId(), request.contenido());

        Mensaje guardado = mensajeRepository.save(mensaje);

        conversacion.setUltimaActividad(guardado.getFechaEnvio());
        conversacionRepository.save(conversacion);

        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeResponse.de(guardado));
    }

    @GetMapping("/{id}/mensajes")
    @Operation(summary = "Listar mensajes", description = "Devuelve los mensajes de la conversacion ordenados por fecha de envio ascendente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de mensajes obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe una conversacion con el id indicado")
    })
    public ResponseEntity<?> listarMensajes(@PathVariable Long id) {
        if (!conversacionRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("La conversación " + id + " no existe");
        }
        return ResponseEntity.ok(mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(id).stream()
                .map(MensajeResponse::de)
                .toList());
    }

    // Marca como leídos todos los mensajes que el usuario recibió en la conversación
    @PutMapping("/{id}/mensajes/leer")
    @Operation(summary = "Marcar mensajes como leidos", description = "Marca como leidos los mensajes recibidos por el usuario en la conversacion. No modifica el contenido de los mensajes.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mensajes marcados como leidos; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "400", description = "El usuario no participa en la conversacion"),
            @ApiResponse(responseCode = "404", description = "No existe una conversacion con el id indicado")
    })
    public ResponseEntity<?> marcarComoLeidos(@PathVariable Long id,
                                              @RequestParam Long usuarioId) {
        Conversacion conversacion = conversacionRepository.findById(id).orElse(null);
        if (conversacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("La conversación " + id + " no existe");
        }
        if (!esParticipante(conversacion, usuarioId)) {
            return ResponseEntity.badRequest()
                    .body("El usuario " + usuarioId + " no participa en esta conversación");
        }

        List<Mensaje> noLeidos = mensajeRepository.findByConversacionIdOrderByFechaEnvioAsc(id).stream()
                .filter(m -> !m.getEmisorId().equals(usuarioId) && !m.isLeido())
                .toList();
        noLeidos.forEach(Mensaje::marcarLeido);
        mensajeRepository.saveAll(noLeidos);

        return ResponseEntity.noContent().build();
    }

    private boolean esParticipante(Conversacion conversacion, Long usuarioId) {
        return conversacion.getUsuario1Id().equals(usuarioId)
                || conversacion.getUsuario2Id().equals(usuarioId);
    }

    private ConversacionResponse toResponse(Conversacion conversacion) {
        return new ConversacionResponse(
                conversacion.getId(),
                conversacion.getItemId(),
                participante(conversacion.getUsuario1Id()),
                participante(conversacion.getUsuario2Id()),
                conversacion.getFechaCreacion(),
                conversacion.getUltimaActividad()
        );
    }

    // Consulta ms-usuario para mostrar quiénes se están comunicando
    private ParticipanteResponse participante(Long usuarioId) {
        try {
            UsuarioDTO usuario = usuarioClient.buscarPorId(usuarioId);
            if (usuario != null) {
                return new ParticipanteResponse(usuario.id(), usuario.nombre(), usuario.correo());
            }
        } catch (Exception ignored) {
            // Si ms-usuario no está disponible, la conversación se puede consultar por id.
        }
        return new ParticipanteResponse(usuarioId, "Usuario " + usuarioId, null);
    }
}
