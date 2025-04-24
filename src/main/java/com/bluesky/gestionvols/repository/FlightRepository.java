package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {
    
    Page<Flight> findByOpenRegistrationTrue(Pageable pageable);
    Page<Flight> findAll(Pageable pageable);
    List<Flight> findByDepTimeAfter(LocalDateTime date);
    List<Flight> findByOpenBuyTrue();
    List<Flight> findByOpenRegistrationTrue();
    Page<Flight> findByFlightNrContainingIgnoreCase(String flightNr, Pageable pageable);

    @Query("SELECT f FROM Flight f WHERE (:flightNr IS NULL OR LOWER(f.flightNr) LIKE LOWER(CONCAT('%', :flightNr, '%'))) " +
           "AND (:departureAirport IS NULL OR LOWER(f.departureAirport.code) LIKE LOWER(CONCAT('%', :departureAirport, '%'))) " +
           "AND (:arrivalAirport IS NULL OR LOWER(f.arrivalAirport.code) LIKE LOWER(CONCAT('%', :arrivalAirport, '%'))) " +
           "AND (:fromDate IS NULL OR f.depTime >= :fromDate) " +
           "AND (:toDate IS NULL OR f.depTime <= :toDate)")
    Page<Flight> findByFilters(@Param("flightNr") String flightNr,
                              @Param("departureAirport") String departureAirport,
                              @Param("arrivalAirport") String arrivalAirport,
                              @Param("fromDate") java.time.LocalDateTime fromDate,
                              @Param("toDate") java.time.LocalDateTime toDate,
                              Pageable pageable);
}
