package com.polec.mschat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
@NoArgsConstructor
@Getter
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false, updatable = false)
    @JsonIgnore
    private Conversacion conversacion;

    @Column(name = "emisor_id", nullable = false, updatable = false)
    private Long emisorId;

    @Column(nullable = false, length = 2000, updatable = false)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false, updatable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Column(nullable = false)
    private boolean leido = false;

    // Un mensaje enviado no se puede editar; solo se crea y se marca como leído
    public Mensaje(Conversacion conversacion, Long emisorId, String contenido) {
        this.conversacion = conversacion;
        this.emisorId = emisorId;
        this.contenido = contenido;
        this.fechaEnvio = LocalDateTime.now();
        this.leido = false;
    }

    public void marcarLeido() {
        this.leido = true;
    }
}
