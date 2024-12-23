package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

	List<Game> findByTournamentStageIdOrderByTypeAscNameAsc(UUID stageIds);

	List<Game> findByTournamentStageIdInOrderByTypeAscNameAsc(List<UUID> stageIds);

	@NativeQuery("""
			SELECT *
			FROM game
			WHERE :id = ANY(participant_id_list)
			""")
	List<Game> findByParticipantIdListContains(UUID id);
}
