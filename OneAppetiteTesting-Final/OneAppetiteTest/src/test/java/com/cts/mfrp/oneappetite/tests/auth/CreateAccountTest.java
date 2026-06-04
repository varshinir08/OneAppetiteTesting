package com.cts.mfrp.oneappetite.tests.auth;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.auth.CreateAccountPage;
import com.cts.mfrp.oneappetite.pages.auth.LoginPage;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;

import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class CreateAccountTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";

    private LoginPage loginPage;
    private CreateAccountPage page;

    @BeforeMethod(alwaysRun = true)
    public void initPages() {
        loginPage = new LoginPage(driver);
        page = new CreateAccountPage(driver);
    }

    @DataProvider(name = "createAccountNegativeData")
    public Object[][] createAccountNegativeData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "CreateAccountNegative");
        return new Object[][] {{ d.get("fullName"), d.get("validPassword"), d.get("cognizantEmail") }};
    }

    // Navigates from login to /register via "Create one now" link
    private void openRegisterPage() {
        new WebDriverWait(driver, Duration.ofSeconds(120))
                .until(d -> {
                    String src = ((JavascriptExecutor) d)
                            .executeScript("return document.documentElement.outerHTML").toString();
                    return src.length() > 500;
                });

        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> {
                    try { return d.findElement(By.xpath("//app-login//p/a")).isDisplayed(); }
                    catch (Exception e) { return false; }
                });
        driver.findElement(By.xpath("//app-login//p/a")).click();

        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> {
                    try {
                        return d.getCurrentUrl().contains("register")
                                && d.findElement(By.cssSelector("input[type='email']")).isDisplayed();
                    } catch (Exception e) { return false; }
                });

        page = new CreateAccountPage(driver);
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.3.1 - Create Account page UI is complete")
    public void createAccountUiComplete() {
        openRegisterPage();

        for (String role : new String[]{AppConstants.EMPLOYEE_ROLE, AppConstants.VENDOR_ROLE, AppConstants.ADMIN_ROLE})
            Assert.assertTrue(page.roleChipVisible(role), "Role chip missing: " + role);

        Assert.assertTrue(page.fullNameVisible(),  "Full Name field missing");
        Assert.assertTrue(page.workEmailVisible(), "Email field missing");
        Assert.assertTrue(page.phoneVisible(),     "Phone field missing");
        Assert.assertTrue(page.passwordVisible(),  "Password field missing");
        Assert.assertTrue(page.createBtnVisible(), "Create button missing");
        Assert.assertTrue(page.signInLinkVisible(),"Sign in link missing");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.3.2 - Selecting Vendor role reveals Vendor-specific fields")
    public void vendorRoleRevealsExtraFields() {
        openRegisterPage();

        page.selectRole(AppConstants.VENDOR_ROLE);
        WaitUtils.wait(driver, 5).until(d -> page.vendorFieldsVisible());
        Assert.assertTrue(page.vendorFieldsVisible(), "Vendor fields should appear for Vendor role");

        page.selectRole(AppConstants.EMPLOYEE_ROLE);
        new WebDriverWait(driver, java.time.Duration.ofSeconds(5)).until(d -> !page.vendorFieldsVisible());
        Assert.assertFalse(page.vendorFieldsVisible(), "Vendor fields should hide for Employee role");
    }

    @Test(dataProvider = "createAccountNegativeData",
          groups = {"regression", "functional", "negative"},
          description = "TC 2.3.3 - Invalid inputs show appropriate inline errors")
    public void invalidInputsShowErrors(String fullName, String validPassword, String cognizantEmail) {
        openRegisterPage();

        // Sub-test A: Non-Cognizant email
        page.selectRole(AppConstants.EMPLOYEE_ROLE);
        page.enterFullName(fullName);
        page.enterWorkEmail(ConfigReader.get("invalid.email"));
        page.enterPhone(ConfigReader.get("valid.phone"));
        page.enterPassword(validPassword);
        page.clickCreate();
        new WebDriverWait(driver, java.time.Duration.ofSeconds(5)).until(d -> page.emailDomainErrorVisible() || d.getPageSource().toLowerCase().contains("cognizant"));
        Assert.assertTrue(
                page.emailDomainErrorVisible() || WaitUtils.textVisible(driver, "cognizant"),
                "Email domain error not shown for @gmail.com"
        );

        // Sub-test B: Short phone
        page.enterWorkEmail(cognizantEmail);
        page.enterPhone(ConfigReader.get("short.phone"));
        page.clickCreate();
        new WebDriverWait(driver, java.time.Duration.ofSeconds(5)).until(d -> page.phoneErrorVisible() || d.getPageSource().contains("10 digits"));
        Assert.assertTrue(
                page.phoneErrorVisible() || WaitUtils.textVisible(driver, "10 digits"),
                "Phone error not shown for 5-digit input"
        );

        // Sub-test C: Short password
        page.enterPhone(ConfigReader.get("valid.phone"));
        page.enterPassword(ConfigReader.get("short.password"));
        page.clickCreate();
        new WebDriverWait(driver, java.time.Duration.ofSeconds(5)).until(d -> page.passwordErrorVisible() || d.getPageSource().contains("8 characters"));
        Assert.assertTrue(
                page.passwordErrorVisible() || WaitUtils.textVisible(driver, "8 characters"),
                "Password error not shown for short password"
        );
    }

    @DataProvider(name = "registrationData")
    public Object[][] registrationData() {
        return ExcelUtils.readAsArray("testdata/TestData.xlsx", "Registration");
    }

    @Test(dataProvider = "registrationData",
          groups = {"regression", "UI"},
          description = "TC 2.3.4 - Valid details register the user and route back to Login")
    public void validRegistrationCreatesAccount(String fullName, String email, String password) {
        openRegisterPage();

        page.selectRole(AppConstants.EMPLOYEE_ROLE);
        page.enterFullName(fullName);
        page.enterWorkEmail(email);
        page.enterPhone(ConfigReader.get("valid.phone"));
        page.enterPassword(password);
        page.clickCreate();

        // Wait up to 40 seconds for redirect — Render.com can be slow
        new WebDriverWait(driver, Duration.ofSeconds(40))
                .until(d -> d.getCurrentUrl().contains("login")
                        || WaitUtils.textVisible(d, "Account created")
                        || WaitUtils.textVisible(d, "already exists")
                        || WaitUtils.textVisible(d, "registered"));

        Assert.assertTrue(
                driver.getCurrentUrl().contains("login"),
                "Expected /login after registration. URL: " + driver.getCurrentUrl()
        );
        Assert.assertTrue(
                WaitUtils.textVisible(driver, "Account created")
                        || WaitUtils.textVisible(driver, "registered")
                        || WaitUtils.textVisible(driver, AppConstants.TOAST_ACCOUNT_CREATED),
                "Account creation toast not shown"
        );
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.3.5 - Sign in link on Create Account navigates back to Login")
    public void signInLinkReturnsToLogin() {
        openRegisterPage();

        Assert.assertTrue(page.signInLinkVisible(), "Sign in link not found on register page");
        page.clickSignIn();

        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(d -> {
                    try { return d.findElement(By.id("email")).isDisplayed(); }
                    catch (Exception e) { return false; }
                });

        Assert.assertTrue(driver.getCurrentUrl().contains("login"),
                "Expected /login. URL: " + driver.getCurrentUrl());
        Assert.assertTrue(new LoginPage(driver).isLoaded(),
                "Login page not visible after Sign in");
    }
}
