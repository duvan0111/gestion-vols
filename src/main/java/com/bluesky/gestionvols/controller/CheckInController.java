package com.bluesky.gestionvols.controller;

import com.bluesky.gestionvols.model.CheckIn;
import com.bluesky.gestionvols.model.Flight;
import com.bluesky.gestionvols.model.FlightTicket;
import com.bluesky.gestionvols.model.Ticket;
import com.bluesky.gestionvols.service.CheckInService;
import com.bluesky.gestionvols.service.FlightService;
import com.bluesky.gestionvols.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/hostess/check-in")
public class CheckInController {

    private final CheckInService checkInService;
    private final FlightService flightService;
    private final TicketService ticketService;
    private static final Logger logger = LoggerFactory.getLogger(CheckInController.class);

    public CheckInController(CheckInService checkInService, 
                            FlightService flightService, 
                            TicketService ticketService) {
        this.checkInService = checkInService;
        this.flightService = flightService;
        this.ticketService = ticketService;
    }

    @GetMapping("/flight/{flightId}")
    public String flightCheckIns(@PathVariable Integer flightId, Model model) {
        Optional<Flight> flightOpt = flightService.getFlightById(flightId);
        
        if (!flightOpt.isPresent()) {
            return "redirect:/hostess/check-in?error=Vol non trouvé";
        }
        
        Flight flight = flightOpt.get();
        
        if (!flight.getOpenRegistration()) {
            return "redirect:/hostess/check-in?error=Ce vol n'est pas ouvert à l'enregistrement";
        }
        
        List<CheckIn> checkIns = checkInService.getCheckInsByFlightId(flightId);
        List<Ticket> availableTickets = checkInService.getTicketsForCheckIn(flightId);
        int availableSeats = checkInService.getAvailableSeatsCount(flightId);
        
        model.addAttribute("flight", flight);
        model.addAttribute("checkIns", checkIns);
        model.addAttribute("availableTickets", availableTickets);
        model.addAttribute("availableSeats", availableSeats);
        
        return "dashboard/hostess/check-in/flight";
    }

    @GetMapping("/create")
    public String showCreateForm(@RequestParam(required = false) Integer ticketId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (ticketId == null) {
            return "dashboard/hostess/check-in/create";
        }
        Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);
        if (!ticketOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Billet non trouvé");
            return "redirect:/hostess/check-in/create";
        }
        Ticket ticket = ticketOpt.get();
        model.addAttribute("ticket", ticket);
        var flights = ticket.getFlightTickets().stream().map(FlightTicket::getFlight).collect(Collectors.toList());
        model.addAttribute("flights", flights);
        return "dashboard/hostess/check-in/create";
    }

    @PostMapping("/create")
    public String createCheckIn(@RequestParam Integer flightId,
                               @RequestParam Integer ticketId,
                               @RequestParam Integer luggageNr,
                               @RequestParam(required = false) Integer seat,
                               RedirectAttributes redirectAttributes) {
        logger.info("createCheckIn called with ticketId={}, flightId={}, luggageNr={}, seat={}", ticketId, flightId, luggageNr, seat);
        try {
            CheckIn checkIn = checkInService.createCheckIn(ticketId, flightId, luggageNr);
            redirectAttributes.addFlashAttribute("success", "Enregistrement créé avec succès");
            return "redirect:/hostess/check-in/flight/" + flightId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de l'enregistrement: " + e.getMessage());
            return "redirect:/hostess/check-in/create?ticketId=" + ticketId;
        }
    }

    @GetMapping("/edit/{ticketId}")
    public String showEditForm(@PathVariable Integer ticketId, Model model) {
        Optional<CheckIn> checkInOpt = checkInService.getCheckInByTicketId(ticketId);
        
        if (!checkInOpt.isPresent()) {
            return "redirect:/hostess/check-in?error=Enregistrement non trouvé";
        }
        
        CheckIn checkIn = checkInOpt.get();
        Integer flightId = checkIn.getTicket().getFlightTickets().iterator().next().getFlight().getId();
        
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("flightId", flightId);
        model.addAttribute("availableSeats", checkInService.getAvailableSeatsCount(flightId));
        
        return "dashboard/hostess/check-in/edit";
    }

    @PostMapping("/edit")
    public String updateCheckIn(@RequestParam Integer ticketId,
                               @RequestParam Integer luggageNr,
                               @RequestParam Integer seat,
                               RedirectAttributes redirectAttributes) {
        try {
            CheckIn checkIn = checkInService.updateCheckIn(ticketId, luggageNr, seat);
            Integer flightId = checkIn.getTicket().getFlightTickets().iterator().next().getFlight().getId();
            
            redirectAttributes.addFlashAttribute("success", "Enregistrement mis à jour avec succès");
            return "redirect:/hostess/check-in/flight/" + flightId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour de l'enregistrement: " + e.getMessage());
            return "redirect:/hostess/check-in/edit/" + ticketId;
        }
    }

    @GetMapping("/delete/{ticketId}")
    public String deleteCheckIn(@PathVariable Integer ticketId, RedirectAttributes redirectAttributes) {
        try {
            Optional<CheckIn> checkInOpt = checkInService.getCheckInByTicketId(ticketId);
            
            if (!checkInOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Enregistrement non trouvé");
                return "redirect:/hostess/check-in";
            }
            
            CheckIn checkIn = checkInOpt.get();
            Integer flightId = checkIn.getTicket().getFlightTickets().iterator().next().getFlight().getId();
            
            checkInService.deleteCheckIn(ticketId);
            
            redirectAttributes.addFlashAttribute("success", "Enregistrement supprimé avec succès");
            return "redirect:/hostess/check-in";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'enregistrement: " + e.getMessage());
            return "redirect:/hostess/check-in";
        }
    }

    @GetMapping("/passenger-list/{flightId}")
    public String passengerList(@PathVariable Integer flightId, Model model) {
        Optional<Flight> flightOpt = flightService.getFlightById(flightId);
        
        if (!flightOpt.isPresent()) {
            return "redirect:/hostess/check-in?error=Vol non trouvé";
        }
        
        Flight flight = flightOpt.get();
        List<CheckIn> checkIns = checkInService.getCheckInsByFlightId(flightId);
        
        model.addAttribute("flight", flight);
        model.addAttribute("checkIns", checkIns);
        
        return "dashboard/hostess/check-in/passenger-list";
    }

    @GetMapping
    public String listAll(Model model) {
        List<CheckIn> checkIns = checkInService.getAllCheckIns();
        model.addAttribute("checkIns", checkIns);
        return "dashboard/hostess/check-in/index";
    }

    @GetMapping("/{ticketId:\\d+}")
    public String showDetail(@PathVariable Integer ticketId, Model model, RedirectAttributes redirectAttributes) {
        Optional<CheckIn> checkInOpt = checkInService.getCheckInByTicketId(ticketId);
        if (!checkInOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Enregistrement non trouvé");
            return "redirect:/hostess/check-in";
        }
        model.addAttribute("checkIn", checkInOpt.get());
        return "dashboard/hostess/check-in/detail";
    }
}
