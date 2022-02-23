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

    @GetMapping("/")
    public String getIndexPage(@ModelAttribute("bookSearchDTO") BookSearchDTO bookSearchDTO,
                               Model model) {
        bookSearchDTO.setQuery("");
        model.addAttribute("books", bookService.findBooks(bookSearchDTO));
        return "index";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }
        return "redirect:/";
    }

    @GetMapping("/register")
    public String getRegistrationPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("user",new User());
            return "registration";
        }
        return "redirect:/";
    }

    @PostMapping("/register")
    public String registerNewUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            return "registration";
        }
        userService.registerNewUser(user, "ROLE_READER");
        return "redirect:/login";
    }

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

    @GetMapping("/messagePage")
    public String getMessagePage(@ModelAttribute("message") String message, Model model) {
        model.addAttribute("message", message);
        return "message";
    }

    @ModelAttribute
    public BookSearchDTO bookSearchDTO() {
        return new BookSearchDTO();
    }
}
