package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "flights_has_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "flights_id")
    private Flight flight;
    
    @ManyToOne
    @JoinColumn(name = "tickets_id")
    private Ticket ticket;
    
    private String status;
}
