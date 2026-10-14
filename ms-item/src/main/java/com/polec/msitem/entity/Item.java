package com.polec.msitem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items")
@NoArgsConstructor
@Getter
@Setter
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column (nullable = false) private String nombre;
    @Min(1800) private Long anio;     //no menor a 1800
    @Min(0)private Long precio;     //no menor a 0
    @Column (nullable = false) private Long vendedorId;
    @Column (nullable = false) private Long categoriaId;

}
