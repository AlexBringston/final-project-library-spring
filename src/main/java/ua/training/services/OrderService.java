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

    private final AbonnementRepository abonnementRepository;
    private final ReadingRoomRepository readingRoomRepository;
    private final RequestRepository requestRepository;
    private final StatusRepository statusRepository;

    public OrderService(AbonnementRepository abonnementRepository, ReadingRoomRepository readingRoomRepository,
                        RequestRepository requestRepository, StatusRepository statusRepository) {
        this.abonnementRepository = abonnementRepository;
        this.readingRoomRepository = readingRoomRepository;
        this.requestRepository = requestRepository;
        this.statusRepository = statusRepository;
    }

    public void createOrderOfBook(Book book, User user) {
        log.info("Creating an order");
        Request request = Request.builder()
                .book(book)
                .user(user)
                .status(statusRepository.findStatusByName("status.pending"))
                .build();
        requestRepository.save(request);
    }

    public Page<Request> getAllPendingRequests(LibrarianSearchDTO librarianSearchDTO) {
        log.info("Retrieving all pending requests");
        return requestRepository.findAllByStatusName("status.pending", PageRequest.of(librarianSearchDTO.getPageNumber()
                , NUMBER_OF_ITEMS_PER_PAGE, Sort.by(Sort.Direction.DESC, "id")));
    }

    public Page<Abonnement> getUserAbonnement(Long userId, LibrarianSearchDTO librarianSearchDTO) {
        log.info("Retrieving user's abonnement on page: " + librarianSearchDTO.getPageNumber());
        return abonnementRepository.findAbonnementsByUserIdAndStatusNameContaining(userId,
                "status.handed.over", PageRequest.of(librarianSearchDTO.getPageNumber(),
                        NUMBER_OF_ITEMS_PER_PAGE, Sort.by(Sort.Direction.DESC, "returnDate")));
    }

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

    public Request findRequestById(Long requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Could not find a " +
                "request"));
    }

    public void rejectRequest(Request request) {
        log.info("Rejecting a request with an id: " + request.getId());
        request.setStatus(statusRepository.findStatusByName("status.rejected"));
        requestRepository.save(request);
    }

    public void saveRequestToUsersAbonnement(Abonnement abonnement, Long requestId) {
        log.info("Saving a request with id: " + requestId + " to an abonnement of user: " + abonnement.getUser().getId());
        Request request = findRequestById(requestId);
        request.setStatus(statusRepository.findStatusByName("status.processed"));
        abonnement.setStatus(statusRepository.findStatusByName("status.handed.over"));
        System.out.println(abonnement);
        abonnementRepository.save(abonnement);
    }

    public Page<Abonnement> findCurrentReaderAbonnement(User user, BookAbonnementDTO bookAbonnementDTO) {
        log.info("Opening an abonnement page of user: " + user.getId());
        return abonnementRepository.findAbonnementsByUserIdAndStatusNameContaining(user.getId(), "",
                PageRequest.of(bookAbonnementDTO.getPageNumber(),
                        NUMBER_OF_ITEMS_PER_PAGE, Sort.by(Sort.Direction.fromString(bookAbonnementDTO.getSortDirection()),
                                bookAbonnementDTO.getSortField())));
    }

    public void changeOrderStatus(String action, Long userId, Long bookId) {
        log.info("Changing order status");
        Abonnement abonnement = abonnementRepository
                .findAbonnementByUserIdAndBookId(userId, bookId)
                .orElseThrow(RuntimeException::new);
        abonnement.setStatus(statusRepository.findStatusByName("status." + action));
        System.out.println(abonnement);
        abonnementRepository.save(abonnement);
    }

    public Page<ReadingRoom> getBooksHandedOverToReadingRoom(ReadingRoomDTO readingRoomDTO) {
        log.info("Retrieving books with a status 'Handed over' on a page");
        return readingRoomRepository.findAllByStatusName("status.handed.over",
                PageRequest.of(readingRoomDTO.getPageNumber(), NUMBER_OF_ITEMS_PER_PAGE,
                        Sort.by(Sort.Direction.fromString(readingRoomDTO.getSortDirection()),
                                readingRoomDTO.getSortField())));
    }

    public boolean removeBookFromReadingRoom(ReadingRoom readingRoom) {
        log.info("Returning a book taken to reading room");
        readingRoom.setStatus(statusRepository.findStatusByName("status.returned"));
        readingRoomRepository.save(readingRoom);
        return true;

    }

    public List<ReadingRoom> getAllByStatus(String status) {
        return readingRoomRepository.findAllByStatusName(status);
    }

    public ReadingRoom getReadingRoomByUserIdAndBookId(Long userId, Long bookId) {
        log.info("Searching for an book with id: " + bookId + " in reading room taken by user with id: " + userId);
        return readingRoomRepository.findByUserIdAndBookId(userId, bookId).orElseThrow(() -> new IllegalArgumentException(
                "There is no such book taken by such user in reading room"));
    }
}
