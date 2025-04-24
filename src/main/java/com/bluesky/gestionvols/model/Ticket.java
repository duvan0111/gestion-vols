package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private Float rate;
    
    private LocalDateTime flightDate;
    
    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "clients_id")
    private Client client;
    
    @OneToOne(mappedBy = "ticket")
    private CheckIn checkIn;
    
    @OneToMany(mappedBy = "ticket")
    private List<FlightTicket> flightTickets;
}
