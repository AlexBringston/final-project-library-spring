package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.training.model.Abonnement;
import ua.training.model.AbonnementKey;

public interface AbonnementRepository extends JpaRepository<Abonnement, AbonnementKey> {
}
