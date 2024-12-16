package xyz.zlatanov.ravenscore.web.service;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.Utils;
import xyz.zlatanov.ravenscore.domain.domain.*;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.tourdetails.admin.*;
import xyz.zlatanov.ravenscore.web.model.toursummary.TournamentForm;
import xyz.zlatanov.ravenscore.web.service.security.TourAdminOperation;
import xyz.zlatanov.ravenscore.web.service.security.TourInfoService;
import xyz.zlatanov.ravenscore.web.service.security.TournamentId;

@Service
@RequiredArgsConstructor
@Validated
public class TournamentAdminService {

	private final TourInfoService			tourInfoService;

	private final TournamentRepository		tournamentRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final SubstituteRepository		substituteRepository;
	private final ParticipantRepository		participantRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	@Transactional
	public UUID createTournament(@Valid TournamentForm tournamentForm) {
		val tournament = tournamentRepository.save(new Tournament()
				.name(tournamentForm.getName())
				.scoring(tournamentForm.getScoring())
				.description(tournamentForm.getDescription())
				.hidden(tournamentForm.getHidden())
				.tournamentKey(tournamentForm.getTournamentKey()));
		return tournament.id();
	}

	@Transactional
	@TourAdminOperation
	public UUID updateTournament(@Valid TournamentForm tournamentForm) {
		val tournament = tournamentRepository.findById(tournamentForm.getId())
				.orElseThrow(() -> new RavenscoreException("Invalid tournament"));
		tournamentRepository.save(tournament
				.id(tourInfoService.getTournamentId())
				.name(tournamentForm.getName())
				.scoring(tournamentForm.getScoring())
				.description(tournamentForm.getDescription())
				.hidden(tournamentForm.getHidden()));
		return tournamentForm.getId();
	}

	@Transactional
	@TourAdminOperation
	public void createNewStage(@Valid StageForm stageForm) {
		if (stageForm.getStageId() == null) {
			createStage(stageForm);
		} else {
			updateStage(stageForm);
		}
	}

	@Transactional
	@TourAdminOperation
	public void player(@Valid PlayerForm playerForm) {
		validatePlayerName(playerForm.getName().trim(), playerForm.getTournamentId());
		if (playerForm.getTournamentStageId() != null) {
			participant(playerForm);
		} else {
			substitute(playerForm);
		}
	}

	@Transactional
	@TourAdminOperation
	public void removeStage(@TournamentId UUID tournamentId, UUID stageId) {
		if (!gameRepository.findByTournamentStageIdOrderByTypeAscNameAsc(stageId).isEmpty()) {
			throw new RavenscoreException("Cannot delete a stage with existing games.");
		}
		val stage = tournamentStageRepository.findById(stageId)
				.orElseThrow(() -> new RavenscoreException("Invalid stage"));
		val participantIds = stage.participantIdList();
		tournamentStageRepository.delete(stage);
		deleteOrphanedParticipants(tournamentId, participantIds);
	}

	@Transactional
	@TourAdminOperation
	public void removePlayer(@TournamentId UUID tournamentId, UUID stageId, UUID playerId) {
		// substitutes can always be deleted
		substituteRepository.findById(playerId).ifPresent(substituteRepository::delete);
		participantRepository.findById(playerId).ifPresent(participant -> deleteParticipant(stageId, participant));
	}

	@Transactional
	@TourAdminOperation
	public void substitution(@TournamentId UUID tournamentId, UUID stageId, UUID participantId, UUID substituteId) {
		val oldParticipant = participantRepository.findById(participantId)
				.orElseThrow(() -> new RavenscoreException("Invalid participant"));

		val substitute = substituteRepository.findById(substituteId).orElseThrow(() -> new RavenscoreException("Invalid substitute"));
		val newParticipantId = participantRepository.save(new Participant()
				.name(substitute.name())
				.profileLinks(substitute.profileLinks()))
				.id();
		participantRepository.save(oldParticipant.replacementParticipantId(newParticipantId));
		substituteRepository.deleteById(substituteId);

		val stage = tournamentStageRepository.findById(stageId).orElseThrow(() -> new RavenscoreException("Invalid stage"));
		tournamentStageRepository.save(stage.participantIdList(Utils.addToArray(newParticipantId, stage.participantIdList())));

		var games = gameRepository.findByTournamentStageIdOrderByTypeAscNameAsc(stageId);
		games = games.stream()
				.map(game -> game.participantIdList(Utils.replaceInArray(oldParticipant.id(), newParticipantId, game.participantIdList())))
				.toList();
		gameRepository.saveAll(games);

		val players = playerRepository.findByGameIdInOrderByPointsDesc(games.stream().map(Game::id).toList())
				.stream()
				.filter(p -> Objects.equals(p.participantId(), oldParticipant.id()))
				.map(p -> p.participantId(newParticipantId))
				.toList();
		playerRepository.saveAll(players);

	}

	@Transactional
	@TourAdminOperation
	public void game(@TournamentId UUID tournamentId, @Valid GameForm gameForm) {
		if (gameForm.getId() == null) {
			createGame(gameForm);
		} else {
			updateGame(gameForm);
		}
	}

	@Transactional
	@TourAdminOperation
	public void removeGame(@TournamentId UUID tournamentId, UUID gameId) {
		val game = gameRepository.findById(gameId).orElseThrow(() -> new RavenscoreException("Invalid game"));
		if (game.completed()) {
			throw new RavenscoreException("Cannot delete a completed game.");
		}
		gameRepository.deleteById(gameId);
	}

	@Transactional
	@TourAdminOperation
	public void updateRound(@TournamentId UUID tournamentId, UUID gameId, Integer round) {
		val game = gameRepository.findById(gameId).orElseThrow(() -> new RavenscoreException("Invalid game"));
		gameRepository.save(game.round(round));
	}

	@Transactional
	@TourAdminOperation
	public void updateRankings(@TournamentId UUID tournamentId, @Valid RankingsForm rankingsForm) throws RavenscoreException {
		validateRankingsForm(rankingsForm);

		val game = gameRepository.findById(rankingsForm.getGameId()).orElseThrow(() -> new RavenscoreException("Invalid game"));
		gameRepository.save(game.completed(rankingsForm.getCompleted()));
		val playerScoringList = rankingsForm.getPlayerRankingList().stream()
				.sorted(Comparator.comparing(PlayerRanking::getPoints))
				.toList();
		playerScoringList.forEach(ps -> {
			val player = playerRepository.findById(ps.getPlayerId()).orElseThrow(() -> new RavenscoreException("Invalid player"));
			playerRepository.save(player
					.castles(ps.getCastles())
					.penaltyPoints(ps.getPenaltyPoints())
					.points(ps.getPoints())
					.participantId(ps.getParticipantId()));
		});
	}

	@Transactional
	@TourAdminOperation
	public void importParticipants(@Valid UUID tournamentId, @Valid ImportParticipantsForm importParticipantsForm) {
		val stage = tournamentStageRepository.findById(importParticipantsForm.getStageId())
				.orElseThrow(() -> new RavenscoreException("Invalid stage"));
		val stageParticipants = participantRepository.findByIdInOrderByName(Arrays.asList(stage.participantIdList()));
		val stageParticipantsNames = stageParticipants.stream().map(Participant::name).toList();
		val selectedParticipants = participantRepository
				.findByIdInOrderByName(Arrays.asList(importParticipantsForm.getParticipantIdList()));
		val selectedParticipantsNames = selectedParticipants.stream().map(Participant::name).toList();
		val commonNames = stageParticipantsNames.stream()
				.filter(selectedParticipantsNames::contains)
				.toList();
		if (!commonNames.isEmpty()) {
			throw new RavenscoreException(String.format("Participant(s) already in present stage: %s", String.join(", ", commonNames)));
		}

		// todo make stage complete
		// todo do not show existing participants in import modal

		val clonedParticipants = participantRepository.saveAll(selectedParticipants.stream()
				.map(Participant::clone)
				.toList());
		var newStageParticipantIds = stage.participantIdList();
		for (val cloned : clonedParticipants) {
			newStageParticipantIds = Utils.addToArray(cloned.id(), newStageParticipantIds);
		}
		tournamentStageRepository.save(stage.participantIdList(newStageParticipantIds));
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

	private void validatePlayerName(String name, UUID tournamentId) {
		val participantIds = tournamentStageRepository.findByTournamentIdOrderByStartDateDesc(tournamentId).stream()
				.map(TournamentStage::participantIdList)
				.flatMap(Arrays::stream)
				.toList();
		val participantNames = participantRepository.findByIdInOrderByName(participantIds).stream()
				.map(Participant::name)
				.toList();
		val substituteNames = substituteRepository.findByTournamentIdOrderByName(tournamentId).stream()
				.map(Substitute::name)
				.toList();
		if (participantNames.contains(name)) {
			throw new RavenscoreException(String.format("Participant with the name \"%s\" already exists.", name));
		}
		if (substituteNames.contains(name)) {
			throw new RavenscoreException(String.format("Substitute with the name \"%s\" already exists.", name));
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
				.name(playerForm.getName().trim())
				.tournamentId(playerForm.getTournamentId())
				.profileLinks(trimEmpty(playerForm.getProfileLinks())));
	}

	private void updateSubstitute(PlayerForm playerForm) {
		val substitute = substituteRepository.findById(playerForm.getPlayerId())
				.orElseThrow(() -> new RavenscoreException("Invalid substitute"));
		substituteRepository.save(substitute
				.name(playerForm.getName().trim())
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
						.name(playerForm.getName().trim())
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
				.name(playerForm.getName().trim())
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
			throw new RavenscoreException("Cannot remove participant already involved in games.");
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
		val players = playerRepository.findByGameIdOrderByPointsDesc(game.id());
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

	private void validateRankingsForm(RankingsForm rankingsForm) {
		if (rankingsForm.getCompleted() && hasUnrevealedPlayers(rankingsForm)) {
			throw new RavenscoreException("Cannot complete game without revealing all players!");
		}
		if (hasDuplicatedParticipants(rankingsForm)) {
			throw new RavenscoreException("Same player selected for more than one house!");
		}
	}

	private boolean hasUnrevealedPlayers(RankingsForm rankingsForm) {
		return rankingsForm.getPlayerRankingList().stream()
				.map(PlayerRanking::getParticipantId)
				.anyMatch(Objects::isNull);
	}

	private boolean hasDuplicatedParticipants(RankingsForm rankingsForm) {
		val participantIdList = rankingsForm.getPlayerRankingList().stream()
				.map(PlayerRanking::getParticipantId)
				.filter(Objects::nonNull)
				.toList();
		val distinctParticipantIdSize = participantIdList.stream().distinct().toList().size();
		return participantIdList.size() != distinctParticipantIdSize;
	}
}
