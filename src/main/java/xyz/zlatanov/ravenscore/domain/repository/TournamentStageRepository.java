package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;

@Repository
public interface TournamentStageRepository extends JpaRepository<TournamentStage, UUID> {

	List<TournamentStage> findByTournamentIdInOrderByStartDateDesc(List<UUID> list);

	List<TournamentStage> findByTournamentIdOrderByStartDateDesc(UUID tournamentId);

	void deleteByTournamentId(UUID tournamentId);
}