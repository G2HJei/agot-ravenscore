package xyz.zlatanov.ravenscore.web.service;

import static xyz.zlatanov.ravenscore.Utils.DATE_FORMATTER;
import static xyz.zlatanov.ravenscore.Utils.capitalizeFirstLetter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.tourdetails.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentDetailsService {

	private final TournamentRepository		tournamentRepository;
	private final ParticipantRepository		participantRepository;
	private final SubstituteRepository		substituteRepository;
	private final TournamentStageRepository	tournamentStageRepository;
	private final GameRepository			gameRepository;
	private final PlayerRepository			playerRepository;

	public TournamentDetailsModel getTournamentDetails(UUID tourId) {
		return tournamentRepository.findById(tourId)
				.map(tour -> new TournamentDetailsModel()
						.id(tourId.toString())
						.name(tour.name())
						.description(tour.description())
						.startDate(DATE_FORMATTER.format(tour.startDate()))
						.substituteModelList(getSubstitutes(tourId))
						.tournamentStageModelList(getTournamentStages(tourId)))
				.orElseThrow(() -> new RuntimeException("Tournament not found."));
	}

	private List<SubstituteModel> getSubstitutes(UUID tourId) {
		return substituteRepository.findByTournamentIdIn(List.of(tourId))
				.stream()
				.map(sub -> new SubstituteModel()
						.name(sub.name())
						.profileLinks(List.of(sub.profileLinks())))
				.toList();
	}

	private List<TournamentStageModel> getTournamentStages(UUID tourId) {
		return tournamentStageRepository.findByTournamentIdIn(List.of(tourId))
				.stream()
				.map(stage -> new TournamentStageModel()
						.name(stage.name())
						.startDate(DATE_FORMATTER.format(stage.startDate()))
						.participantModelList(getParticipants(stage.participantIdList()))
						.gameModelList(getGames(stage.id())))
				.toList();
	}

	private List<GameModel> getGames(UUID id) {
		return gameRepository.findByTournamentStageIdIn(List.of(id))
				.stream().map(g -> new GameModel()
						.name(g.name())
						.type(g.type().toString())
						.link(g.link())
						.round(g.round().toString())
						.playerModelList(getPlayers(g.id())))
				.toList();
	}

	private List<PlayerModel> getPlayers(UUID id) {
		return playerRepository.findByGameIdIn(List.of(id))
				.stream()
				.map(p -> new PlayerModel()
						.name(Optional.ofNullable(p.participantId())
								.map(participantId -> "todo")
								.orElse(capitalizeFirstLetter(p.house())))
						.house(capitalizeFirstLetter(p.house()))
						.castles(p.castles())
						.score(p.score())
						.penaltyPoints(p.penaltyPoints()))
				.toList();
	}

	private List<ParticipantModel> getParticipants(UUID[] participantIds) {
		return participantRepository.findByIdIn(Arrays.stream(participantIds).toList())
				.stream()
				.map(p -> new ParticipantModel())
				// todo rankings and stuff
				.toList();
	}

}
