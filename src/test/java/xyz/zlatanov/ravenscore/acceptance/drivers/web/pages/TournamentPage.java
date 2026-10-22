package xyz.zlatanov.ravenscore.acceptance.drivers.web.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.PageReload;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.Ui;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.StageModal;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.modals.UnlockAdminModal;

import java.util.Optional;

/**
 * A tournament's page: its stages, each with rankings and games.
 */
public class TournamentPage {

    private final Page page;

    public TournamentPage(Page page) {
        this.page = page;
    }

    public String url() {
        return page.url().split("\\?")[0];
    }

    public String name() {
        return page.locator("#tournamentRoot").innerText().trim();
    }

    /**
     * The error message shown at the top of the page, or the generic error page.
     */
    public Optional<String> error() {
        if (page.getByTestId("error-page").count() > 0) {
            return Optional.of(page.getByTestId("error-page").innerText().trim());
        }
        return Ui.textIfPresent(page.getByTestId("tournament-error"));
    }

    public boolean isAdminUnlocked() {
        return page.getByTestId("tournament-admin-mode").isVisible();
    }

    public boolean offersAdminUnlock() {
        return page.getByTestId("tournament-unlock-admin").isVisible();
    }

    public boolean offersStageCreation() {
        return page.getByTestId("stage-new").isVisible();
    }

    public UnlockAdminModal unlockAdmin() {
        page.getByTestId("tournament-unlock-admin").click();
        return new UnlockAdminModal(page);
    }

    public void lockAdmin() {
        PageReload.expectReload(page, () -> page.getByTestId("tournament-lock-admin").click());
    }

    public StageModal newStage() {
        page.getByTestId("stage-new").click();
        return new StageModal(page);
    }

    public boolean showsCompletedIcon(String stageName) {
        return stageTab(stageName).getByTestId("stage-completed-icon").count() > 0;
    }

    /**
     * Selects the stage's tab, unless already selected.
     */
    public StagePane stage(String stageName) {
        val tab = stageTab(stageName);
        if (!Ui.hasClass(tab, "active")) {
            tab.click();
        }
        val pane = page.locator(".stage-data" + Ui.attribute("data-rs-stage-name", stageName));
        page.locator(".stage-data.active.show" + Ui.attribute("data-rs-stage-name", stageName)).waitFor();
        return new StagePane(page, pane);
    }

    /**
     * The currently selected stage.
     */
    public StagePane selectedStage() {
        return stage(page.locator("#stageNav [data-testid=stage-tab].active").getAttribute("data-rs-stage-name"));
    }

    private Locator stageTab(String stageName) {
        return page.locator("#stageNav [data-testid=stage-tab]" + Ui.attribute("data-rs-stage-name", stageName));
    }
}
