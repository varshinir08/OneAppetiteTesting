package com.cts.mfrp.oneappetite.pages.admin;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class AdminSettingsPage extends BasePage {

    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Profile']]")
    private List<WebElement> profileCard;
    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Notifications']]")
    private List<WebElement> notificationsCard;
    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Password']]")
    private List<WebElement> passwordCard;
    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Wallet']]")
    private List<WebElement> walletCard;

    @FindBy(css = "input[name='name']")
    private WebElement fullName;

    @FindBy(css = "input[name='phone']")
    private WebElement phone;

    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Notifications']]//button[contains(@class,'switch')]")
    private WebElement adminAlertsToggle;

    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Profile']]//button[@type='submit']")
    private WebElement profileSaveBtn;

    @FindBy(css = "input[name='current']")
    private WebElement currentPassword;
    @FindBy(css = "input[name='new']")
    private WebElement newPassword;
    @FindBy(css = "input[name='confirm']")
    private WebElement confirmPassword;
    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Password']]//button[@type='submit']")
    private WebElement passwordSaveBtn;

    public AdminSettingsPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.settings-grid article.settings-card")));
    }

    public boolean profileCardVisible()       { return anyDisplayed(profileCard); }
    public boolean notificationsCardVisible() { return anyDisplayed(notificationsCard); }
    public boolean passwordCardVisible()      { return anyDisplayed(passwordCard); }
    public boolean walletCardVisible()        { return anyDisplayed(walletCard); }

    public void editFullName(String v) { type(fullName, v); }
    public void editPhone(String v)    { type(phone, v); }
    public void clickProfileSave()     { click(profileSaveBtn); }
    public void toggleAdminAlerts()    { click(adminAlertsToggle); }

    public void enterCurrentPassword(String v) { type(currentPassword, v); }
    public void enterNewPassword(String v)     { type(newPassword, v); }
    public void enterConfirmPassword(String v) { type(confirmPassword, v); }
    public void clickPasswordSave()            { click(passwordSaveBtn); }
}
