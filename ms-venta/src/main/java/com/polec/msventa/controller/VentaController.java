package com.polec.msventa.controller;

import com.polec.msventa.client.ItemClient;
import com.polec.msventa.client.ItemDTO;
import com.polec.msventa.client.UsuarioClient;
import com.polec.msventa.dto.CambiarEstadoVentaRequest;
import com.polec.msventa.dto.RegistrarVentaRequest;
import com.polec.msventa.dto.VenderItemRequest;
import com.polec.msventa.dto.VentaResponse;
import com.polec.msventa.entity.EstadoVenta;
import com.polec.msventa.entity.Venta;
import com.polec.msventa.repository.VentaRepository;
import feign.FeignException;
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
@RequestMapping("/ventas")
@Tag(name = "Ventas", description = "Estados de venta de los items: EN_VENTA, RESERVADO y VENDIDO")
public class VentaController {

    private final VentaRepository repository;
    private final ItemClient itemClient;
    private final UsuarioClient usuarioClient;

    public VentaController(VentaRepository repository,
                           ItemClient itemClient,
                           UsuarioClient usuarioClient) {
        this.repository = repository;
        this.itemClient = itemClient;
        this.usuarioClient = usuarioClient;
    }

    @PostMapping
    @Operation(summary = "Registrar venta", description = "Registra el estado inicial de un item. Si ya existe una venta para el item, devuelve la existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta registrada correctamente"),
            @ApiResponse(responseCode = "200", description = "Ya existia una venta para el item; se devuelve la actual"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o se intento registrar directamente el estado VENDIDO"),
            @ApiResponse(responseCode = "404", description = "El item indicado no existe"),
            @ApiResponse(responseCode = "503", description = "No se pudo consultar el microservicio de items")
    })
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistrarVentaRequest request) {
        Optional<Venta> existente = repository.findByItemId(request.itemId());
        if (existente.isPresent()) {
            return ResponseEntity.ok(VentaResponse.de(existente.get()));
        }

        ItemDTO item;
        try {
            item = itemClient.buscarPorId(request.itemId());
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El item " + request.itemId() + " no existe");
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo consultar el item " + request.itemId());
        }

        EstadoVenta estado = request.estado() == null ? EstadoVenta.EN_VENTA : request.estado();
        if (estado == EstadoVenta.VENDIDO) {
            return ResponseEntity.badRequest()
                    .body("Para marcar un item como VENDIDO usa PUT /ventas/{itemId}/vender");
        }

        Venta venta = new Venta();
        venta.setItemId(item.id());
        venta.setVendedorId(item.vendedorId());
        venta.setEstado(estado);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VentaResponse.de(repository.save(venta)));
    }

    @GetMapping
    @Operation(summary = "Listar ventas", description = "Devuelve todas las ventas registradas. Si no hay ventas, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida correctamente")
    })
    public List<VentaResponse> listar() {
        return repository.findAll().stream()
                .map(VentaResponse::de)
                .toList();
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "Obtener venta por item", description = "Busca la venta asociada al item indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "No hay una venta registrada para el item indicado")
    })
    public ResponseEntity<VentaResponse> buscarPorItem(@PathVariable Long itemId) {
        return repository.findByItemId(itemId)
                .map(venta -> ResponseEntity.ok(VentaResponse.de(venta)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar ventas por estado", description = "Devuelve las ventas cuyo estado coincide con el indicado. Estados validos: EN_VENTA, RESERVADO, VENDIDO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "El estado indicado no es valido")
    })
    public List<VentaResponse> listarPorEstado(@PathVariable EstadoVenta estado) {
        return repository.findByEstado(estado).stream()
                .map(VentaResponse::de)
                .toList();
    }

    @GetMapping("/comprador/{compradorId}")
    @Operation(summary = "Listar ventas por comprador", description = "Devuelve las ventas realizadas por el comprador indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida correctamente")
    })
    public List<VentaResponse> listarPorComprador(@PathVariable Long compradorId) {
        return repository.findByCompradorId(compradorId).stream()
                .map(VentaResponse::de)
                .toList();
    }

    @GetMapping("/vendedor/{vendedorId}")
    @Operation(summary = "Listar ventas por vendedor", description = "Devuelve las ventas asociadas al vendedor indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas obtenida correctamente")
    })
    public List<VentaResponse> listarPorVendedor(@PathVariable Long vendedorId) {
        return repository.findByVendedorId(vendedorId).stream()
                .map(VentaResponse::de)
                .toList();
    }

    @PutMapping("/{itemId}/estado")
    @Operation(summary = "Cambiar estado de venta", description = "Cambia el estado de la venta. Para marcar VENDIDO se requiere compradorId y se aplican las validaciones del comprador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de venta actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Falta compradorId para VENDIDO, el vendedor intenta comprar su item, el comprador no existe o el estado es invalido"),
            @ApiResponse(responseCode = "404", description = "No hay una venta registrada para el item indicado"),
            @ApiResponse(responseCode = "409", description = "El item ya fue vendido")
    })
    public ResponseEntity<?> cambiarEstado(@PathVariable Long itemId,
                                           @Valid @RequestBody CambiarEstadoVentaRequest request) {
        Venta venta = repository.findByItemId(itemId).orElse(null);
        if (venta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No hay una venta registrada para el item " + itemId);
        }

        if (request.estado() == EstadoVenta.VENDIDO) {
            if (request.compradorId() == null) {
                return ResponseEntity.badRequest()
                        .body("Se requiere compradorId para marcar el item como VENDIDO");
            }
            return marcarVendido(venta, request.compradorId());
        }

        venta.setEstado(request.estado());
        venta.setCompradorId(null);
        venta.setFechaActualizacion(LocalDateTime.now());

        return ResponseEntity.ok(VentaResponse.de(repository.save(venta)));
    }

    @PutMapping("/{itemId}/vender")
    @Operation(summary = "Marcar item como vendido", description = "Marca la venta como VENDIDO y registra al comprador indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item marcado como vendido correctamente"),
            @ApiResponse(responseCode = "400", description = "El vendedor intenta comprar su propio item o el comprador no existe"),
            @ApiResponse(responseCode = "404", description = "No hay una venta registrada para el item indicado"),
            @ApiResponse(responseCode = "409", description = "El item ya fue vendido")
    })
    public ResponseEntity<?> vender(@PathVariable Long itemId,
                                    @Valid @RequestBody VenderItemRequest request) {
        Venta venta = repository.findByItemId(itemId).orElse(null);
        if (venta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No hay una venta registrada para el item " + itemId);
        }
        return marcarVendido(venta, request.compradorId());
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Eliminar venta", description = "Elimina la venta asociada al item indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Venta eliminada; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No hay una venta registrada para el item indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long itemId) {
        Venta venta = repository.findByItemId(itemId).orElse(null);
        if (venta == null) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(venta);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> marcarVendido(Venta venta, Long compradorId) {
        if (venta.getEstado() == EstadoVenta.VENDIDO) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El item " + venta.getItemId() + " ya fue vendido");
        }
        if (venta.getVendedorId().equals(compradorId)) {
            return ResponseEntity.badRequest()
                    .body("El vendedor no puede comprar su propio item");
        }
        if (!usuarioClient.existe(compradorId)) {
            return ResponseEntity.badRequest()
                    .body("El comprador " + compradorId + " no existe");
        }

        venta.setCompradorId(compradorId);
        venta.setEstado(EstadoVenta.VENDIDO);
        venta.setFechaActualizacion(LocalDateTime.now());

        return ResponseEntity.ok(VentaResponse.de(repository.save(venta)));
    }
}
