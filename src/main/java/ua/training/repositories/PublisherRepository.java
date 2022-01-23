package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.training.model.Publisher;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher,Long> {
}
