package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.training.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
