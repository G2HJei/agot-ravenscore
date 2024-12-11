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
import xyz.zlatanov.ravenscore.domain.domain.Game;
import xyz.zlatanov.ravenscore.domain.domain.Participant;
import xyz.zlatanov.ravenscore.domain.domain.Substitute;
import xyz.zlatanov.ravenscore.domain.domain.TournamentStage;
import xyz.zlatanov.ravenscore.domain.repository.*;
import xyz.zlatanov.ravenscore.web.model.tourdetails.PlayerForm;
import xyz.zlatanov.ravenscore.web.model.tourdetails.StageForm;

@Service
@RequiredArgsConstructor
public class TournamentAdminService {

	private final TournamentRepository		tournamentRepository;

	private final TournamentStageRepository	tournamentStageRepository;
	private final SubstituteRepository		substituteRepository;
	private final ParticipantRepository		participantRepository;
	private final GameRepository			gameRepository;

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
	public void removePlayer(String tournamentKeyHash, UUID tournamentId, UUID stageId, UUID playerId) {
		validateAdminRights(tournamentKeyHash, tournamentId);
		// substitutes can always be deleted
		substituteRepository.findById(playerId).ifPresent(substituteRepository::delete);
		participantRepository.findById(playerId).ifPresent(participant -> deleteParticipant(stageId, participant));
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

	private static boolean participatesInGames(UUID participantId, List<Game> games) {
		return games.stream().flatMap(g -> Arrays.stream(g.participantIdList())).anyMatch(pId -> pId.equals(participantId));
	}

	private String[] trimEmpty(String[] items) {
		val trimmed = Arrays.stream(items)
				.filter(StringUtils::isNotEmpty)
				.distinct()
				.toList();
		return trimmed.toArray(new String[] {});
	}
}
