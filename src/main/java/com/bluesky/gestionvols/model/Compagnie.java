package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "compagnies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compagnie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    private String country;
    
    @OneToMany(mappedBy = "compagnie")
    private List<Aeroplane> aeroplanes;
}
