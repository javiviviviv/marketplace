package com.polec.msusuario.controller;

import com.polec.msusuario.client.RolClient;
import com.polec.msusuario.client.RolDTO;
import com.polec.msusuario.dto.CambiarPasswordRequest;
import com.polec.msusuario.dto.UsuarioRequest;
import com.polec.msusuario.dto.UsuarioResponse;
import com.polec.msusuario.entity.Usuario;
import com.polec.msusuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestion de usuarios y validacion de roles")
public class UsuarioController {
    //inyectar el repository
    private final UsuarioRepository repository;
    //inyectar el objeto rest
    private final RolClient rolClient;
    //crear un objeto para hashear las password
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioController(UsuarioRepository repository, RolClient rolClient){
        this.repository = repository;
        this.rolClient = rolClient;
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un usuario, hashea su password y valida que el rol exista. El id es autogenerado y no debe enviarse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos, id enviado en la creacion o rol inexistente"),
            @ApiResponse(responseCode = "500", description = "Error interno o correo ya registrado")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Usuario usuario){
        if (usuario.getId() != null) {
            return ResponseEntity.badRequest()
                    .body("El id es autogenerado por el servidor; no debe enviarse en la creacion");
        }
        if(!rolClient.existe(usuario.getRolId())){
            return ResponseEntity.badRequest()
                    .body("El rol " + usuario.getRolId() + " no existe");
        }
        usuario.setPassword(encoder.encode(usuario.getPassword()));
        Usuario guardado = repository.save(usuario);
        RolDTO rol = rolClient.buscarPorId(guardado.getRolId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(guardado, rol));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados. Si no hay usuarios, retorna un arreglo vacio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente")
    })
    public List<Usuario> listar(){
        return repository.findAll();
    }

    // actualizar el usuario (datos fuera de la password)
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza nombre, correo y rol. No modifica la password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o rol inexistente"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con el id indicado")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest datos){
        Usuario usuario = repository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        if (!rolClient.existe(datos.rolId())) {
            return ResponseEntity.badRequest()
                    .body("El rol " + datos.rolId() + " no existe");
        }
        usuario.setNombre(datos.nombre());
        usuario.setCorreo(datos.correo());
        usuario.setRolId(datos.rolId());

        Usuario guardado = repository.save(usuario);
        return ResponseEntity.ok(UsuarioResponse.de(guardado, rolClient.buscarPorId(guardado.getRolId())));
    }
    // password se debe volver a hashear
    @PutMapping("/{id}/password")
    @Operation(summary = "Cambiar password", description = "Hashea y actualiza la password del usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password actualizada; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "400", description = "Password vacia o datos invalidos"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con el id indicado")
    })
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id,
                                                @Valid @RequestBody CambiarPasswordRequest passwordNueva){
        Usuario usuario = repository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        usuario.setPassword(encoder.encode(passwordNueva.password()));
        repository.save(usuario);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina el usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado; la respuesta no tiene contenido"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con el id indicado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        if(!repository.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id", description = "Busca un usuario por su identificador e incluye los datos de su rol.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con el id indicado")
    })
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(usuario -> ResponseEntity.ok(UsuarioResponse.de(usuario, rolClient.buscarPorId(usuario.getRolId()))))
                .orElse(ResponseEntity.notFound().build());
    }




}
