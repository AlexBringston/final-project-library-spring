package ua.training.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import ua.training.model.User;
import ua.training.model.dto.input.BookSearchDTO;
import ua.training.services.BookService;
import ua.training.services.UserService;

import javax.validation.Valid;
import java.util.Set;

@Controller
@SessionAttributes("bookSearchDTO")
public class BaseController {

    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public BaseController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Method for retrieving a main page, which contains a catalog of books, available in library
     * @param bookSearchDTO - DTO used for sorting and pagination
     * @param model - Spring model to pass data to a view
     * @return - index page's view with a catalog
     */
    @GetMapping("/")
    public String getIndexPage(@ModelAttribute("bookSearchDTO") BookSearchDTO bookSearchDTO,
                               Model model) {
        bookSearchDTO.setQuery("");
        model.addAttribute("books", bookService.findBooks(bookSearchDTO));
        return "index";
    }

    /**
     * Method for retrieving a login page
     * @return - login view, if user is not logged, redirect to index page otherwise
     */
    @GetMapping("/login")
    public String getLoginPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }
        return "redirect:/";
    }

    /**
     * Method for retrieving a registration page
     * @param model - Spring model to pass a user object which will collect the data from form
     * @return - registration view, if user is not logged, redirect to index view otherwise
     */
    @GetMapping("/register")
    public String getRegistrationPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("user",new User());
            return "registration";
        }
        return "redirect:/";
    }

    /**
     * Method which collects data from registration form, validates and saves user to database
     * @param user - user object with data binded from form fields to appropriate class fields
     * @param bindingResult - Spring binding result object, which stores validation errors
     * @return - registration view if errors were found, redirect to login page otherwise
     */
    @PostMapping("/register")
    public String registerNewUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            return "registration";
        }
        userService.registerNewUser(user, "ROLE_READER");
        return "redirect:/login";
    }

    /**
     * Method which retrieves user role and redirects them to the corresponding page of their cabinet
     * @return - redirect to the correct role page, or to the index page, if user is not logged
     */
    @GetMapping("/account")
    public String getCabinet() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin";
        }
        else if (roles.contains("ROLE_LIBRARIAN")) {
            return "redirect:/librarian";
        }
        else if (roles.contains("ROLE_READER")) {
            return "redirect:/reader";
        }
        else {
            return "redirect:/";
        }
    }

    /**
     * Method which retrieves a message page, with text received as a model attribute
     * @param message - message to display
     * @param model - Spring model to pass a message to the view
     * @return - message view
     */
    @GetMapping("/messagePage")
    public String getMessagePage(@ModelAttribute("message") String message, Model model) {
        model.addAttribute("message", message);
        return "message";
    }

    /**
     * Method which is used to return default DTO (with parameters of sorting and page number) if no other was provided
     * before
     * @return - new instance of BookSearchDTO
     */
    @ModelAttribute
    public BookSearchDTO bookSearchDTO() {
        return new BookSearchDTO();
    }
}
