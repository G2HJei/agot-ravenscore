package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

	List<Player> findByGameIdInOrderByPointsDesc(List<UUID> gameIdList);

	List<Player> findByGameIdOrderByPointsDesc(UUID gameId);
}
