package com.cts.mfrp.oneappetite.pages.shared;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class TopBarComponent extends BasePage {

    @FindAll({
            @FindBy(css = "button.theme-toggle"),
            @FindBy(css = "button.theme-pill")
    })
    private List<WebElement> themeToggleEls;

    @FindAll({
            @FindBy(css = "input.search-input"),
            @FindBy(css = "input.topbar-search"),
            @FindBy(css = "input[placeholder*='Search menu' i]"),
            @FindBy(css = "input[placeholder*='Search' i][type='search']"),
            @FindBy(css = "input[placeholder*='Search' i][type='text']")
    })
    private List<WebElement> searchInputEls;

    @FindAll({
            @FindBy(css = "span.role-pill"),
            @FindBy(css = "span.role-badge"),
            @FindBy(css = "div.role-pill"),
            @FindBy(css = "span.user-role"),
            @FindBy(xpath = "//header//span[normalize-space()='Employee' or normalize-space()='EMPLOYEE'"
                    + " or normalize-space()='Vendor' or normalize-space()='VENDOR'"
                    + " or normalize-space()='Admin' or normalize-space()='ADMIN']"),
            @FindBy(xpath = "//nav//span[normalize-space()='Employee' or normalize-space()='EMPLOYEE'"
                    + " or normalize-space()='Vendor' or normalize-space()='VENDOR'"
                    + " or normalize-space()='Admin' or normalize-space()='ADMIN']")
    })
    private List<WebElement> rolePillEls;

    @FindAll({
            @FindBy(css = "button.notif-btn"),
            @FindBy(css = "i.bi-bell.topbar-icon")
    })
    private List<WebElement> bellEls;

    @FindBy(css = "span.notif-dot")
    private List<WebElement> unreadBadgeEls;

    @FindBy(css = "div.notif-panel")
    private List<WebElement> notificationPanelEls;

    @FindBy(css = "button.mark-read-btn")
    private List<WebElement> markAllReadBtns;

    @FindBy(css = "button.logout-btn")
    private WebElement logoutBtn;

    @FindBy(tagName = "html")
    private WebElement htmlEl;

    @FindBy(css = "div.notif-panel:not(.mobile-notif-panel) li.notif-item")
    private List<WebElement> notificationItems;

    public TopBarComponent(WebDriver driver) { super(driver); }

    public boolean themeToggleVisible() { return anyDisplayed(themeToggleEls); }
    public void toggleTheme() {
        for (WebElement b : themeToggleEls) {
            try { if (b.isDisplayed()) { waitClickable(b).click(); return; } }
            catch (Exception ignored) {}
        }
    }

    public boolean bellVisible() { return anyDisplayed(bellEls); }
    public String unreadBadgeText() {
        for (WebElement b : unreadBadgeEls) {
            try { if (b.isDisplayed()) return b.getText().trim(); }
            catch (Exception ignored) {}
        }
        return "";
    }
    public void openNotificationPanel() {
        if (notificationPanelOpen()) return;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement target = null;
        for (WebElement b : bellEls) {
            try { if (b.isDisplayed()) { target = b; break; } }
            catch (Exception ignored) {}
        }
        if (target == null) {
            target = (WebElement) js.executeScript(
                    "return Array.from(document.querySelectorAll('button.notif-btn,i.bi-bell'))" +
                    ".find(el => el.offsetParent !== null) || null;");
            if (target == null) return;
        }

        try { waitClickable(target).click(); }
        catch (Exception ignored) {}
        if (waitForPanel()) return;

        try { js.executeScript("arguments[0].click();", target); }
        catch (Exception ignored) {}
        if (waitForPanel()) return;

        try {
            js.executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('click', " +
                "{ bubbles: true, cancelable: true, view: window, button: 0 }));",
                target);
        } catch (Exception ignored) {}
        waitForPanel();
    }

    private boolean waitForPanel() {
        try {
            wait.until(d -> {
                Boolean open = (Boolean) ((JavascriptExecutor) d).executeScript(
                        "return Array.from(document.querySelectorAll('div.notif-panel'))" +
                        ".some(p => p.offsetParent !== null);");
                return Boolean.TRUE.equals(open);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean notificationPanelOpen() {
        try {
            Boolean open = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "return Array.from(document.querySelectorAll('div.notif-panel'))" +
                    ".some(p => p.offsetParent !== null);");
            return Boolean.TRUE.equals(open);
        } catch (Exception e) {
            return anyDisplayed(notificationPanelEls);
        }
    }

    @SuppressWarnings("unchecked")
    public List<WebElement> notifications() {
        if (!notificationPanelOpen()) return List.of();
        List<WebElement> items = (List<WebElement>) ((JavascriptExecutor) driver).executeScript(
                "const panels = Array.from(document.querySelectorAll('div.notif-panel'))" +
                "    .filter(p => !p.classList.contains('mobile-notif-panel') && p.offsetParent !== null);" +
                "if (panels.length === 0) return [];" +
                "return Array.from(panels[0].querySelectorAll('li.notif-item'));");
        return items == null ? List.of() : items;
    }

    public void markAllRead() {
        WebElement target = null;
        for (WebElement b : markAllReadBtns) {
            try {
                if (b.isDisplayed() && b.isEnabled()) { target = b; break; }
            } catch (Exception ignored) {}
        }
        if (target == null) return;
        try { waitClickable(target).click(); }
        catch (Exception e) {
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target); }
            catch (Exception ignored) {}
        }
        try {
            wait.until(d -> {
                Boolean cleared = (Boolean) ((JavascriptExecutor) d).executeScript(
                        "return document.querySelectorAll('span.notif-dot').length === 0;");
                return Boolean.TRUE.equals(cleared);
            });
        } catch (Exception ignored) {}
    }

    public String currentTheme() {
        for (WebElement b : themeToggleEls) {
            try {
                if (!b.isDisplayed()) continue;
                String cls = b.getAttribute("class");
                if (cls != null && cls.contains("is-dark")) return "dark";
                return "light";
            } catch (Exception ignored) {}
        }
        String dataTheme = htmlEl.getAttribute("data-theme");
        if (dataTheme != null && !dataTheme.isBlank()) return dataTheme;
        String classes = htmlEl.getAttribute("class");
        return (classes != null && classes.contains("dark")) ? "dark" : "light";
    }

    public void logout() {
        try {
            waitClickable(logoutBtn).click();
        } catch (Exception e) {
            // Fall back to JS click in case an overlay is covering the button
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn); }
            catch (Exception ignored) {}
        }
    }

    /* ------------------------------------------------------------------ */
    /* Search bar (FRD: present in top bar — verify presence + behaviour) */
    /* ------------------------------------------------------------------ */

    public boolean searchBarPresent() {
        if (anyDisplayed(searchInputEls)) return true;
        try {
            Boolean found = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "return Array.from(document.querySelectorAll('input'))"
                            + ".some(el => el.offsetParent !== null && (el.placeholder || '')"
                            + ".toLowerCase().includes('search'));");
            return Boolean.TRUE.equals(found);
        } catch (Exception e) {
            return false;
        }
    }

    public WebElement visibleSearchInput() {
        for (WebElement el : searchInputEls) {
            try { if (el.isDisplayed()) return el; }
            catch (Exception ignored) {}
        }
        try {
            return driver.findElement(By.cssSelector("input[placeholder*='Search' i]"));
        } catch (Exception e) {
            return null;
        }
    }

    public void searchMenuItem(String query) {
        WebElement input = visibleSearchInput();
        if (input == null) {
            throw new IllegalStateException(
                    "Search bar is not present in the top bar — cannot search for '" + query + "'.");
        }
        WebElement v = waitVisible(input);
        v.clear();
        v.sendKeys(query);
        v.sendKeys(Keys.ENTER);
    }

    /* ------------------------------------------------------------------ */
    /* Role pill (Employee / Vendor / Admin) shown in the top bar         */
    /* ------------------------------------------------------------------ */

    public boolean rolePillVisible() {
        return loggedInRole() != null;
    }

    public String loggedInRole() {
        for (WebElement el : rolePillEls) {
            try {
                if (!el.isDisplayed()) continue;
                String text = el.getText();
                String role = normalizeRole(text);
                if (role != null) return role;
            } catch (Exception ignored) {}
        }
        try {
            String text = (String) ((JavascriptExecutor) driver).executeScript(
                    "const wanted = ['employee','vendor','admin'];"
                            + "const nodes = Array.from(document.querySelectorAll("
                            + "  'header *, nav *, [class*=topbar] *, [class*=top-bar] *'));"
                            + "for (const n of nodes) {"
                            + "  if (n.offsetParent === null) continue;"
                            + "  const t = (n.textContent || '').trim().toLowerCase();"
                            + "  if (t.length > 16) continue;"
                            + "  if (wanted.includes(t)) return t;"
                            + "} return null;");
            return normalizeRole(text);
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeRole(String raw) {
        if (raw == null) return null;
        String t = raw.trim().toLowerCase();
        return switch (t) {
            case "employee" -> "EMPLOYEE";
            case "admin"    -> "ADMIN";
            default         -> null;
        };
    }
}
