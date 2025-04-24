package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Integer> {
    
    List<CheckIn> findByTicketFlightTicketsFlightId(Integer flightId);
    
    int countByTicketFlightTicketsFlightId(Integer flightId);
}
