package com.polec.mspago.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos",
        uniqueConstraints = @UniqueConstraint(name = "uk_pago_venta", columnNames = "venta_id"))
@NoArgsConstructor
@Getter
@Setter
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(name = "venta_id", nullable = false) private Long ventaId;

    @Column(nullable = false) private Long monto;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion", nullable = false) private LocalDateTime fechaActualizacion = LocalDateTime.now();
}
