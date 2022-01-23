package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.training.model.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {


}
