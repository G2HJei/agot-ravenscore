package xyz.zlatanov.ravenscore.acceptance.infrastructure;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;

/**
 * One WireMock server per JVM standing in for swordsandravens.net. Tests never reset it: every game stubbed by a test
 * has its own random Swords & Ravens game id.
 */
public final class SnrWireMockServer {

    private static WireMockServer server;

    private SnrWireMockServer() {
    }

    public static synchronized WireMockServer server() {
        if (server == null) {
            server = new WireMockServer(options().dynamicPort().notifier(new ConsoleNotifier(false)));
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        }
        return server;
    }

    public static String baseUrl() {
        return server().baseUrl();
    }
}
