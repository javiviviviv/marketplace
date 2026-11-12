package com.polec.mscarrito.controller;

import com.polec.mscarrito.client.ItemClient;
import com.polec.mscarrito.client.ItemDTO;
import com.polec.mscarrito.client.RegistrarVentaRequest;
import com.polec.mscarrito.client.VentaClient;
import com.polec.mscarrito.client.VentaDTO;
import com.polec.mscarrito.dto.AgregarItemRequest;
import com.polec.mscarrito.entity.Carrito;
import com.polec.mscarrito.entity.CarritoItem;
import com.polec.mscarrito.repository.CarritoRepository;
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
@RequestMapping("/carritos")
@Tag(name = "Carrito", description = "Carrito de compras y validacion de disponibilidad de items")
public class CarritoController {
    private final CarritoRepository repository;
    private final ItemClient itemClient;
    private final VentaClient ventaClient;

    public CarritoController(CarritoRepository repository, ItemClient itemClient, VentaClient ventaClient) {
        this.repository = repository;
        this.itemClient = itemClient;
        this.ventaClient = ventaClient;
    }

    @PostMapping("/items")
    @Operation(summary = "Agregar item al carrito", description = "Agrega un item a un carrito nuevo o existente. Valida que el item no sea del propio usuario, que exista, que este EN_VENTA y que no este ya en el carrito.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item agregado al carrito correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o el usuario intenta agregar su propio producto"),
            @ApiResponse(responseCode = "404", description = "El item no existe"),
            @ApiResponse(responseCode = "409", description = "El item ya fue vendido, no esta disponible o ya esta en el carrito"),
            @ApiResponse(responseCode = "503", description = "No se pudo verificar el estado de venta del item")
    })
    public ResponseEntity<?> agregarItem(@Valid @RequestBody AgregarItemRequest request) {

        ItemDTO item;
        try {
            item = itemClient.obtenerItemPorId(request.itemId());
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El item " + request.itemId() + " no existe");
        }

        if (item.vendedorId().equals(request.usuarioId())) {
            return ResponseEntity.badRequest()
                    .body("No puedes agregar tu propio producto al carrito");
        }

        // si el item no tiene venta se crea en EN_VENTA y se devuelve su estado
        VentaDTO venta;
        try {
            venta = ventaClient.registrar(new RegistrarVentaRequest(item.id()));
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El item " + request.itemId() + " no existe");
        } catch (FeignException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo verificar el estado de venta del item " + request.itemId());
        }

        if ("VENDIDO".equalsIgnoreCase(venta.estado())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El item " + request.itemId() + " ya fue vendido");
        }
        if (!"EN_VENTA".equalsIgnoreCase(venta.estado())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El item " + request.itemId() + " no está disponible para la venta");
        }

        Carrito carrito = repository.findByUsuarioId(request.usuarioId())
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuarioId(request.usuarioId());
                    return nuevo;
                });

        boolean yaEnElCarrito = carrito.getItems().stream()
                .anyMatch(i -> i.getItemId().equals(request.itemId()));
        if (yaEnElCarrito) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El item ya está en el carrito");
        }

        CarritoItem nuevoItem = new CarritoItem();
        nuevoItem.setItemId(item.id());
        nuevoItem.setItemNombre(item.nombre());
        nuevoItem.setItemPrecio(item.precio());
        nuevoItem.setCarrito(carrito);
        carrito.getItems().add(nuevoItem);

        // calcular el total que vale el carrito sumando el precio de cada item
        carrito.setTotal(carrito.getItems().stream()
                .mapToLong(CarritoItem::getItemPrecio)
                .sum());

        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(carrito));
    }
    // mostrar todos los carritos
    @GetMapping
    @Operation(summary = "Listar carritos", description = "Devuelve todos los carritos registrados. Si no hay carritos, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de carritos obtenida correctamente")
    })
    public List<Carrito> listarCarritos(){
        return repository.findAll();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar carrito por id", description = "Elimina el carrito indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrito eliminado; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe un carrito con el id indicado")
    })
    public ResponseEntity<Void> eliminarPorId(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}")
    @Operation(summary = "Eliminar carrito por usuario", description = "Elimina el carrito asociado al usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrito eliminado; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene un carrito registrado")
    })
    public ResponseEntity<Void> eliminarPorUsuario(@PathVariable Long usuarioId) {
        Carrito carrito = repository.findByUsuarioId(usuarioId).orElse(null);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(carrito);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{usuarioId}")
    @Operation(summary = "Obtener carrito por usuario", description = "Devuelve el carrito del usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
            @ApiResponse(responseCode = "404", description = "El usuario no tiene un carrito registrado")
    })
    public ResponseEntity<Carrito> verCarrito(@PathVariable Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



}
