package ua.training.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.exceptions.BookNotFoundException;
import ua.training.model.Author;
import ua.training.model.Book;
import ua.training.model.Publisher;
import ua.training.model.dto.input.AdminBookDTO;
import ua.training.model.dto.input.BookDTO;
import ua.training.model.dto.input.BookSearchDTO;
import ua.training.repositories.AuthorRepository;
import ua.training.repositories.BookRepository;
import ua.training.repositories.PublisherRepository;
import ua.training.utils.Constants;

@Log4j2
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;

    @Autowired
    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       PublisherRepository publisherRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public Page<Book> findBooks(BookSearchDTO bookSearchDTO) {
        log.info("Looking for books to display on a page.");
        String sortField = bookSearchDTO.getSortField();
        String sortDirection = bookSearchDTO.getSortDirection();
        Page<Book> books = bookRepository.findBooksByGivenQuery(bookSearchDTO.getQuery(),
                PageRequest.of(bookSearchDTO.getPageNumber(), Constants.NUMBER_OF_ITEMS_PER_PAGE,
                        Sort.by(Sort.Direction.fromString(sortDirection),
                                sortField)));

        return books;
    }

    public Book findBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException("This book was not found"));
    }

    public Page<Book> getAllAvailableBooks(AdminBookDTO adminBookDTO) {
        log.info("Get books for admin to see");
        Page<Book> books = bookRepository.findAllByIsAvailable(true, PageRequest.of(adminBookDTO.getPageNumber(),
                Constants.NUMBER_OF_ITEMS_PER_PAGE,Sort.by(Sort.Direction.fromString(adminBookDTO.getSortDirection()),
                        adminBookDTO.getSortField())));
        return books;
    }

    @Transactional
    public Book setUpdatesForBook(Long bookId, BookDTO bookDTO) {
        Book book = findBookById(bookId);
        String action = bookDTO.getAction();
        if (action.equals("+")) {
            log.info("Adding an author to a book");
            Author author =
                    authorRepository.findByNameAndSurname(bookDTO.getAdditionalAuthorName(),
                            bookDTO.getAdditionalAuthorSurname()).orElseThrow(RuntimeException::new);
            book.getAuthors().add(author);
        }
        if (action.equals("-")) {
            log.info("Removing an author to a book");
            String name = bookDTO.getAuthorToDelete().split(" ")[0];
            String surname = bookDTO.getAuthorToDelete().split(" ")[1];
            Author author =
                    authorRepository.findByNameAndSurname(name,
                            surname).orElseThrow(RuntimeException::new);
            book.getAuthors().remove(author);
        }
        return bookRepository.save(book);
    }

    public boolean checkIfAuthorExists(Book book) {
        log.info("Checking if author exists");
        return authorRepository.findByNameAndSurname(book.getMainAuthor().getName(),
                book.getMainAuthor().getSurname()).isPresent();
    }

    public boolean checkIfPublisherExists(Book book) {
        log.info("Checking if publisher");
        return publisherRepository.findByName(book.getPublisher().getName()).isPresent();
    }

    public Author getAuthor(Author author) {
        log.info("Getting an author by name and surname");
        return authorRepository.findByNameAndSurname(author.getName(),
                author.getSurname()).orElseThrow(() -> new RuntimeException("Author is not found"));
    }

    public Publisher getPublisher(Publisher publisher) {
        log.info("Getting a publisher by name");
        return publisherRepository.findByName(publisher.getName()).orElseThrow(() -> new RuntimeException("Publisher " +
                "is not found"));
    }

    public void createABook(Book book) {
        book.setMainAuthor(getAuthor(book.getMainAuthor()));
        book.setPublisher(getPublisher(book.getPublisher()));
        bookRepository.save(book);
    }
}