package xyz.zlatanov.ravenscore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.zlatanov.ravenscore.domain.repository.TournamentRepository;

import static java.util.concurrent.TimeUnit.MINUTES;

@Component
@RequiredArgsConstructor
public class SnrLastActiveJob {

    private final TournamentRepository tournamentRepository;
    private final GameAdminService     gameAdminService;

    @Scheduled(fixedRate = 10, timeUnit = MINUTES)
    public void updateSnrLastActive() {
        //todo run every 10 minutes and trigger update only for tournaments who fall in the current 10-minute interval. For example if the @Scheduled is running at 12:02, update for all tournaments within the hh:00 - hh:09 (incl) minute range regardless of the hour. This way only 1/6th of the tournaments will be updated at any given time.
        gameAdminService.updateSnrLastActive();
    }
}
