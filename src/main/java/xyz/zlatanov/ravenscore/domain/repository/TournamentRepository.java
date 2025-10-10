package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import xyz.zlatanov.ravenscore.domain.domain.Tournament;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

	List<Tournament> findByHiddenFalseOrderByPinnedDescLastUpdatedDesc();

	@Transactional // https://stackoverflow.com/questions/25821579/transactionrequiredexception-executing-an-update-delete-query
	@Modifying
	@NativeQuery("""
			DELETE FROM tournament_stage
			WHERE tournament_id = :tournamentId;

			DELETE FROM substitute
			WHERE tournament_id = :tournamentId;

			DELETE FROM participant
			WHERE NOT EXISTS (
				SELECT 1
				FROM tournament_stage
				WHERE participant.id = ANY(tournament_stage.participant_id_list)
			);
			""")
	// deletes the tournament data without the tournament itself
	void deleteTournamentData(UUID tournamentId);
}
