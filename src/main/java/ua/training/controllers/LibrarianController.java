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

    /**
     * Order service instance
     */
    private final OrderService orderService;

    /**
     * User service instance
     */
    private final UserService userService;

    /**
     * Operations Service instance
     */
    private final OperationsService operationsService;

    @Autowired
    public LibrarianController(OrderService orderService, UserService userService, OperationsService operationsService) {
        this.orderService = orderService;
        this.userService = userService;
        this.operationsService = operationsService;
    }


    /**
     * Method to get librarian account view
     * @return - corresponding view
     */
    @GetMapping
    public String getLibrarianPage() {
        return "/librarian/librarian";
    }

    /**
     * Method to retrieve pending orders in system and display them on the page
     * @param librarianSearchDTO - DTO used to implement pagination
     * @param model - Spring model to pass data to the view
     * @return - view with orders of current page
     */
    @GetMapping("/orders")
    public String getCurrentOrders(@ModelAttribute("librarianSearchDTO") LibrarianSearchDTO librarianSearchDTO,
                                   Model model) {
        model.addAttribute("requests", orderService.getAllPendingRequests(librarianSearchDTO));
        System.out.println("/orders controller");
        return "/librarian/librarianOrders";
    }

    /**
     * Method to apply chosen action to the order that was chosen by user
     * @param action - String literal with the action name
     * @param requestId - id of order to which the action needs to be applied
     * @param model - Spring model to pass the data to the view
     * @return - view of additional abonnement parameters or redirect to the orders view
     */
    @PostMapping("/orders/confirmation/{id}")
    public String getOrderConfirmation(@RequestParam("action") String action,
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

    /**
     * Method to add order to abonnement
     * @param abonnement - instance with all info about order
     * @param requestId - id of order in table of pending requests
     * @return - redirect to the show orders view
     */
    @PostMapping("/addOrderToAbonnement")
    public String addOrderToAbonnement(@ModelAttribute("abonnement") Abonnement abonnement,
                                       @RequestParam("requestId") Long requestId) {
        operationsService.fillDataOfRequestToBeAddedToAbonnement(abonnement, requestId);
        return "redirect:/librarian/orders";
    }

    /**
     * Method to get all readers view
     * @param librarianSearchDTO - DTO used to implement pagination
     * @param model - Spring model to pass the data to the view
     * @return - view of readers on current page
     */
    @GetMapping("/readers")
    public String getReaders(@ModelAttribute("librarianSearchDTO") LibrarianSearchDTO librarianSearchDTO,
                             Model model) {
        model.addAttribute("readers", userService.getUsersByRolePerPage(librarianSearchDTO,
                "ROLE_READER"));
        return "/librarian/librarianReaders";
    }

    /**
     * Method to get abonnement of user with given id
     * @param id - id of user which abonnement needs to be displayed
     * @param librarianSearchDTO - DTO used to implement pagination
     * @param model - Spring model instance to pass the data to the view
     * @return - orders in abonnement view
     */
    @GetMapping("/abonnement/{id}")
    public String getReaderAbonnement(@PathVariable("id") Long id,
                                      @ModelAttribute("librarianSearchDTO") LibrarianSearchDTO librarianSearchDTO,
                                      Model model) {
        model.addAttribute("contents", orderService.getUserAbonnement(id, librarianSearchDTO));
        return "/librarian/librarianReaderAbonnement";
    }

    /**
     * Method to change status of order in abonnement
     * @param action - action to be applied
     * @param userId - id of user whom the abonnement belongs to
     * @param bookId - id of book in order which status is to be changed
     * @return - redirect to the show abonnement view
     */
    @PostMapping("/changeAbonnementStatus")
    public String changeOrderStatus(@RequestParam("action") String action,
                                    @RequestParam("userId") Long userId,
                                    @RequestParam("bookId") Long bookId) {

        orderService.changeOrderStatus(action, userId, bookId);
        return "redirect:/librarian/abonnement/" + userId;
    }

    /**
     * Method to display all orders that are in reading room at the moment
     * @param readingRoomDTO - DTO to implement pagination
     * @param model - Spring model instance to pass the data to the view
     * @return - all orders in reading room view
     */
    @GetMapping("/readingRoom")
    public String getReadingRoomPage(@ModelAttribute("readingRoomDTO") ReadingRoomDTO readingRoomDTO,
                                     Model model) {
        model.addAttribute("readingRoomPage", orderService.getBooksHandedOverToReadingRoom(readingRoomDTO));
        return "/librarian/librarianReadingRoom";
    }

    /**
     * Method to remove a book from reading room
     * @param action - action 'remove all' or 'remove one'
     * @param userId - id of user from order to be removed, if exists
     * @param bookId - id of book from order to be removed, if exists
     * @return - redirect to the reading room view
     */
    @PostMapping("/readingRoom")
    public String removeBookFromReadingRoom(@RequestParam("action") String action,
                                            @RequestParam("userId") Long userId,
                                            @RequestParam("bookId") Long bookId) {

        operationsService.removeRequestedBooks(action, userId, bookId);
        return "redirect:/librarian/readingRoom";
    }

    /**
     * Method to get default DTO if no other was provided
     * @return - DTO for pagination
     */
    @ModelAttribute("librarianSearchDTO")
    public LibrarianSearchDTO librarianSearchDTO() {
        return new LibrarianSearchDTO();
    }

    /**
     * Method to get default DTO if no other was provided
     * @return - DTO for pagination
     */
    @ModelAttribute("readingRoomDTO")
    public ReadingRoomDTO readingRoomDTO() {
        return new ReadingRoomDTO();
    }

}
