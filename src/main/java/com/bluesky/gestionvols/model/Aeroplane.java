package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "aeroplanes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aeroplane {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String model;
    
    private Integer capacity;
    
    @ManyToOne
    @JoinColumn(name = "compagnies_id")
    private Compagnie compagnie;
    
    @OneToMany(mappedBy = "aeroplane")
    private List<Flight> flights;
}
