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

    private final OperationsService operationsService;
    private final BookService bookService;
    private final OrderService orderService;

    public ReaderController(OperationsService operationsService, BookService bookService, OrderService orderService) {
        this.operationsService = operationsService;
        this.bookService = bookService;
        this.orderService = orderService;
    }

    @GetMapping
    public String getReaderPage() {
        return "/reader/reader";
    }

    @GetMapping("/getAbonnement")
    public String getUserAbonnement(@ModelAttribute("bookAbonnementDTO")BookAbonnementDTO bookAbonnementDTO,
                                    Model model) {
        model.addAttribute("abonnements", operationsService.findCurrentUserAbonnement(bookAbonnementDTO));
        return "/reader/readerAbonnement";
    }

    @GetMapping("/search")
    public String search(@ModelAttribute("bookSearchDTO") BookSearchDTO bookSearchDTO,
                         Model model) {
        model.addAttribute("books", bookService.findBooks(bookSearchDTO));
        return "/reader/search";
    }
    @PostMapping("/orderBook/{id}")
    public String orderBook(@PathVariable("id")Long id, RedirectAttributes redirectAttributes) {
        operationsService.createOrderOnGivenBook(id);
        redirectAttributes.addFlashAttribute("message", "Your order was successfully created, please wait for the librarian to " +
                "confirm it");
        return "redirect:/messagePage";
    }

    @ModelAttribute
    public BookSearchDTO bookSearchDTO() {
        return new BookSearchDTO();
    }
}
