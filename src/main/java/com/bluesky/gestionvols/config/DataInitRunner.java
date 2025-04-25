package com.bluesky.gestionvols.config;

import com.bluesky.gestionvols.service.DataInitializationService;
import com.bluesky.gestionvols.repository.TicketRepository;
import com.bluesky.gestionvols.repository.FlightTicketRepository;
import com.bluesky.gestionvols.repository.CheckInRepository;
import com.bluesky.gestionvols.repository.CompagnieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

// @Component disabled: use DataInitializationService.initializeData
public class DataInitRunner implements CommandLineRunner {

    private final DataInitializationService dataService;
    private final TicketRepository ticketRepo;
    private final FlightTicketRepository flightTicketRepo;
    private final CheckInRepository checkInRepo;
    private final CompagnieRepository compagnieRepo;

    public DataInitRunner(DataInitializationService dataService,
                          TicketRepository ticketRepo,
                          FlightTicketRepository flightTicketRepo,
                          CheckInRepository checkInRepo,
                          CompagnieRepository compagnieRepo) {
        this.dataService = dataService;
        this.ticketRepo = ticketRepo;
        this.flightTicketRepo = flightTicketRepo;
        this.checkInRepo = checkInRepo;
        this.compagnieRepo = compagnieRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Base data
        if (compagnieRepo.count() == 0) {
            System.out.println("Initialisation des données de base...");
            dataService.initializeCompagnies();
            dataService.initializeAirports();
            dataService.initializeAeroplanes();
            dataService.initializeFlights();
            dataService.initializeClients();
            System.out.println("Données de base initialisées.");
        }
        // Users
        System.out.println("Vérification des utilisateurs par défaut...");
        dataService.initializeUsers();
        // Tickets
        System.out.println("Réinitialisation des billets de test...");
        flightTicketRepo.deleteAll();
        ticketRepo.deleteAll();
        dataService.initializeTickets();
        // Check-ins
        System.out.println("Réinitialisation des enregistrements de test...");
        checkInRepo.deleteAll();
        dataService.initializeCheckIns();
    }
}
