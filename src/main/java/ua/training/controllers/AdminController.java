package ua.training.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.training.model.Book;
import ua.training.model.User;
import ua.training.model.dto.input.AdminBookDTO;
import ua.training.model.dto.input.BookDTO;
import ua.training.model.dto.input.SearchUserDTO;
import ua.training.model.dto.output.UserDTO;
import ua.training.services.BookService;
import ua.training.services.UserService;

import javax.validation.Valid;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final UserService userService;

    @Autowired
    public AdminController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping
    public String getAdminPage() {
        return "/admin/admin";
    }

    @GetMapping("/showBooks")
    public String showAllBooks(@ModelAttribute("adminBookDTO") AdminBookDTO adminBookDTO,
                               Model model) {
        model.addAttribute("books", bookService.getAllAvailableBooks(adminBookDTO));
        return "/admin/adminBooks";
    }

    @GetMapping("/addBook")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "/admin/adminNewBook";
    }

    @PostMapping("/addBook")
    public String addBook(@Valid @ModelAttribute("book") Book book,
                          BindingResult bindingResult) {
        boolean errors = bindingResult.hasErrors();
        if (!bookService.checkIfAuthorExists(book)) {
            bindingResult.rejectValue("mainAuthor.name", "404", "There is no author with given values");
            errors = true;
        }
        if (!bookService.checkIfPublisherExists(book)) {
            bindingResult.rejectValue("publisher.name", "404", "There is no publisher with given values");
            errors = true;
        }
        if (errors) {
            return "/admin/adminNewBook";
        }
        bookService.createABook(book);
        return "redirect:/admin/showBooks";
    }


    @PostMapping("/deleteBook/{id}")
    public String deleteBook(@PathVariable("id") Long bookId) {
        Book book = bookService.findBookById(bookId);
        book.setAvailable(false);
        System.out.println(book);
        bookService.saveBook(book);
        return "redirect:/admin/showBooks";
    }

    @GetMapping("/updateBookAuthors/{id}")
    public String updateBookAuthorsForm(@PathVariable("id") Long bookId, Model model) {

        Book book = bookService.findBookById(bookId);
        model.addAttribute("book", book);
        model.addAttribute("bookDTO", new BookDTO());

        return "/admin/adminEditBookAuthors";
    }

    @PostMapping("/addDetail/{id}")
    public String addBookDetail(@PathVariable("id") Long bookId,
                                @ModelAttribute("bookDTO") BookDTO bookDTO) {
        bookService.setUpdatesForBook(bookId, bookDTO);
        return "redirect:/admin/updateBookAuthors/" + bookId;
    }

    @GetMapping("/updateBook/{id}")
    public String updateBookForm(@PathVariable("id") Long bookId, Model model) {

        Book book = bookService.findBookById(bookId);
        model.addAttribute("book", book);

        return "/admin/adminEditBook";
    }

    @PostMapping("/updateBook")
    public String updateBook(@Valid @ModelAttribute("book") Book book, BindingResult bindingResult) {
        boolean errors = bindingResult.hasErrors();
        if (!bookService.checkIfAuthorExists(book)) {
            bindingResult.rejectValue("mainAuthor.name", "404", "There is no author with given values");
            errors = true;
        }
        if (!bookService.checkIfPublisherExists(book)) {
            bindingResult.rejectValue("publisher.name", "404", "There is no publisher with given values");
            errors = true;
        }
        if (errors) {
            return "/admin/adminEditBook";
        }
        bookService.createABook(book);
        return "redirect:/admin/showBooks";
    }

    @GetMapping("/showLibrarians")
    public String showAllLibrarians(@ModelAttribute("searchUserDTO") SearchUserDTO searchUserDTO, Model model) {
        model.addAttribute("librarians", userService.getUsersByRolePerPage(searchUserDTO, "ROLE_LIBRARIAN"));
        return "/admin/adminLibrarians";
    }

    @GetMapping("/createLibrarian")
    public String getCreateLibrarianForm(Model model) {
        model.addAttribute("user", new User());
        return "/admin/adminNewLibrarian";
    }

    @PostMapping("/createLibrarian")
    public String createLibrarianUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/admin/adminNewLibrarian";
        }

        userService.registerNewUser(user, "ROLE_LIBRARIAN");
        return "redirect:/admin/showLibrarians";
    }

    @PostMapping("/removeLibrarian/{id}")
    public String removeLibrarianUser(@PathVariable("id") Long userId) {
        userService.removeLibrarianRole(userId);
        return "redirect:/admin/showLibrarians";
    }

    @GetMapping("/showUsers")
    public String showAllUsers(@ModelAttribute("searchUserDTO") SearchUserDTO searchUserDTO,
                               Model model) {
        UserDTO usersOrderedByName = userService.findUsersOrderedByName(searchUserDTO);

        model.addAttribute("userDTO", usersOrderedByName);
        return "/admin/adminUsers";
    }

    @PostMapping("/updateUserStatus/{id}")
    public String updateUserStatus(@ModelAttribute("searchUserDTO") SearchUserDTO searchUserDTO,
                                   @RequestParam("action") String action,
                                   @PathVariable("id") Long id,
                                   Model model) {
        userService.updateUserStatus(action, id);
        model.addAttribute("userDTO", userService.findUsersOrderedByName(searchUserDTO));
        return "/admin/adminUsers";
    }

    @ModelAttribute("searchUserDTO")
    public SearchUserDTO searchUserDTO() {
        return new SearchUserDTO();
    }

    @ModelAttribute("adminBookDTO")
    public AdminBookDTO adminBookDTO() {
        return new AdminBookDTO();
    }
}
