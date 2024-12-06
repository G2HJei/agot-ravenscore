package xyz.zlatanov.ravenscore.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.model.Tournament;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {
}
