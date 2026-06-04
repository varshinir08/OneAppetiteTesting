package com.cts.mfrp.oneappetite.base;

import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExtentReportListener;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Listeners(ExtentReportListener.class)
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser", "headless"})
    public void setUp(@Optional("") String browser, @Optional("") String headless) {
        String resolvedBrowser = browser == null || browser.isBlank()
                ? ConfigReader.get("browser", "chrome") : browser;
        boolean resolvedHeadless = headless == null || headless.isBlank()
                ? ConfigReader.getBoolean("headless", false)
                : Boolean.parseBoolean(headless);
        driver = DriverFactory.create(resolvedBrowser, resolvedHeadless);
        // Render free-tier cold starts can exceed even a 60s page-load timeout on
        // the first hit after idle. If the first navigation times out, the server
        // is now waking up — try once more and it should respond quickly.
        String baseUrl = ConfigReader.get("base.url");
        try {
            driver.get(baseUrl);
        } catch (org.openqa.selenium.TimeoutException coldStart) {
            driver.get(baseUrl);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && driver != null) {
            captureScreenshot(result.getMethod().getMethodName());
        }
        DriverFactory.quit();
    }

    protected void captureScreenshot(String testName) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dir = Paths.get("test-output", "screenshots");
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            Files.copy(src.toPath(), dir.resolve(testName + "-" + stamp + ".png"));
        } catch (IOException ignored) {}
    }
}
