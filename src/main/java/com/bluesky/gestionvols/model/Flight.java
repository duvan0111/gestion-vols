package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "flights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String flightNr;
    
    private Boolean openBuy;
    
    private Boolean openRegistration;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime depTime;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrTime;
    
    @ManyToOne
    @JoinColumn(name = "aeroplane_id")
    private Aeroplane aeroplane;
    
    @ManyToOne
    @JoinColumn(name = "dep_airports_id")
    private Airport departureAirport;
    
    @ManyToOne
    @JoinColumn(name = "arr_airports_id")
    private Airport arrivalAirport;
    
    @OneToMany(mappedBy = "flight")
    private List<FlightTicket> flightTickets;
    
    @OneToOne(mappedBy = "flight")
    private Flying flying;
}
