package com.bluesky.gestionvols.controller;

import com.bluesky.gestionvols.repository.FlightRepository;
import com.bluesky.gestionvols.repository.TicketRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard/hostess")
public class HostessDashboardController {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;

    public HostessDashboardController(TicketRepository ticketRepository, FlightRepository flightRepository) {
        this.ticketRepository = ticketRepository;
        this.flightRepository = flightRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de bord - Hôtesse");
        return "dashboard/hostess/index";
    }

    @GetMapping("/tickets")
    public String tickets(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("tickets", ticketRepository.findAll(
                PageRequest.of(page, 100, Sort.by("flightDate").descending())
        ));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageTitle", "Gestion des billets");
        return "dashboard/hostess/tickets";
    }

    @GetMapping("/check-ins")
    public String checkIns(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("flights", flightRepository.findByOpenRegistrationTrue());
        model.addAttribute("pageTitle", "Enregistrements");
        return "dashboard/hostess/check-ins";
    }
}
