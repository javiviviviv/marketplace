package com.polec.mscategoria.controller;

import com.polec.mscategoria.entity.Categoria;
import com.polec.mscategoria.repository.CategoriaRepository;
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
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Gestion de categorias de productos")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository){
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Devuelve todas las categorias registradas. Si no hay categorias, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de categorias obtenida correctamente")
    })
    public List<Categoria> listar(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoria por id", description = "Busca una categoria por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una categoria con el id indicado")
    })
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear categoria", description = "Registra una categoria nueva. El id es autogenerado y no debe enviarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, nombre vacio o id enviado en la creacion"),
            @ApiResponse(responseCode = "500", description = "Error interno o nombre de categoria duplicado")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Categoria categoria){
        if (categoria.getId() != null) {
            return ResponseEntity.badRequest()
                    .body("El id es autogenerado por el servidor; no debe enviarse en la creacion");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(categoria));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoria", description = "Elimina la categoria indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria eliminada; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe una categoria con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
