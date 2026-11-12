package com.polec.msenvio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "envios",
        uniqueConstraints = @UniqueConstraint(name = "uk_envio_venta", columnNames = "venta_id"))
@NoArgsConstructor
@Getter
@Setter
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pedido", unique = true, length = 30)
    private String numeroPedido;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "comprador_id", nullable = false)
    private Long compradorId;

    @Column(name = "repartidor_id", nullable = false)
    private Long repartidorId;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEnvio estado = EstadoEnvio.EN_PREPARACION;

    @Column(name = "posicion", nullable = false)
    private Integer posicion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
}
