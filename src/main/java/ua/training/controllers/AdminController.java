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

    /**
     * Method for retrieving an admin account page
     * @return - admin account view
     */
    @GetMapping
    public String getAdminPage() {
        return "/admin/admin";
    }

    /**
     * Method for retrieving all available books in catalog
     * @param adminBookDTO - DTO used for pagination and sorting
     * @param model - Spring model to pass books list to the view
     * @return - admin books management view
     */
    @GetMapping("/showBooks")
    public String showAllBooks(@ModelAttribute("adminBookDTO") AdminBookDTO adminBookDTO,
                               Model model) {
        model.addAttribute("books", bookService.getAllAvailableBooks(adminBookDTO));
        return "/admin/adminBooks";
    }

    /**
     * Method for retrieving a form for adding a new book
     * @param model - Spring model, used to pass a book instance to the view, which will allow to assign all data
     *              from form to that instance
     * @return - add new book form view
     */
    @GetMapping("/addBook")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "/admin/adminNewBook";
    }

    /**
     * Method used to add a book to the catalog
     * @param book - book instance, which contains all the data
     * @param bindingResult - binding result instance, used to validate every field entered by user
     * @return - redirect to the list of books, if the book was successfully added, otherwise returns to the form page
     */
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


    /**
     * Method used to delete a book by given id
     * @param bookId - id of a book, that user wants to delete, in a catalog
     * @return - redirect to the total list of books view
     */
    @PostMapping("/deleteBook/{id}")
    public String deleteBook(@PathVariable("id") Long bookId) {
        Book book = bookService.findBookById(bookId);
        book.setAvailable(false);
        System.out.println(book);
        bookService.saveBook(book);
        return "redirect:/admin/showBooks";
    }

    /**
     * Method used to retrieve a page where admin user can update book's additional authors
     * @param bookId - id of book, where user want to change info
     * @param model - Spring model to pass book id and DTO object to the view
     * @return - view of form, where authors can be added or deleted
     */
    @GetMapping("/updateBookAuthors/{id}")
    public String updateBookAuthorsForm(@PathVariable("id") Long bookId, Model model) {

        Book book = bookService.findBookById(bookId);
        model.addAttribute("book", book);
        model.addAttribute("bookDTO", new BookDTO());

        return "/admin/adminEditBookAuthors";
    }

    /**
     * Method which updates book's additional authors
     * @param bookId - id of book to be updated
     * @param bookDTO - DTO instance , which contains action whether to add or to delete an author, name of
     *                corresponding author to be added/deleted
     * @return - redirect to the update authors view
     */
    @PostMapping("/addDetail/{id}")
    public String addBookDetail(@PathVariable("id") Long bookId,
                                @ModelAttribute("bookDTO") BookDTO bookDTO) {
        bookService.setUpdatesForBook(bookId, bookDTO);
        return "redirect:/admin/updateBookAuthors/" + bookId;
    }

    /**
     * Method to get a form to update book's info by given id
     * @param bookId - id of book to be updated
     * @param model - Spring model instance to pass data to view
     * @return - view of an update book form
     */
    @GetMapping("/updateBook/{id}")
    public String updateBookForm(@PathVariable("id") Long bookId, Model model) {

        Book book = bookService.findBookById(bookId);
        model.addAttribute("book", book);

        return "/admin/adminEditBook";
    }

    /**
     * Method to update book's info in database
     * @param book - book with updated info
     * @param bindingResult - binding result instance to validate received data properly
     * @return - redirect to the total list of books view
     */
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

    /**
     * Method to get a view with all librarians listed
     * @param searchUserDTO - DTO used to implement pagination
     * @param model - Spring model instance to pass data to a view
     * @return - view with listed librarians per page
     */
    @GetMapping("/showLibrarians")
    public String showAllLibrarians(@ModelAttribute("searchUserDTO") SearchUserDTO searchUserDTO, Model model) {
        model.addAttribute("librarians", userService.getUsersByRolePerPage(searchUserDTO, "ROLE_LIBRARIAN"));
        return "/admin/adminLibrarians";
    }

    /**
     * Method to get create librarian form view
     * @param model - Spring model to pass data to the view
     * @return - view with a form to create a librarian
     */
    @GetMapping("/createLibrarian")
    public String getCreateLibrarianForm(Model model) {
        model.addAttribute("user", new User());
        return "/admin/adminNewLibrarian";
    }

    /**
     * Method to save a new librarian to the database
     * @param user - librarian info
     * @param bindingResult - instance to validated data stored in user object
     * @return - redirect to show all librarians view
     */
    @PostMapping("/createLibrarian")
    public String createLibrarianUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/admin/adminNewLibrarian";
        }

        userService.registerNewUser(user, "ROLE_LIBRARIAN");
        return "redirect:/admin/showLibrarians";
    }

    /**
     * Method to remove an already existing librarian
     * @param userId - id of librarian to remove
     * @return - redirect to show all librarians view
     */
    @PostMapping("/removeLibrarian/{id}")
    public String removeLibrarianUser(@PathVariable("id") Long userId) {
        userService.removeLibrarianRole(userId);
        return "redirect:/admin/showLibrarians";
    }

    /**
     * Method to retrieve all users registered in a system and list them on a page
     * @param searchUserDTO - DTO with parameters to implement pagination properly
     * @param model - Spring model to pass the data to the view
     * @return - all users with options view
     */
    @GetMapping("/showUsers")
    public String showAllUsers(@ModelAttribute("searchUserDTO") SearchUserDTO searchUserDTO,
                               Model model) {
        UserDTO usersOrderedByName = userService.findUsersOrderedByName(searchUserDTO);

        model.addAttribute("userDTO", usersOrderedByName);
        return "/admin/adminUsers";
    }

    /**
     * Method used to update user status (to block or to unblock)
     * @param action - String which contains 'block' or 'unblock'
     * @param id - id of user on whom the action will be applied
     * @return - redirect to the list all users view
     */
    @PostMapping("/updateUserStatus/{id}")
    public String updateUserStatus(@RequestParam("action") String action,
                                   @PathVariable("id") Long id) {
        userService.updateUserStatus(action, id);
        return "redirect:/admin/showUsers";
    }

    /**
     * Method used to get SearchUserDTO with default data if no other was provided
     * @return - SearchUserDTO
     */
    @ModelAttribute("searchUserDTO")
    public SearchUserDTO searchUserDTO() {
        return new SearchUserDTO();
    }

    /**
     * Method used to get AdminBookDTO with default data if no other was provided
     * @return - AdminBookDTO
     */
    @ModelAttribute("adminBookDTO")
    public AdminBookDTO adminBookDTO() {
        return new AdminBookDTO();
    }
}
