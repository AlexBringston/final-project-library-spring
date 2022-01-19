package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.training.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByNameOrderByName(String name);

    List<Book> findByAuthorsName(String name);

    List<Book> findBooksByOrderByPublisherName();
}
