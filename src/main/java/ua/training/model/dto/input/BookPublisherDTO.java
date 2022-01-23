package ua.training.model.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.training.model.Book;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookPublisherDTO {

    private Book book;
    private Long authorId;
    private Long publisherId;
    private Integer quantity;
    private LocalDate publishedAt;
    //todo check if needed to work
}
