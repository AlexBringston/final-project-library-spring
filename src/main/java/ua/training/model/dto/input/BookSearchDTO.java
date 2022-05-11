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
public class BookSearchDTO {

    private String query = "";

    private Integer pageNumber = 0;

    private String sortField = "name";

    private String sortDirection = "ASC";
}
