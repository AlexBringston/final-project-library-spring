package ua.training.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ua.training.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT COUNT(a) FROM abonnements AS a WHERE a.user.id = :userId")
    Integer countBooksByUserId(Long userId);


    @Query("SELECT b FROM books AS b WHERE " +
            "(UPPER(CONCAT(b.mainAuthor.name, ' ', b.mainAuthor.surname)) LIKE UPPER(CONCAT('%',:query,'%'))\n" +
            "OR UPPER(CONCAT(b.mainAuthor.surname,' ', b.mainAuthor.name)) LIKE UPPER(CONCAT('%',:query,'%')))" +
            " OR UPPER(b.name) LIKE UPPER(CONCAT('%',:query,'%')) AND b.isAvailable = true")
    Page<Book> findBooksByGivenQuery(String query, Pageable pageable);

    Page<Book> findAllByIsAvailable(boolean isAvailable, Pageable pageable);
}
