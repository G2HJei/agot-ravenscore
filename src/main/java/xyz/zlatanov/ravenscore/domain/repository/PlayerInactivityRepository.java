package xyz.zlatanov.ravenscore.domain.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.model.PlayerInactivity;

@Repository
public interface PlayerInactivityRepository extends JpaRepository<PlayerInactivity, UUID> {

	List<PlayerInactivity> findByPlayerIdInOrderByStartTime(Collection<UUID> playerIdList);

	Optional<PlayerInactivity> findByPlayerIdAndStartTime(UUID playerId, LocalDateTime startTime);
}
