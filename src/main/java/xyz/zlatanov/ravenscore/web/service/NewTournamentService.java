package xyz.zlatanov.ravenscore.web.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.ravenscore.domain.domain.Tournament;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;
import xyz.zlatanov.ravenscore.web.model.toursummary.NewTournamentForm;

@Service
@RequiredArgsConstructor
public class NewTournamentService {

	private final TournamentRepository tournamentRepository;

	@Transactional
	public UUID newTournament(@Valid NewTournamentForm form) {
		val tournament = tournamentRepository.save(new Tournament()
				.name(form.getName())
				.scoring(form.getScoring())
				.description(form.getDescription())
				.hidden(form.getHidden())
				.tournamentKey(form.getTournamentKey()));
		return tournament.id();
	}
}
