package com.cts.mfrp.oneappetite.pages.auth;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * CreateAccountPage — uses direct driver.findElement() instead of PageFactory @FindBy
 * because Angular's formcontrolname attribute is not supported by Selenium CSS selectors.
 * URL confirmed: /register
 */
public class CreateAccountPage extends BasePage {

    public CreateAccountPage(WebDriver driver) { super(driver); }

    // -----------------------------------------------------------------------
    // Page state — confirmed URL is /register
    // -----------------------------------------------------------------------
    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("register");
    }

    // -----------------------------------------------------------------------
    // Field finders — called fresh every time
    // Confirmed from browser inspection:
    //   fullName  → placeholder="John Doe"
    //   email     → type="email"
    //   phone     → type="tel", placeholder="10-digit number"
    //   password  → placeholder="Min 8 characters"
    // -----------------------------------------------------------------------
    private WebElement fullName() {
        return driver.findElement(By.cssSelector("input[placeholder='John Doe']"));
    }

    private WebElement workEmail() {
        return driver.findElement(By.cssSelector("input[type='email']"));
    }

    private WebElement phone() {
        return driver.findElement(By.cssSelector("input[type='tel']"));
    }

    private WebElement password() {
        return driver.findElement(By.cssSelector("input[placeholder='Min 8 characters']"));
    }

    private WebElement createBtn() {
        return driver.findElement(By.cssSelector("button.auth-submit"));
    }

    // Confirmed: <a href="/login">Sign in</a>
    private WebElement signInLink() {
        return driver.findElement(By.xpath("//a[@href='/login']"));
    }

    // -----------------------------------------------------------------------
    // Visibility checks
    // -----------------------------------------------------------------------
    public boolean fullNameVisible() {
        try { return fullName().isDisplayed(); } catch (Exception e) { return false; }
    }

    public boolean workEmailVisible() {
        try { return workEmail().isDisplayed(); } catch (Exception e) { return false; }
    }

    public boolean phoneVisible() {
        try { return phone().isDisplayed(); } catch (Exception e) { return false; }
    }

    public boolean passwordVisible() {
        try { return password().isDisplayed(); } catch (Exception e) { return false; }
    }

    public boolean createBtnVisible() {
        try { return createBtn().isDisplayed(); } catch (Exception e) { return false; }
    }

    public boolean signInLinkVisible() {
        try { return signInLink().isDisplayed(); } catch (Exception e) { return false; }
    }

    // -----------------------------------------------------------------------
    // Role chips — confirmed: div.role-grid > button.role-chip > span
    // -----------------------------------------------------------------------
    public boolean roleChipVisible(String role) {
        try {
            return driver.findElement(
                    By.xpath("//div[contains(@class,'role-grid')]//span[normalize-space()='" + role + "']")
            ).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public void selectRole(String role) {
        try {
            driver.findElement(
                    By.xpath("//div[contains(@class,'role-grid')]//span[normalize-space()='" + role + "']")
            ).click();
        } catch (Exception e) {
            System.out.println("[selectRole] Could not click: " + role + " — " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Vendor-specific fields
    // -----------------------------------------------------------------------
    public boolean vendorFieldsVisible() {
        try {
            List<WebElement> els = driver.findElements(
                    By.xpath("//input[contains(@placeholder,'vendor') or contains(@placeholder,'Vendor') " +
                            "or contains(@placeholder,'building') or contains(@placeholder,'Building') " +
                            "or contains(@placeholder,'description') or contains(@placeholder,'Description')]")
            );
            if (!els.isEmpty()) return els.get(0).isDisplayed();
            // Also try textarea
            List<WebElement> areas = driver.findElements(By.cssSelector("textarea"));
            return !areas.isEmpty() && areas.get(0).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // -----------------------------------------------------------------------
    // Form interactions
    // -----------------------------------------------------------------------
    public void enterFullName(String v) {
        WebElement el = fullName(); el.clear(); el.sendKeys(v);
    }

    public void enterWorkEmail(String v) {
        WebElement el = workEmail(); el.clear(); el.sendKeys(v);
    }

    public void enterPhone(String v) {
        WebElement el = phone(); el.clear(); el.sendKeys(v);
    }

    public void enterPassword(String v) {
        WebElement el = password(); el.clear(); el.sendKeys(v);
    }

    public void clickCreate() {
        try {
            WebElement btn = createBtn();
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        } catch (Exception e) {
            System.out.println("[clickCreate] " + e.getMessage());
        }
    }

    public void clickSignIn() {
        try {
            WebElement link = signInLink();
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        } catch (Exception e) {
            System.out.println("[clickSignIn] " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Error visibility
    // -----------------------------------------------------------------------
    public boolean emailDomainErrorVisible() {
        return pageContains("cognizant.com") || pageContains("cts.com");
    }

    public boolean phoneErrorVisible() {
        return pageContains("10 digits") || pageContains("Invalid format");
    }

    public boolean passwordErrorVisible() {
        return pageContains("8 characters") || pageContains("Min 8");
    }
}
