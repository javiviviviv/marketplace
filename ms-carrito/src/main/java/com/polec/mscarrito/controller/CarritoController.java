package com.polec.mscarrito.controller;

import com.polec.mscarrito.client.ItemClient;
import com.polec.mscarrito.client.ItemDTO;
import com.polec.mscarrito.dto.AgregarItemRequest;
import com.polec.mscarrito.entity.Carrito;
import com.polec.mscarrito.entity.CarritoItem;
import com.polec.mscarrito.repository.CarritoRepository;
import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carritos")
public class CarritoController {
    private final CarritoRepository repository;
    private final ItemClient itemClient;

    public CarritoController(CarritoRepository repository, ItemClient itemClient) {
        this.repository = repository;
        this.itemClient = itemClient;
    }

    @PostMapping("/items")
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
    @GetMapping
    public List<Carrito> listarCarritos(){
        return repository.findAll();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPorId(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> eliminarPorUsuario(@PathVariable Long usuarioId) {
        Carrito carrito = repository.findByUsuarioId(usuarioId).orElse(null);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(carrito);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<Carrito> verCarrito(@PathVariable Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



}
