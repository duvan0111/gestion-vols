package com.bluesky.gestionvols.controller;

import com.bluesky.gestionvols.model.Aeroplane;
import com.bluesky.gestionvols.model.Airport;
import com.bluesky.gestionvols.model.Flight;
import com.bluesky.gestionvols.repository.AeroplaneRepository;
import com.bluesky.gestionvols.repository.AirportRepository;
import com.bluesky.gestionvols.repository.CompagnieRepository;
import com.bluesky.gestionvols.repository.FlightRepository;
import com.bluesky.gestionvols.service.FlightService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final FlightRepository flightRepository;
    private final AeroplaneRepository aeroplaneRepository;
    private final CompagnieRepository compagnieRepository;
    private final AirportRepository airportRepository;
    private final FlightService flightService;

    public ManagerDashboardController(FlightRepository flightRepository, 
                                     AeroplaneRepository aeroplaneRepository,
                                     CompagnieRepository compagnieRepository,
                                     AirportRepository airportRepository,
                                     FlightService flightService) {
        this.flightRepository = flightRepository;
        this.aeroplaneRepository = aeroplaneRepository;
        this.compagnieRepository = compagnieRepository;
        this.airportRepository = airportRepository;
        this.flightService = flightService;
    }

    @GetMapping
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("flights", flightRepository.findAll(PageRequest.of(page, 10, Sort.by("depTime").descending())));
        model.addAttribute("aeroplanes", aeroplaneRepository.findAll());
        model.addAttribute("compagnies", compagnieRepository.findAll());
        model.addAttribute("pageTitle", "Tableau de bord - Responsable de vols");
        return "dashboard/manager/index";
    }

    @PostMapping("/flights/edit/{id}")
    public String editFlight(@PathVariable Integer id,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime depTime,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrTime,
                             @RequestParam Integer departureAirportId,
                             @RequestParam Integer arrivalAirportId,
                             @RequestParam Integer aeroplaneId,
                             @RequestParam(required = false) Boolean openBuy,
                             @RequestParam(required = false) Boolean openRegistration,
                             RedirectAttributes redirectAttributes) {
        try {
            Flight flightDetails = new Flight();
            flightDetails.setDepTime(depTime);
            flightDetails.setArrTime(arrTime);
            flightDetails.setOpenBuy(openBuy != null && openBuy);
            flightDetails.setOpenRegistration(openRegistration != null && openRegistration);
            Aeroplane plane = new Aeroplane(); plane.setId(aeroplaneId);
            flightDetails.setAeroplane(plane);
            Airport dep = new Airport(); dep.setId(departureAirportId);
            flightDetails.setDepartureAirport(dep);
            Airport arr = new Airport(); arr.setId(arrivalAirportId);
            flightDetails.setArrivalAirport(arr);
            flightService.updateFlight(id, flightDetails);
            redirectAttributes.addFlashAttribute("success", "Vol modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification du vol: " + e.getMessage());
        }
        return "redirect:/manager/flights";
    }

    @GetMapping("/flights/{id}")
    public String viewFlightDetails(@PathVariable Integer id, Model model) {
        Flight flight = flightService.getFlightById(id)
                .orElseThrow(() -> new RuntimeException("Vol non trouvé"));
        model.addAttribute("flight", flight);
        return "dashboard/manager/flight-details";
    }

    @GetMapping("/flights")
    public String flights(Model model, 
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) String flightNr,
                        @RequestParam(required = false) String departureAirport,
                        @RequestParam(required = false) String arrivalAirport,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("depTime").descending());
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59) : null;
        Page<Flight> flights = flightRepository.findByFilters(
                flightNr, departureAirport, arrivalAirport, fromDateTime, toDateTime, pageable);
        model.addAttribute("flights", flights);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", flights.getTotalPages());
        model.addAttribute("airports", airportRepository.findAll());
        model.addAttribute("aeroplanes", aeroplaneRepository.findAll());
        return "dashboard/manager/flights";
    }
    
    @PostMapping("/flights/add")
    public String addFlight(@ModelAttribute Flight flight,
                          @RequestParam Integer departureAirportId,
                          @RequestParam Integer arrivalAirportId,
                          @RequestParam Integer aeroplaneId,
                          RedirectAttributes redirectAttributes) {
        try {
            Aeroplane plane = new Aeroplane(); plane.setId(aeroplaneId);
            flight.setAeroplane(plane);
            Airport dep = new Airport(); dep.setId(departureAirportId);
            flight.setDepartureAirport(dep);
            Airport arr = new Airport(); arr.setId(arrivalAirportId);
            flight.setArrivalAirport(arr);
            flightService.createFlight(flight);
            redirectAttributes.addFlashAttribute("success", "Vol ajouté avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout du vol: " + e.getMessage());
        }
        return "redirect:/manager/flights";
    }

    @GetMapping("/flights/delete/{id}")
    public String deleteFlight(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            flightService.deleteFlight(id);
            redirectAttributes.addFlashAttribute("success", "Vol supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/manager/flights";
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
