package xyz.zlatanov.ravenscore.acceptance.drivers.web.modals;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import lombok.val;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.PageReload;
import xyz.zlatanov.ravenscore.acceptance.drivers.web.model.SnrReport;

/**
 * "Refresh Swords & Ravens Games Status": checks every unfinished game of the stage on Swords & Ravens, then
 * saves the updates once confirmed.
 */
public class RefreshSnrModal extends Modal {

    public RefreshSnrModal(Page page) {
        super(page, "#refreshSnrGamesModal");
    }

    /**
     * Waits until every game has been checked.
     */
    public SnrReport report() {
        root.locator("#updateSnrStatusBar:not(.visually-hidden)")
                .or(root.locator("#snrProgress").filter(new Locator.FilterOptions().setHasText("No updates were found.")))
                .first()
                .waitFor();
        return SnrReport.parse(root.locator("#snrProgress").innerText(),
                root.getByTestId("snr-inactivity-warning").allInnerTexts().stream().map(String::trim).toList());
    }

    /**
     * "Confirm" saves the updates, then "Close" reloads the page.
     */
    public void confirm() {
        root.locator("#updateSnrStatusBtn").click();
        val closeButton = root.locator("#closeRefreshSnrModalBtn:not(.visually-hidden)");
        closeButton.waitFor();
        PageReload.expectReload(page, closeButton::click);
    }
}
