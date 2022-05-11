package ua.training.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.training.model.dto.input.BookAbonnementDTO;
import ua.training.model.dto.input.BookSearchDTO;
import ua.training.services.BookService;
import ua.training.services.OperationsService;
import ua.training.services.OrderService;

@Controller
@RequestMapping("/reader")
@SessionAttributes("bookSearchDTO")
public class ReaderController {

    /**
     * Operations service instance
     */
    private final OperationsService operationsService;

    /**
     * Book service instance
     */
    private final BookService bookService;


    public ReaderController(OperationsService operationsService, BookService bookService) {
        this.operationsService = operationsService;
        this.bookService = bookService;
    }

    /**
     * Method to get reader account page
     * @return - reader account view
     */
    @GetMapping
    public String getReaderPage() {
        return "/reader/reader";
    }

    /**
     * Method to get reader abonnement page
     * @param bookAbonnementDTO - DTO used to implement pagination
     * @param model - Spring model used to pass the data to the view
     * @return - view of current page with orders of user
     */
    @GetMapping("/getAbonnement")
    public String getUserAbonnement(@ModelAttribute("bookAbonnementDTO")BookAbonnementDTO bookAbonnementDTO,
                                    Model model) {
        model.addAttribute("abonnements", operationsService.findCurrentUserAbonnement(bookAbonnementDTO));
        return "/reader/readerAbonnement";
    }

    /**
     * Method to get search result page
     * @param bookSearchDTO - DTO used to implement pagination
     * @param model - Spring model used to pass the data to the view
     * @return - search page view
     */
    @GetMapping("/search")
    public String search(@ModelAttribute("bookSearchDTO") BookSearchDTO bookSearchDTO,
                         Model model) {
        model.addAttribute("books", bookService.findBooks(bookSearchDTO));
        return "/reader/search";
    }

    /**
     * Method used for creating an order by user
     * @param id - id of book to be ordered
     * @param redirectAttributes - RedirectAttributes instance used to pass an attribute to other controller method
     * @return
     */
    @PostMapping("/orderBook/{id}")
    public String orderBook(@PathVariable("id")Long id, RedirectAttributes redirectAttributes) {
        operationsService.createOrderOnGivenBook(id);
        redirectAttributes.addFlashAttribute("message", "Your order was successfully created, please wait for the librarian to " +
                "confirm it");
        return "redirect:/messagePage";
    }

    /**
     * Method used to provide a DTO with default data if no other was provided
     * @return - BookSearchDTO instance
     */
    @ModelAttribute
    public BookSearchDTO bookSearchDTO() {
        return new BookSearchDTO();
    }
}
