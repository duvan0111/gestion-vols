package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkIns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckIn {
    
    @Id
    private Integer idTicket;
    
    private Integer seat;
    
    private Integer luggageNr;
    
    private LocalDateTime checkTime;
    
    @OneToOne
    @JoinColumn(name = "id_ticket")
    @MapsId
    private Ticket ticket;
}
