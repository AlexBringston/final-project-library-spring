package ua.training.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

/**
 * Entity used to store data from corresponding table in database
 */
@Entity(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @NotBlank(message = "Name is mandatory")
    private String name;

    private boolean onlyForReadingHall;

    private boolean isAvailable;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "authors_books",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private Set<Author> authors;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @NotNull
    private Integer quantity;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate publishedAt;

    @NotNull
    @Column(name = "img_url")
    private String imgUrl;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "main_author_id", nullable = false)
    private Author mainAuthor;

    @Column(name = "amount_of_books_taken")
    private Integer amountOfBooksTaken;

}
