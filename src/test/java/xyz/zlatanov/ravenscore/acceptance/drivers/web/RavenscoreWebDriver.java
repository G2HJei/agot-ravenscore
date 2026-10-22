package xyz.zlatanov.ravenscore.acceptance.drivers.web;

import com.microsoft.playwright.Page;

import xyz.zlatanov.ravenscore.acceptance.drivers.web.pages.HomePage;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.pages.TournamentPage;

/**
 * Protocol driver for Ravenscore's public interface: the web pages, used through a real browser. It knows about pages,
 * buttons and forms, but nothing about the board game rules or the scenario being tested.
 */
public class RavenscoreWebDriver {

    private final Page   page;
    private final String baseUrl;

    public RavenscoreWebDriver(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public HomePage openHomePage() {
        page.navigate(baseUrl + "/");
        return new HomePage(page);
    }

    public TournamentPage openTournament(String url) {
        page.navigate(url);
        return new TournamentPage(page);
    }

    public TournamentPage currentTournamentPage() {
        return new TournamentPage(page);
    }

    public boolean isShowing(String url) {
        return page.url().split("\\?")[0].equals(url);
    }
}
