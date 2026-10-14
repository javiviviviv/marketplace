package com.polec.mscarrito.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@NoArgsConstructor
@Getter
@Setter
    public class Carrito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false) private Long usuarioId;

    private Long total = 0L;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true) private List<CarritoItem> items = new ArrayList<>();
}
