package com.polec.msenvio.controller;

import com.polec.msenvio.client.UsuarioClient;
import com.polec.msenvio.client.UsuarioDTO;
import com.polec.msenvio.dto.AsignarRepartidorRequest;
import com.polec.msenvio.dto.CambiarEstadoEnvioRequest;
import com.polec.msenvio.dto.CrearEnvioRequest;
import com.polec.msenvio.dto.EnvioResponse;
import com.polec.msenvio.dto.RepartidorResumenResponse;
import com.polec.msenvio.entity.Envio;
import com.polec.msenvio.entity.EstadoEnvio;
import com.polec.msenvio.repository.EnvioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/envios")
@Tag(name = "Envios", description = "Simulacion de entregas y panel del repartidor")
public class EnvioController {

    private static final String ROL_REPARTIDOR = "REPARTIDOR";
    private static final EstadoEnvio ENTREGADO = EstadoEnvio.ENTREGADO;

    private final EnvioRepository repository;
    private final UsuarioClient usuarioClient;

    public EnvioController(EnvioRepository repository, UsuarioClient usuarioClient) {
        this.repository = repository;
        this.usuarioClient = usuarioClient;
    }

    // Crea un pedido de envio para una venta y lo agrega al final de la ruta del repartidor.
    @PostMapping
    @Operation(summary = "Crear envio", description = "Crea un envio en EN_PREPARACION y lo agrega al final de la ruta del repartidor. Valida que la venta no tenga envio y que el repartidor exista y tenga rol REPARTIDOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Envio creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, comprador inexistente, repartidor inexistente o sin rol REPARTIDOR"),
            @ApiResponse(responseCode = "409", description = "La venta ya tiene un envio registrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody CrearEnvioRequest request) {
        if (repository.existsByVentaId(request.ventaId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La venta " + request.ventaId() + " ya tiene un envio registrado");
        }

        ResponseEntity<?> errorRepartidor = validarRepartidor(request.repartidorId());
        if (errorRepartidor != null) {
            return errorRepartidor;
        }

        if (usuarioClient.buscarSinLanzar(request.compradorId()) == null) {
            return ResponseEntity.badRequest()
                    .body("El comprador " + request.compradorId() + " no existe");
        }

        Envio envio = new Envio();
        envio.setVentaId(request.ventaId());
        envio.setCompradorId(request.compradorId());
        envio.setRepartidorId(request.repartidorId());
        envio.setDireccion(request.direccion());
        envio.setEstado(EstadoEnvio.EN_PREPARACION);
        envio.setPosicion(siguientePosicion(request.repartidorId()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EnvioResponse.de(repository.save(envio)));
    }

    @GetMapping
    @Operation(summary = "Listar envios", description = "Devuelve todos los envios registrados. Si no hay envios, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de envios obtenida correctamente")
    })
    public List<EnvioResponse> listar() {
        return repository.findAll().stream()
                .map(EnvioResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener envio por id", description = "Busca un envio por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Envio encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un envio con el id indicado")
    })
    public ResponseEntity<EnvioResponse> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(envio -> ResponseEntity.ok(EnvioResponse.de(envio)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/venta/{ventaId}")
    @Operation(summary = "Obtener envio por venta", description = "Busca el envio asociado a la venta indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Envio encontrado"),
            @ApiResponse(responseCode = "404", description = "La venta no tiene un envio registrado")
    })
    public ResponseEntity<EnvioResponse> buscarPorVenta(@PathVariable Long ventaId) {
        return repository.findByVentaId(ventaId)
                .map(envio -> ResponseEntity.ok(EnvioResponse.de(envio)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar envios por estado", description = "Devuelve los envios cuyo estado coincide con el indicado. Estados validos: EN_PREPARACION, EN_CAMINO, LLEGANDO, ENTREGADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de envios obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "El estado indicado no es valido")
    })
    public List<EnvioResponse> listarPorEstado(@PathVariable EstadoEnvio estado) {
        return repository.findAll().stream()
                .filter(envio -> envio.getEstado() == estado)
                .map(EnvioResponse::de)
                .toList();
    }

    @GetMapping("/repartidor/{repartidorId}")
    @Operation(summary = "Listar envios de un repartidor", description = "Devuelve todos los envios del repartidor ordenados por posicion en la ruta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de envios obtenida correctamente")
    })
    public List<EnvioResponse> listarPorRepartidor(@PathVariable Long repartidorId) {
        return repository.findByRepartidorIdOrderByPosicionAsc(repartidorId).stream()
                .map(EnvioResponse::de)
                .toList();
    }

    @GetMapping("/repartidor/{repartidorId}/pendientes")
    @Operation(summary = "Listar envios pendientes de un repartidor", description = "Devuelve los envios que aun no estan ENTREGADO, ordenados por posicion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de envios pendientes obtenida correctamente")
    })
    public List<EnvioResponse> listarPendientes(@PathVariable Long repartidorId) {
        return repository.findByRepartidorIdAndEstadoNotOrderByPosicionAsc(repartidorId, ENTREGADO).stream()
                .map(EnvioResponse::de)
                .toList();
    }

    @GetMapping("/repartidor/{repartidorId}/entregados")
    @Operation(summary = "Listar envios entregados de un repartidor", description = "Devuelve los envios en estado ENTREGADO, ordenados por posicion.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de envios entregados obtenida correctamente")
    })
    public List<EnvioResponse> listarEntregados(@PathVariable Long repartidorId) {
        return repository.findByRepartidorIdAndEstadoOrderByPosicionAsc(repartidorId, ENTREGADO).stream()
                .map(EnvioResponse::de)
                .toList();
    }

    @GetMapping("/repartidor/{repartidorId}/siguiente")
    @Operation(summary = "Obtener siguiente envio del repartidor", description = "Devuelve el primer envio pendiente de la ruta, que es el unico que puede estar en LLEGANDO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Siguiente envio encontrado"),
            @ApiResponse(responseCode = "204", description = "El repartidor no tiene envios pendientes; la respuesta no tiene contenido")
    })
    public ResponseEntity<EnvioResponse> siguiente(@PathVariable Long repartidorId) {
        return repository.findFirstByRepartidorIdAndEstadoNotOrderByPosicionAsc(repartidorId, ENTREGADO)
                .map(envio -> ResponseEntity.ok(EnvioResponse.de(envio)))
                .orElse(ResponseEntity.noContent().build());
    }

    // Panel del repartidor: pedidos, entregados, pendientes, siguiente y conteos.
    @GetMapping("/repartidor/{repartidorId}/resumen")
    @Operation(summary = "Resumen del repartidor", description = "Devuelve el panel del repartidor con pedidos, entregados, pendientes, siguiente envio y conteos totales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido correctamente"),
            @ApiResponse(responseCode = "403", description = "El usuario existe pero no tiene el rol REPARTIDOR"),
            @ApiResponse(responseCode = "404", description = "El repartidor indicado no existe")
    })
    public ResponseEntity<?> resumen(@PathVariable Long repartidorId) {
        UsuarioDTO repartidor = usuarioClient.buscarSinLanzar(repartidorId);
        if (repartidor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El repartidor " + repartidorId + " no existe");
        }
        if (repartidor.rol() == null || !ROL_REPARTIDOR.equalsIgnoreCase(repartidor.rol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("El usuario " + repartidorId + " no tiene el rol " + ROL_REPARTIDOR);
        }

        List<Envio> pedidos = repository.findByRepartidorIdOrderByPosicionAsc(repartidorId);
        List<Envio> pendientes = pedidos.stream()
                .filter(envio -> envio.getEstado() != ENTREGADO)
                .toList();
        List<Envio> entregados = pedidos.stream()
                .filter(envio -> envio.getEstado() == ENTREGADO)
                .toList();

        EnvioResponse siguiente = pendientes.isEmpty() ? null : EnvioResponse.de(pendientes.get(0));

        RepartidorResumenResponse response = new RepartidorResumenResponse(
                repartidorId,
                repartidor.nombre(),
                pedidos.size(),
                entregados.size(),
                pendientes.size(),
                siguiente,
                pedidos.stream().map(EnvioResponse::de).toList(),
                pendientes.stream().map(EnvioResponse::de).toList(),
                entregados.stream().map(EnvioResponse::de).toList()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de envio", description = "Cambia el estado respetando la secuencia EN_PREPARACION, EN_CAMINO, LLEGANDO, ENTREGADO. Solo el siguiente pedido pendiente puede estar en LLEGANDO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de envio actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "El estado solicitado no es valido"),
            @ApiResponse(responseCode = "404", description = "No existe un envio con el id indicado"),
            @ApiResponse(responseCode = "409", description = "Transicion no permitida o el envio no es el siguiente de la ruta")
    })
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambiarEstadoEnvioRequest request) {
        Envio envio = repository.findById(id).orElse(null);
        if (envio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El envio " + id + " no existe");
        }
        return aplicarEstado(envio, request.estado());
    }

    // EN_PREPARACION -> EN_CAMINO
    @PutMapping("/{id}/iniciar")
    @Operation(summary = "Iniciar envio", description = "Pasa el envio de EN_PREPARACION a EN_CAMINO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Envio iniciado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un envio con el id indicado"),
            @ApiResponse(responseCode = "409", description = "El envio no esta en estado EN_PREPARACION")
    })
    public ResponseEntity<?> iniciar(@PathVariable Long id) {
        Envio envio = repository.findById(id).orElse(null);
        if (envio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El envio " + id + " no existe");
        }
        if (envio.getEstado() != EstadoEnvio.EN_PREPARACION) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Solo se puede iniciar un envio en EN_PREPARACION");
        }
        return aplicarEstado(envio, EstadoEnvio.EN_CAMINO);
    }

    // Avanza al siguiente estado permitido.
    @PutMapping("/{id}/avanzar")
    @Operation(summary = "Avanzar envio", description = "Avanza al siguiente estado permitido: EN_PREPARACION a EN_CAMINO, EN_CAMINO a LLEGANDO, LLEGANDO a ENTREGADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Envio avanzado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un envio con el id indicado"),
            @ApiResponse(responseCode = "409", description = "El envio ya fue entregado o la transicion no es valida")
    })
    public ResponseEntity<?> avanzar(@PathVariable Long id) {
        Envio envio = repository.findById(id).orElse(null);
        if (envio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El envio " + id + " no existe");
        }

        EstadoEnvio siguiente = switch (envio.getEstado()) {
            case EN_PREPARACION -> EstadoEnvio.EN_CAMINO;
            case EN_CAMINO -> EstadoEnvio.LLEGANDO;
            case LLEGANDO -> ENTREGADO;
            case ENTREGADO -> null;
        };

        if (siguiente == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El envio " + id + " ya fue entregado");
        }

        return aplicarEstado(envio, siguiente);
    }

    // Marca el envio como ENTREGADO y promueve el siguiente pedido a LLEGANDO.
    @PutMapping("/{id}/entregar")
    @Operation(summary = "Entregar envio", description = "Marca el envio como ENTREGADO y promueve automaticamente el siguiente pedido de la ruta a LLEGANDO. Deben entregarse primero los pedidos anteriores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Envio entregado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un envio con el id indicado"),
            @ApiResponse(responseCode = "409", description = "El envio ya fue entregado o aun hay pedidos anteriores pendientes")
    })
    public ResponseEntity<?> entregar(@PathVariable Long id) {
        Envio envio = repository.findById(id).orElse(null);
        if (envio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El envio " + id + " no existe");
        }
        if (envio.getEstado() == ENTREGADO) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El envio " + id + " ya fue entregado");
        }
        if (!esSiguiente(envio)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Aun hay pedidos anteriores pendientes. El siguiente pedido debe entregarse primero");
        }
        return aplicarEstado(envio, ENTREGADO);
    }

    // Reasigna el pedido a otro repartidor y lo pone al final de su ruta.
    @PutMapping("/{id}/repartidor")
    @Operation(summary = "Reasignar repartidor", description = "Reasigna el envio a otro repartidor y lo coloca al final de su ruta. Si estaba en LLEGANDO pasa a EN_CAMINO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Repartidor reasignado correctamente"),
            @ApiResponse(responseCode = "400", description = "Repartidor nulo, inexistente o sin rol REPARTIDOR"),
            @ApiResponse(responseCode = "404", description = "No existe un envio con el id indicado")
    })
    public ResponseEntity<?> asignarRepartidor(@PathVariable Long id,
                                               @Valid @RequestBody AsignarRepartidorRequest request) {
        Envio envio = repository.findById(id).orElse(null);
        if (envio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El envio " + id + " no existe");
        }

        ResponseEntity<?> errorRepartidor = validarRepartidor(request.repartidorId());
        if (errorRepartidor != null) {
            return errorRepartidor;
        }

        Long anterior = envio.getRepartidorId();
        if (anterior.equals(request.repartidorId())) {
            return ResponseEntity.ok(EnvioResponse.de(envio));
        }

        envio.setRepartidorId(request.repartidorId());
        envio.setPosicion(siguientePosicion(request.repartidorId()));
        // si estaba marcado como LLEGANDO deja de ser el siguiente de la ruta anterior
        if (envio.getEstado() == EstadoEnvio.LLEGANDO) {
            envio.setEstado(EstadoEnvio.EN_CAMINO);
        }
        envio.setFechaActualizacion(LocalDateTime.now());
        Envio guardado = repository.save(envio);

        promoverSiguiente(anterior);

        return ResponseEntity.ok(EnvioResponse.de(guardado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar envio", description = "Elimina el envio y promueve el siguiente pedido pendiente del repartidor a LLEGANDO.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Envio eliminado; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe un envio con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Envio envio = repository.findById(id).orElse(null);
        if (envio == null) {
            return ResponseEntity.notFound().build();
        }
        Long repartidorId = envio.getRepartidorId();
        repository.delete(envio);
        promoverSiguiente(repartidorId);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> aplicarEstado(Envio envio, EstadoEnvio nuevo) {
        EstadoEnvio actual = envio.getEstado();

        if (actual == nuevo) {
            return ResponseEntity.ok(EnvioResponse.de(envio));
        }

        if (nuevo == EstadoEnvio.LLEGANDO && !esSiguiente(envio)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Solo el siguiente pedido pendiente puede estar en LLEGANDO");
        }

        if (!transicionValida(actual, nuevo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede cambiar el envio de " + actual + " a " + nuevo
                            + ". Secuencia permitida: EN_PREPARACION -> EN_CAMINO -> LLEGANDO -> ENTREGADO");
        }

        envio.setEstado(nuevo);
        envio.setFechaActualizacion(LocalDateTime.now());
        if (nuevo == ENTREGADO) {
            envio.setFechaEntrega(LocalDateTime.now());
        }

        Envio guardado = repository.save(envio);

        if (nuevo == ENTREGADO) {
            promoverSiguiente(guardado.getRepartidorId());
        }

        return ResponseEntity.ok(EnvioResponse.de(guardado));
    }

    private boolean transicionValida(EstadoEnvio actual, EstadoEnvio nuevo) {
        return switch (actual) {
            case EN_PREPARACION -> nuevo == EstadoEnvio.EN_CAMINO;
            case EN_CAMINO -> nuevo == EstadoEnvio.LLEGANDO;
            case LLEGANDO -> nuevo == ENTREGADO;
            case ENTREGADO -> false;
        };
    }

    private boolean esSiguiente(Envio envio) {
        Optional<Envio> siguiente = repository
                .findFirstByRepartidorIdAndEstadoNotOrderByPosicionAsc(envio.getRepartidorId(), ENTREGADO);
        return siguiente.isPresent() && siguiente.get().getId().equals(envio.getId());
    }

    // El primer pedido pendiente de la ruta queda en LLEGANDO automaticamente.
    private void promoverSiguiente(Long repartidorId) {
        repository.findFirstByRepartidorIdAndEstadoNotOrderByPosicionAsc(repartidorId, ENTREGADO)
                .ifPresent(siguiente -> {
                    if (siguiente.getEstado() != EstadoEnvio.LLEGANDO) {
                        siguiente.setEstado(EstadoEnvio.LLEGANDO);
                        siguiente.setFechaActualizacion(LocalDateTime.now());
                        repository.save(siguiente);
                    }
                });
    }

    private int siguientePosicion(Long repartidorId) {
        Integer ultima = repository.ultimaPosicion(repartidorId);
        return (ultima == null ? 0 : ultima) + 1;
    }

    private ResponseEntity<?> validarRepartidor(Long repartidorId) {
        if (repartidorId == null) {
            return ResponseEntity.badRequest().body("Se requiere el id del repartidor");
        }
        UsuarioDTO repartidor = usuarioClient.buscarSinLanzar(repartidorId);
        if (repartidor == null) {
            return ResponseEntity.badRequest()
                    .body("El repartidor " + repartidorId + " no existe");
        }
        if (repartidor.rol() == null || !ROL_REPARTIDOR.equalsIgnoreCase(repartidor.rol())) {
            return ResponseEntity.badRequest()
                    .body("El usuario " + repartidorId + " no tiene el rol " + ROL_REPARTIDOR);
        }
        return null;
    }
}
