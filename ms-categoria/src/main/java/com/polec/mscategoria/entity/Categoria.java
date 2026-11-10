package com.polec.mscategoria.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorias")
@NoArgsConstructor
@Getter
@Setter
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY,
            description = "Identificador autogenerado por el servidor; no debe enviarse en la creacion")
    private Long id;

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(max = 40, message = "El nombre de la categoria no puede superar los 40 caracteres")
    @Column(nullable = false, unique = true, length = 40)
    private String nombre;

    @Size(max = 150, message = "La descripcion no puede superar los 150 caracteres")
    @Column(length = 150)
    private String descripcion;

}
