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

    /**
     * Book repository instance
     */
    private final BookRepository bookRepository;

    /**
     * Author repository instance
     */
    private final AuthorRepository authorRepository;

    /**
     * Publisher repository instance
     */
    private final PublisherRepository publisherRepository;

    /**
     * Constructor used to implement dependency injection
     *
     * @param bookRepository      - BookRepository instance
     * @param authorRepository    - AuthorRepository instance
     * @param publisherRepository - PublisherRepository instance
     */
    @Autowired
    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       PublisherRepository publisherRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
    }

    /**
     * Method used to save a book in database
     *
     * @param book - book instance with data to be saved
     * @return - book instance with assigned id
     */
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Method used to get a page object of books based on data in received DTO
     *
     * @param bookSearchDTO - DTO used to implement pagination
     * @return - Page object with list of books inside corresponding to the asked page number
     */
    public Page<Book> findBooks(BookSearchDTO bookSearchDTO) {
        log.info("Looking for books to display on a page.");
        String sortField = bookSearchDTO.getSortField();
        String sortDirection = bookSearchDTO.getSortDirection();
        Page<Book> books = bookRepository.findBooksByGivenQuery(bookSearchDTO.getQuery(),
                PageRequest.of(bookSearchDTO.getPageNumber(),
                        Constants.NUMBER_OF_ITEMS_PER_PAGE,
                        Sort.by(Sort.Direction.fromString(sortDirection), sortField)
                )
        );
        return books;
    }

    /**
     * Method used to find a book by given id
     * @param bookId - id of searched book
     * @return - book with given id or else exception will be thrown
     */
    public Book findBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException("This book was not found"));
    }

    /**
     * Method used to get all available books with given DTO
     * @param adminBookDTO - DTO used to implement pagination
     * @return - Page object with list of books
     */
    public Page<Book> getAllAvailableBooks(AdminBookDTO adminBookDTO) {
        log.info("Get books for admin to see");
        Page<Book> books = bookRepository.findAllByIsAvailable(true, PageRequest.of(adminBookDTO.getPageNumber(),
                Constants.NUMBER_OF_ITEMS_PER_PAGE, Sort.by(Sort.Direction.fromString(adminBookDTO.getSortDirection()),
                        adminBookDTO.getSortField())));
        return books;
    }

    /**
     * Method used to update books authors
     * @param bookId - id of book to be updated
     * @param bookDTO - DTO which contains action and author to be added or removed
     * @return - updated book object
     */
    @Transactional
    public Book setUpdatesForBook(Long bookId, BookDTO bookDTO) {
        Book book = findBookById(bookId);
        String action = bookDTO.getAction();
        if (action.equals("+")) {
            log.info("Adding an author to a book");
            Author author =
                    authorRepository.findByNameAndSurname(bookDTO.getAdditionalAuthorName(),
                            bookDTO.getAdditionalAuthorSurname()).orElseThrow(() -> new RuntimeException("Author " +
                            "with given name and surname was not found"));
            book.getAuthors().add(author);
        }
        if (action.equals("-")) {
            log.info("Removing an author to a book");
            String name = bookDTO.getAuthorToDelete().split(" ")[0];
            String surname = bookDTO.getAuthorToDelete().split(" ")[1];
            Author author =
                    authorRepository.findByNameAndSurname(name,
                            surname).orElseThrow(() -> new RuntimeException("Author with given name and surname was " +
                            "not found"));
            book.getAuthors().remove(author);
        }
        return bookRepository.save(book);
    }

    /**
     * Method used to check if authors with given name and surname exists in database
     * @param book - book instance
     * @return - true, if author exists, false otherwise
     */
    public boolean checkIfAuthorExists(Book book) {
        log.info("Checking if author exists");
        return authorRepository.findByNameAndSurname(book.getMainAuthor().getName(),
                book.getMainAuthor().getSurname()).isPresent();
    }

    /**
     * Method used to check if publisher with given nam
     * @param book - book instance
     * @return - true, if publisher exists, false otherwise
     */
    public boolean checkIfPublisherExists(Book book) {
        log.info("Checking if publisher");
        return publisherRepository.findByName(book.getPublisher().getName()).isPresent();
    }

    /**
     * Method used to get author from database
     * @param author - author instance with name and surname
     * @return - author instance from database
     */
    public Author getAuthor(Author author) {
        log.info("Getting an author by name and surname");
        return authorRepository.findByNameAndSurname(author.getName(),
                author.getSurname()).orElseThrow(() -> new RuntimeException("Author is not found"));
    }

    /**
     * Method used to get a publisher from database
     * @param publisher - publisher instance with name to be searched
     * @return - publisher instance from database
     */
    public Publisher getPublisher(Publisher publisher) {
        log.info("Getting a publisher by name");
        return publisherRepository.findByName(publisher.getName()).orElseThrow(() -> new RuntimeException("Publisher " +
                "is not found"));
    }

    /**
     * Method used to create a book entry in database and all corresponding bonds
     * @param book - book instance to be saved
     */
    public void createABook(Book book) {
        book.setMainAuthor(getAuthor(book.getMainAuthor()));
        book.setPublisher(getPublisher(book.getPublisher()));
        bookRepository.save(book);
    }
}