package xyz.zlatanov.ravenscore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.zlatanov.ravenscore.model.swordsandravens.SnrGameState;

@Component
public class SnrClient {

    private final WebClient snrWebClient;

    public SnrClient(@Value("${ravenscore.snr.api-url}") String snrApiUrl) {
        snrWebClient = WebClient.create(snrApiUrl);
    }


    public SnrGameState getGameState(String snrGameId) {
        return snrWebClient
                .get()
                .uri("/api/public/game/{snrGameId}?format=json", snrGameId)
                .retrieve()
                .bodyToMono(SnrGameState.class)
                .block();
    }
}
