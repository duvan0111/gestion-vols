package com.bluesky.gestionvols.service;

import com.bluesky.gestionvols.model.*;
import com.bluesky.gestionvols.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final FlightTicketRepository flightTicketRepository;
    private final CheckInRepository checkInRepository;

    public TicketService(TicketRepository ticketRepository,
                        FlightRepository flightRepository,
                        ClientRepository clientRepository,
                        UserRepository userRepository,
                        FlightTicketRepository flightTicketRepository,
                        CheckInRepository checkInRepository) {
        this.ticketRepository = ticketRepository;
        this.flightRepository = flightRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.flightTicketRepository = flightTicketRepository;
        this.checkInRepository = checkInRepository;
    }

    public Page<Ticket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    public Optional<Ticket> getTicketById(Integer id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> getTicketsByFlightId(Integer flightId) {
        return ticketRepository.findByFlightId(flightId);
    }

    public List<Ticket> getTicketsByClientId(Integer clientId) {
        return ticketRepository.findByClientId(clientId);
    }

    @Transactional
    public Ticket createTicket(Integer clientId, Integer userId, List<Integer> flightIds, Float rate) {
        // Vérifier si le client existe
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        
        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Vérifier si les vols existent et sont ouverts à l'achat
        List<Flight> flights = new ArrayList<>();
        for (Integer flightId : flightIds) {
            Flight flight = flightRepository.findById(flightId)
                    .orElseThrow(() -> new RuntimeException("Vol non trouvé: " + flightId));
            
            if (!flight.getOpenBuy()) {
                throw new RuntimeException("Le vol n'est pas ouvert à l'achat: " + flightId);
            }
            
            flights.add(flight);
        }
        
        // Créer le billet
        Ticket ticket = new Ticket();
        ticket.setClient(client);
        ticket.setUser(user);
        ticket.setRate(rate);
        ticket.setFlightDate(LocalDateTime.now());
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Associer le billet aux vols
        for (Flight flight : flights) {
            FlightTicket flightTicket = new FlightTicket();
            flightTicket.setFlight(flight);
            flightTicket.setTicket(savedTicket);
            flightTicket.setStatus("CONFIRMED");
            
            flightTicketRepository.save(flightTicket);
        }
        
        return savedTicket;
    }

    @Transactional
    public void cancelTicket(Integer ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Billet non trouvé"));
        
        // Vérifier si le vol a déjà eu lieu
        boolean flightAlreadyDeparted = false;
        for (FlightTicket flightTicket : ticket.getFlightTickets()) {
            if (flightTicket.getFlight().getDepTime().isBefore(LocalDateTime.now())) {
                flightAlreadyDeparted = true;
                break;
            }
        }
        
        if (flightAlreadyDeparted) {
            throw new RuntimeException("Impossible d'annuler un billet pour un vol qui a déjà eu lieu");
        }
        
        // Vérifier si le billet a déjà un enregistrement
        if (ticket.getCheckIn() != null) {
            checkInRepository.delete(ticket.getCheckIn());
        }
        
        // Supprimer les associations avec les vols
        flightTicketRepository.deleteAll(ticket.getFlightTickets());
        
        // Supprimer le billet
        ticketRepository.delete(ticket);
    }

    @Transactional
    public Ticket updateTicketStatus(Integer ticketId, Integer flightId, String status) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Billet non trouvé"));
        
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        
        // Trouver l'association ticket-vol
        FlightTicket flightTicket = null;
        for (FlightTicket ft : ticket.getFlightTickets()) {
            if (ft.getFlight().getId().equals(flightId)) {
                flightTicket = ft;
                break;
            }
        }
        
        if (flightTicket == null) {
            throw new RuntimeException("Le billet n'est pas associé à ce vol");
        }
        
        flightTicket.setStatus(status);
        flightTicketRepository.save(flightTicket);
        
        return ticket;
    }
}
