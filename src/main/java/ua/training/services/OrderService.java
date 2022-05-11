package ua.training.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.model.*;
import ua.training.model.dto.input.BookAbonnementDTO;
import ua.training.model.dto.input.LibrarianSearchDTO;
import ua.training.model.dto.input.ReadingRoomDTO;
import ua.training.repositories.AbonnementRepository;
import ua.training.repositories.ReadingRoomRepository;
import ua.training.repositories.RequestRepository;
import ua.training.repositories.StatusRepository;

import java.util.List;

import static ua.training.utils.Constants.NUMBER_OF_ITEMS_PER_PAGE;

@Log4j2
@Service
public class OrderService {

    /**
     * Abonnement repository
     */
    private final AbonnementRepository abonnementRepository;

    /**
     * ReadingRoom repository
     */
    private final ReadingRoomRepository readingRoomRepository;

    /**
     * Request repository
     */
    private final RequestRepository requestRepository;

    /**
     * Status repository
     */
    private final StatusRepository statusRepository;

    /**
     * Constructor used to implement dependency injection
     * @param abonnementRepository - AbonnementRepository instance
     * @param readingRoomRepository - ReadingRoomRepository instance
     * @param requestRepository - RequestRepository instance
     * @param statusRepository - StatusRepository instance
     */
    public OrderService(AbonnementRepository abonnementRepository, ReadingRoomRepository readingRoomRepository,
                        RequestRepository requestRepository, StatusRepository statusRepository) {
        this.abonnementRepository = abonnementRepository;
        this.readingRoomRepository = readingRoomRepository;
        this.requestRepository = requestRepository;
        this.statusRepository = statusRepository;
    }

    /**
     * Method used to create a request from given user on given book
     * @param book - book instance
     * @param user - user instance
     */
    public void createOrderOfBook(Book book, User user) {
        log.info("Creating an order");
        Request request = Request.builder()
                .book(book)
                .user(user)
                .status(statusRepository.findStatusByName("status.pending"))
                .build();
        requestRepository.save(request);
    }

    /**
     * Method used to gett all requests with status 'pending'
     * @param librarianSearchDTO - DTO used to implement pagination
     * @return - Page object with list of request on given page
     */
    public Page<Request> getAllPendingRequests(LibrarianSearchDTO librarianSearchDTO) {
        log.info("Retrieving all pending requests");
        return requestRepository.findAllByStatusName("status.pending",
                PageRequest.of(librarianSearchDTO.getPageNumber(),
                        NUMBER_OF_ITEMS_PER_PAGE,
                        Sort.by(Sort.Direction.DESC, "id")
                )
        );
    }

    /**
     * Method used to get all abonnement entries of user with given id
     * @param userId - id of user
     * @param librarianSearchDTO - DTO used to implement pagination
     * @return - Page object with list of abonnement entries on given page
     */
    public Page<Abonnement> getUserAbonnement(Long userId, LibrarianSearchDTO librarianSearchDTO) {
        log.info("Retrieving user's abonnement on page: " + librarianSearchDTO.getPageNumber());
        return abonnementRepository.findAbonnementsByUserIdAndStatusNameContaining(userId,
                "status.handed.over", PageRequest.of(librarianSearchDTO.getPageNumber(),
                        NUMBER_OF_ITEMS_PER_PAGE, Sort.by(Sort.Direction.DESC, "returnDate")));
    }

    /**
     * Method used to add an entry to a reading room
     * @param request - request instance
     */
    @Transactional
    public void addRequestToReadingRoom(Request request) {
        log.info("Adding a request to a reading room");
        ReadingRoom readingRoom = ReadingRoom.builder()
                .book(request.getBook())
                .user(request.getUser())
                .status(statusRepository.findStatusByName("status.handed.over"))
                .build();
        readingRoomRepository.save(readingRoom);
        request.setStatus(statusRepository.findStatusByName("status.processed"));
        requestRepository.save(request);
    }

    /**
     * Method used to find a request by id
     * @param requestId - request id to be found
     * @return - request instance or exception is thrown
     */
    public Request findRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Could not find a " +
                "request"));
    }

    /**
     * Method used to reject a request and change it's status to 'rejected' so it won't be appearing later
     * @param request - request instance to be updated
     */
    public void rejectRequest(Request request) {
        log.info("Rejecting a request with an id: " + request.getId());
        request.setStatus(statusRepository.findStatusByName("status.rejected"));
        requestRepository.save(request);
    }

    /**
     * Method used to add an entry to abonnement of user based on request
     * @param abonnement - abonnement instance
     * @param requestId - id of request
     */
    public void saveRequestToUsersAbonnement(Abonnement abonnement, Long requestId) {
        log.info("Saving a request with id: " + requestId + " to an abonnement of user: " + abonnement.getUser().getId());
        Request request = findRequestById(requestId);
        request.setStatus(statusRepository.findStatusByName("status.processed"));
        abonnement.setStatus(statusRepository.findStatusByName("status.handed.over"));
        System.out.println(abonnement);
        abonnementRepository.save(abonnement);
    }

    /**
     * Method used to find all entries per page in abonnement of user
     * @param user - user instance
     * @param bookAbonnementDTO - DTO used to implement pagination
     * @return - Page object of abonnement entries
     */
    public Page<Abonnement> findCurrentReaderAbonnement(User user, BookAbonnementDTO bookAbonnementDTO) {
        log.info("Opening an abonnement page of user: " + user.getId());
        return abonnementRepository.findAbonnementsByUserIdAndStatusNameContaining(user.getId(), "",
                PageRequest.of(bookAbonnementDTO.getPageNumber(),
                        NUMBER_OF_ITEMS_PER_PAGE, Sort.by(Sort.Direction.fromString(bookAbonnementDTO.getSortDirection()),
                                bookAbonnementDTO.getSortField())));
    }

    /**
     * Method used to change order status
     * @param action - string containing action name to be executed
     * @param userId - id of user in order
     * @param bookId - id of book in order
     */
    public void changeOrderStatus(String action, Long userId, Long bookId) {
        log.info("Changing order status");
        Abonnement abonnement = abonnementRepository
                .findAbonnementByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new RuntimeException("Abonnement entry with given user id and book id was not " +
                        "found"));
        abonnement.setStatus(statusRepository.findStatusByName("status." + action));
        System.out.println(abonnement);
        abonnementRepository.save(abonnement);
    }

    /**
     * Method used to get all entries of books per page handed over to reading room
     * @param readingRoomDTO - DTO used to implement a pagination
     * @return - Page object with reading room entries
     */
    public Page<ReadingRoom> getBooksHandedOverToReadingRoom(ReadingRoomDTO readingRoomDTO) {
        log.info("Retrieving books with a status 'Handed over' on a page");
        return readingRoomRepository.findAllByStatusName("status.handed.over",
                PageRequest.of(readingRoomDTO.getPageNumber(), NUMBER_OF_ITEMS_PER_PAGE,
                        Sort.by(Sort.Direction.fromString(readingRoomDTO.getSortDirection()),
                                readingRoomDTO.getSortField())));
    }

    /**
     * Method used to remove an entry from reading room
     * @param readingRoom - reading room instance with data of entry
     */
    public void removeBookFromReadingRoom(ReadingRoom readingRoom) {
        log.info("Returning a book taken to reading room");
        readingRoom.setStatus(statusRepository.findStatusByName("status.returned"));
        readingRoomRepository.save(readingRoom);
    }

    /**
     * Method used to get all entries from reading room by status name
     * @param status - status name
     * @return - list of reading room entries
     */
    public List<ReadingRoom> getAllByStatus(String status) {
        return readingRoomRepository.findAllByStatusName(status);
    }

    /**
     * Method used to get a reading room entry by user id and book id
     * @param userId - id of user
     * @param bookId - id of book
     * @return - reading room entry
     */
    public ReadingRoom getReadingRoomByUserIdAndBookId(Long userId, Long bookId) {
        log.info("Searching for an book with id: " + bookId + " in reading room taken by user with id: " + userId);
        return readingRoomRepository.findByUserIdAndBookId(userId, bookId).orElseThrow(() -> new IllegalArgumentException(
                "There is no such book taken by such user in reading room"));
    }
}
