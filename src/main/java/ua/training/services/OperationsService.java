package ua.training.services;

import org.springframework.stereotype.Service;
import ua.training.model.Book;
import ua.training.model.User;

@Service
public class OperationsService {

    private BookService bookService;
    private OrderService orderService;
    private UserService userService;

    public OperationsService(BookService bookService, OrderService orderService, UserService userService) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
    }

    public void createOrderOnGivenBook(Long bookId) {
        User user = userService.getCurrentUser();
        Book book = bookService.findBookById(bookId);
        orderService.createOrderOfBook(book, user);
    }


}
