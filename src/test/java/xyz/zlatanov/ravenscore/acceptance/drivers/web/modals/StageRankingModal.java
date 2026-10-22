package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.PageReload;

import java.util.List;

/**
 * "Stage Final Rankings": players can be rearranged by drag and drop before the stage is completed.
 */
public class StageRankingModal extends Modal {

    public StageRankingModal(Page page) {
        super(page, "#rankingStageModal");
    }

    public List<String> players() {
        return rows().locator("span").allInnerTexts().stream().map(String::trim).toList();
    }

    public StageRankingModal movePlayerAbove(String player, String otherPlayer) {
        row(player).dragTo(row(otherPlayer));
        return this;
    }

    /**
     * Completes the stage; the browser's "Are you sure...?" confirmation is accepted.
     */
    public void done() {
        PageReload.expectReload(page, () -> root.getByTestId("modal-done").click());
    }

    private Locator rows() {
        return root.getByTestId("stage-ranking-row");
    }

    private Locator row(String player) {
        return rows().filter(new Locator.FilterOptions().setHas(page.getByText(player, new Page.GetByTextOptions().setExact(true))));
    }
}
