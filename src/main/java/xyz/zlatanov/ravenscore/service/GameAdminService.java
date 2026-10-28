package xyz.zlatanov.ravenscore.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.zlatanov.ravenscore.domain.model.*;
import xyz.zlatanov.ravenscore.domain.repository.GameRepository;
import xyz.zlatanov.ravenscore.domain.repository.ParticipantRepository;
import xyz.zlatanov.ravenscore.domain.repository.PlayerInactivityRepository;
import xyz.zlatanov.ravenscore.domain.repository.PlayerRepository;
import xyz.zlatanov.ravenscore.domain.repository.TournamentStageRepository;
import xyz.zlatanov.ravenscore.model.swordsandravens.SnrGameState;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.GameForm;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.GameUpdates;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.PlayerRanking;
import xyz.zlatanov.ravenscore.model.tourdetails.admin.RankingsForm;
import xyz.zlatanov.ravenscore.security.TournamentAdminOperation;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static xyz.zlatanov.ravenscore.model.swordsandravens.SnrState.FINISHED;
import static xyz.zlatanov.ravenscore.model.swordsandravens.SnrState.ONGOING;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameAdminService {

    public static final  long   LAST_ACTIVE_HOURS_WARNING_THRESHOLD = 48;
    private static final String SNR_DOMAIN                          = "swordsandravens.net";

    private final GameRepository             gameRepository;
    private final PlayerRepository           playerRepository;
    private final PlayerInactivityRepository playerInactivityRepository;
    private final ParticipantRepository      participantRepository;
    private final TournamentStageRepository  tournamentStageRepository;
    private final SnrClient                  snrClient;

    @Transactional
    @TournamentAdminOperation
    public void game(@Valid GameForm gameForm) {
        validateParticipantCount(gameForm);
        val ptsModifier = validateParticipantPointsModifier(gameForm.getParticipantIdList());
        if (gameForm.getId() == null) {
            createGame(gameForm, ptsModifier);
        } else {
            updateGame(gameForm, ptsModifier);
        }
    }

    @Transactional
    @TournamentAdminOperation
    public void removeGame(UUID gameId) {
        val game = gameRepository.findById(gameId).orElseThrow();
        if (game.completed()) {
            throw new RavenscoreException("Cannot delete a completed game.");
        }
        gameRepository.deleteById(gameId);
    }

    @Transactional
    @TournamentAdminOperation
    public void updateRankings(@Valid RankingsForm rankingsForm) throws RavenscoreException {
        validateRankingsForm(rankingsForm);

        val game = gameRepository.findById(rankingsForm.getGameId()).orElseThrow();
        gameRepository.save(game.completed(rankingsForm.getCompleted()));
        val playerScoringList = rankingsForm.getPlayerRankingList().stream()
                .sorted(Comparator.comparing(PlayerRanking::getPoints))
                .toList();
        playerScoringList.forEach(ps -> {
            val player = playerRepository.findById(ps.getPlayerId()).orElseThrow();
            playerRepository.save(player
                    .castles(ps.getCastles())
                    .penaltyPoints(ps.getPenaltyPoints())
                    .points(ps.getPoints())
                    .participantId(ps.getParticipantId()));
        });
    }

    @Transactional
    @TournamentAdminOperation
    public void updateRound(UUID gameId, Integer round) {
        val game = gameRepository.findById(gameId).orElseThrow();
        gameRepository.save(game.round(round));
    }

    @Transactional
    //not admin operation because it is triggered by cron job
    public void updateSnrLastActive(List<UUID> tournamentIds) {
        if (tournamentIds.isEmpty()) {
            return;
        }
        val stageIds = tournamentStageRepository.findByTournamentIdInOrderByStartDateDesc(tournamentIds).stream()
                .map(TournamentStage::id)
                .toList();
        if (stageIds.isEmpty()) {
            return;
        }
        gameRepository.findByTournamentStageIdInAndCompletedFalseAndLinkContainingIgnoreCase(stageIds, SNR_DOMAIN)
                .forEach(game -> {
                    val snrGameState = getSnrGameState(game);
                    gameRepository.save(game.lastActive(snrGameState.lastActiveAt()));
                    trackInactivePlayers(game, snrGameState);
                });
    }

    // records the players being waited on once the game is inactive past the threshold, keyed by the game's last
    // activity, so later runs only update the duration of the same inactivity
    private void trackInactivePlayers(Game game, SnrGameState snrGameState) {
        if (snrGameState.state() != ONGOING || snrGameState.lastActiveAt() == null) {
            return;
        }
        // whole seconds, so the key is found again regardless of the database's timestamp precision
        val lastActiveAt = snrGameState.lastActiveAt().truncatedTo(ChronoUnit.SECONDS);
        val inactiveHours = hoursSince(lastActiveAt);
        if (inactiveHours < LAST_ACTIVE_HOURS_WARNING_THRESHOLD) {
            return;
        }
        val waitingHouses = waitingHouses(snrGameState.viewOfGame().waitingFor());
        playerRepository.findByGameIdOrderByPointsDesc(game.id()).stream()
                .filter(player -> waitingHouses.contains(player.house()))
                .forEach(player -> {
                    val inactivity = playerInactivityRepository.findByPlayerIdAndStartTime(player.id(), lastActiveAt)
                            .orElseGet(() -> new PlayerInactivity()
                                    .playerId(player.id())
                                    .startTime(lastActiveAt));
                    playerInactivityRepository.save(inactivity.durationHours((int) inactiveHours));
                });
    }

    // waitingFor lists "House (user)" entries, or only "House" in faceless games; entries not starting with a known
    // house are ignored, which tolerates user names containing ", "
    private static Set<House> waitingHouses(String waitingFor) {
        if (waitingFor == null || waitingFor.isBlank()) {
            return Set.of();
        }
        val houses = EnumSet.noneOf(House.class);
        for (val entry : waitingFor.split(", ")) {
            val houseLabel = entry.replaceAll(" \\(.*", "").trim();
            Arrays.stream(House.values())
                    .filter(house -> house.label().equalsIgnoreCase(houseLabel))
                    .findFirst()
                    .ifPresent(houses::add);
        }
        return houses;
    }

    @Transactional
    @TournamentAdminOperation
    public void updateLastActive(UUID gameId, LocalDateTime lastActive) {
        val game = gameRepository.findById(gameId).orElseThrow();
        gameRepository.save(game.lastActive(lastActive));
    }

    private void validateParticipantCount(@Valid GameForm gameForm) {
        val maxNumberOfParticipants = gameForm.getType().houses().size();
        val selectedParticipants = gameForm.getParticipantIdList().length;
        if (selectedParticipants > maxNumberOfParticipants) {
            throw new RavenscoreException(
                    String.format("Maximum number of participants for this type of game is %s.", maxNumberOfParticipants));
        }
    }

    private BigDecimal validateParticipantPointsModifier(UUID[] participantIdList) {
        val ptsModifiers = participantRepository.findByIdInOrderByName(Arrays.stream(participantIdList).toList())
                .stream()
                .map(Participant::pointsModifier)
                .distinct()
                .toList();
        if (ptsModifiers.size() > 1) {
            throw new RavenscoreException("All game participants must be with the same points modifier");
        } else {
            return ptsModifiers.getFirst();
        }
    }

    private void createGame(GameForm gameForm, BigDecimal ptsModifier) {
        val game = gameRepository.save(new Game()
                .type(gameForm.getType())
                .name(gameForm.getName())
                .link(gameForm.getLink())
                .completed(false)
                .tournamentStageId(gameForm.getStageId())
                .participantIdList(gameForm.getParticipantIdList())
                .pointsModifier(ptsModifier));
        val defaultPlayers = gameForm.getType().houses()
                .stream()
                .map(house -> new Player()
                        .gameId(game.id())
                        .house(house))
                .toList();
        playerRepository.saveAll(defaultPlayers);
    }

    private void updateGame(GameForm gameForm, BigDecimal ptsModifier) {
        val game = gameRepository.findById(gameForm.getId()).orElseThrow();
        if (game.completed()) {
            throw new RavenscoreException("Cannot update game details of a completed game.");
        }
        gameRepository.save(game
                .type(gameForm.getType())
                .name(gameForm.getName())
                .link(gameForm.getLink())
                .tournamentStageId(gameForm.getStageId())
                .participantIdList(gameForm.getParticipantIdList())
                .pointsModifier(ptsModifier));
        // disassociate player from removed participant
        val players = playerRepository.findByGameIdOrderByPointsDesc(game.id()).stream()
                .filter(p -> p.participantId() != null)
                .filter(p -> !participatesInGames(p.participantId(), List.of(game)))
                .map(p -> p.participantId(null))
                .toList();
        playerRepository.saveAll(players);
    }

    private boolean participatesInGames(UUID participantId, List<Game> games) {
        return games.stream().flatMap(g -> Arrays.stream(g.participantIdList())).anyMatch(pId -> pId.equals(participantId));
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

    public GameUpdates refreshSnrGame(UUID gameId) {
        val game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RavenscoreException("Game not found"));
        val isSnrGame = game.link().toLowerCase().contains(SNR_DOMAIN);
        if (!isSnrGame) {
            throw new RavenscoreException("This game is not a Swords & Ravens game");
        }
        val snrGameState = getSnrGameState(game);
        return checkForUpdates(game, snrGameState);
    }

    private SnrGameState getSnrGameState(Game game) {
        val snrGameId = game.link()
                .replaceAll(".*swordsandravens\\.net/play/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}).*", "$1");
        return snrClient.getGameState(snrGameId);
    }

    private GameUpdates checkForUpdates(Game game, SnrGameState snrGameState) {
        val snrRound = snrGameState.viewOfGame().round();
        val snrCompleted = snrGameState.state() == FINISHED;
        return new GameUpdates()
                .gameId(game.id())
                .gameName(game.name())
                .round(snrRound > 0 && !snrRound.equals(game.round()) ? snrRound : null)
                .completed(snrCompleted ? true : null)
                .playerRankingList(snrCompleted ? getPlayerRankings(game, snrGameState) : null)
                .inactiveHours(snrCompleted ? null : getInactiveHours(snrGameState.lastActiveAt()))
                .lastActive(isNewLastActive(game, snrGameState.lastActiveAt()) ? snrGameState.lastActiveAt() : null);
    }

    // compared in whole seconds, as the database may not keep all the precision of the SnR timestamp
    private static boolean isNewLastActive(Game game, LocalDateTime snrLastActive) {
        return snrLastActive != null && (game.lastActive() == null
                || !snrLastActive.truncatedTo(ChronoUnit.SECONDS).equals(game.lastActive().truncatedTo(ChronoUnit.SECONDS)));
    }

    private static Long getInactiveHours(LocalDateTime lastActiveAt) {
        if (lastActiveAt == null) {
            return null;
        }
        val inactiveHours = hoursSince(lastActiveAt);
        return inactiveHours >= LAST_ACTIVE_HOURS_WARNING_THRESHOLD ? inactiveHours : null;
    }

    private static long hoursSince(LocalDateTime dateTime) {
        return Duration.between(dateTime, LocalDateTime.now()).toHours();
    }

    private ArrayList<PlayerRanking> getPlayerRankings(Game game, SnrGameState snrGameState) {
        val playerRankingList = new ArrayList<PlayerRanking>();
        val players = playerRepository.findByGameIdOrderByPointsDesc(game.id());
        val participants = participantRepository.findByIdInOrderByName(Arrays.stream(game.participantIdList()).toList());
        val victoryTrack = snrGameState.viewOfGame().victoryTrack();
        for (int rank = 1; rank <= victoryTrack.size(); rank++) {
            val snrPlayer = victoryTrack.get(rank - 1);
            val player = findPlayer(players, snrPlayer.house());
            val participant = findParticipant(participants, snrPlayer.name());
            playerRankingList.add(
                    new PlayerRanking()
                            .setPlayerId(player.id())
                            .setHouse(player.house().label())
                            .setParticipantId(participant.id())
                            .setParticipantName(participant.name())
                            .setPoints(victoryTrack.size() - rank + snrPlayer.victoryPoints() + (rank == 1 ? 3 : 0))
                            .setCastles(snrPlayer.victoryPoints())
                            .setPenaltyPoints(player.penaltyPoints()));
        }
        return playerRankingList;
    }

    private static Participant findParticipant(List<Participant> participants, String playerName) {
        return participants.stream()
                .filter(p -> hasSameNameOrAlias(p, playerName))
                .findFirst()
                .orElseThrow();
    }

    private static boolean hasSameNameOrAlias(Participant participant, String playerName) {
        val playerNameMatch = participant.name().equalsIgnoreCase(playerName);
        val aliasMatch = Arrays.stream(participant.profileLinks())
                .anyMatch(pl -> pl.equalsIgnoreCase(playerName));
        return playerNameMatch || aliasMatch;
    }

    private static Player findPlayer(List<Player> players, House house) {
        return players.stream()
                .filter(p -> p.house() == house)
                .findFirst()
                .orElseThrow();
    }
}
