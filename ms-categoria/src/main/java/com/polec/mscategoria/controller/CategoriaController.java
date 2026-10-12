package com.polec.mscategoria.controller;

import com.polec.mscategoria.entity.Categoria;
import com.polec.mscategoria.repository.CategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository){
        this.repository = repository;
    }

    @GetMapping
    public List<Categoria> listar(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Categoria crear(@RequestBody Categoria categoria){
        return repository.save(categoria);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id){
        repository.deleteById(id);
    }

}
