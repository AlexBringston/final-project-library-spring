package ua.training.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.training.model.ReadingRoom;

import java.util.List;
import java.util.Optional;

public interface ReadingRoomRepository extends JpaRepository<ReadingRoom, Long> {

    Page<ReadingRoom> findAllByStatusName(String status, Pageable pageable);


    Optional<ReadingRoom> findByUserIdAndBookId(Long userId, Long bookId);

    List<ReadingRoom> findAllByStatusName(String status);
}
