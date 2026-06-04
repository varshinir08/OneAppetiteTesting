package com.cts.mfrp.oneappetite.pages.vendor;

import com.cts.mfrp.oneappetite.pages.BasePage;
import com.cts.mfrp.oneappetite.pages.employee.CartPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Vendor menu page served at /vendor/{vendorId} by MenuComponent.
 * Item cards use .menu-card, course filters use .filter-chip, dietary chips
 * use .diet-chip, qty stepper uses .qty-stepper with .stepper-btn children.
 */
public class VendorMenuPage extends BasePage {

    @FindBy(xpath = "//button[contains(@class,'filter-chip')][normalize-space()='All']")
    private WebElement allChip;

    @FindAll({
            @FindBy(css = ".search-box input"),
            @FindBy(css = "input[placeholder*='Search' i]")
    })
    private WebElement searchBox;

    @FindAll({
            @FindBy(css = "[data-testid='menu-item']"),
            @FindBy(css = ".menu-card"),
            @FindBy(css = ".menu-item-card")
    })
    private List<WebElement> menuItems;

    @FindBy(css = "button.nav-item-button")
    private WebElement changeLocationPill;

    @FindBy(css = "button.add-btn")
    private List<WebElement> addButtons;

    @FindBy(css = ".menu-card.out-of-stock")
    private List<WebElement> outOfStockCards;

    @FindAll({
            @FindBy(css = "[data-testid='sticky-cart-bar']"),
            @FindBy(css = ".sticky-cart-btn"),
            @FindBy(css = ".cart-pill"),
            @FindBy(css = ".sticky-bottom-bar")
    })
    private List<WebElement> stickyBarEls;

    @FindAll({
            @FindBy(css = ".view-cart-btn"),
            @FindBy(xpath = "//button[contains(normalize-space(),'View Cart')]"),
            @FindBy(xpath = "//a[contains(normalize-space(),'View Cart')]")
    })
    private WebElement viewCartLink;

    public VendorMenuPage(WebDriver driver) { super(driver); }

    public boolean allChipActive() {
        WebElement el = waitVisible(allChip);
        String cls = el.getAttribute("class");
        return cls != null && (cls.contains("active") || cls.contains("selected"));
    }
    public void clickAll()       { clickByText("All"); }
    public void clickBreakfast() { clickByText("Breakfast"); }
    public void clickLunch()     { clickByText("Lunch"); }
    public void clickDinner()    { clickByText("Dinner"); }
    public void clickVeg()       { clickByText("Veg"); }
    public void clickNonVeg()    { clickByText("Non-Veg"); }
    public void search(String q) { type(searchBox, q); }

    public boolean breakfastVisible() { return isDisplayed(By.xpath("//button[contains(@class,'filter-chip')][normalize-space()='Breakfast' or contains(.,'Breakfast')]")); }
    public boolean lunchVisible()     { return isDisplayed(By.xpath("//button[contains(@class,'filter-chip')][normalize-space()='Lunch' or contains(.,'Lunch')]")); }
    public boolean dinnerVisible()    { return isDisplayed(By.xpath("//button[contains(@class,'filter-chip')][normalize-space()='Dinner' or contains(.,'Dinner')]")); }
    public boolean searchVisible()    { return isDisplayed(searchBox); }
    public boolean dietaryToggleVisible() {
        return isDisplayed(By.cssSelector("button.diet-chip.diet-veg"))
                && isDisplayed(By.cssSelector("button.diet-chip.diet-nonveg"));
    }

    public List<WebElement> visibleItems() { return menuItems; }
    public int itemCount() { return menuItems.size(); }

    /**
     * Click the Add button for a specific item by name.
     * Scrolls the button into the viewport center before clicking.
     */
    public void addItemByName(String itemName) {
        By by = By.xpath(
                "//button[contains(@class,'add-btn') and not(@disabled)]"
                        + "[ancestor::*[contains(.,'" + itemName + "')]]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(by));
        scrollToCenter(btn);
        btn.click();
    }

    public void addFirstItem() {
        if (addButtons.isEmpty()) return;
        // Pick the first Add button that's actually enabled. The first .add-btn
        // in DOM order is often disabled (out of stock) — e.g. Chicken Kebab —
        // and waitClickable() would sit there until timeout. Skip those.
        for (WebElement btn : addButtons) {
            if (btn.getAttribute("disabled") != null) continue;
            // Sticky 'View Cart' bar at the bottom of the viewport intercepts clicks
            // on items below the fold — scroll the Add button to the center first.
            scrollToCenter(btn);
            waitClickable(btn).click();
            return;
        }
        throw new IllegalStateException(
                "No enabled Add button found — every visible menu item is either "
                        + "out of stock or already in the cart");
    }

    private void scrollToCenter(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
    }

    public boolean outOfStockOverlayVisible() {
        return !driver.findElements(By.cssSelector(".out-of-stock-overlay")).isEmpty()
                || !outOfStockCards.isEmpty();
    }

    public boolean addButtonDisabledForOutOfStock() {
        for (WebElement card : outOfStockCards) {
            List<WebElement> buttons = card.findElements(By.cssSelector("button.add-btn"));
            if (!buttons.isEmpty() && buttons.get(0).getAttribute("disabled") != null) return true;
        }
        return false;
    }

    public boolean stickyBarVisible()    { return anyDisplayed(stickyBarEls); }
    public boolean viewCartLinkVisible() { return isDisplayed(viewCartLink); }

    public CartPage clickViewCart() {
        WebElement link = waitVisible(viewCartLink);
        scrollToCenter(link);
        waitClickable(link).click();
        return new CartPage(driver);
    }

    public void incrementFirst() {
        WebElement btn = driver.findElement(By.cssSelector(".qty-stepper .stepper-btn.stepper-plus"));
        scrollToCenter(btn);
        btn.click();
    }
    public void decrementFirst() {
        WebElement btn = driver.findElement(By.cssSelector(".qty-stepper .stepper-btn:not(.stepper-plus)"));
        scrollToCenter(btn);
        btn.click();
    }

    public String firstQuantityText() {
        return driver.findElement(By.cssSelector(".qty-stepper .qty-count")).getText().trim();
    }
}
