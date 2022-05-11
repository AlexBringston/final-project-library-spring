package ua.training.model.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import ua.training.model.User;

import java.util.List;

/**
 * Data transfer object used to store data which doesn't belong to the sole entity
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Page<User> users;
    private List<Integer> age;
    private List<Integer> booksTaken;

}
