package com.bluesky.gestionvols.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flying")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flying {
    
    @Id
    private Integer idFlight;
    
    private LocalDateTime flightDate;
    
    @OneToOne
    @JoinColumn(name = "id_flight")
    @MapsId
    private Flight flight;
}
