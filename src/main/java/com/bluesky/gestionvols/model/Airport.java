package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "airports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String name;
    
    private String city;
    
    private String state;
    
    @OneToMany(mappedBy = "departureAirport")
    private List<Flight> departureFlights;
    
    @OneToMany(mappedBy = "arrivalAirport")
    private List<Flight> arrivalFlights;
}
