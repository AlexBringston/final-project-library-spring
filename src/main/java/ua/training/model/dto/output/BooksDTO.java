package ua.training.model.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import ua.training.model.Author;
import ua.training.model.Book;

import java.util.List;

/**
 * Data transfer object used to store data which doesn't belong to the sole entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BooksDTO {

    private Page<Book> books;

    private List<Author> mainAuthors;
}
