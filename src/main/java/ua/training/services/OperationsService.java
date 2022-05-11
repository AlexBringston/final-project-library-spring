package ua.training.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.model.*;
import ua.training.model.dto.input.BookAbonnementDTO;

import java.util.List;

@Log4j2
@Service
public class OperationsService {

    /**
     * Book service instance
     */
    private final BookService bookService;

    /**
     * Order service instance
     */
    private final OrderService orderService;

    /**
     * User service instance
     */
    private final UserService userService;

    /**
     * Constructor used for dependency injection
     * @param bookService - BookService instance
     * @param orderService - OrderService instance
     * @param userService - UserService instance
     */
    public OperationsService(BookService bookService, OrderService orderService, UserService userService) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
    }

    /**
     * Method used to create order of book with given id
     * @param bookId - id of ordered book
     */
    @Transactional
    public void createOrderOnGivenBook(Long bookId) {
        User user = userService.getCurrentUser();
        Book book = bookService.findBookById(bookId);
        if (book.getQuantity().equals(0) || book.getAmountOfBooksTaken().equals(book.getQuantity())) {
            log.warn("The book with id: " + book.getId() + " is not available to be given");
            throw new RuntimeException("The book is not available to be given");
        }
        book.setAmountOfBooksTaken(book.getAmountOfBooksTaken() + 1);
        orderService.createOrderOfBook(book, user);
        bookService.saveBook(book);
    }

    /**
     * Method used to create an abonnement entry with data received from request
     * @param abonnement - abonnement instance with user id and book id
     * @param requestId - id of request created by user with given id on book with given id
     */
    @Transactional
    public void fillDataOfRequestToBeAddedToAbonnement(Abonnement abonnement, Long requestId) {
        User user = userService.findUserById(abonnement.getUser().getId());
        Book book = bookService.findBookById(abonnement.getBook().getId());
        if (book.isOnlyForReadingHall() || book.getQuantity().equals(0) || book.getAmountOfBooksTaken().equals(book.getQuantity()+1)) {
            log.warn("The book with id: " + book.getId() + " is not available to be given");
            throw new RuntimeException("The book is not available to be given");
        }
        abonnement.setBook(book);
        abonnement.setUser(user);

        orderService.saveRequestToUsersAbonnement(abonnement, requestId);
        bookService.saveBook(book);
    }

    /**
     * Method used to check if the book can be passed to reading book and creating an entry in reading room
     * @param requestId - id of request
     */
    @Transactional
    public void setupBookToBeGivenToReadingRoom(Long requestId) {
        Request request = orderService.findRequestById(requestId);
        Book book = request.getBook();
        if (book.getQuantity().equals(0) || book.getAmountOfBooksTaken().equals(book.getQuantity()+1)) {
            log.warn("The book with id: " + book.getId() + " is not available to be given");
            throw new RuntimeException("The book is not available to be given");
        }

        orderService.addRequestToReadingRoom(request);
        bookService.saveBook(book);
    }

    /**
     * Method used to get a page object with all abonnement entries of current user
     * @param bookAbonnementDTO - DTO with data used to implement pagination
     * @return - Page object with abonnement entries
     */
    public Page<Abonnement> findCurrentUserAbonnement(BookAbonnementDTO bookAbonnementDTO) {
        User currentUser = userService.getCurrentUser();
        return orderService.findCurrentReaderAbonnement(currentUser, bookAbonnementDTO);
    }

    /**
     * Method used to remove one or all books from reading room
     * @param action - String 'returnOne' or 'returnAll'
     * @param userId - id of user in order
     * @param bookId - id of book in order
     */
    public void removeRequestedBooks(String action, Long userId, Long bookId) {
        if (action.equals("returnOne")) {
            log.info("Return one book with id: " + bookId + " from a reading room");
            removeTakenBookFromReadingRoom(userId,bookId);
        }
        if (action.equals("returnAll")) {
            log.info("Return all books from a reading room");
            removeAllBooksCurrentlyInReadingRoom();
        }
    }

    /**
     * Method used to remove all books that are currently in reading room
     */
    @Transactional
    public void removeAllBooksCurrentlyInReadingRoom() {
        List<ReadingRoom> readingRoomList = orderService.getAllByStatus("status.handed.over");

        for (ReadingRoom readingRoom : readingRoomList) {
            orderService.removeBookFromReadingRoom(readingRoom);
            Book book = readingRoom.getBook();
            book.setAmountOfBooksTaken(book.getAmountOfBooksTaken() - 1);
            bookService.saveBook(book);
        }
    }

    /**
     * Method used to remove an entry from reading room
     * @param userId - id of user in entry
     * @param bookId - id of book in entry
     */
    @Transactional
    public void removeTakenBookFromReadingRoom(Long userId, Long bookId) {
        ReadingRoom readingRoom = orderService.getReadingRoomByUserIdAndBookId(userId, bookId);
        orderService.removeBookFromReadingRoom(readingRoom);
        Book book = readingRoom.getBook();
        book.setAmountOfBooksTaken(book.getAmountOfBooksTaken() - 1);
        bookService.saveBook(book);
    }

    /**
     * Method used to reject request of user
     * @param requestId - id of request to be rejected
     */
    @Transactional
    public void rejectRequestOfUser(Long requestId) {
        log.info("Request with id: " + requestId + " is being rejected");
        Request request = orderService.findRequestById(requestId);
        Book book = request.getBook();
        book.setAmountOfBooksTaken(book.getAmountOfBooksTaken()-1);
        orderService.rejectRequest(request);
        bookService.saveBook(book);
    }
}
