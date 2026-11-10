package com.polec.mschat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversaciones")
@NoArgsConstructor
@Getter
@Setter
public class Conversacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario1_id", nullable = false)
    private Long usuario1Id;

    @Column(name = "usuario2_id", nullable = false)
    private Long usuario2Id;

    // item sobre el que se consulta (opcional)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_actividad", nullable = false)
    private LocalDateTime ultimaActividad = LocalDateTime.now();

    @OneToMany(mappedBy = "conversacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajes = new ArrayList<>();
}
