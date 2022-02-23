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

    private final BookService bookService;
    private final OrderService orderService;
    private final UserService userService;

    public OperationsService(BookService bookService, OrderService orderService, UserService userService) {
        this.bookService = bookService;
        this.orderService = orderService;
        this.userService = userService;
    }

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

    public Page<Abonnement> findCurrentUserAbonnement(BookAbonnementDTO bookAbonnementDTO) {
        User currentUser = userService.getCurrentUser();
        return orderService.findCurrentReaderAbonnement(currentUser, bookAbonnementDTO);
    }

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

    @Transactional
    public void removeTakenBookFromReadingRoom(Long userId, Long bookId) {
        ReadingRoom readingRoom = orderService.getReadingRoomByUserIdAndBookId(userId, bookId);
        orderService.removeBookFromReadingRoom(readingRoom);
        Book book = readingRoom.getBook();
        book.setAmountOfBooksTaken(book.getAmountOfBooksTaken() - 1);
        bookService.saveBook(book);
    }

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
