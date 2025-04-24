package com.bluesky.gestionvols.controller;

import com.bluesky.gestionvols.repository.AeroplaneRepository;
import com.bluesky.gestionvols.repository.CompagnieRepository;
import com.bluesky.gestionvols.repository.FlightRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/dashboard/manager")
public class ManagerDashboardController {

    private final FlightRepository flightRepository;
    private final AeroplaneRepository aeroplaneRepository;
    private final CompagnieRepository compagnieRepository;

    public ManagerDashboardController(FlightRepository flightRepository, 
                                     AeroplaneRepository aeroplaneRepository,
                                     CompagnieRepository compagnieRepository) {
        this.flightRepository = flightRepository;
        this.aeroplaneRepository = aeroplaneRepository;
        this.compagnieRepository = compagnieRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Tableau de bord - Responsable de vols");
        return "dashboard/manager/index";
    }

    @GetMapping("/flights")
    public String flights(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("flights", flightRepository.findAll(
                PageRequest.of(page, 100, Sort.by("depTime").descending())
        ));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageTitle", "Gestion des vols");
        return "dashboard/manager/flights";
    }

    @GetMapping("/aeroplanes")
    public String aeroplanes(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("aeroplanes", aeroplaneRepository.findAll(
                PageRequest.of(page, 100, Sort.by("id").ascending())
        ));
        model.addAttribute("compagnies", compagnieRepository.findAll());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageTitle", "Gestion des avions");
        return "dashboard/manager/aeroplanes";
    }

    @GetMapping("/compagnies")
    public String compagnies(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("compagnies", compagnieRepository.findAll(
                PageRequest.of(page, 100, Sort.by("name").ascending())
        ));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageTitle", "Gestion des compagnies");
        return "dashboard/manager/compagnies";
    }

    @GetMapping("/passenger-list")
    public String passengerList(@RequestParam Integer flightId, Model model) {
        model.addAttribute("flight", flightRepository.findById(flightId).orElseThrow());
        model.addAttribute("pageTitle", "Liste des passagers");
        return "dashboard/manager/passenger-list";
    }
}
