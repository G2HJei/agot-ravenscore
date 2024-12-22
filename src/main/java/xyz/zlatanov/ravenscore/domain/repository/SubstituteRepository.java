package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.Substitute;

@Repository
public interface SubstituteRepository extends JpaRepository<Substitute, UUID> {

	List<Substitute> findByTournamentIdInOrderByName(List<UUID> tourIds);

	List<Substitute> findByTournamentIdOrderByName(UUID tournamentId);
}
