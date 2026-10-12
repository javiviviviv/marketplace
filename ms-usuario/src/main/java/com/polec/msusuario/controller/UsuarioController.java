package com.polec.msusuario.controller;

import com.polec.msusuario.client.RolClient;
import com.polec.msusuario.client.RolDTO;
import com.polec.msusuario.dto.UsuarioResponse;
import com.polec.msusuario.entity.Usuario;
import com.polec.msusuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    //inyectar el repository
    private final UsuarioRepository repository;
    //inyectar el objeto rest
    private final RolClient rolClient;
    //crear un objeto para hashear mis password
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioController(UsuarioRepository repository, RolClient rolClient){
        this.repository = repository;
        this.rolClient = rolClient;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario){
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
    public List<Usuario> listar(){
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
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id){
        return repository.findById(id)
                .map(usuario -> ResponseEntity.ok(UsuarioResponse.de(usuario, rolClient.buscarPorId(usuario.getRolId()))))
                .orElse(ResponseEntity.notFound().build());
    }




}
