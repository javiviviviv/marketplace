package com.polec.msitem.controller;

import com.polec.msitem.client.CategoriaClient;
import com.polec.msitem.client.CategoriaDTO;
import com.polec.msitem.dto.ItemResponse;
import com.polec.msitem.entity.Item;
import com.polec.msitem.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    //inyectar el repository
    private final ItemRepository repository;
    //inyectar el objeto rest
    private final CategoriaClient categoriaClient;

    public ItemController(ItemRepository repository, CategoriaClient categoriaClient){
        this.repository = repository;
        this.categoriaClient = categoriaClient;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Item item){
        if(!categoriaClient.existe(item.getCategoriaId())){
            return ResponseEntity.badRequest()
                    .body("La categoria " + item.getCategoriaId() + " no existe");
        }
        Item guardado = repository.save(item);
        CategoriaDTO categoria = categoriaClient.buscarPorId(guardado.getCategoriaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.de(guardado, categoria));
    }

    @GetMapping
    public List<Item> listar(){
        return repository.findAll();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        if(!repository.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(item -> ResponseEntity.ok(ItemResponse.de(item, categoriaClient.buscarPorId(item.getCategoriaId()))))
                .orElse(ResponseEntity.notFound().build());
    }




}
