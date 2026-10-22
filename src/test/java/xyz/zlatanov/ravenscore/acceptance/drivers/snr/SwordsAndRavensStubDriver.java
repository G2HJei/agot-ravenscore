package xyz.zlatanov.ravenscore.acceptance.drivers.snr;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.UUID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;

/**
 * Protocol driver for the external Swords & Ravens site, which Ravenscore calls for the state of online games. A
 * WireMock server plays its part; when a game is stubbed again, the latest state wins.
 */
public class SwordsAndRavensStubDriver {

    private final WireMockServer server;

    public SwordsAndRavensStubDriver(WireMockServer server) {
        this.server = server;
    }

    public void stubGame(UUID snrGameId, SnrGameJson game) {
        server.stubFor(gameRequest(snrGameId).willReturn(okJson(game.toJson())));
    }

    public void stubUnavailable(UUID snrGameId) {
        server.stubFor(gameRequest(snrGameId).willReturn(serviceUnavailable()));
    }

    private static MappingBuilder gameRequest(UUID snrGameId) {
        return get(urlPathEqualTo("/api/public/game/" + snrGameId)).withQueryParam("format", equalTo("json"));
    }
}
