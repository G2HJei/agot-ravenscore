package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.Participant;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

	List<Participant> findByIdInOrderByName(List<UUID> participantIds);

	@NativeQuery("""
			SELECT *
			FROM participant p
			WHERE EXISTS (
			    SELECT 1
			    FROM tournament_stage ts
			    WHERE   ts.tournament_id = :tournamentId
			        AND p.id = ANY (ts.participant_id_list)
			)
			""")
	List<Participant> findByTournamentId(UUID tournamentId);
}
