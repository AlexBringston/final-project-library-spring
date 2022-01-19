package ua.training.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.model.*;
import ua.training.model.dto.input.BookPublisherDTO;
import ua.training.repositories.AuthorRepository;
import ua.training.repositories.BookRepository;
import ua.training.repositories.PublisherRepository;
import ua.training.utils.CustomComparators;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final BookPublisherRepository bookPublisherRepository;

    @Autowired
    public BookService(BookRepository bookRepository,
                       AuthorRepository authorRepository,
                       PublisherRepository publisherRepository,
                       BookPublisherRepository bookPublisherRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.bookPublisherRepository = bookPublisherRepository;
    }

    public Book addBookDetails(Book book, Long authorId, Long publisherId, Integer quantity, LocalDate publishedAt) {
        if (authorId != null) {
            Author author = authorRepository.findById(authorId).orElseThrow(RuntimeException::new);
            book.getAuthors().add(author);
        }
        if (publisherId != null && quantity != null && publishedAt != null) {
            Publisher publisher = publisherRepository.findById(publisherId).orElseThrow(RuntimeException::new);
            BookPublisher bookPublisher = assignPublisherForBook(book, publisher, quantity, publishedAt);
            book.getBooksPublishers().add(bookPublisher);
        }
        return book;
    }

    public BookPublisher assignPublisherForBook(Book book, Publisher publisher, Integer quantity,
                                                LocalDate publishedAt) {
        return BookPublisher.builder()
                .publisher(publisher)
                .book(book)
                .quantity(quantity)
                .publishedAt(publishedAt)
                .id(new BookPublisherKey(book.getId(), publisher.getId()))
                .build();
    }

    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    @Transactional
    public Book changeBookInfo(BookPublisherDTO bookPublisherDTO, boolean deleteFlag) {
        Optional<Author> authorById = authorRepository.findById(bookPublisherDTO.getAuthorId());
        Book book = bookPublisherDTO.getBook();
        Set<Author> authors = book.getAuthors();
        Author author;
        if (bookPublisherDTO.getAuthorId() == null && bookPublisherDTO.getPublisherId() == null) {
            throw new RuntimeException();
        }
        else {
            if (deleteFlag) {
                author = authorById.orElse(new Author());
                authors.remove(author);
            } else {
                author = authorById.orElseThrow(RuntimeException::new);
                authors.add(author);
            }
            bookRepository.save(book);
            return book;
        }
    }

    public List<Book> findBooks(String sortField, String sortDirection) {

        switch (sortField) {
            case "NAME":
                return bookRepository.findAll(Sort.by(Sort.Direction.fromString(sortDirection), "name"));
            case "AUTHOR":
                return bookRepository.findAll()
                        .stream()
                        .sorted(CustomComparators.authorComparator())
                        .collect(Collectors.toList());
        }
        return bookRepository.findAll();
    }
}
