package ua.training.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.training.model.Abonnement;
import ua.training.model.dto.input.LibrarianSearchDTO;
import ua.training.model.dto.input.ReadingRoomDTO;
import ua.training.services.OperationsService;
import ua.training.services.OrderService;
import ua.training.services.UserService;

@Controller
@RequestMapping("/librarian")
@SessionAttributes("librarianSearchDTO")
public class LibrarianController {

    private final OrderService orderService;
    private final UserService userService;
    private final OperationsService operationsService;

    @Autowired
    public LibrarianController(OrderService orderService, UserService userService, OperationsService operationsService) {
        this.orderService = orderService;
        this.userService = userService;
        this.operationsService = operationsService;
    }


    @GetMapping
    public String getLibrarianPage() {
        return "/librarian/librarian";
    }

    @GetMapping("/orders")
    public String getCurrentOrders(@ModelAttribute("librarianSearchDTO") LibrarianSearchDTO librarianSearchDTO,
                                   Model model) {
        model.addAttribute("requests", orderService.getAllPendingRequests(librarianSearchDTO));
        System.out.println("/orders controller");
        return "/librarian/librarianOrders";
    }

    @PostMapping("/orders/confirmation/{id}")
    public String getOrderConfirmation(@RequestParam("action") String action,
                                       @ModelAttribute("librarianSearchDTO") LibrarianSearchDTO librarianSearchDTO,
                                       @PathVariable("id") Long requestId,
                                       Model model) {
        switch (action) {
            case "abonnement":
                model.addAttribute("request", orderService.findRequestById(requestId));
                model.addAttribute("abonnement", new Abonnement());
                return "/librarian/librarianOrdersAbonnement";
            case "readingHall":
                operationsService.setupBookToBeGivenToReadingRoom(requestId);
                return "redirect:/librarian/orders";
            case "reject":
                operationsService.rejectRequestOfUser(requestId);
                return "redirect:/librarian/orders";
        }
        return "redirect:/librarian/orders";
    }

    @PostMapping("/addOrderToAbonnement")
    public String addOrderToAbonnement(@ModelAttribute("abonnement") Abonnement abonnement,
                                       @RequestParam("requestId") Long requestId) {
        operationsService.fillDataOfRequestToBeAddedToAbonnement(abonnement, requestId);
        return "redirect:/librarian/orders";
    }

    @GetMapping("/readers")
    public String getReaders(@ModelAttribute("librarianSearchDTO") LibrarianSearchDTO librarianSearchDTO,
                             Model model) {
        model.addAttribute("readers", userService.getUsersByRolePerPage(librarianSearchDTO,
                "ROLE_READER"));
        return "/librarian/librarianReaders";
    }

    @GetMapping("/abonnement/{id}")
    public String getReaderAbonnement(@PathVariable("id") Long id,
                                      @ModelAttribute("librarianSearchDTO") LibrarianSearchDTO librarianSearchDTO,
                                      Model model) {
        model.addAttribute("contents", orderService.getUserAbonnement(id, librarianSearchDTO));
        return "/librarian/librarianReaderAbonnement";
    }



    @PostMapping("/changeAbonnementStatus")
    public String changeOrderStatus(@RequestParam("action") String action,
                                    @RequestParam("userId") Long userId,
                                    @RequestParam("bookId") Long bookId) {

        orderService.changeOrderStatus(action, userId, bookId);
        return "redirect:/librarian/abonnement/" + userId;
    }

    @GetMapping("/readingRoom")
    public String getReadingRoomPage(@ModelAttribute("readingRoomDTO") ReadingRoomDTO readingRoomDTO,
                                     Model model) {
        model.addAttribute("readingRoomPage", orderService.getBooksHandedOverToReadingRoom(readingRoomDTO));
        return "/librarian/librarianReadingRoom";
    }

    @PostMapping("/readingRoom")
    public String removeBookFromReadingRoom(@RequestParam("action") String action,
                                            @RequestParam("userId") Long userId,
                                            @RequestParam("bookId") Long bookId) {

        operationsService.removeRequestedBooks(action, userId, bookId);
        return "redirect:/librarian/readingRoom";
    }

    @ModelAttribute("librarianSearchDTO")
    public LibrarianSearchDTO librarianSearchDTO() {
        return new LibrarianSearchDTO();
    }

    @ModelAttribute("readingRoomDTO")
    public ReadingRoomDTO readingRoomDTO() {
        return new ReadingRoomDTO();
    }

}
