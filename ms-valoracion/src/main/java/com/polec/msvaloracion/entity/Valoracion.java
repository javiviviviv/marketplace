package com.polec.msvaloracion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "valoraciones",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_valoracion_item_usuario",
                columnNames = {"item_id", "usuario_id"}))
@NoArgsConstructor
@Getter
@Setter
public class Valoracion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    // usuario que compró el item y escribe la reseña
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "vendedor_id", nullable = false)
    private Long vendedorId;

    @Column(nullable = false)
    private Integer puntuacion;

    @Column(length = 1000)
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
