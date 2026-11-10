package com.polec.msitem.controller;

import com.polec.msitem.client.CategoriaClient;
import com.polec.msitem.client.CategoriaDTO;
import com.polec.msitem.dto.ItemRequest;
import com.polec.msitem.dto.ItemResponse;
import com.polec.msitem.entity.Item;
import com.polec.msitem.repository.ItemRepository;
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
@RequestMapping("/items")
@Tag(name = "Items", description = "Gestion de los productos publicados")
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
    @Operation(summary = "Crear item", description = "Registra un producto y valida que la categoria exista. El id es autogenerado y no debe enviarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, id enviado en la creacion o categoria inexistente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Item item){
        if (item.getId() != null) {
            return ResponseEntity.badRequest()
                    .body("El id es autogenerado por el servidor; no debe enviarse en la creacion");
        }
        if(!categoriaClient.existe(item.getCategoriaId())){
            return ResponseEntity.badRequest()
                    .body("La categoria " + item.getCategoriaId() + " no existe");
        }
        Item guardado = repository.save(item);
        CategoriaDTO categoria = categoriaClient.buscarPorId(guardado.getCategoriaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.de(guardado, categoria));
    }
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar item", description = "Actualiza nombre, anio, precio y categoria de un producto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o categoria inexistente"),
            @ApiResponse(responseCode = "404", description = "No existe un item con el id indicado")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ItemRequest datos){
        Item item = repository.findById(id).orElse(null);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        if (!categoriaClient.existe(datos.categoriaId())) {
            return ResponseEntity.badRequest()
                    .body("La categoria " + datos.categoriaId() + " no existe");
        }
        item.setNombre(datos.nombre());
        item.setAnio(datos.anio());
        item.setPrecio(datos.precio());
        item.setCategoriaId(datos.categoriaId());

        Item guardado = repository.save(item);
        CategoriaDTO categoria = categoriaClient.buscarPorId(guardado.getCategoriaId());
        return ResponseEntity.ok(ItemResponse.de(guardado, categoria));
    }

    @GetMapping
    @Operation(summary = "Listar items", description = "Devuelve todos los productos registrados. Si no hay items, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de items obtenida correctamente")
    })
    public List<Item> listar(){
        return repository.findAll();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar item", description = "Elimina el producto indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item eliminado; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe un item con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        if(!repository.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener item por id", description = "Busca un producto por su identificador e incluye los datos de su categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un item con el id indicado")
    })
    public ResponseEntity<ItemResponse> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(item -> ResponseEntity.ok(ItemResponse.de(item, categoriaClient.buscarPorId(item.getCategoriaId()))))
                .orElse(ResponseEntity.notFound().build());
    }




}
