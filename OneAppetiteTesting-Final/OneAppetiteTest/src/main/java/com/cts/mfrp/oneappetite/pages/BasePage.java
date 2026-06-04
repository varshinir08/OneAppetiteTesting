package com.cts.mfrp.oneappetite.pages;

import com.cts.mfrp.oneappetite.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * PageFactory-style base. Subclasses declare {@code @FindBy} {@link WebElement} fields and call
 * {@code super(driver)} — the constructor invokes {@link PageFactory#initElements(WebDriver, Object)}
 * once for the whole hierarchy.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds", 20)));
        PageFactory.initElements(driver, this);
    }

    /* ------------------------------------------------------------------ */
    /* Wait helpers that work directly on PageFactory-proxied WebElements. */
    /* ------------------------------------------------------------------ */

    protected WebElement waitVisible(WebElement el) {
        return wait.until(ExpectedConditions.visibilityOf(el));
    }

    protected WebElement waitClickable(WebElement el) {
        return wait.until(ExpectedConditions.elementToBeClickable(el));
    }

    protected void click(WebElement el) { waitClickable(el).click(); }

    protected void type(WebElement el, String text) {
        WebElement v = waitVisible(el);
        v.clear();
        v.sendKeys(text);
    }

    /** Truthy if the (proxied) element resolves and is displayed. */
    protected boolean isDisplayed(WebElement el) {
        try {
            return el.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isDisplayed(By by) {
        try {
            WebElement e = driver.findElement(by);
            return e.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean anyDisplayed(List<WebElement> els) {
        if (els == null) return false;
        for (WebElement e : els) {
            try { if (e.isDisplayed()) return true; }
            catch (StaleElementReferenceException | NoSuchElementException ignored) {}
        }
        return false;
    }

    /** Click an element resolved by visible text (for dynamic role/chip/filter labels). */
    protected void clickByText(String text) {
        By by = By.xpath("//*[self::button or self::a or self::div or self::label or self::span][normalize-space()='"
                + text + "']");
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    protected boolean pageContains(String text) {
        return driver.getPageSource() != null
                && driver.getPageSource().toLowerCase().contains(text.toLowerCase());
    }

    protected void pressEscape() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", waitVisible(el));
    }

    public String currentUrl() { return driver.getCurrentUrl(); }
}
