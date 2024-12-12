package xyz.zlatanov.ravenscore.web.service;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.StageForm;

@Service
@RequiredArgsConstructor
public class TournamentAdminService {

	private final TournamentRepository		tournamentRepository;

	private final TournamentStageRepository	tournamentStageRepository;
	private final SubstituteRepository		substituteRepository;
	private final ParticipantRepository		participantRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional
	public void createNewStage(String tournamentKeyHash, @Valid StageForm stageForm) {
		validateAdminRights(tournamentKeyHash, stageForm.getTournamentId());
		if (stageForm.getStageId() == null) {
			createStage(stageForm);
		} else {
			updateStage(stageForm);
		}
	}

	@Transactional
	public void player(String tournamentKeyHash, @Valid PlayerForm playerForm) {
		validateAdminRights(tournamentKeyHash, playerForm.getTournamentId());
		if (playerForm.getTournamentStageId() != null) {
			participant(playerForm);
		} else {
			substitute(playerForm);
		}
	}

	@Transactional
	public void removeStage(String tournamentKeyHash, UUID tournamentId, UUID stageId) {
		validateAdminRights(tournamentKeyHash, tournamentId);
		if (!gameRepository.findByTournamentStageIdOrderByTypeAscNameAsc(stageId).isEmpty()) {
			throw new RavenscoreException("Cannot delete a stage with games.");
		}
		val stage = tournamentStageRepository.findById(stageId)
				.orElseThrow(() -> new RavenscoreException("Invalid stage"));
		val participantIds = stage.participantIdList();
		tournamentStageRepository.delete(stage);
		deleteOrphanedParticipants(tournamentId, participantIds);
	}

	@Transactional
	public void removePlayer(String tournamentKeyHash, UUID tournamentId, UUID stageId, UUID playerId) {
		validateAdminRights(tournamentKeyHash, tournamentId);
		// substitutes can always be deleted
		substituteRepository.findById(playerId).ifPresent(substituteRepository::delete);
		participantRepository.findById(playerId).ifPresent(participant -> deleteParticipant(stageId, participant));
	}

	@Transactional
	public void game(String tournamentKeyHash, UUID tournamentId, @Valid GameForm gameForm) {
		validateAdminRights(tournamentKeyHash, tournamentId);
		if (gameForm.getId() == null) {
			createGame(gameForm);
		} else {
			updateGame(gameForm);
		}
	}

	@Transactional
	public void removeGame(String tournamentKeyHash, UUID tournamentId, UUID gameId) {
		validateAdminRights(tournamentKeyHash, tournamentId);
		gameRepository.deleteById(gameId);
	}

	@Transactional
	public void updateRound(String tournamentKeyHash, UUID tournamentId, UUID gameId, Integer round) {
		validateAdminRights(tournamentKeyHash, tournamentId);
		val game = gameRepository.findById(gameId)
				.orElseThrow(() -> new RavenscoreException("Invalid game"));
		gameRepository.save(game.round(round));
	}

	private void createStage(@Valid StageForm stageForm) {
		tournamentStageRepository.save(new TournamentStage()
				.name(stageForm.getName())
				.tournamentId(stageForm.getTournamentId())
				.qualificationCount(stageForm.getQualificationCount()));
	}

	private void updateStage(@Valid StageForm stageForm) {
		val stage = tournamentStageRepository.findById(stageForm.getStageId())
				.orElseThrow(() -> new RavenscoreException("Invalid stage"));
		tournamentStageRepository.save(stage
				.name(stageForm.getName())
				.qualificationCount(stageForm.getQualificationCount()));
	}

	private void validateAdminRights(String tournamentKeyHash, UUID tournamentId) {
		val tournament = tournamentRepository.findById(tournamentId)
				.orElseThrow(() -> new RavenscoreException("Tournament not found."));
		if (!tournament.validateUnlockHash(tournamentKeyHash)) {
			throw new RavenscoreException("Tournament administration locked.");
		}
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
				.name(playerForm.getName())
				.tournamentId(playerForm.getTournamentId())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void updateSubstitute(PlayerForm playerForm) {
		val substitute = substituteRepository.findById(playerForm.getPlayerId())
				.orElseThrow(() -> new RavenscoreException("Invalid substitute"));
		substituteRepository.save(substitute
				.name(playerForm.getName())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
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
						.name(playerForm.getName())
						.profileLinks(trimEmpty(playerForm.getProfileLinks())));
		val stage = tournamentStageRepository.findById(playerForm.getTournamentStageId())
				.orElseThrow(() -> new RavenscoreException("Invalid tournament stage."));
		val participantIdList = new TreeSet<>(List.of(stage.participantIdList()));
		participantIdList.add(participant.id());
		tournamentStageRepository.save(stage.participantIdList(participantIdList.toArray(new UUID[] {})));
	}

	private void updateParticipant(PlayerForm playerForm) {
		val participant = participantRepository.findById(playerForm.getPlayerId())
				.orElseThrow(() -> new RavenscoreException("Invalid participant"));
		participantRepository.save(participant
				.name(playerForm.getName())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void deleteOrphanedParticipants(UUID tournamentId, UUID[] participantIdList) {
		var orphanCandidateIds = Arrays.copyOf(participantIdList, participantIdList.length);
		val otherStageList = tournamentStageRepository.findByTournamentIdOrderByStartDateDesc(tournamentId);
		for (val otherStage : otherStageList) {
			UUID[] osParticipantIds = otherStage.participantIdList();
			orphanCandidateIds = Arrays.stream(orphanCandidateIds)
					.filter(pId -> !Arrays.asList(osParticipantIds).contains(pId))
					.toArray(UUID[]::new);
		}
		participantRepository.deleteAllById(Arrays.asList(orphanCandidateIds));
	}

	private void deleteParticipant(UUID stageId, Participant participant) {
		if (participant.replacementParticipantId() != null) {
			throw new RavenscoreException("Cannot remove participant that has been replaced.");
		}
		// find tour stages and games manually due to JPA and Postgres limitations on foreign key arrays
		val stage = tournamentStageRepository.findById(stageId).orElseThrow(() -> new RavenscoreException("Invalid stage"));
		val games = gameRepository.findByTournamentStageIdOrderByTypeAscNameAsc(stage.id());
		if (participatesInGames(participant.id(), games)) {
			throw new RavenscoreException("Cannot remove participant that is involved in ongoing stage games.");
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

	private void createGame(GameForm gameForm) {
		val game = gameRepository.save(new Game()
				.type(gameForm.getType())
				.name(gameForm.getName())
				.link(gameForm.getLink())
				.completed(false)
				.tournamentStageId(gameForm.getStageId())
				.participantIdList(gameForm.getParticipantIdList()));
		val defaultPlayers = gameForm.getType().houses()
				.stream()
				.map(house -> new Player()
						.gameId(game.id())
						.house(house))
				.toList();
		playerRepository.saveAll(defaultPlayers);
	}

	private void updateGame(GameForm gameForm) {
		val game = gameRepository.findById(gameForm.getId())
				.orElseThrow(() -> new RavenscoreException("Invalid game"));
		gameRepository.save(game
				.type(gameForm.getType())
				.name(gameForm.getName())
				.link(gameForm.getLink())
				.tournamentStageId(gameForm.getStageId())
				.participantIdList(gameForm.getParticipantIdList()));
		// disassociate player from removed participant
		val players = playerRepository.findByGameIdOrderByRankAscHouseAsc(game.id());
		val updatedPlayers = players.stream()
				.filter(p -> p.participantId() != null)
				.filter(p -> !participatesInGames(p.participantId(), List.of(game)))
				.map(p -> p.participantId(null))
				.toList();
		playerRepository.saveAll(updatedPlayers);
	}

	private String[] trimEmpty(String[] items) {
		val trimmed = Arrays.stream(items)
				.filter(StringUtils::isNotEmpty)
				.distinct()
				.toList();
		return trimmed.toArray(new String[] {});
	}
}
