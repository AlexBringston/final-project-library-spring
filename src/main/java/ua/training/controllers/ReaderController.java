package ua.training.controllers;

import com.sun.deploy.net.HttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.training.model.dto.input.BookSearchDTO;
import ua.training.services.BookService;
import ua.training.services.OperationsService;

@Controller
@RequestMapping("/reader")
@SessionAttributes("bookSearchDTO")
public class ReaderController {

    private final OperationsService operationsService;

    private final BookService bookService;

    public ReaderController(OperationsService operationsService, BookService bookService) {
        this.operationsService = operationsService;
        this.bookService = bookService;
    }

    @GetMapping
    public String getReaderPage() {
        return "/reader/reader";
    }

    @GetMapping("/getAbonnement")
    public String getUserAbonnement() {
        return "/reader/readerAbonnement";
    }

    @GetMapping("/search")
    public String search(@ModelAttribute("bookSearchDTO") BookSearchDTO bookSearchDTO,
                         Model model) {
        model.addAttribute("booksDTO", bookService.findBooks(bookSearchDTO));
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
