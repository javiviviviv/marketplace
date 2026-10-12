package com.polec.mscarrito.controller;

import com.polec.mscarrito.client.RolClient;
import com.polec.mscarrito.client.RolDTO;
import com.polec.mscarrito.dto.UsuarioResponse;
import com.polec.mscarrito.entity.carrito;
import com.polec.mscarrito.repository.carritoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carritos")
public class CarritoController {
    //inyectar el repository
    private final CarritoRepository repository;
    //inyectar el objeto rest
    private final UsuarioClient usuarioClient;


    public CarritoController(carritoRepository repository, RolClient rolClient){
        this.repository = repository;
        this.rolClient = rolClient;
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody carrito carrito){
        if(!rolClient.existe(carrito.getRolId())){
            return ResponseEntity.badRequest()
                    .body("El rol " + carrito.getRolId() + " no existe en ms-role");
        }
        carrito.setPassword(encoder.encode(carrito.getPassword()));
        carrito guardado = repository.save(carrito);
        RolDTO rol = rolClient.buscarPorId(guardado.getRolId());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(guardado, rol));
    }

    @GetMapping
    public List<carrito> listar(){
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
                .map(carrito -> ResponseEntity.ok(UsuarioResponse.de(carrito, rolClient.buscarPorId(carrito.getRolId()))))
                .orElse(ResponseEntity.notFound().build());
    }




}
