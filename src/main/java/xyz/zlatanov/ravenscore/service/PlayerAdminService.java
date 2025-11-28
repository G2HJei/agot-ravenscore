package xyz.zlatanov.ravenscore.service;

import static xyz.zlatanov.ravenscore.Utils.*;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.model.Game;
import xyz.zlatanov.ravenscore.domain.model.Participant;
import xyz.zlatanov.ravenscore.domain.model.Substitute;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.PlayerForm;
import xyz.zlatanov.ravenscore.security.TournamentAdminOperation;

@Service
@RequiredArgsConstructor
public class PlayerAdminService {

	private final TournamentStageRepository	tournamentStageRepository;
	private final ParticipantRepository		participantRepository;
	private final SubstituteRepository		substituteRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional
	@TournamentAdminOperation
	public void player(@Valid PlayerForm playerForm) {
		validatePlayerName(playerForm);
		if (playerForm.getTournamentStageId() != null) {
			participant(playerForm);
		} else {
			substitute(playerForm);
		}
	}

	@Transactional
	@TournamentAdminOperation
	public void removePlayer(UUID stageId, UUID playerId) {
		substituteRepository.findById(playerId).ifPresent(substituteRepository::delete);// substitutes can always be deleted
		participantRepository.findById(playerId).ifPresent(participant -> deleteParticipant(stageId, participant));
	}

	@Transactional
	@TournamentAdminOperation
	public void substitution(UUID participantId, UUID substituteId) {
		val oldParticipant = participantRepository.findById(participantId).orElseThrow();
		val substitute = substituteRepository.findById(substituteId).orElseThrow();
		val newParticipantId = participantRepository.save(new Participant()
				.name(substitute.name())
				.profileLinks(substitute.profileLinks()))
				.id();
		participantRepository.save(oldParticipant.replacementParticipantId(newParticipantId));
		substituteRepository.deleteById(substituteId);

		val stages = tournamentStageRepository.findByParticipantIdListContains(oldParticipant.id());
		stages.forEach(s -> s.participantIdList(addToArray(newParticipantId, s.participantIdList())));
		tournamentStageRepository.saveAll(stages);

		val games = gameRepository.findByParticipantIdListContains(oldParticipant.id());
		games.forEach(g -> g.participantIdList(replaceInArray(oldParticipant.id(), newParticipantId, g.participantIdList())));
		gameRepository.saveAll(games);

		val players = playerRepository.findByGameIdInAndParticipantId(games.stream().map(Game::id).toList(), oldParticipant.id());
		players.forEach(p -> p.participantId(newParticipantId));
		playerRepository.saveAll(players);
	}

	private void validatePlayerName(PlayerForm playerForm) {
		final List<String> participantNames;
		if (playerForm.getTournamentStageId() == null) { // case of substitute
			participantNames = List.of();
		} else {
			val participantIds = participantRepository.findByTournamentId(playerForm.getTournamentId())
					.stream()
					.map(Participant::id)
					.toList();
			participantNames = participantRepository.findByIdInOrderByName(participantIds).stream()
					.filter(p -> !p.id().equals(playerForm.getPlayerId()))
					.map(Participant::name)
					.toList();
		}

		val substituteNames = substituteRepository.findByTournamentIdOrderByName(playerForm.getTournamentId()).stream()
				.filter(s -> !s.id().equals(playerForm.getPlayerId()))
				.map(Substitute::name)
				.toList();

		val name = playerForm.getName().trim();
		if (participantNames.contains(name)) {
			throw new RavenscoreException(String.format(
					"Player with the name \"%s\" is already in this tournament. You can use the import function to include it in the selected stage.",
					name));
		}
		if (substituteNames.contains(name)) {
			throw new RavenscoreException(String.format("Substitute with the name \"%s\" already exists.", name));
		}
	}

	private void participant(PlayerForm playerForm) {
		if (playerForm.getPlayerId() == null) {
			createParticipant(playerForm);
		} else {
			updateParticipant(playerForm);
		}
	}

	private void createParticipant(PlayerForm playerForm) {
		val participant = participantRepository.save(
				new Participant()
						.name(playerForm.getName().trim())
						.profileLinks(trimEmpty(playerForm.getProfileLinks())));
		val stage = tournamentStageRepository.findById(playerForm.getTournamentStageId()).orElseThrow();
		val participantIdList = new TreeSet<>(List.of(stage.participantIdList()));
		participantIdList.add(participant.id());
		tournamentStageRepository.save(stage.participantIdList(participantIdList.toArray(new UUID[] {})));
	}

	private void updateParticipant(PlayerForm playerForm) {
		val participant = participantRepository.findById(playerForm.getPlayerId()).orElseThrow();
		participantRepository.save(participant
				.name(playerForm.getName().trim())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void substitute(PlayerForm playerForm) {
		if (playerForm.getPlayerId() == null) {
			createSubstitute(playerForm);
		} else {
			updateSubstitute(playerForm);
		}
	}

	private void createSubstitute(PlayerForm playerForm) {
		substituteRepository.save(new Substitute()
				.name(playerForm.getName().trim())
				.tournamentId(playerForm.getTournamentId())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void updateSubstitute(PlayerForm playerForm) {
		val substitute = substituteRepository.findById(playerForm.getPlayerId()).orElseThrow();
		substituteRepository.save(substitute
				.name(playerForm.getName().trim())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void deleteParticipant(UUID stageId, Participant participant) {
		if (participant.replacementParticipantId() != null) {
			throw new RavenscoreException("Cannot remove participant that has been replaced.");
		}
		// find tour stages and games manually due to JPA and Postgres limitations on foreign key arrays
		val stage = tournamentStageRepository.findById(stageId).orElseThrow();
		val games = gameRepository.findByTournamentStageIdOrderByTypeAscNameAsc(stage.id());
		if (participatesInGames(participant.id(), games)) {
			throw new RavenscoreException("Cannot remove participant already involved in games in the selected stage.");
		}
		tournamentStageRepository.save(stage
				.participantIdList(Arrays.stream(stage.participantIdList())
						.filter(pId -> !pId.equals(participant.id()))
						.toArray(UUID[]::new)));
		participantRepository.delete(participant);
	}

	private boolean participatesInGames(UUID participantId, List<Game> games) {
		return games.stream().flatMap(g -> Arrays.stream(g.participantIdList())).anyMatch(pId -> pId.equals(participantId));
	}
}
