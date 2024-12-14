package xyz.zlatanov.ravenscore.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.Tournament;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

	List<Tournament> findByHiddenFalseOrderByPinnedDescStartDateDesc();

}
