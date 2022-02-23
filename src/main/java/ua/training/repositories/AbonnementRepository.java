package ua.training.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.training.model.Abonnement;

import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    Page<Abonnement> findAbonnementsByUserIdAndStatusNameContaining(Long usedId, String status, Pageable pageable);

    Optional<Abonnement> findAbonnementByUserIdAndBookId(Long userId, Long bookId);
}
