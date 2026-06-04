package com.cts.mfrp.oneappetite.base;

import com.cts.mfrp.oneappetite.utils.ConfigReader;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {}

    public static WebDriver create(String browser, boolean headless) {
        WebDriver driver;
        String b = browser == null ? "chrome" : browser.toLowerCase();
        switch (b) {
            case "firefox" -> {
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("-headless");
                driver = new FirefoxDriver(opts);
            }
            case "edge" -> {
                EdgeOptions opts = new EdgeOptions();
                if (headless) opts.addArguments("--headless=new");
                driver = new EdgeDriver(opts);
            }
            default -> {
                ChromeOptions opts = new ChromeOptions();
                // --incognito: start each session with a clean cookie/localStorage state.
                // Without it, a leftover login session from a prior manual browser run can
                // make the app redirect /login → /dashboard, the 'id=email' field never
                // renders, and the 60-second waitVisible in LoginPage.enterEmail times out.
                opts.addArguments("--incognito",
                        "--disable-notifications", "--disable-popup-blocking",
                        "--disable-gpu", "--remote-allow-origins=*");
                if (headless) opts.addArguments("--headless=new", "--window-size=1440,900");
                else opts.addArguments("--start-maximized", "--window-size=1440,900");
                driver = new ChromeDriver(opts);
            }
        }
        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigReader.getInt("implicit.wait.seconds", 5)));
        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(ConfigReader.getInt("page.load.timeout.seconds", 45)));
        try {
            driver.manage().window().setSize(new Dimension(1440, 900));
        } catch (Exception ignored) { /* window already sized via Chrome flags */ }
        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver get() {
        return DRIVER.get();
    }

    public static void quit() {
        WebDriver d = DRIVER.get();
        if (d != null) {
            try { d.quit(); } finally { DRIVER.remove(); }
        }
    }
}
