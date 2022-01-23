package ua.training.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.training.model.*;
import ua.training.model.dto.input.BookPublisherDTO;
import ua.training.model.dto.input.BookSearchDTO;
import ua.training.model.dto.output.BooksDTO;
import ua.training.repositories.AuthorRepository;
import ua.training.repositories.BookRepository;
import ua.training.repositories.PublisherRepository;
import ua.training.utils.Constants;
import ua.training.utils.CustomComparators;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    public Book addBookDetails(Book book, Long authorId, Long publisherId, Integer quantity, LocalDate publishedAt) {
        if (authorId != null) {
            Author author = authorRepository.findById(authorId).orElseThrow(RuntimeException::new);
            book.getAuthors().add(author);
        }
        if (publisherId != null && quantity != null && publishedAt != null) {
            Publisher publisher = publisherRepository.findById(publisherId).orElseThrow(RuntimeException::new);
            book.setPublisher(publisher);
            book.setQuantity(quantity);
            book.setPublishedAt(publishedAt);
        }
        return book;
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

    public Page<Book> findBooks(BookSearchDTO bookSearchDTO) {
        String sortField = bookSearchDTO.getSortField();
        String sortDirection = bookSearchDTO.getSortDirection();
        Page<Book> books = bookRepository.findBooksByGivenQuery(bookSearchDTO.getQuery(),
                PageRequest.of(bookSearchDTO.getPageNumber(), Constants.NUMBER_OF_ITEMS_PER_PAGE,
                        Sort.by(Sort.Direction.fromString(sortDirection),
                                sortField)));

        //List<Author> mainAuthors = books.stream().map(this::findBooksMainAuthor).collect(Collectors.toList());
        return books;
    }

    private Author findBooksMainAuthor(Book book) {
        return book.getAuthors().stream().min(Comparator.comparing(Author::getSurname
        )).orElseThrow(RuntimeException::new);
    }
    public Book findBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(RuntimeException::new);
    }

//    public BooksDTO findBooksByQuery(BookSearchDTO bookSearchDTO) {
//        Page<Book> books = bookRepository.findBooksByGivenQuery(bookSearchDTO.getQuery(),
//                PageRequest.of(bookSearchDTO.getPageNumber(), Constants.NUMBER_OF_ITEMS_PER_PAGE,
//                Sort.by(Sort.Direction.fromString(bookSearchDTO.getSortDirection()),
//                        bookSearchDTO.getSortField())));
//    }

}
