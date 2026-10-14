package com.polec.mscarrito.controller;

import com.polec.mscarrito.client.ItemClient;
import com.polec.mscarrito.client.ItemDTO;
import com.polec.mscarrito.dto.AgregarItemRequest;
import com.polec.mscarrito.entity.Carrito;
import com.polec.mscarrito.entity.CarritoItem;
import com.polec.mscarrito.repository.CarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Carrito> agregarItem(@RequestBody AgregarItemRequest request) {

        ItemDTO itemOriginal = itemClient.obtenerItemPorId(request.itemId());

        Optional<Carrito> carritoExistente = repository.findByUsuarioId(request.usuarioId());
        Carrito carrito;

        if (carritoExistente.isPresent()) {
            carrito = carritoExistente.get();
        } else {
            carrito = new Carrito();
            carrito.setUsuarioId(request.usuarioId());
        }

        CarritoItem nuevoItem = new CarritoItem();
        nuevoItem.setItemId(itemOriginal.id());

        nuevoItem.setItemPrecio(itemOriginal.precio());

        nuevoItem.setCarrito(carrito);
        carrito.getItems().add(nuevoItem);

        Carrito carritoGuardado = repository.save(carrito);

        return ResponseEntity.status(HttpStatus.CREATED).body(carritoGuardado);
    }
    @GetMapping
    public List<Carrito> listarCarritos(){
        return repository.findAll();
    }


    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long usuarioId){
        if(!repository.existsByUsuarioId(usuarioId)){
            return ResponseEntity.notFound().build();
        }
        repository.deleteByUsuarioId(usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<Carrito> verCarrito(@PathVariable Long usuarioId) {
        return repository.findByUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



}
