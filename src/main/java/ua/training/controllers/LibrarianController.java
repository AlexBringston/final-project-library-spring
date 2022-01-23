package ua.training.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.training.services.OrderService;

@Controller
@RequestMapping("/librarian")
public class LibrarianController {

    private final OrderService orderService;

    public LibrarianController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String getLibrarianPage() {
        return "/librarian/librarian";
    }

    @GetMapping("/orders")
    public String getCurrentOrders(Model model) {
        model.addAttribute("requests", orderService.getAllRequests());
        return "/librarian/librarianOrders";
    }

    @GetMapping("/orders/confirmation")
    public String getOrderConfirmationForm() {
        return "/librarian/librarianOrdersAbonnement";
    }

    @GetMapping("/abonnement")
    public String getReaderAbonnement() {
        return "/librarian/librarianReaderAbonnement";
    }

    @GetMapping("/readers")
    public String getReaders() {
        return "/librarian/librarianReaders";
    }
}
