package com.polec.mspago.controller;

import com.polec.mspago.dto.CambiarEstadoPagoRequest;
import com.polec.mspago.dto.CrearPagoRequest;
import com.polec.mspago.dto.PagoResponse;
import com.polec.mspago.entity.EstadoPago;
import com.polec.mspago.entity.Pago;
import com.polec.mspago.repository.PagoRepository;
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
@RequestMapping("/pagos")
@Tag(name = "Pagos", description = "Simulacion de pagos y sus estados PENDIENTE, PAGADO y RECHAZADO")
public class PagoController {

    private final PagoRepository repository;

    public PagoController(PagoRepository repository) {
        this.repository = repository;
    }

    // simula un pago, si la venta ya tiene un pago devuelve el actual
    @PostMapping
    @Operation(summary = "Crear pago", description = "Crea un pago en estado PENDIENTE para una venta. Si la venta ya tiene un pago, devuelve el existente. El id es autogenerado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago creado correctamente en estado PENDIENTE"),
            @ApiResponse(responseCode = "200", description = "La venta ya tenia un pago; se devuelve el existente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos: ventaId nulo o monto negativo"),
            @ApiResponse(responseCode = "500", description = "Error interno o pago duplicado para la misma venta")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody CrearPagoRequest request) {
        Optional<Pago> existente = repository.findByVentaId(request.ventaId());
        if (existente.isPresent()) {
            return ResponseEntity.ok(PagoResponse.de(existente.get()));
        }

        Pago pago = new Pago();
        pago.setVentaId(request.ventaId());
        pago.setMonto(request.monto());
        pago.setEstado(EstadoPago.PENDIENTE);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PagoResponse.de(repository.save(pago)));
    }

    @GetMapping
    @Operation(summary = "Listar pagos", description = "Devuelve todos los pagos registrados. Si no hay pagos, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida correctamente")
    })
    public List<PagoResponse> listar() {
        return repository.findAll().stream()
                .map(PagoResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por id", description = "Busca un pago por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un pago con el id indicado")
    })
    public ResponseEntity<PagoResponse> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(pago -> ResponseEntity.ok(PagoResponse.de(pago)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/venta/{ventaId}")
    @Operation(summary = "Obtener pago por venta", description = "Busca el pago asociado a la venta indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "La venta no tiene un pago registrado")
    })
    public ResponseEntity<PagoResponse> buscarPorVenta(@PathVariable Long ventaId) {
        return repository.findByVentaId(ventaId)
                .map(pago -> ResponseEntity.ok(PagoResponse.de(pago)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar pagos por estado", description = "Devuelve los pagos cuyo estado coincide con el indicado. Estados validos: PENDIENTE, PAGADO, RECHAZADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "El estado indicado no es valido")
    })
    public List<PagoResponse> listarPorEstado(@PathVariable EstadoPago estado) {
        return repository.findByEstado(estado).stream()
                .map(PagoResponse::de)
                .toList();
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de pago", description = "Cambia el estado del pago respetando las transiciones: PENDIENTE a PAGADO o RECHAZADO, RECHAZADO a PENDIENTE. PAGADO es estado final.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de pago actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "El estado solicitado no es valido"),
            @ApiResponse(responseCode = "404", description = "No existe un pago con el id indicado"),
            @ApiResponse(responseCode = "409", description = "Transicion de estado no permitida")
    })
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambiarEstadoPagoRequest request) {
        Pago pago = repository.findById(id).orElse(null);
        if (pago == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El pago " + id + " no existe");
        }
        return aplicarEstado(pago, request.estado());
    }

    @PutMapping("/{id}/pagar")
    @Operation(summary = "Pagar", description = "Marca el pago como PAGADO. Solo es posible desde PENDIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago marcado como PAGADO"),
            @ApiResponse(responseCode = "404", description = "No existe un pago con el id indicado"),
            @ApiResponse(responseCode = "409", description = "El pago no se encuentra en estado PENDIENTE")
    })
    public ResponseEntity<?> pagar(@PathVariable Long id) {
        return accion(id, EstadoPago.PAGADO);
    }

    @PutMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar", description = "Marca el pago como RECHAZADO. Solo es posible desde PENDIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago marcado como RECHAZADO"),
            @ApiResponse(responseCode = "404", description = "No existe un pago con el id indicado"),
            @ApiResponse(responseCode = "409", description = "El pago no se encuentra en estado PENDIENTE")
    })
    public ResponseEntity<?> rechazar(@PathVariable Long id) {
        return accion(id, EstadoPago.RECHAZADO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pago", description = "Elimina el pago indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pago eliminado; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe un pago con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> accion(Long id, EstadoPago estado) {
        Pago pago = repository.findById(id).orElse(null);
        if (pago == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El pago " + id + " no existe");
        }
        return aplicarEstado(pago, estado);
    }

    private ResponseEntity<?> aplicarEstado(Pago pago, EstadoPago nuevo) {
        EstadoPago actual = pago.getEstado();

        if (actual == nuevo) {
            return ResponseEntity.ok(PagoResponse.de(pago));
        }

        if (!transicionValida(actual, nuevo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede cambiar el pago de " + actual + " a " + nuevo
                            + ". Transiciones permitidas: PENDIENTE -> PAGADO o RECHAZADO, "
                            + "RECHAZADO -> PENDIENTE. PAGADO es un estado final");
        }

        pago.setEstado(nuevo);
        pago.setFechaActualizacion(LocalDateTime.now());

        return ResponseEntity.ok(PagoResponse.de(repository.save(pago)));
    }

    private boolean transicionValida(EstadoPago actual, EstadoPago nuevo) {
        return switch (actual) {
            case PENDIENTE -> nuevo == EstadoPago.PAGADO || nuevo == EstadoPago.RECHAZADO;
            case PAGADO -> false; // estado final
            case RECHAZADO -> nuevo == EstadoPago.PENDIENTE;
        };
    }
}
