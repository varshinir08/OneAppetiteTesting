package com.cts.mfrp.oneappetite.pages.employee;

import com.cts.mfrp.oneappetite.pages.BasePage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorMenuPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class DashboardPage extends BasePage {

    @FindBy(xpath = "//h2[contains(.,'Near You')] | //*[contains(.,'Vendors Near You')]")
    private List<WebElement> vendorListHeader;

    @FindAll({
            @FindBy(css = "[data-testid='current-location']"),
            @FindBy(css = ".vendor-location-tag"),
            @FindBy(css = ".current-location-card")
    })
    private List<WebElement> currentLocationCard;

    @FindBy(css = "button.nav-item-button")
    private WebElement changeLocationPill;

    @FindAll({
            @FindBy(css = ".vendor-search input"),
            @FindBy(css = "input[type='search']"),
            @FindBy(css = "input[placeholder*='Search' i]")
    })
    private WebElement searchBox;

    @FindAll({
            @FindBy(css = "[data-testid='vendor-card']"),
            @FindBy(css = ".vendor-card"),
            @FindBy(css = ".vendor-item"),
            @FindBy(css = ".vendor-list-item")
    })
    private List<WebElement> vendorCards;

    @FindBy(xpath = "//*[contains(.,'No vendors match your filters')]")
    private List<WebElement> emptyStateEls;

    /** Reliable locator: every vendor card has exactly one "View Menu" button. */
    private static final By VIEW_MENU_BTN =
            By.xpath("//button[normalize-space()='View Menu']");

    public DashboardPage(WebDriver driver) { super(driver); }

    public boolean vendorListMode()         { return anyDisplayed(vendorListHeader); }
    public boolean currentLocationVisible() { return anyDisplayed(currentLocationCard); }
    public boolean changeLocationVisible()  { return isDisplayed(changeLocationPill); }
    public String vendorListHeaderText()    { return waitVisible(vendorListHeader.get(0)).getText(); }

    public void clickAll()            { clickByText("All"); }
    public void clickVeg()            { clickByText("Veg"); }
    public void clickNonVeg()         { clickByText("Non-Veg"); }
    public void clickChangeLocation() { click(changeLocationPill); }

    /**
     * Return vendor card elements. Uses PageFactory list first (if selectors match);
     * falls back to discovering cards via their guaranteed "View Menu" button.
     */
    public List<WebElement> visibleVendors() {
        if (!vendorCards.isEmpty()) return vendorCards;
        // Innermost ancestor containing both an img and a "View Menu" button = the card.
        return driver.findElements(By.xpath(
                "//button[normalize-space()='View Menu']"
                        + "/ancestor::*[.//img and .//button[normalize-space()='View Menu']][1]"));
    }

    /** Count vendor cards by their "View Menu" buttons — one per card, always present. */
    public int visibleVendorCount() {
        return driver.findElements(VIEW_MENU_BTN).size();
    }

    public void search(String q) { type(searchBox, q); }

    /**
     * Angular binds the filter to the input's (input) DOM event. WebDriver's
     * native .clear() empties the DOM value but does NOT dispatch an input
     * event, so the component's vendorSearch property keeps the stale query
     * and the list stays at 0 results. Select-all + DELETE via sendKeys fires
     * a real input event and Angular re-runs its filter.
     */
    public void clearSearch() {
        WebElement input = waitVisible(searchBox);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
    }

    public boolean emptyStateVisible() { return anyDisplayed(emptyStateEls); }

    /** Click the first available "View Menu" button. */
    public VendorMenuPage openFirstVendorMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(VIEW_MENU_BTN)).click();
        return new VendorMenuPage(driver);
    }

    /**
     * Find the vendor card whose text contains {@code vendorName} and click its
     * "View Menu" button. Throws TimeoutException if the named vendor is not visible.
     */
    public VendorMenuPage openVendorMenuByName(String vendorName) {
        By by = By.xpath(
                "//button[normalize-space()='View Menu']"
                        + "[ancestor::*[contains(normalize-space(),'" + vendorName + "')]]");
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
        return new VendorMenuPage(driver);
    }

    /**
     * Iterates every visible vendor's menu in order, looking for the first one
     * that has at least one out-of-stock item. Returns that VendorMenuPage so the
     * caller can assert on it; returns null if no vendor currently has out-of-stock items.
     *
     * After each failed check the method clicks the browser Back button and waits
     * for the dashboard's "View Menu" buttons to re-appear before trying the next vendor.
     */
    public VendorMenuPage findVendorMenuWithOutOfStock() {
        int total = visibleVendorCount();
        for (int i = 0; i < total; i++) {
            // Re-query every iteration — earlier navigation stales previously resolved elements.
            List<WebElement> buttons = driver.findElements(VIEW_MENU_BTN);
            if (i >= buttons.size()) break;
            buttons.get(i).click();

            VendorMenuPage vendorMenu = new VendorMenuPage(driver);

            // Wait for the menu's item cards or Add buttons to appear (max 15 s).
            // If the vendor's menu is empty or slow, skip it and move on.
            try {
                wait.until(d ->
                        !d.findElements(By.cssSelector("button.add-btn")).isEmpty()
                                || !d.findElements(By.cssSelector(".menu-card")).isEmpty()
                                || !d.findElements(By.cssSelector(".menu-item-card")).isEmpty());
            } catch (TimeoutException ignored) {
                driver.navigate().back();
                wait.until(ExpectedConditions.presenceOfElementLocated(VIEW_MENU_BTN));
                continue;
            }

            if (vendorMenu.outOfStockOverlayVisible()) return vendorMenu;

            driver.navigate().back();
            wait.until(ExpectedConditions.presenceOfElementLocated(VIEW_MENU_BTN));
        }
        return null;
    }
}
