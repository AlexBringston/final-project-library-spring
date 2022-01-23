package ua.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.training.model.ReadingRoom;

public interface ReadingRoomRepository extends JpaRepository<ReadingRoom, Long> {
}
