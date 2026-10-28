package xyz.zlatanov.ravenscore.acceptance.drivers.jobs;

import lombok.val;
import xyz.zlatanov.ravenscore.service.GameAdminService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Protocol driver for Ravenscore's scheduled jobs. They are switched off during tests (see
 * application-acceptance.properties), so a test runs them on demand instead, for its own tournament only.
 */
public class ScheduledJobsDriver {

    private final GameAdminService gameAdminService;

    public ScheduledJobsDriver(GameAdminService gameAdminService) {
        this.gameAdminService = gameAdminService;
    }

    /**
     * Runs the hourly Swords & Ravens update (SnrLastActiveJob) for the tournament shown at the given page URL.
     */
    public void updateSwordsAndRavensGames(String tournamentUrl) {
        val path = URI.create(tournamentUrl).getPath();
        val tournamentId = UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
        gameAdminService.updateSnrLastActive(List.of(tournamentId));
    }
}
