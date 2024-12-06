package xyz.zlatanov.ravenscore.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import xyz.zlatanov.ravenscore.domain.domain.Participant;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
}
