package xyz.zlatanov.ravenscore.acceptance.drivers.snr;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A Swords & Ravens game as returned by {@code GET /api/public/game/{id}?format=json}, limited to the fields
 * Ravenscore reads. The round and victory track are always present, like on the real API.
 */
public class SnrGameJson {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final ObjectNode root         = JSON.createObjectNode();
    private final ObjectNode viewOfGame   = root.putObject("view_of_game");
    private final ArrayNode  victoryTrack = viewOfGame.putArray("victoryTrack");

    public SnrGameJson(UUID id, String name) {
        root.put("id", id.toString());
        root.put("name", name);
        state("ONGOING");
        round(1);
        waitingFor("");
        lastActiveAt(Instant.now());
    }

    /** IN_LOBBY, ONGOING, FINISHED, CLOSED or CANCELLED */
    public SnrGameJson state(String state) {
        root.put("state", state);
        return this;
    }

    public SnrGameJson round(int round) {
        viewOfGame.put("round", round);
        return this;
    }

    /** e.g. "Stark (alice), Lannister (bob)", only the houses in faceless games, empty when nobody is waited on */
    public SnrGameJson waitingFor(String waitingFor) {
        viewOfGame.put("waitingFor", waitingFor);
        return this;
    }

    public SnrGameJson lastActiveAt(Instant lastActiveAt) {
        root.put("last_active_at", lastActiveAt.toString());
        return this;
    }

    /** Adds the next player on the victory track, i.e. in finishing order once the game is over. */
    public SnrGameJson player(String house, String player, int victoryPoints, int landAreas, int supply, int ironThrone) {
        victoryTrack.addObject()
                .put("house", house.toLowerCase())
                .put("player", player)
                .put("victoryPoints", victoryPoints)
                .put("totalLandAreas", landAreas)
                .put("supplyLevel", supply)
                .put("ironThronePosition", ironThrone);
        return this;
    }

    public String toJson() {
        return root.toString();
    }
}
