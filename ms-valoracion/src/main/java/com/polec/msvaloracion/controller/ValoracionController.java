package com.polec.msvaloracion.controller;

import com.polec.msvaloracion.client.ItemClient;
import com.polec.msvaloracion.client.ItemDTO;
import com.polec.msvaloracion.client.VentaClient;
import com.polec.msvaloracion.client.VentaDTO;
import com.polec.msvaloracion.dto.CrearValoracionRequest;
import com.polec.msvaloracion.dto.PromedioResponse;
import com.polec.msvaloracion.dto.ValoracionResponse;
import com.polec.msvaloracion.entity.Valoracion;
import com.polec.msvaloracion.repository.ValoracionRepository;
import feign.FeignException;
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
@RequestMapping("/valoraciones")
@Tag(name = "Valoraciones", description = "Resenas de items vendidos y promedio de puntuaciones")
public class ValoracionController {

    private final ValoracionRepository repository;
    private final VentaClient ventaClient;
    private final ItemClient itemClient;

    public ValoracionController(ValoracionRepository repository,
                                VentaClient ventaClient,
                                ItemClient itemClient) {
        this.repository = repository;
        this.ventaClient = ventaClient;
        this.itemClient = itemClient;
    }

    @PostMapping
    @Operation(summary = "Crear valoracion", description = "Registra una valoracion de un item vendido. Solo el comprador puede valorar y una vez por item.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Valoracion creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, puntuacion fuera de 1 a 5 o comentario demasiado largo"),
            @ApiResponse(responseCode = "403", description = "El usuario no es el comprador o el vendedor intenta valorar su propio item"),
            @ApiResponse(responseCode = "404", description = "El item indicado no existe"),
            @ApiResponse(responseCode = "409", description = "El item no tiene venta, aun no fue vendido, la venta no corresponde al vendedor o ya existe una valoracion de este usuario"),
            @ApiResponse(responseCode = "503", description = "No se pudo consultar el microservicio de items o de ventas")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody CrearValoracionRequest request) {
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

        VentaDTO venta;
        try {
            venta = ventaClient.buscarPorItem(request.itemId());
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El item " + request.itemId() + " no tiene una venta registrada");
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo consultar el estado de venta del item " + request.itemId());
        }

        if (!"VENDIDO".equalsIgnoreCase(venta.estado())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El item " + request.itemId() + " aún no ha sido vendido");
        }

        if (venta.compradorId() == null || !venta.compradorId().equals(request.usuarioId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo el comprador del item puede valorarlo");
        }

        if (!venta.vendedorId().equals(item.vendedorId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La venta no corresponde al vendedor del item");
        }

        if (request.usuarioId().equals(item.vendedorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("El vendedor no puede valorar su propio item");
        }

        if (repository.existsByItemIdAndUsuarioId(request.itemId(), request.usuarioId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe una valoración de este usuario para este item");
        }

        Valoracion valoracion = new Valoracion();
        valoracion.setItemId(request.itemId());
        valoracion.setUsuarioId(request.usuarioId());
        valoracion.setVendedorId(venta.vendedorId());
        valoracion.setPuntuacion(request.puntuacion());
        valoracion.setComentario(request.comentario());

        Valoracion guardada = repository.save(valoracion);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ValoracionResponse.de(guardada, item.nombre()));
    }

    @GetMapping
    @Operation(summary = "Listar valoraciones", description = "Devuelve todas las valoraciones registradas. Si no hay valoraciones, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de valoraciones obtenida correctamente")
    })
    public List<ValoracionResponse> listar() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener valoracion por id", description = "Busca una valoracion por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valoracion encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una valoracion con el id indicado")
    })
    public ResponseEntity<ValoracionResponse> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(valoracion -> ResponseEntity.ok(toResponse(valoracion)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "Listar valoraciones por item", description = "Devuelve las valoraciones del item indicado, de la mas reciente a la mas antigua.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de valoraciones obtenida correctamente")
    })
    public List<ValoracionResponse> listarPorItem(@PathVariable Long itemId) {
        return repository.findByItemIdOrderByFechaCreacionDesc(itemId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/item/{itemId}/promedio")
    @Operation(summary = "Promedio de valoraciones por item", description = "Calcula el promedio de puntuaciones y el total de valoraciones del item. Si no hay valoraciones, el promedio es 0.0.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promedio calculado correctamente")
    })
    public PromedioResponse promedioPorItem(@PathVariable Long itemId) {
        Double promedio = repository.promedioPorItem(itemId);
        long total = repository.countByItemId(itemId);
        double redondeado = promedio == null ? 0.0 : Math.round(promedio * 10.0) / 10.0;
        return new PromedioResponse(itemId, redondeado, total);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar valoraciones por usuario", description = "Devuelve las valoraciones realizadas por el usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de valoraciones obtenida correctamente")
    })
    public List<ValoracionResponse> listarPorUsuario(@PathVariable Long usuarioId) {
        return repository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/vendedor/{vendedorId}")
    @Operation(summary = "Listar valoraciones por vendedor", description = "Devuelve las valoraciones recibidas por el vendedor indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de valoraciones obtenida correctamente")
    })
    public List<ValoracionResponse> listarPorVendedor(@PathVariable Long vendedorId) {
        return repository.findByVendedorIdOrderByFechaCreacionDesc(vendedorId).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar valoracion", description = "Elimina la valoracion indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Valoracion eliminada; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe una valoracion con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ValoracionResponse toResponse(Valoracion valoracion) {
        return ValoracionResponse.de(valoracion, itemNombre(valoracion.getItemId()));
    }

    private String itemNombre(Long itemId) {
        try {
            ItemDTO item = itemClient.buscarPorId(itemId);
            if (item != null) {
                return item.nombre();
            }
        } catch (Exception ignored) {
            // Si ms-item no está disponible, la valoración se devuelve con el id del item.
        }
        return "Item " + itemId;
    }
}
