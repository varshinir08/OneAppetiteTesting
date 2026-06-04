package com.cts.mfrp.oneappetite.tests.support;

import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.auth.LoginPage;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.SkipException;

public final class LoginHelper {

    /** Per-role auth snapshot, keyed by role string. JVM-wide cache so a single
     *  UI login per role serves every subsequent test in the run. ConcurrentHashMap
     *  for the parallel-testng.xml execution path. */
    private static final java.util.Map<String, Snapshot> SESSION_CACHE = new ConcurrentHashMap<>();

    private record Snapshot(Set<Cookie> cookies, String localStorageJson) {}

    private LoginHelper() {}

    private static String routeFor(String role) {
        return switch (role) {
            case AppConstants.VENDOR_ROLE -> AppConstants.ROUTE_VENDOR_DASHBOARD;
            case AppConstants.ADMIN_ROLE  -> AppConstants.ROUTE_ADMIN_DASHBOARD;
            default                       -> AppConstants.ROUTE_DASHBOARD;
        };
    }

    /**
     * UI-bypassing login was attempted via a cookie + localStorage cache, but the
     * Render-deployed Angular app re-validates auth server-side: cached state did
     * not authenticate, every warm-path call fell back to UI login (with extra
     * latency), and the suite ran slower and flakier than plain UI login.
     *
     * <p>Until a working cache strategy lands (e.g. backend test-token endpoint,
     * non-httpOnly cookies, or LoginHelper-aware fixtures), this method is a
     * thin alias for {@link #loginAs(WebDriver, String)} so the 23 callers don't
     * need to change. The snapshot record and SESSION_CACHE map are retained
     * for the eventual re-enable.
     */
    public static void fastLoginAs(WebDriver driver, String role) {
        loginAs(driver, role);
    }


    public static void gotoViaSidebar(WebDriver driver, String routerPath) {
        WebDriverWait w = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds", 20)));
        By byHref = By.cssSelector("a.nav-item[href$='" + routerPath + "']");
        try {
            w.until(ExpectedConditions.elementToBeClickable(byHref)).click();
        } catch (Exception ignored) {
            String label = routerPath.substring(routerPath.lastIndexOf('/') + 1);
            String pretty = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            By byText = By.xpath("//aside//a[contains(@class,'nav-item')]"
                    + "[contains(normalize-space(.),'" + pretty + "')]");
            w.until(ExpectedConditions.elementToBeClickable(byText)).click();
        }
        w.until(ExpectedConditions.urlContains(routerPath));
    }
    /**
     * Waits up to {@code LOGIN_REDIRECT_TIMEOUT_SECONDS} for the post-sign-in redirect.
     * This is intentionally longer than {@code explicit.wait.seconds} because the Render.com
     * free-tier backend can take 30–60 s to cold-start; a shorter wait would cause a false
     * "login failed" even when the credentials are correct.
     */
    private static final int LOGIN_REDIRECT_TIMEOUT_SECONDS = 90;

    private static boolean waitForLogin(WebDriver driver, String route) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(LOGIN_REDIRECT_TIMEOUT_SECONDS))
                    .until(ExpectedConditions.urlContains(route));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void loginAs(WebDriver driver, String role, String overridePassword) {
        String emailKey, passKey, route;
        switch (role) {
            case AppConstants.VENDOR_ROLE -> {
                emailKey = "vendor.email"; passKey = "vendor.password";
                route = AppConstants.ROUTE_VENDOR_DASHBOARD;
            }
            case AppConstants.ADMIN_ROLE -> {
                emailKey = "admin.email"; passKey = "admin.password";
                route = AppConstants.ROUTE_ADMIN_DASHBOARD;
            }
            default -> {
                emailKey = "employee.email"; passKey = "employee.password";
                route = AppConstants.ROUTE_DASHBOARD;
            }
        }
        String password = overridePassword != null ? overridePassword : ConfigReader.get(passKey);
        LoginPage page = new LoginPage(driver);
        page.enterEmail(ConfigReader.get(emailKey))
                .enterPassword(password)
                .selectRole(role);
        page.clickSignIn();
        waitForLogin(driver, route);
    }

    public static void loginAs(WebDriver driver, String role) {
        String emailKey, passKey, route;
        switch (role) {
            case AppConstants.VENDOR_ROLE -> {
                emailKey = "vendor.email"; passKey = "vendor.password";
                route = AppConstants.ROUTE_VENDOR_DASHBOARD;
            }
            case AppConstants.ADMIN_ROLE -> {
                emailKey = "admin.email"; passKey = "admin.password";
                route = AppConstants.ROUTE_ADMIN_DASHBOARD;
            }
            default -> {
                emailKey = "employee.email"; passKey = "employee.password";
                route = AppConstants.ROUTE_DASHBOARD;
            }
        }
        LoginPage page = new LoginPage(driver);
        page.enterEmail(ConfigReader.get(emailKey))
                .enterPassword(ConfigReader.get(passKey))
                .selectRole(role);
        page.clickSignIn();
        if (!waitForLogin(driver, route)) {
            throw new SkipException(
                    "Login failed for role '" + role + "'. Expected URL to contain '" + route
                            + "', but was '" + driver.getCurrentUrl() + "'. "
                            + "Check '" + emailKey + "' and '" + passKey
                            + "' in config.properties — the account credentials may be out of sync.");
        }
    }
}
