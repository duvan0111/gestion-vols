package com.bluesky.gestionvols.service;

import com.bluesky.gestionvols.model.*;
import com.bluesky.gestionvols.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataInitializationService {

    private final CompagnieRepository compagnieRepository;
    private final AeroplaneRepository aeroplaneRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final FlightTicketRepository flightTicketRepository;
    private final CheckInRepository checkInRepository;
    private final FlyingRepository flyingRepository;

    public DataInitializationService(CompagnieRepository compagnieRepository,
                                    AeroplaneRepository aeroplaneRepository,
                                    AirportRepository airportRepository,
                                    FlightRepository flightRepository,
                                    ClientRepository clientRepository,
                                    UserRepository userRepository,
                                    TicketRepository ticketRepository,
                                    FlightTicketRepository flightTicketRepository,
                                    CheckInRepository checkInRepository,
                                    FlyingRepository flyingRepository) {
        this.compagnieRepository = compagnieRepository;
        this.aeroplaneRepository = aeroplaneRepository;
        this.airportRepository = airportRepository;
        this.flightRepository = flightRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.flightTicketRepository = flightTicketRepository;
        this.checkInRepository = checkInRepository;
        this.flyingRepository = flyingRepository;
    }

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Vérifier si des données existent déjà
            if (compagnieRepository.count() > 0) {
                System.out.println("Les données de test existent déjà. Initialisation ignorée.");
                return;
            }

            System.out.println("Initialisation des données de test...");
            
            initializeCompagnies();
            initializeAirports();
            initializeAeroplanes();
            initializeFlights();
            initializeClients();
            initializeTickets();
            initializeCheckIns();
            
            System.out.println("Initialisation des données de test terminée.");
        };
    }

    @Transactional
    public void initializeCompagnies() {
        List<Compagnie> compagnies = new ArrayList<>();
        
        compagnies.add(createCompagnie("Air France", "FR"));
        compagnies.add(createCompagnie("Lufthansa", "DE"));
        compagnies.add(createCompagnie("British Airways", "GB"));
        compagnies.add(createCompagnie("Emirates", "AE"));
        compagnies.add(createCompagnie("Qatar Airways", "QA"));
        
        compagnieRepository.saveAll(compagnies);
        System.out.println("Compagnies initialisées : " + compagnies.size());
    }

    private Compagnie createCompagnie(String name, String country) {
        Compagnie compagnie = new Compagnie();
        compagnie.setName(name);
        compagnie.setCountry(country);
        return compagnie;
    }

    @Transactional
    public void initializeAirports() {
        List<Airport> airports = new ArrayList<>();
        
        airports.add(createAirport("CDG", "Paris Charles de Gaulle", "Paris", "France", "Europe"));
        airports.add(createAirport("ORY", "Paris Orly", "Paris", "France", "Europe"));
        airports.add(createAirport("LHR", "London Heathrow", "London", "United Kingdom", "Europe"));
        airports.add(createAirport("JFK", "John F. Kennedy", "New York", "United States", "North America"));
        airports.add(createAirport("LAX", "Los Angeles International", "Los Angeles", "United States", "North America"));
        airports.add(createAirport("DXB", "Dubai International", "Dubai", "United Arab Emirates", "Asia"));
        airports.add(createAirport("HND", "Tokyo Haneda", "Tokyo", "Japan", "Asia"));
        airports.add(createAirport("SYD", "Sydney Airport", "Sydney", "Australia", "Oceania"));
        
        airportRepository.saveAll(airports);
        System.out.println("Aéroports initialisés : " + airports.size());
    }

    private Airport createAirport(String code, String name, String city, String country, String continent) {
        Airport airport = new Airport();
        airport.setCode(code);
        airport.setName(name);
        airport.setCity(city);
        airport.setCountry(country);
        airport.setContinent(continent);
        return airport;
    }

    @Transactional
    public void initializeAeroplanes() {
        List<Aeroplane> aeroplanes = new ArrayList<>();
        List<Compagnie> compagnies = compagnieRepository.findAll();
        
        // Pour chaque compagnie, créer quelques avions
        for (Compagnie compagnie : compagnies) {
            aeroplanes.add(createAeroplane("Airbus A320", 180, compagnie));
            aeroplanes.add(createAeroplane("Boeing 737", 160, compagnie));
            
            if ("Air France".equals(compagnie.getName()) || "Emirates".equals(compagnie.getName())) {
                aeroplanes.add(createAeroplane("Airbus A380", 525, compagnie));
                aeroplanes.add(createAeroplane("Boeing 787", 330, compagnie));
            }
        }
        
        aeroplaneRepository.saveAll(aeroplanes);
        System.out.println("Avions initialisés : " + aeroplanes.size());
    }

    private Aeroplane createAeroplane(String model, int capacity, Compagnie compagnie) {
        Aeroplane aeroplane = new Aeroplane();
        aeroplane.setModel(model);
        aeroplane.setCapacity(capacity);
        aeroplane.setCompagnie(compagnie);
        return aeroplane;
    }

    @Transactional
    public void initializeFlights() {
        List<Flight> flights = new ArrayList<>();
        List<Airport> airports = airportRepository.findAll();
        List<Aeroplane> aeroplanes = aeroplaneRepository.findAll();
        
        // Créer des vols entre différents aéroports
        for (int i = 0; i < airports.size(); i++) {
            for (int j = 0; j < airports.size(); j++) {
                if (i != j) {  // Éviter les vols d'un aéroport vers lui-même
                    Airport departure = airports.get(i);
                    Airport arrival = airports.get(j);
                    
                    // Choisir un avion aléatoire
                    Aeroplane aeroplane = aeroplanes.get(new Random().nextInt(aeroplanes.size()));
                    
                    // Créer des vols pour les prochains jours
                    for (int day = 0; day < 7; day++) {
                        flights.add(createFlight(
                            departure, 
                            arrival, 
                            aeroplane,
                            LocalDateTime.now().plusDays(day).withHour(8 + i % 12).withMinute(0),
                            LocalDateTime.now().plusDays(day).withHour(10 + (i + j) % 12).withMinute(30)
                        ));
                    }
                }
            }
        }
        
        flightRepository.saveAll(flights);
        System.out.println("Vols initialisés : " + flights.size());
        
        // Créer des instances de vol (Flying)
        List<Flying> flyings = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getDepTime().isAfter(LocalDateTime.now())) {
                flyings.add(createFlying(flight, flight.getDepTime()));
            }
        }
        
        flyingRepository.saveAll(flyings);
        System.out.println("Instances de vol initialisées : " + flyings.size());
    }

    private Flight createFlight(Airport departure, Airport arrival, Aeroplane aeroplane, 
                               LocalDateTime depTime, LocalDateTime arrTime) {
        Flight flight = new Flight();
        flight.setFlightNr("BS" + (1000 + new Random().nextInt(9000)));
        flight.setDepartureAirport(departure);
        flight.setArrivalAirport(arrival);
        flight.setAeroplane(aeroplane);
        flight.setDepTime(depTime);
        flight.setArrTime(arrTime);
        flight.setOpenBuy(true);
        flight.setOpenRegistration(depTime.minusHours(24).isBefore(LocalDateTime.now()));
        return flight;
    }

    private Flying createFlying(Flight flight, LocalDateTime flightDate) {
        Flying flying = new Flying();
        flying.setFlight(flight);
        flying.setFlightDate(flightDate);
        return flying;
    }

    @Transactional
    public void initializeClients() {
        List<Client> clients = new ArrayList<>();
        
        clients.add(createClient("Dupont", "Jean", "jean.dupont@example.com", "+33123456789"));
        clients.add(createClient("Martin", "Sophie", "sophie.martin@example.com", "+33234567890"));
        clients.add(createClient("Smith", "John", "john.smith@example.com", "+44123456789"));
        clients.add(createClient("Johnson", "Emily", "emily.johnson@example.com", "+44234567890"));
        clients.add(createClient("Müller", "Hans", "hans.muller@example.com", "+49123456789"));
        clients.add(createClient("Schmidt", "Anna", "anna.schmidt@example.com", "+49234567890"));
        clients.add(createClient("Rossi", "Marco", "marco.rossi@example.com", "+39123456789"));
        clients.add(createClient("Ferrari", "Laura", "laura.ferrari@example.com", "+39234567890"));
        clients.add(createClient("Garcia", "Carlos", "carlos.garcia@example.com", "+34123456789"));
        clients.add(createClient("Lopez", "Maria", "maria.lopez@example.com", "+34234567890"));
        
        clientRepository.saveAll(clients);
        System.out.println("Clients initialisés : " + clients.size());
    }

    private Client createClient(String lastName, String firstName, String email, String phone) {
        Client client = new Client();
        client.setLastName(lastName);
        client.setFirstName(firstName);
        client.setEmail(email);
        client.setPhone(phone);
        return client;
    }

    @Transactional
    public void initializeTickets() {
        List<Ticket> tickets = new ArrayList<>();
        List<Client> clients = clientRepository.findAll();
        List<Flight> flights = flightRepository.findAll();
        List<User> users = userRepository.findAll();
        
        // Filtrer les utilisateurs pour ne garder que les managers
        List<User> managers = new ArrayList<>();
        for (User user : users) {
            if ("MANAGER".equals(user.getRole())) {
                managers.add(user);
            }
        }
        
        if (managers.isEmpty()) {
            System.out.println("Aucun manager trouvé pour créer des billets");
            return;
        }
        
        Random random = new Random();
        
        // Pour chaque client, créer quelques billets
        for (Client client : clients) {
            // Nombre aléatoire de billets par client (1 à 3)
            int nbTickets = 1 + random.nextInt(3);
            
            for (int i = 0; i < nbTickets; i++) {
                // Choisir un vol aléatoire
                Flight flight = flights.get(random.nextInt(flights.size()));
                
                // Créer le billet si le vol est ouvert à l'achat
                if (flight.getOpenBuy()) {
                    User manager = managers.get(random.nextInt(managers.size()));
                    Ticket ticket = createTicket(client, manager, random.nextFloat() * 1000);
                    tickets.add(ticket);
                    
                    // Associer le billet au vol
                    FlightTicket flightTicket = createFlightTicket(ticket, flight);
                    flightTicketRepository.save(flightTicket);
                }
            }
        }
        
        ticketRepository.saveAll(tickets);
        System.out.println("Billets initialisés : " + tickets.size());
    }

    private Ticket createTicket(Client client, User user, float rate) {
        Ticket ticket = new Ticket();
        ticket.setClient(client);
        ticket.setUser(user);
        ticket.setRate(rate);
        ticket.setFlightDate(LocalDateTime.now());
        return ticket;
    }

    private FlightTicket createFlightTicket(Ticket ticket, Flight flight) {
        FlightTicket flightTicket = new FlightTicket();
        flightTicket.setTicket(ticket);
        flightTicket.setFlight(flight);
        flightTicket.setStatus("CONFIRMED");
        return flightTicket;
    }

    @Transactional
    public void initializeCheckIns() {
        List<CheckIn> checkIns = new ArrayList<>();
        List<Ticket> tickets = ticketRepository.findAll();
        Random random = new Random();
        
        for (Ticket ticket : tickets) {
            // Vérifier si le billet a au moins un vol
            if (!ticket.getFlightTickets().isEmpty()) {
                // Récupérer le premier vol associé au billet
                Flight flight = ticket.getFlightTickets().iterator().next().getFlight();
                
                // Vérifier si le vol est ouvert à l'enregistrement
                if (flight.getOpenRegistration()) {
                    // 70% de chance de créer un enregistrement
                    if (random.nextDouble() < 0.7) {
                        CheckIn checkIn = createCheckIn(ticket, random.nextInt(3), random.nextInt(flight.getAeroplane().getCapacity()) + 1);
                        checkIns.add(checkIn);
                    }
                }
            }
        }
        
        checkInRepository.saveAll(checkIns);
        System.out.println("Enregistrements initialisés : " + checkIns.size());
    }

    private CheckIn createCheckIn(Ticket ticket, int luggageNr, int seat) {
        CheckIn checkIn = new CheckIn();
        checkIn.setTicket(ticket);
        checkIn.setLuggageNr(luggageNr);
        checkIn.setSeat(seat);
        checkIn.setCheckTime(LocalDateTime.now());
        return checkIn;
    }
}
