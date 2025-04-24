package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.FlightTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightTicketRepository extends JpaRepository<FlightTicket, Integer> {
    
    List<FlightTicket> findByFlightId(Integer flightId);
    
    List<FlightTicket> findByTicketId(Integer ticketId);
    
    List<FlightTicket> findByStatus(String status);
    
    void deleteByFlightIdAndTicketId(Integer flightId, Integer ticketId);
}
