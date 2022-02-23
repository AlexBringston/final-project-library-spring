package ua.training;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import ua.training.model.Author;
import ua.training.model.Book;
import ua.training.model.Publisher;
import ua.training.model.dto.input.AdminBookDTO;
import ua.training.model.dto.input.BookDTO;
import ua.training.model.dto.input.BookSearchDTO;
import ua.training.repositories.AuthorRepository;
import ua.training.repositories.BookRepository;
import ua.training.repositories.PublisherRepository;
import ua.training.services.BookService;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BookServiceTest {

    @Autowired
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private PublisherRepository publisherRepository;


    @Test
    @DisplayName("Test findById Success")
    public void testFindById() {
        Book book = new Book();
        book.setId(1L);
        doReturn(Optional.of(book)).when(bookRepository).findById(1L);

        Book returnedBook = bookService.findBookById(1L);
        assertSame(book, returnedBook, "The book returned was not the same as the mock");
    }

    @Test(expected = RuntimeException.class)
    @DisplayName("Test findById Not Found")
    public void testFindByIdNotFound() {
        doReturn(Optional.empty()).when(bookRepository).findById(1L);
        bookService.findBookById(1L);
    }

    @Test
    @DisplayName("Test findBooks on page")
    public void testFindBooks() {
        Book book1 = new Book();
        Book book2 = new Book();
        Page<Book> books = new PageImpl<>(new ArrayList<>(Arrays.asList(book1, book2)));
        doReturn(books).when(bookRepository).findBooksByGivenQuery(anyString(), any(Pageable.class));
        Page<Book> returnedBooks = bookService.findBooks(new BookSearchDTO());
        assertSame(returnedBooks, books, "Page contains different books");
    }

    @Test
    @DisplayName("Test saveBook")
    public void testSaveBook() {
        Book book = new Book();
        book.setId(1L);
        doReturn(book).when(bookRepository).save(any(Book.class));
        Book returnedBook = bookService.saveBook(book);
        assertEquals(returnedBook.getId(), book.getId(), "Failed to save a book");
    }

    @Test
    @DisplayName("Test checkIfAuthorExists")
    public void testIfAuthorExists() {
        Book book = new Book();
        Author author = new Author(1L, "John", "Hopkins");
        book.setMainAuthor(author);
        doReturn(Optional.of(author)).when(authorRepository).findByNameAndSurname(anyString(), anyString());
        boolean check = bookService.checkIfAuthorExists(book);
        assertTrue(check, "Author with given parameters does not exist");
    }

    @Test
    @DisplayName("Test checkIfPublisherExists")
    public void testIfPublisherExists() {
        Book book = new Book();
        Publisher publisher = new Publisher(1L, "Books of Bliss");
        book.setPublisher(publisher);
        doReturn(Optional.of(publisher)).when(publisherRepository).findByName(anyString());
        boolean check = bookService.checkIfPublisherExists(book);
        assertTrue(check, "Publisher with given parameters does not exist");
    }

    @Test
    @DisplayName("Test getAllAvailableBooks")
    public void testGetAllAvailableBooks() {
        Book book1 = new Book();
        Book book2 = new Book();
        Page<Book> books = new PageImpl<>(new ArrayList<>(Arrays.asList(book1, book2)));
        doReturn(books).when(bookRepository).findAllByIsAvailable(anyBoolean(), any(Pageable.class));
        Page<Book> returnedBooks = bookService.getAllAvailableBooks(new AdminBookDTO());
        assertSame(returnedBooks, books, "The returned books were not the same as expected");
    }

    @Test
    @DisplayName("Test setUpdatesForBook")
    public void testSetUpdatesForBook() {
        Author author = new Author(1L, "John", "Hopkins");
        Book book1 = getBook();
        Book book2 = getBook();
        Set<Author> authors = new HashSet<>();
        authors.add(author);
        book2.setAuthors(authors);

        doReturn(Optional.of(book1)).when(bookRepository).findById(anyLong());
        doReturn(Optional.of(author)).when(authorRepository).findByNameAndSurname(anyString(),
                anyString());
        doReturn(book2).when(bookRepository).save(book1);

        Book book3 = bookService.setUpdatesForBook(1L, new BookDTO("","","","+"));

        assertEquals(book1, book3);
    }

    private Book getBook() {
        return Book.builder().id(1L)
                .name("quisque porta")
                .onlyForReadingHall(false)
                .isAvailable(true)
                .authors(new HashSet<>())
                .publisher(new Publisher(1L,"publisher1"))
                .quantity(500)
                .publishedAt(LocalDate.of(2001,12,12))
                .imgUrl("https://images-na.ssl-images-amazon.com/images/I/71pX+JTU8EL.jpg")
                .mainAuthor(new Author(8L,"Gilemette","Sinott"))
                .amountOfBooksTaken(0)
                .build();
    }
}
