package xyz.zlatanov.ravenscore.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.zlatanov.ravenscore.domain.model.Tournament;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class SnrLastActiveJob {


    private final TournamentRepository tournamentRepository;
    private final GameAdminService     gameAdminService;

    // runs at hh:00, hh:10, ..., hh:50 and updates only the tournaments last updated within the current 10-minute
    // interval regardless of the hour (e.g. at 12:00 -> hh:00 - hh:09), so each tournament is updated once per hour
    @Scheduled(cron = "0 */10 * * * *")
    public void updateSnrLastActive() {
        val fromMinute = (LocalTime.now().getMinute() / 10) * 10;
        val tournamentIds = tournamentRepository.findByLastUpdatedMinuteBetween(fromMinute, fromMinute + 9)
                .stream()
                .map(Tournament::id)
                .toList();
        gameAdminService.updateSnrLastActive(tournamentIds);
    }
}
