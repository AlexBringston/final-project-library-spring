package ua.training.services;

import org.springframework.stereotype.Service;
import ua.training.model.Book;
import ua.training.model.Request;
import ua.training.model.User;
import ua.training.repositories.AbonnementRepository;
import ua.training.repositories.ReadingRoomRepository;
import ua.training.repositories.RequestRepository;

import java.util.List;

@Service
public class OrderService {

    private AbonnementRepository abonnementRepository;
    private ReadingRoomRepository readingRoomRepository;
    private RequestRepository requestRepository;

    public OrderService(AbonnementRepository abonnementRepository, ReadingRoomRepository readingRoomRepository, RequestRepository requestRepository) {
        this.abonnementRepository = abonnementRepository;
        this.readingRoomRepository = readingRoomRepository;
        this.requestRepository = requestRepository;
    }

    public void createOrderOfBook(Book book, User user) {
        Request request = Request.builder()
                .book(book)
                .user(user)
                .build();
        requestRepository.save(request);
    }

    public List<Request> getAllRequests() {
        return requestRepository.findAll();
    }


}
