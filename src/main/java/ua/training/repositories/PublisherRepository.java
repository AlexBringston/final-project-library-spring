package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.training.model.Publisher;

public interface PublisherRepository extends JpaRepository<Publisher,Long> {
}
