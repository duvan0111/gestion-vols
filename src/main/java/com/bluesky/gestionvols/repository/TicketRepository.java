package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    Page<Ticket> findAll(Pageable pageable);
    
    @Query("SELECT t FROM Ticket t JOIN t.flightTickets ft JOIN ft.flight f WHERE f.id = ?1")
    List<Ticket> findByFlightId(Integer flightId);
    
    List<Ticket> findByFlightDateAfter(LocalDateTime date);
    
    List<Ticket> findByClientId(Integer clientId);
}
