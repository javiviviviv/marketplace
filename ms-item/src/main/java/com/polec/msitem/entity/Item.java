package com.polec.msitem.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items")
@NoArgsConstructor
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY,
            description = "Identificador autogenerado por el servidor; no debe enviarse en la creacion")
    private Long id;

    @NotBlank(message = "El nombre del item es obligatorio")
    @Column (nullable = false)
    private String nombre;

    @Min(value = 1800, message = "El anio debe ser mayor o igual a 1800")
    private Long anio;

    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    private Long precio;

    @NotNull(message = "El vendedorId es obligatorio")
    @Column (nullable = false)
    private Long vendedorId;

    @NotNull(message = "El categoriaId es obligatorio")
    @Column (nullable = false)
    private Long categoriaId;

}
