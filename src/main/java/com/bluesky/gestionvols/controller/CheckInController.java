package com.bluesky.gestionvols.controller;

import com.bluesky.gestionvols.model.CheckIn;
import com.bluesky.gestionvols.model.Flight;
import com.bluesky.gestionvols.model.Ticket;
import com.bluesky.gestionvols.service.CheckInService;
import com.bluesky.gestionvols.service.FlightService;
import com.bluesky.gestionvols.service.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hostess/check-in")
public class CheckInController {

    private final CheckInService checkInService;
    private final FlightService flightService;
    private final TicketService ticketService;

    public CheckInController(CheckInService checkInService, 
                            FlightService flightService, 
                            TicketService ticketService) {
        this.checkInService = checkInService;
        this.flightService = flightService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public String checkInDashboard(Model model, 
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "100") int size) {
        Page<Flight> flights = flightService.getOpenRegistrationFlights(PageRequest.of(page, size));
        model.addAttribute("flights", flights);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", flights.getTotalPages());
        return "dashboard/hostess/check-in/index";
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

    @GetMapping("/create/{flightId}/{ticketId}")
    public String showCheckInForm(@PathVariable Integer flightId, 
                                 @PathVariable Integer ticketId, 
                                 Model model) {
        Optional<Flight> flightOpt = flightService.getFlightById(flightId);
        Optional<Ticket> ticketOpt = ticketService.getTicketById(ticketId);
        
        if (!flightOpt.isPresent() || !ticketOpt.isPresent()) {
            return "redirect:/hostess/check-in/flight/" + flightId + "?error=Vol ou billet non trouvé";
        }
        
        Flight flight = flightOpt.get();
        Ticket ticket = ticketOpt.get();
        
        model.addAttribute("flight", flight);
        model.addAttribute("ticket", ticket);
        model.addAttribute("availableSeats", checkInService.getAvailableSeatsCount(flightId));
        
        return "dashboard/hostess/check-in/create";
    }

    @PostMapping("/create")
    public String createCheckIn(@RequestParam Integer flightId,
                               @RequestParam Integer ticketId,
                               @RequestParam Integer luggageNr,
                               @RequestParam(required = false) Integer seat,
                               RedirectAttributes redirectAttributes) {
        try {
            CheckIn checkIn = checkInService.createCheckIn(ticketId, flightId, luggageNr);
            redirectAttributes.addFlashAttribute("success", "Enregistrement créé avec succès");
            return "redirect:/hostess/check-in/flight/" + flightId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de l'enregistrement: " + e.getMessage());
            return "redirect:/hostess/check-in/create/" + flightId + "/" + ticketId;
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
            return "redirect:/hostess/check-in/flight/" + flightId;
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
}
