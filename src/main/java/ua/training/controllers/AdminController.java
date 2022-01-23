package ua.training.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.training.model.Book;
import ua.training.model.User;
import ua.training.model.dto.input.BookPublisherDTO;
import ua.training.model.dto.input.SearchUserDTO;
import ua.training.model.dto.output.UserDTO;
import ua.training.services.BookService;
import ua.training.services.UserService;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
@SessionAttributes("searchUserDTO")
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
    public String showAllBooks() {
        return "/admin/adminBooks";
    }

    @GetMapping("/addBook")
    public String addBookForm(@ModelAttribute("bookPublisherDTO") BookPublisherDTO bookPublisherDTO, Model model) {
        model.addAttribute("bookPublisherDTO", bookPublisherDTO);
        return "/admin/adminNewBook";
    }

    @PostMapping("/addDetail")
    public String addBookDetail(@ModelAttribute("bookPublisherDTO") BookPublisherDTO bookPublisherDTO,
                                RedirectAttributes redirectAttributes) {
        Book book = bookService.addBookDetails(bookPublisherDTO.getBook(), bookPublisherDTO.getAuthorId(),
                bookPublisherDTO.getPublisherId(), bookPublisherDTO.getQuantity(), bookPublisherDTO.getPublishedAt());
        bookPublisherDTO.setBook(book);
        redirectAttributes.addFlashAttribute("bookPublisherDTO", bookPublisherDTO);
        return "redirect:/addBook";
    }

    @PostMapping("/addBook")
    public String addBook(@Valid @ModelAttribute("bookPublisherDTO") BookPublisherDTO bookPublisherDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/admin/adminNewBook";
        }

        System.out.println(bookPublisherDTO.getBook());
        System.out.println(bookPublisherDTO.getPublisherId());
        System.out.println(bookPublisherDTO.getAuthorId());
        //bookService.saveBook(book);
        return "redirect:/admin";
    }

    @ModelAttribute("bookPublisherDTO")
    public BookPublisherDTO bookPublisherDTO() {
        return new BookPublisherDTO();
    }

    @PutMapping("/deleteBook")
    public String deleteBook(@ModelAttribute("book") Book book) {
        book.setAvailable(false);
        bookService.saveBook(book);
        return "redirect:/admin";
    }


    @GetMapping("/updateBook")
    public String updateBookForm(@ModelAttribute("bookPublisherDTO") BookPublisherDTO bookPublisherDTO, Model model) {
        model.addAttribute("bookPublisherDTO", bookPublisherDTO);
        return "/admin/adminEditBook";
    }

    @PutMapping("/updateBook")
    public String updateBook(@ModelAttribute("bookPublisherDTO") BookPublisherDTO bookPublisherDTO,
                             @ModelAttribute("deleteFlag") boolean deleteFlag) {

        bookService.changeBookInfo(bookPublisherDTO, deleteFlag);
        return "redirect:/admin";
    }

    @GetMapping("/showLibrarians")
    public String showAllLibrarians() {
        return "/admin/adminLibrarians";
    }

    @PutMapping("/createLibrarian")
    public String createLibrarianUser(@ModelAttribute("user")User user) {
        userService.setLibrarianRole(user);
        return "redirect:/admin";
    }

    @PutMapping("/removeLibrarian")
    public String removeLibrarianUser(@ModelAttribute("user")User user) {
        userService.removeLibrarianRole(user);
        return "redirect:/admin";
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
        System.out.println("USER ID " + id);
        if (action.equals("block")) {
            userService.blockUser(id);
        }
        if (action.equals("unblock")) {
            userService.unblockUser(id);
        }
        model.addAttribute("userDTO", userService.findUsersOrderedByName(searchUserDTO));
        return "/admin/adminUsers";
    }
    /*@PutMapping("/blockUser")
    public String blockUser(@ModelAttribute("user")User user) {
        userService.blockUser(user);
        return "redirect:/admin";
    }

    @PutMapping("/unblockUser")
    public String unblockUser(@ModelAttribute("user")User user) {
        userService.unblockUser(user);
        return "redirect:/admin";
    }*/

    @ModelAttribute("searchUserDTO")
    public SearchUserDTO searchUserDTO() {
        return new SearchUserDTO();
    }

}
