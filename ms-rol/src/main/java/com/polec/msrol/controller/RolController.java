package com.polec.msrol.controller;

import com.polec.msrol.entity.Rol;
import com.polec.msrol.repository.RolRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolController {

    private final RolRepository repository;

    public RolController(RolRepository repository){
        this.repository = repository;
    }

    @GetMapping
    public List<Rol> listar(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rol> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Rol crear(@RequestBody Rol rol){
        return repository.save(rol);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id){
        repository.deleteById(id);
    }

}
