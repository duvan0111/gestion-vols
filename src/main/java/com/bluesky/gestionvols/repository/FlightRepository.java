package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {
    Page<Flight> findAll(Pageable pageable);
    List<Flight> findByDepTimeAfter(LocalDateTime date);
    List<Flight> findByOpenBuyTrue();
    List<Flight> findByOpenRegistrationTrue();
}
