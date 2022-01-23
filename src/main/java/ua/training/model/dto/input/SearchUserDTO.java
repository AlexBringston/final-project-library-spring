package ua.training.model.dto.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchUserDTO {

    private Integer pageNumber = 0;

    private String sortField = "surname";

    private String sortDirection = "ASC";
}
