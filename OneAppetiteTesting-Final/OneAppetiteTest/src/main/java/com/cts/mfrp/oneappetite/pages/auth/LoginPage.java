package com.cts.mfrp.oneappetite.pages.auth;

import com.cts.mfrp.oneappetite.pages.BasePage;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class LoginPage extends BasePage {

    @FindAll({
            @FindBy(xpath = "//h1[contains(.,\"Welcome back to One Appetite\")]"),
            @FindBy(xpath = "//h2[contains(.,\"Welcome back to One Appetite\")]"),
            @FindBy(xpath = "//*[contains(.,\"Welcome back\") and (self::h1 or self::h2 or self::h3)]"),
            @FindBy(xpath = "//app-login//*[self::h1 or self::h2 or self::h3]")
    })
    private List<WebElement> headingEls;

    @FindAll({
            @FindBy(xpath = "//p[@class='hero-tagline']"),
            @FindBy(xpath = "//*[contains(@class,'tagline')]")
    })
    private List<WebElement> taglineEls;

    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(xpath = "//button[@aria-label='Toggle password visibility']")
    private WebElement passwordEyeIcon;

    @FindBy(xpath = "//button[normalize-space()='Sign In' or normalize-space()='Sign in']")
    private WebElement signInButton;

    @FindAll({
            @FindBy(xpath = "//a[contains(.,\"forgot\")]"),
            @FindBy(xpath = "//button[contains(.,\"forgot\")]"),
            @FindBy(xpath = "//span[contains(.,\"forgot\")]")
    })
    private List<WebElement> forgotPasswordEls;

    @FindAll({
            @FindBy(xpath = "//app-login//p/a"),
            @FindBy(xpath = "//a[contains(.,\"Create one now\")]"),
            @FindBy(xpath = "//a[contains(.,\"Create\")]"),
            @FindBy(xpath = "//p//a")
    })
    private List<WebElement> createAccountEls;

    @FindAll({
            @FindBy(xpath = "//*[contains(.,\"Email is required\")]"),
            @FindBy(xpath = "//*[contains(@class,'error')][contains(.,\"email\")]")
    })
    private List<WebElement> emailErrors;

    @FindAll({
            @FindBy(xpath = "//*[contains(.,\"Must be at least 8 characters\")]"),
            @FindBy(xpath = "//*[contains(.,\"Password is required\")]")
    })
    private List<WebElement> passwordErrors;

    @FindBy(xpath = "//*[contains(.,\"Please select a role to continue\")]")
    private List<WebElement> roleErrors;

    @FindAll({
            @FindBy(xpath = "//*[contains(.,\"cognizant.com\")]"),
            @FindBy(xpath = "//*[contains(.,\"cts.com\")]")
    })
    private List<WebElement> domainErrors;

    @FindAll({
            @FindBy(xpath = "//*[contains(.,\"No vendor account found\")]"),
            @FindBy(xpath = "//*[contains(.,\"Your account is registered\")]"),
            @FindBy(xpath = "//*[contains(.,\"registered as employee\")]")
    })
    private List<WebElement> roleMismatchErrors;

    @FindAll({
            @FindBy(css = "[data-testid='google-login']"),
            @FindBy(css = "button[aria-label*='Google' i]"),
            @FindBy(css = "img[alt*='Google' i]")
    })
    private List<WebElement> googleSocialEls;

    @FindAll({
            @FindBy(css = "[data-testid='microsoft-login']"),
            @FindBy(css = "button[aria-label*='Microsoft' i]"),
            @FindBy(css = "img[alt*='Microsoft' i]")
    })
    private List<WebElement> microsoftSocialEls;

    public LoginPage(WebDriver driver) { super(driver); }

    public boolean isLoaded() {
        return (anyDisplayed(headingEls) || driver.getCurrentUrl().contains("login"))
                && isDisplayed(emailInput)
                && isDisplayed(passwordInput)
                && isDisplayed(signInButton);
    }

    public String headingText() {
        for (WebElement el : headingEls) {
            try { if (el.isDisplayed()) return el.getText(); }
            catch (Exception ignored) {}
        }
        return "";
    }

    public String taglineText() {
        for (WebElement el : taglineEls) {
            try { if (el.isDisplayed()) return el.getText(); }
            catch (Exception ignored) {}
        }
        return "";
    }

    public boolean emailFieldVisible()     { return isDisplayed(emailInput); }
    public boolean passwordFieldVisible()  { return isDisplayed(passwordInput); }
    public boolean signInButtonVisible()   { return isDisplayed(signInButton); }
    public boolean eyeIconVisible()        { return isDisplayed(passwordEyeIcon); }
    public boolean forgotPasswordVisible() { return anyDisplayed(forgotPasswordEls); }
    public boolean createAccountVisible()  { return anyDisplayed(createAccountEls); }
    public boolean googleSocialVisible()   { return anyDisplayed(googleSocialEls); }
    public boolean microsoftSocialVisible(){ return anyDisplayed(microsoftSocialEls); }

    public String emailPlaceholder()  { return waitVisible(emailInput).getAttribute("placeholder"); }
    public String passwordInputType() { return waitVisible(passwordInput).getAttribute("type"); }

    public LoginPage enterEmail(String email)       { type(emailInput, email);       return this; }
    public LoginPage enterPassword(String password) { type(passwordInput, password); return this; }
    public LoginPage selectRole(String role)        { clickByText(role); return this; }

    public boolean isRoleSelected(String role) {
        WebElement chip = driver.findElement(
                org.openqa.selenium.By.xpath(
                        "//*[self::button or self::div or self::label][normalize-space()='" + role + "']"));
        String classes = chip.getAttribute("class");
        String aria    = chip.getAttribute("aria-pressed");
        return (classes != null && (classes.contains("active") || classes.contains("selected")))
                || "true".equalsIgnoreCase(aria);
    }

    public void togglePasswordVisibility() { click(passwordEyeIcon); }
    public void clickSignIn()              { click(signInButton); }

    public ForgotPasswordModal openForgotPassword() {
        for (WebElement el : forgotPasswordEls) {
            try { if (el.isDisplayed()) { el.click(); break; } }
            catch (Exception ignored) {}
        }
        return new ForgotPasswordModal(driver);
    }

    public CreateAccountPage openCreateAccount() {
        for (WebElement el : createAccountEls) {
            try { if (el.isDisplayed()) { el.click(); break; } }
            catch (Exception ignored) {}
        }
        return new CreateAccountPage(driver);
    }

    public boolean emailErrorVisible()        { return anyDisplayed(emailErrors) || pageContains("email"); }
    public boolean passwordErrorVisible()     { return anyDisplayed(passwordErrors) || WaitUtils.textVisible(driver, "8 characters"); }
    public boolean roleErrorVisible()         { return anyDisplayed(roleErrors) || WaitUtils.textVisible(driver, "select a role"); }
    public boolean domainErrorVisible()       { return anyDisplayed(domainErrors) || WaitUtils.textVisible(driver, "cognizant.com"); }
    public boolean roleMismatchErrorVisible() { return anyDisplayed(roleMismatchErrors) || WaitUtils.textVisible(driver, "registered"); }
}
