package com.cts.mfrp.oneappetite.pages.employee;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

/**
 * Page object for the Employee "Settings" page (route /settings, custom element &lt;app-settings&gt;).
 * Surface:
 *   - Header (title + subtitle)
 *   - Four cards in a .settings-grid: Profile, Notifications, Wallet, Password
 *   - Profile: read-only email (.meta-val), editable name/phone, "Save changes" button
 *   - Notifications: "Order updates" row with a .switch button (aria-pressed reflects on/off)
 *   - Wallet: .wallet-amt balance, four .quick-chip buttons, custom amount + UPI inputs, "Top up wallet"
 *   - Password: current/new/confirm password inputs, "Update password" button
 */
public class EmployeeSettingsPage extends BasePage {

    /* ---------- Sidebar nav link (entry point) ---------- */

    @FindAll({
            @FindBy(css = "app-sidebar a[href$='/settings']"),
            @FindBy(css = "a[href$='/settings']"),
            @FindBy(css = "a[routerlink='/settings']"),
            @FindBy(xpath = "//app-sidebar//a[normalize-space()='Settings']"),
            @FindBy(xpath = "//nav//a[normalize-space()='Settings']")
    })
    private WebElement sidebarSettingsLink;

    /* ---------- Header ---------- */

    @FindBy(css = "app-settings .settings-header h1")
    private WebElement heading;

    @FindBy(css = "app-settings .settings-header .subtitle")
    private WebElement subtitle;

    /* ---------- Cards ---------- */

    @FindBy(css = "app-settings .settings-grid > article.settings-card")
    private List<WebElement> allCards;

    @FindBy(xpath = "//app-settings//article[contains(@class,'settings-card')][.//h2[normalize-space()='Profile']]")
    private List<WebElement> profileCardEls;

    @FindBy(xpath = "//app-settings//article[contains(@class,'settings-card')][.//h2[normalize-space()='Notifications']]")
    private List<WebElement> notificationsCardEls;

    @FindBy(xpath = "//app-settings//article[contains(@class,'settings-card')][.//h2[normalize-space()='Wallet']]")
    private List<WebElement> walletCardEls;

    @FindBy(xpath = "//app-settings//article[contains(@class,'settings-card')][.//h2[normalize-space()='Password']]")
    private List<WebElement> passwordCardEls;

    /* ---------- Profile ---------- */

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Profile']]//span[contains(@class,'badge-role')]")
    private WebElement profileRoleBadge;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Profile']]//div[contains(@class,'meta-row')][.//*[normalize-space()='Email']]//span[contains(@class,'meta-val')]")
    private WebElement profileEmailValue;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Profile']]//input[@name='name']")
    private WebElement profileNameInput;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Profile']]//input[@name='phone']")
    private WebElement profilePhoneInput;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Profile']]//button[@type='submit']")
    private WebElement profileSaveBtn;

    /* ---------- Notifications ---------- */

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Notifications']]//button[contains(@class,'switch')]")
    private WebElement orderUpdatesToggle;

    /* ---------- Wallet ---------- */

    @FindBy(css = "app-settings .wallet-balance .wallet-amt")
    private WebElement walletBalanceEl;

    @FindBy(css = "app-settings .quick-amounts .quick-chip")
    private List<WebElement> quickChips;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Wallet']]//input[@name='amount']")
    private WebElement customAmountInput;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Wallet']]//input[@name='upi']")
    private WebElement upiInput;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Wallet']]//button[@type='submit']")
    private WebElement topUpBtn;

    /* ---------- Password ---------- */

    @FindBy(xpath = "//app-settings//input[@name='current']")
    private WebElement currentPasswordInput;

    @FindBy(xpath = "//app-settings//input[@name='new']")
    private WebElement newPasswordInput;

    @FindBy(xpath = "//app-settings//input[@name='confirm']")
    private WebElement confirmPasswordInput;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Password']]//button[@type='submit']")
    private WebElement updatePasswordBtn;

    @FindBy(xpath = "//app-settings//article[.//h2[normalize-space()='Password']]//*[self::p or self::div or self::span or self::small][contains(@class,'form-error') or contains(@class,'error') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'do not match') or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'incorrect')]")
    private List<WebElement> passwordErrorEls;

    /* ---------- Toast (global, outside app-settings) ---------- */

    @FindBy(css = "app-toast-host .toast-stack .toast")
    private List<WebElement> toastEls;

    public EmployeeSettingsPage(WebDriver driver) { super(driver); }

    /* ====================================================================== */
    /* Navigation                                                             */
    /* ====================================================================== */

    /**
     * Click the sidebar "Settings" link from the dashboard and wait for the page
     * to render. Avoids direct URL nav (which white-pages on the Render deploy).
     */
    public EmployeeSettingsPage openFromSidebar() {
        WebElement link = waitVisible(sidebarSettingsLink);
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", link);
        try {
            waitClickable(link).click();
        } catch (Exception e) {
            jsClick(link);
        }
        wait.until(d -> d.getCurrentUrl().contains("/settings"));
        waitVisible(heading);
        return this;
    }

    /* ====================================================================== */
    /* Header                                                                 */
    /* ====================================================================== */

    public boolean isLoaded() {
        return isDisplayed(heading) && "Settings".equalsIgnoreCase(safeText(heading));
    }

    public String getHeading()  { return safeText(heading); }
    public String getSubtitle() { return safeText(subtitle); }

    /* ====================================================================== */
    /* Cards                                                                  */
    /* ====================================================================== */

    public int cardCount()                  { return allCards.size(); }
    public boolean profileCardVisible()       { return anyDisplayed(profileCardEls); }
    public boolean notificationsCardVisible() { return anyDisplayed(notificationsCardEls); }
    public boolean walletCardVisible()        { return anyDisplayed(walletCardEls); }
    public boolean passwordCardVisible()      { return anyDisplayed(passwordCardEls); }

    public List<String> cardTitles() {
        List<String> titles = new ArrayList<>();
        for (WebElement card : allCards) {
            try { titles.add(card.findElement(By.cssSelector(".card-title h2")).getText().trim()); }
            catch (NoSuchElementException ignored) {}
        }
        return titles;
    }

    /* ====================================================================== */
    /* Profile                                                                */
    /* ====================================================================== */

    public String roleBadgeText() { return safeText(profileRoleBadge); }
    public String profileEmail()  { return safeText(profileEmailValue); }

    /** Email is rendered as a static span, not an editable input. */
    public boolean emailRenderedAsReadOnlyText() {
        return isDisplayed(profileEmailValue)
                && profileEmailValue.getTagName().equalsIgnoreCase("span");
    }

    public boolean nameInputEditable() {
        WebElement el = waitVisible(profileNameInput);
        return el.getAttribute("readonly") == null && el.getAttribute("disabled") == null;
    }

    public boolean phoneInputEditable() {
        WebElement el = waitVisible(profilePhoneInput);
        return el.getAttribute("readonly") == null && el.getAttribute("disabled") == null;
    }

    public String phonePattern() {
        return waitVisible(profilePhoneInput).getAttribute("pattern");
    }

    public void editFullName(String v) { type(profileNameInput, v); }
    public void editPhone(String v)    { type(profilePhoneInput, v); }
    public void clickProfileSave()     { click(profileSaveBtn); }
    public String profileSaveBtnText() { return safeText(profileSaveBtn); }

    /* ====================================================================== */
    /* Notifications                                                          */
    /* ====================================================================== */

    public boolean orderUpdatesOn() {
        WebElement el = waitVisible(orderUpdatesToggle);
        String classes = el.getAttribute("class");
        String pressed = el.getAttribute("aria-pressed");
        // Only treat ".on" class as a strong signal; the bare ".switch" class
        // shouldn't false-positive on a substring match for "on".
        boolean hasOnClass = classes != null
                && java.util.Arrays.asList(classes.split("\\s+")).contains("on");
        return hasOnClass || "true".equalsIgnoreCase(pressed);
    }

    /** Toggle and wait for Angular to actually flip the state before returning. */
    public void toggleOrderUpdates() {
        boolean before = orderUpdatesOn();
        WebElement el = waitVisible(orderUpdatesToggle);
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try {
            waitClickable(el).click();
        } catch (Exception e) {
            jsClick(el);
        }
        wait.until(d -> {
            try { return orderUpdatesOn() != before; }
            catch (Exception ex) { return false; }
        });
    }

    /* ====================================================================== */
    /* Wallet                                                                 */
    /* ====================================================================== */

    public String walletBalanceText() { return safeText(walletBalanceEl); }

    public List<String> quickChipLabels() {
        List<String> out = new ArrayList<>();
        for (WebElement c : quickChips) out.add(c.getText().trim());
        return out;
    }

    public int quickChipCount() { return quickChips.size(); }

    public void clickQuickChip(String label) {
        for (WebElement chip : quickChips) {
            if (label.equalsIgnoreCase(chip.getText().trim())) {
                waitClickable(chip).click();
                return;
            }
        }
        throw new NoSuchElementException("Quick chip not found: " + label);
    }

    public String activeQuickChipLabel() {
        for (WebElement chip : quickChips) {
            String cls = chip.getAttribute("class");
            if (cls != null && cls.contains("active")) return chip.getText().trim();
        }
        return "";
    }

    public void enterCustomAmount(String v) { type(customAmountInput, v); }
    public void enterUpi(String v)          { type(upiInput, v); }
    public void clickTopUp()                { click(topUpBtn); }
    public String topUpBtnText()            { return safeText(topUpBtn); }

    /* ====================================================================== */
    /* Password                                                               */
    /* ====================================================================== */

    public void enterCurrentPassword(String v) { type(currentPasswordInput, v); }
    public void enterNewPassword(String v)     { type(newPasswordInput, v); }
    public void enterConfirmPassword(String v) { type(confirmPasswordInput, v); }
    public void clickPasswordSave()            { click(updatePasswordBtn); }
    public String updatePasswordBtnText()      { return safeText(updatePasswordBtn); }

    public boolean updatePasswordBtnEnabled() {
        WebElement el = waitVisible(updatePasswordBtn);
        return el.getAttribute("disabled") == null;
    }

    public int newPasswordMinLength() {
        String ml = waitVisible(newPasswordInput).getAttribute("minlength");
        try { return Integer.parseInt(ml); } catch (Exception e) { return -1; }
    }

    public boolean passwordErrorVisible() { return anyDisplayed(passwordErrorEls); }

    /** Text of the first visible inline error in the Password card, or "" if none. */
    public String passwordErrorText() {
        for (WebElement el : passwordErrorEls) {
            try { if (el.isDisplayed()) return el.getText().trim(); }
            catch (Exception ignored) {}
        }
        return "";
    }

    /** Waits up to the explicit-wait timeout for an inline error whose text contains {@code needle}. */
    public boolean waitForPasswordErrorContaining(String needle) {
        try {
            wait.until(d -> {
                for (WebElement el : passwordErrorEls) {
                    try {
                        if (el.isDisplayed()
                                && el.getText().toLowerCase().contains(needle.toLowerCase())) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
                return false;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* ====================================================================== */
    /* Input validity helpers (HTML5 validity.valid via JS)                   */
    /* ====================================================================== */

    private boolean validityValid(WebElement el) {
        Object res = ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return arguments[0] && arguments[0].validity && arguments[0].validity.valid;", el);
        return Boolean.TRUE.equals(res);
    }

    public boolean profileNameValid()  { return validityValid(waitVisible(profileNameInput)); }
    public boolean profilePhoneValid() { return validityValid(waitVisible(profilePhoneInput)); }
    public boolean customAmountValid() { return validityValid(waitVisible(customAmountInput)); }
    public boolean upiValid()          { return validityValid(waitVisible(upiInput)); }

    public boolean newPasswordValid()    { return validityValid(waitVisible(newPasswordInput)); }
    public boolean currentPasswordValid(){ return validityValid(waitVisible(currentPasswordInput)); }
    public boolean confirmPasswordValid(){ return validityValid(waitVisible(confirmPasswordInput)); }

    /* ====================================================================== */
    /* Toast helpers                                                          */
    /* ====================================================================== */

    /** Waits up to the explicit-wait timeout for a toast whose text contains {@code needle}. */
    public boolean waitForToastContaining(String needle) {
        try {
            wait.until(d -> {
                for (WebElement t : toastEls) {
                    try {
                        if (t.isDisplayed() && t.getText().toLowerCase().contains(needle.toLowerCase())) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                }
                return false;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* ====================================================================== */
    /* Internal helpers                                                       */
    /* ====================================================================== */

    private static String safeText(WebElement el) {
        try { return el.getText().trim(); }
        catch (Exception e) { return ""; }
    }
}
