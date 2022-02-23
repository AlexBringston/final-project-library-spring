package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.training.model.Status;

public interface StatusRepository extends JpaRepository<Status, Long> {

    Status findStatusByName(String name);
}
