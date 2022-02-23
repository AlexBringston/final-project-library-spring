package ua.training.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.training.model.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    Page<Request> findAllByStatusName(String status, Pageable pageable);
}
