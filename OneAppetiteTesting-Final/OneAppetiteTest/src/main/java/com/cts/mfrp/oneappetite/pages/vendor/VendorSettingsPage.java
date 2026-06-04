package com.cts.mfrp.oneappetite.pages.vendor;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class VendorSettingsPage extends BasePage {

    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Profile']]")
    private List<WebElement> profileCard;
    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Notifications']]")
    private List<WebElement> notificationsCard;
    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Password']]")
    private List<WebElement> passwordCard;
    @FindBy(xpath = "//article[contains(@class,'settings-card')][.//h2[normalize-space()='Stall Locations']]")
    private List<WebElement> stallLocationsCard;

    @FindBy(xpath = "//article[contains(@class,'earnings-card')]")
    private List<WebElement> earningsCard;

    @FindBy(css = ".earnings-card .earnings-amt")
    private WebElement earningsAmount;

    // Primary Building is shown as a <span class="meta-val">, not an editable input —
    // its mere presence proves it's read-only by design.
    @FindBy(xpath = "//span[@class='meta-key' and normalize-space()='Primary Building']/following-sibling::span[@class='meta-val']")
    private List<WebElement> primaryBuildingValue;

    @FindBy(xpath = "//span[contains(@class,'loc-badge') and normalize-space()='Registered']")
    private List<WebElement> registeredBadges;

    @FindBy(xpath = "//article[contains(@class,'stall-card')]//button[contains(normalize-space(.),'Add Location')]")
    private WebElement addLocationBtn;

    @FindBy(xpath = "//div[@role='dialog']")
    private List<WebElement> cascadeModalEls;

    @FindBy(xpath = "//div[@role='dialog']//button[normalize-space()='Add Location']")
    private WebElement confirmBtn;

    // Cascade modal dropdowns — scoped to the dialog. The <select> tags have no name/id,
    // so they're located by their label text.
    @FindBy(xpath = "//div[@role='dialog']//label[.//span[normalize-space()='City']]//select")
    private WebElement citySelect;
    @FindBy(xpath = "//div[@role='dialog']//label[.//span[normalize-space()='Campus']]//select")
    private WebElement campusSelect;
    @FindBy(xpath = "//div[@role='dialog']//label[.//span[normalize-space()='Building']]//select")
    private WebElement buildingSelect;

    // Primary location card has class "loc-card primary"; "Also Serving" entries are
    // any loc-card without the "primary" modifier.
    @FindBy(xpath = "//div[contains(@class,'loc-card') and not(contains(@class,'primary'))]")
    private List<WebElement> alsoServingItems;

    @FindBy(xpath="//button[@class='loc-remove']")
    private List<WebElement> removeXIcons;

    @FindBy(css = "input[name='name']")
    private WebElement fullName;

    @FindBy(css = "input[name='phone']")
    private WebElement phoneInput;

    @FindBy(xpath = "//article[.//h2[normalize-space()='Profile']]//button[@type='submit']")
    private WebElement profileSaveBtn;

    // Notifications uses a <button class="switch on/off">, not a checkbox.
    @FindBy(xpath = "//article[.//h2[normalize-space()='Notifications']]//button[contains(@class,'switch')]")
    private WebElement orderUpdatesToggle;

    @FindBy(css = "input[name='current']")
    private WebElement currentPassword;
    @FindBy(css = "input[name='new']")
    private WebElement newPassword;
    @FindBy(css = "input[name='confirm']")
    private WebElement confirmPassword;
    @FindBy(xpath = "//article[.//h2[normalize-space()='Password']]//button[@type='submit']")
    private WebElement passwordSaveBtn;

    public VendorSettingsPage(WebDriver driver) { super(driver); }

    public boolean profileCardVisible()        { return anyDisplayed(profileCard); }
    public boolean notificationsCardVisible()  { return anyDisplayed(notificationsCard); }
    public boolean passwordCardVisible()       { return anyDisplayed(passwordCard); }
    public boolean stallLocationsCardVisible() { return anyDisplayed(stallLocationsCard); }
    public boolean earningsCardVisible()       { return anyDisplayed(earningsCard); }
    public boolean primaryBuildingReadOnly()   { return anyDisplayed(primaryBuildingValue); }
    public boolean registeredBadgeVisible()    { return anyDisplayed(registeredBadges); }

    public String earningsAmountText() {
        try { return waitVisible(earningsAmount).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    public void editFullName(String v) { type(fullName, v); }
    public void editPhone(String v)    { type(phoneInput, v); }
    public void clickProfileSave()     { click(profileSaveBtn); }

    public void clickAddLocation()    { robustClick(addLocationBtn); }
    public boolean cascadeModalOpen() { return anyDisplayed(cascadeModalEls); }
    public void confirmAddLocation()  { robustClick(confirmBtn); }

    /** Scroll the element into the viewport centre and click; fall back to a
     *  JS click if Chrome reports the click was intercepted by an overlapping
     *  sticky/footer element. The Stall Locations card sits near the bottom
     *  of a long settings page, so plain {@link #click} hits an intercept
     *  unless we recentre first. */
    private void robustClick(WebElement el) {
        WebElement v = waitVisible(el);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});", v);
        try {
            waitClickable(v).click();
        } catch (Exception e) {
            jsClick(v);
        }
    }

    /** Open the modal, pick city → campus → building, and submit. Each
     *  selectByLabelText call already waits internally for options to load,
     *  so no pauses are needed between selects.
     *
     *  The app saves the location server-side on confirm but does NOT auto-close
     *  the dialog. So success is signaled by the also-serving list growing — not
     *  by modal close. After we see growth (or time out), we dismiss any leftover
     *  modal via Escape so subsequent tests don't see a stuck overlay. */
    public void addLocationFull(String city, String campus, String building) {
        int before = alsoServingItems.size();
        clickAddLocation();
        wait.until(d -> cascadeModalOpen());
        selectByLabelText(citySelect, city);
        selectByLabelText(campusSelect, campus);
        selectByLabelText(buildingSelect, building);
        confirmAddLocation();
        try {
            wait.until(d -> alsoServingItems.size() > before);
        } catch (Exception ignored) {
            // Fall through — the caller's assertion will report the real failure.
        }
        if (cascadeModalOpen()) {
            pressEscape();
            try { wait.until(d -> !cascadeModalOpen()); } catch (Exception ignored) {}
        }
    }

    /** Wait up to the explicit-wait timeout for the also-serving list to grow past
     *  the given baseline. Returns true on growth, false on timeout. */
    public boolean waitForAlsoServingCountAbove(int baseline) {
        try {
            wait.until(d -> alsoServingItems.size() > baseline);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Selects an option by visible text. Strips ALL whitespace (including non-breaking
     *  spaces that .trim() leaves alone) and ignores case. Waits briefly for the option
     *  list to populate since cascade selects load options asynchronously.
     *
     *  Angular re-renders cascade dropdown options after each upstream selection, which
     *  makes the previously-collected WebElement references stale. The method retries
     *  up to 3 times, re-waiting for the option list to stabilise before each attempt. */
    private void selectByLabelText(WebElement selectEl, String text) {
        waitVisible(selectEl);
        waitForOptions(selectEl);

        String want = squash(text);
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Select s = new Select(selectEl);
                StringBuilder seen = new StringBuilder();
                for (WebElement opt : s.getOptions()) {
                    String label = opt.getAttribute("textContent");
                    if (label == null || label.isEmpty()) label = opt.getText();
                    seen.append("['").append(label).append("'] ");
                    if (squash(label).equals(want)) {
                        opt.click();
                        // Angular/React reactive forms listen on 'change' (and sometimes 'input').
                        // A native opt.click() doesn't always bubble a change event up to the
                        // <select> in modern Chrome, so dispatch them explicitly — otherwise
                        // the form thinks no value was picked and the submit is rejected.
                        ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                                        + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                                selectEl);
                        return;
                    }
                }
                // All options scanned with no match — option genuinely absent, no point retrying.
                throw new org.openqa.selenium.NoSuchElementException(
                        "No option matching '" + text + "'. Saw: " + seen);
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                if (attempt == 2) throw e;
                // The cascade dropdown re-rendered; wait for it to settle before retrying.
                waitForOptions(selectEl);
            }
        }
    }

    private void waitForOptions(WebElement selectEl) {
        wait.until(d -> {
            try { return new Select(selectEl).getOptions().size() > 1; }
            catch (Exception e) { return false; }
        });
    }

    /** Removes ALL Unicode whitespace (regular, non-breaking, tab, newline) and lower-cases. */
    private static String squash(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s\\u00A0]+", "").toLowerCase();
    }

    public List<WebElement> alsoServing() { return alsoServingItems; }
    public void removeFirstAlsoServing() {
        if (removeXIcons.isEmpty()) return;
        int before = alsoServingItems.size();
        robustClick(removeXIcons.get(0));
        // The remove is async (API call + Angular re-render). Wait for the list
        // to actually shrink before returning so the test's assertion sees the change.
        try {
            wait.until(d -> alsoServingItems.size() < before);
        } catch (Exception ignored) {
            // Fall through — assertion in the test will catch the real problem.
        }
    }

    public void toggleOrderUpdates() { click(orderUpdatesToggle); }
    public void enterCurrentPassword(String v) { type(currentPassword, v); }
    public void enterNewPassword(String v)     { type(newPassword, v); }
    public void enterConfirmPassword(String v) { type(confirmPassword, v); }
    public void clickPasswordSave()            { click(passwordSaveBtn); }

    public boolean updatePasswordBtnEnabled() {
        try {
            WebElement btn = waitVisible(passwordSaveBtn);
            return btn.isEnabled() && !"true".equals(btn.getAttribute("disabled"));
        } catch (Exception e) { return false; }
    }

    public int newPasswordMinLength() {
        try {
            String v = waitVisible(newPassword).getAttribute("minlength");
            return v != null ? Integer.parseInt(v.trim()) : -1;
        } catch (Exception e) { return -1; }
    }
}
