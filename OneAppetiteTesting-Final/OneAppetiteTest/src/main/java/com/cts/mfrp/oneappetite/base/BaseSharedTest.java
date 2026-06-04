package com.cts.mfrp.oneappetite.base;

import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExtentReportListener;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class-level alternative to {@link BaseTest}. The driver is created ONCE in
 * {@code @BeforeClass} and reused across every {@code @Test} method in the subclass,
 * then quit in {@code @AfterClass}. This is suitable for test classes whose
 * {@code @BeforeMethod} setup is expensive (full login → navigation → order placement)
 * and whose tests are read-only against the resulting page state, or can be ordered
 * via {@code @Test(priority = N)} so mutating tests run last.
 *
 * <p>Compared with {@link BaseTest}, this trades test isolation for run speed:
 * a class with N tests no longer pays N × setup-cost, only 1 × setup-cost.
 * Subclasses that mutate shared state (cart, login session, navigation) must
 * be ordered or self-restore — they share one browser session.
 */
@Listeners(ExtentReportListener.class)
public abstract class BaseSharedTest {

    protected WebDriver driver;

    @BeforeClass(alwaysRun = true)
    @Parameters({"browser", "headless"})
    public void setUpClass(@Optional("") String browser, @Optional("") String headless) {
        String resolvedBrowser = browser == null || browser.isBlank()
                ? ConfigReader.get("browser", "chrome") : browser;
        boolean resolvedHeadless = headless == null || headless.isBlank()
                ? ConfigReader.getBoolean("headless", false)
                : Boolean.parseBoolean(headless);
        driver = DriverFactory.create(resolvedBrowser, resolvedHeadless);
        // Render free-tier cold starts can exceed the configured page-load timeout on the
        // first hit after idle. If the first navigation times out, the server is waking up —
        // retry once.
        String baseUrl = ConfigReader.get("base.url");
        try {
            driver.get(baseUrl);
        } catch (org.openqa.selenium.TimeoutException coldStart) {
            driver.get(baseUrl);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        DriverFactory.quit();
    }

    /**
     * Capture a screenshot when an individual @Test method fails — but do NOT quit
     * the driver afterwards. The driver is class-scoped and continues to serve the
     * next @Test method in the class.
     */
    @AfterMethod(alwaysRun = true)
    public void captureFailureScreenshot(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE && driver != null) {
            captureScreenshot(result.getMethod().getMethodName());
        }
    }

    protected void captureScreenshot(String testName) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dir = Paths.get("test-output", "screenshots");
            Files.createDirectories(dir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            Files.copy(src.toPath(), dir.resolve(testName + "-" + stamp + ".png"));
        } catch (Exception ignored) {}
    }
}
