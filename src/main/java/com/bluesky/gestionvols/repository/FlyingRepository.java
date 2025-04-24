package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Flying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlyingRepository extends JpaRepository<Flying, Integer> {
    
    List<Flying> findByFlightDateAfter(LocalDateTime date);
    
    List<Flying> findByFlightDateBetween(LocalDateTime start, LocalDateTime end);
    
    List<Flying> findByFlightDepartureAirportId(Integer airportId);
    
    List<Flying> findByFlightArrivalAirportId(Integer airportId);
}
