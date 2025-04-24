package com.bluesky.gestionvols.repository;

import com.bluesky.gestionvols.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Integer> {
    
    Optional<Airport> findByCode(String code);
    
    List<Airport> findByCity(String city);
    
    List<Airport> findByCountry(String country);
    
    List<Airport> findByCityAndCountry(String city, String country);
}
