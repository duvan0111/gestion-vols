package com.bluesky.gestionvols.service;

import com.bluesky.gestionvols.model.*;
import com.bluesky.gestionvols.repository.AeroplaneRepository;
import com.bluesky.gestionvols.repository.AirportRepository;
import com.bluesky.gestionvols.repository.FlightRepository;
import com.bluesky.gestionvols.repository.FlyingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlyingRepository flyingRepository;
    private final AeroplaneRepository aeroplaneRepository;
    private final AirportRepository airportRepository;

    public FlightService(FlightRepository flightRepository, 
                         FlyingRepository flyingRepository,
                         AeroplaneRepository aeroplaneRepository,
                         AirportRepository airportRepository) {
        this.flightRepository = flightRepository;
        this.flyingRepository = flyingRepository;
        this.aeroplaneRepository = aeroplaneRepository;
        this.airportRepository = airportRepository;
    }

    public Page<Flight> getAllFlights(Pageable pageable) {
        return flightRepository.findAll(pageable);
    }
    
    public Page<Flight> getOpenRegistrationFlights(Pageable pageable) {
        return flightRepository.findByOpenRegistrationTrue(pageable);
    }

    public Optional<Flight> getFlightById(Integer id) {
        return flightRepository.findById(id);
    }

    public List<Flight> getFlightsOpenForPurchase() {
        return flightRepository.findByOpenBuyTrue();
    }

    public List<Flight> getFlightsOpenForRegistration() {
        return flightRepository.findByOpenRegistrationTrue();
    }

    public List<Flight> getUpcomingFlights() {
        return flightRepository.findByDepTimeAfter(LocalDateTime.now());
    }

    @Transactional
    public Flight createFlight(Flight flight) {
        // Vérifier si l'avion existe
        Aeroplane aeroplane = aeroplaneRepository.findById(flight.getAeroplane().getId())
                .orElseThrow(() -> new RuntimeException("Avion non trouvé"));
        
        // Vérifier si les aéroports existent
        Airport departureAirport = airportRepository.findById(flight.getDepartureAirport().getId())
                .orElseThrow(() -> new RuntimeException("Aéroport de départ non trouvé"));
        
        Airport arrivalAirport = airportRepository.findById(flight.getArrivalAirport().getId())
                .orElseThrow(() -> new RuntimeException("Aéroport d'arrivée non trouvé"));
        
        // Définir les valeurs par défaut
        if (flight.getOpenBuy() == null) {
            flight.setOpenBuy(false);
        }
        
        if (flight.getOpenRegistration() == null) {
            flight.setOpenRegistration(false);
        }
        
        // Générer automatiquement le numéro de vol si non fourni
        if (flight.getFlightNr() == null || flight.getFlightNr().isEmpty()) {
            String generatedNr = "FL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            flight.setFlightNr(generatedNr);
        }
        
        // Enregistrer le vol
        Flight savedFlight = flightRepository.save(flight);
        
        return savedFlight;
    }

    @Transactional
    public Flying scheduleFlight(Integer flightId, LocalDateTime flightDate) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        Flying flying = new Flying();
        flying.setFlight(flight);
        flying.setFlightDate(flightDate);
        
        return flyingRepository.save(flying);
    }

    @Transactional
    public Flight updateFlight(Integer id, Flight flightDetails) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        // Mettre à jour les détails du vol
        if (flightDetails.getDepTime() != null) {
            flight.setDepTime(flightDetails.getDepTime());
        }
        
        if (flightDetails.getArrTime() != null) {
            flight.setArrTime(flightDetails.getArrTime());
        }
        
        if (flightDetails.getOpenBuy() != null) {
            flight.setOpenBuy(flightDetails.getOpenBuy());
        }
        
        if (flightDetails.getOpenRegistration() != null) {
            flight.setOpenRegistration(flightDetails.getOpenRegistration());
        }
        
        if (flightDetails.getAeroplane() != null && flightDetails.getAeroplane().getId() != null) {
            Aeroplane aeroplane = aeroplaneRepository.findById(flightDetails.getAeroplane().getId())
                    .orElseThrow(() -> new RuntimeException("Avion non trouvé"));
            flight.setAeroplane(aeroplane);
        }
        
        if (flightDetails.getDepartureAirport() != null && flightDetails.getDepartureAirport().getId() != null) {
            Airport departureAirport = airportRepository.findById(flightDetails.getDepartureAirport().getId())
                    .orElseThrow(() -> new RuntimeException("Aéroport de départ non trouvé"));
            flight.setDepartureAirport(departureAirport);
        }
        
        if (flightDetails.getArrivalAirport() != null && flightDetails.getArrivalAirport().getId() != null) {
            Airport arrivalAirport = airportRepository.findById(flightDetails.getArrivalAirport().getId())
                    .orElseThrow(() -> new RuntimeException("Aéroport d'arrivée non trouvé"));
            flight.setArrivalAirport(arrivalAirport);
        }
        
        return flightRepository.save(flight);
    }

    @Transactional
    public void deleteFlight(Integer id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        // Vérifier si le vol a des billets associés
        if (flight.getFlightTickets() != null && !flight.getFlightTickets().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer un vol avec des billets associés");
        }
        
        // Supprimer le vol programmé si existant
        if (flight.getFlying() != null) {
            flyingRepository.delete(flight.getFlying());
        }
        
        flightRepository.delete(flight);
    }

    @Transactional
    public Flight openFlightForPurchase(Integer id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        flight.setOpenBuy(true);
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight closeFlightForPurchase(Integer id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        flight.setOpenBuy(false);
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight openFlightForRegistration(Integer id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        flight.setOpenRegistration(true);
        return flightRepository.save(flight);
    }

    @Transactional
    public Flight closeFlightForRegistration(Integer id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        flight.setOpenRegistration(false);
        return flightRepository.save(flight);
    }
}
