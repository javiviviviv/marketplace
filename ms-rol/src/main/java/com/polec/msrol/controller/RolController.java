package com.polec.msrol.controller;

import com.polec.msrol.entity.Rol;
import com.polec.msrol.repository.RolRepository;
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
@RequestMapping("/roles")
@Tag(name = "Roles", description = "Gestion de los roles del marketplace")
public class RolController {

    private final RolRepository repository;

    public RolController(RolRepository repository){
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar roles", description = "Devuelve todos los roles registrados. Si no hay roles, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de roles obtenida correctamente")
    })
    public List<Rol> listar(){
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por id", description = "Busca un rol por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un rol con el id indicado")
    })
    public ResponseEntity<Rol> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Actualiza el nombre y la descripcion de un rol. El id de la ruta es obligatorio; el id del cuerpo se ignora.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o nombre vacio"),
            @ApiResponse(responseCode = "404", description = "No existe un rol con el id indicado")
    })
    public ResponseEntity<Rol> actualizar(@PathVariable Long id, @Valid @RequestBody Rol datos){
        Rol rol = repository.findById(id).orElse(null);
        if (rol == null) {
            return ResponseEntity.notFound().build();
        }
        rol.setNombre(datos.getNombre());
        rol.setDescripcion(datos.getDescripcion());
        return ResponseEntity.ok(repository.save(rol));
    }

    @PostMapping
    @Operation(summary = "Crear rol", description = "Registra un rol nuevo. El id es autogenerado y no debe enviarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rol creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, nombre vacio o id enviado en la creacion"),
            @ApiResponse(responseCode = "500", description = "Error interno o nombre de rol duplicado")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Rol rol){
        if (rol.getId() != null) {
            return ResponseEntity.badRequest()
                    .body("El id es autogenerado por el servidor; no debe enviarse en la creacion");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(rol));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Elimina el rol indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rol eliminado; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe un rol con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
