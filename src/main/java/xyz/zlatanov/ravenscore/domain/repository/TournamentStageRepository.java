package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;

@Repository
public interface TournamentStageRepository extends JpaRepository<TournamentStage, UUID> {

	List<TournamentStage> findByTournamentIdInOrderByStartDateDesc(List<UUID> list);

	List<TournamentStage> findByTournamentIdOrderByStartDateDesc(UUID tournamentId);

	@Modifying
	@NativeQuery("""
			DELETE FROM tournament_stage
			WHERE id = :stageId;

			DELETE FROM participant
			WHERE NOT EXISTS (
				SELECT 1
				FROM tournament_stage
				WHERE participant.id = ANY(tournament_stage.participant_id_list)
			);
			""")
	// deletes the tournament data without the tournament itself
	void deleteAndCleanup(UUID stageId);

	@NativeQuery("""
			SELECT *
			FROM tournament_stage
			WHERE :participantId = ANY(participant_id_list)
			""")
	List<TournamentStage> findByParticipantIdListContains(UUID participantId);
}