package com.bluesky.gestionvols.service;

import com.bluesky.gestionvols.model.CheckIn;
import com.bluesky.gestionvols.model.Flight;
import com.bluesky.gestionvols.model.FlightTicket;
import com.bluesky.gestionvols.model.Ticket;
import com.bluesky.gestionvols.repository.CheckInRepository;
import com.bluesky.gestionvols.repository.FlightRepository;
import com.bluesky.gestionvols.repository.FlightTicketRepository;
import com.bluesky.gestionvols.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final FlightTicketRepository flightTicketRepository;

    public CheckInService(CheckInRepository checkInRepository, 
                         TicketRepository ticketRepository,
                         FlightRepository flightRepository,
                         FlightTicketRepository flightTicketRepository) {
        this.checkInRepository = checkInRepository;
        this.ticketRepository = ticketRepository;
        this.flightRepository = flightRepository;
        this.flightTicketRepository = flightTicketRepository;
    }

    public List<CheckIn> getAllCheckIns() {
        return checkInRepository.findAll();
    }

    public Optional<CheckIn> getCheckInByTicketId(Integer ticketId) {
        return checkInRepository.findById(ticketId);
    }

    public List<CheckIn> getCheckInsByFlightId(Integer flightId) {
        return checkInRepository.findByTicketFlightTicketsFlightId(flightId);
    }

    @Transactional
    public CheckIn createCheckIn(Integer ticketId, Integer flightId, Integer luggageNr) {
        // Vérifier si le billet existe
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Billet non trouvé"));
        
        // Vérifier si le vol existe et est ouvert à l'enregistrement
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        if (!flight.getOpenRegistration()) {
            throw new RuntimeException("Le vol n'est pas ouvert à l'enregistrement");
        }
        
        // Vérifier si le billet est associé au vol
        boolean ticketAssociatedWithFlight = false;
        for (FlightTicket flightTicket : ticket.getFlightTickets()) {
            if (flightTicket.getFlight().getId().equals(flightId)) {
                ticketAssociatedWithFlight = true;
                break;
            }
        }
        
        if (!ticketAssociatedWithFlight) {
            throw new RuntimeException("Le billet n'est pas associé à ce vol");
        }
        
        // Vérifier si un enregistrement existe déjà pour ce billet
        if (checkInRepository.existsById(ticketId)) {
            throw new RuntimeException("Un enregistrement existe déjà pour ce billet");
        }
        
        // Créer l'enregistrement
        CheckIn checkIn = new CheckIn();
        checkIn.setTicket(ticket);
        checkIn.setLuggageNr(luggageNr);
        checkIn.setCheckTime(LocalDateTime.now());
        
        // Attribuer un siège aléatoire (pour simplifier)
        Random random = new Random();
        checkIn.setSeat(random.nextInt(flight.getAeroplane().getCapacity()) + 1);
        
        return checkInRepository.save(checkIn);
    }

    @Transactional
    public CheckIn updateCheckIn(Integer ticketId, Integer luggageNr, Integer seat) {
        CheckIn checkIn = checkInRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Enregistrement non trouvé"));
        
        if (luggageNr != null) {
            checkIn.setLuggageNr(luggageNr);
        }
        
        if (seat != null) {
            // Vérifier si le siège est disponible
            List<CheckIn> checkIns = getCheckInsByFlightId(
                    checkIn.getTicket().getFlightTickets().iterator().next().getFlight().getId());
            
            for (CheckIn existingCheckIn : checkIns) {
                if (existingCheckIn.getSeat().equals(seat) && !existingCheckIn.getIdTicket().equals(ticketId)) {
                    throw new RuntimeException("Le siège est déjà occupé");
                }
            }
            
            checkIn.setSeat(seat);
        }
        
        return checkInRepository.save(checkIn);
    }

    @Transactional
    public void deleteCheckIn(Integer ticketId) {
        if (!checkInRepository.existsById(ticketId)) {
            throw new RuntimeException("Enregistrement non trouvé");
        }
        
        checkInRepository.deleteById(ticketId);
    }

    @Transactional
    public List<Ticket> getTicketsForCheckIn(Integer flightId) {
        // Vérifier si le vol existe et est ouvert à l'enregistrement
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        if (!flight.getOpenRegistration()) {
            throw new RuntimeException("Le vol n'est pas ouvert à l'enregistrement");
        }
        
        // Récupérer tous les billets associés à ce vol qui n'ont pas encore d'enregistrement
        List<Ticket> tickets = ticketRepository.findByFlightId(flightId);
        tickets.removeIf(ticket -> checkInRepository.existsById(ticket.getId()));
        
        return tickets;
    }

    public int getAvailableSeatsCount(Integer flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        int totalSeats = flight.getAeroplane().getCapacity();
        int occupiedSeats = checkInRepository.countByTicketFlightTicketsFlightId(flightId);
        
        return totalSeats - occupiedSeats;
    }
}
