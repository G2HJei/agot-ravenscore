package xyz.zlatanov.ravenscore.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;
import xyz.zlatanov.ravenscore.domain.model.Game;

import java.util.List;
import java.util.UUID;

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

    List<Game> findByTournamentStageIdInAndCompletedFalseAndLinkContainingIgnoreCase(List<UUID> stageIds, String link);
}
