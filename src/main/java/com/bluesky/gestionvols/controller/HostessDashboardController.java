package com.bluesky.gestionvols.controller;

import com.bluesky.gestionvols.repository.FlightRepository;
import com.bluesky.gestionvols.repository.TicketRepository;
import com.bluesky.gestionvols.repository.ClientRepository;
import com.bluesky.gestionvols.repository.AirportRepository;
import com.bluesky.gestionvols.repository.FlightTicketRepository;
import com.bluesky.gestionvols.model.Client;
import com.bluesky.gestionvols.model.Ticket;
import com.bluesky.gestionvols.model.Flight;
import com.bluesky.gestionvols.model.FlightTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard/hostess")
public class HostessDashboardController {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final ClientRepository clientRepository;
    private final AirportRepository airportRepository;
    private final FlightTicketRepository flightTicketRepository;

    public HostessDashboardController(TicketRepository ticketRepository, FlightRepository flightRepository, ClientRepository clientRepository, AirportRepository airportRepository, FlightTicketRepository flightTicketRepository) {
        this.ticketRepository = ticketRepository;
        this.flightRepository = flightRepository;
        this.clientRepository = clientRepository;
        this.airportRepository = airportRepository;
        this.flightTicketRepository = flightTicketRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de bord - Hôtesse");
        return "dashboard/hostess/index";
    }

    @GetMapping("/tickets")
    public String tickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFilter,
            @RequestParam(required = false) Float minPriceFilter,
            @RequestParam(required = false) Float maxPriceFilter,
            @RequestParam(required = false) String passengerFilter,
            Model model) {
        Specification<Ticket> spec = Specification.where(null);
        if (dateFilter != null) {
            LocalDateTime start = dateFilter.atStartOfDay();
            LocalDateTime end = dateFilter.atTime(LocalTime.MAX);
            spec = spec.and((root, query, cb) -> cb.between(root.get("flightDate"), start, end));
        }
        if (minPriceFilter != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rate"), minPriceFilter));
        }
        if (maxPriceFilter != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("rate"), maxPriceFilter));
        }
        if (passengerFilter != null && !passengerFilter.isEmpty()) {
            String pf = passengerFilter.toLowerCase();
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("client").get("firstName")), "%" + pf + "%"),
                    cb.like(cb.lower(root.get("client").get("lastName")), "%" + pf + "%")
            ));
        }
        Page<Ticket> tickets = ticketRepository.findAll(spec, PageRequest.of(page, 10, org.springframework.data.domain.Sort.by("flightDate").descending()));
        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("dateFilter", dateFilter);
        model.addAttribute("minPriceFilter", minPriceFilter);
        model.addAttribute("maxPriceFilter", maxPriceFilter);
        model.addAttribute("passengerFilter", passengerFilter);
        model.addAttribute("totalTickets", tickets.getTotalElements());
        model.addAttribute("pageTitle", "Gestion des billets");
        return "dashboard/hostess/tickets";
    }

    @GetMapping("/tickets/add")
    public String showAddTicketForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("flights", flightRepository.findByOpenBuyTrue());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("pageTitle", "Ajouter un billet");
        return "dashboard/hostess/add-ticket";
    }

    @PostMapping("/tickets")
    public String createTicket(@ModelAttribute Ticket ticket, @RequestParam("flightIds") List<Integer> flightIds) {
        List<Flight> selectedFlights = flightRepository.findAllById(flightIds);
        // Set ticket date/time to first flight's departure
        ticket.setFlightDate(selectedFlights.get(0).getDepTime());
        // Auto-generate price per flight segment
        float basePrice = 100.0f;
        float totalPrice = selectedFlights.size() * basePrice;
        ticket.setRate(totalPrice);
        // Persist ticket
        ticketRepository.save(ticket);
        // Associate flights
        for (Flight f : selectedFlights) {
            FlightTicket ft = new FlightTicket();
            ft.setFlight(f);
            ft.setTicket(ticket);
            ft.setStatus("CONFIRMED");
            flightTicketRepository.save(ft);
        }
        return "redirect:/dashboard/hostess/tickets";
    }

    @GetMapping("/tickets/{id}/edit")
    public String showEditTicketForm(@PathVariable Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID billet invalide :" + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("flights", flightRepository.findByOpenBuyTrue());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("pageTitle", "Modifier un billet");
        return "dashboard/hostess/edit-ticket";
    }

    @PostMapping("/tickets/{id}")
    public String updateTicket(@PathVariable Integer id, @ModelAttribute Ticket formTicket, @RequestParam("flightIds") List<Integer> flightIds) {
        Ticket t = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID billet invalide :" + id));
        // Remove existing flights
        List<FlightTicket> existing = flightTicketRepository.findByTicketId(id);
        existing.forEach(ft -> flightTicketRepository.deleteByFlightIdAndTicketId(ft.getFlight().getId(), id));
        // Associate selected flights and auto-generate date and price
        List<Flight> selectedFlights = flightRepository.findAllById(flightIds);
        t.setFlightDate(selectedFlights.get(0).getDepTime());
        t.setRate(selectedFlights.size() * 100.0f);
        t.setClient(formTicket.getClient());
        ticketRepository.save(t);
        selectedFlights.forEach(f -> {
            FlightTicket ft = new FlightTicket();
            ft.setFlight(f);
            ft.setTicket(t);
            ft.setStatus("CONFIRMED");
            flightTicketRepository.save(ft);
        });
        return "redirect:/dashboard/hostess/tickets";
    }

    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable Integer id) {
        ticketRepository.deleteById(id);
        return "redirect:/dashboard/hostess/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Integer id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID billet invalide :" + id));
        model.addAttribute("ticket", ticket);
        model.addAttribute("pageTitle", "Détail du billet");
        return "dashboard/hostess/ticket-detail";
    }

    @GetMapping("/check-ins")
    public String checkIns(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("flights", flightRepository.findByOpenRegistrationTrue());
        model.addAttribute("pageTitle", "Enregistrements");
        return "dashboard/hostess/check-ins";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("pageTitle", "Gestion des clients");
        return "dashboard/hostess/clients";
    }

    @PostMapping("/clients")
    public String createClient(@ModelAttribute Client client) {
        clientRepository.save(client);
        return "redirect:/dashboard/hostess/clients";
    }

    @PostMapping("/clients/{id}")
    public String updateClient(@PathVariable Integer id, @ModelAttribute Client formClient) {
        Client c = clientRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("ID client invalide :" + id));
        c.setLastName(formClient.getLastName());
        c.setFirstName(formClient.getFirstName());
        c.setEmail(formClient.getEmail());
        c.setPhone(formClient.getPhone());
        clientRepository.save(c);
        return "redirect:/dashboard/hostess/clients";
    }

    @PostMapping("/clients/{id}/delete")
    public String deleteClient(@PathVariable Integer id) {
        clientRepository.deleteById(id);
        return "redirect:/dashboard/hostess/clients";
    }
}
