package ua.training.model.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object used to store data which doesn't belong to the sole entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    private String additionalAuthorName;
    private String additionalAuthorSurname;
    private String authorToDelete;
    private String action;
}
