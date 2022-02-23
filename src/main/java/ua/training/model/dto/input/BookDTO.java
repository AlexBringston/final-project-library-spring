package ua.training.model.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDTO {

    private String additionalAuthorName;
    private String additionalAuthorSurname;
    private String authorToDelete;
    private String action;
}
