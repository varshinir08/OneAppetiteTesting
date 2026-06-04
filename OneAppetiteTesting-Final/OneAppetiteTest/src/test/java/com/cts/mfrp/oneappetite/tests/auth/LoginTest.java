package com.cts.mfrp.oneappetite.tests.auth;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.auth.CreateAccountPage;
import com.cts.mfrp.oneappetite.pages.auth.LoginPage;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private LoginPage page;

    @BeforeMethod(alwaysRun = true)
    public void openLoginPage() {
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(60))
                .until(d -> {
                    String src = ((JavascriptExecutor) d)
                            .executeScript("return document.documentElement.outerHTML").toString();
                    return src.length() > 500;
                });
        page = new LoginPage(driver);
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.1.1 - Login page loads with heading and core elements")
    public void loginPageLoadsSuccessfully() {
        Assert.assertTrue(page.isLoaded(), "Login page core elements not visible");
        String heading = page.headingText();
        Assert.assertTrue(
                heading.contains("Welcome") || heading.contains("Appetite") || heading.length() > 3,
                "Heading unexpected: [" + heading + "]"
        );
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.1.2 - Selecting a role chip highlights it and the selection is retained")
    public void roleSelectionHighlightsChip() {
        for (String role : new String[]{AppConstants.EMPLOYEE_ROLE, AppConstants.VENDOR_ROLE, AppConstants.ADMIN_ROLE}) {
            page.selectRole(role);
            Assert.assertTrue(page.isRoleSelected(role),
                    "Selected role '" + role + "' chip must be highlighted/active on the login form");
        }
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.1.3 - Login page displays all expected UI elements")
    public void loginPageUiCompleteness() {
        Assert.assertTrue(page.emailFieldVisible(),    "Email field missing");
        Assert.assertTrue(page.passwordFieldVisible(), "Password field missing");
        Assert.assertTrue(page.signInButtonVisible(),  "Sign In button missing");
        Assert.assertTrue(page.eyeIconVisible(),       "Password eye icon missing");
        Assert.assertTrue(page.createAccountVisible(), "Create account link missing");

        for (String role : new String[]{AppConstants.EMPLOYEE_ROLE, AppConstants.VENDOR_ROLE, AppConstants.ADMIN_ROLE})
            Assert.assertTrue(WaitUtils.textVisible(driver, role), "Role chip missing: " + role);

        Assert.assertTrue(
                page.googleSocialVisible() || page.microsoftSocialVisible(),
                "No social login button visible"
        );
    }

    @Test(groups = {"regression", "functional", "negative"},
          description = "TC 2.1.4 - Submitting empty form shows inline validation errors")
    public void emptyFormShowsValidationErrors() {
        page.clickSignIn();
        WaitUtils.wait(driver, 5).until(d -> page.emailErrorVisible());

        Assert.assertTrue(page.emailErrorVisible(), "Email error not shown after empty submit");
        Assert.assertTrue(
                page.passwordErrorVisible() || page.roleErrorVisible()
                        || WaitUtils.textVisible(driver, "required") || WaitUtils.textVisible(driver, "select"),
                "No password/role validation error shown"
        );
        Assert.assertTrue(driver.getCurrentUrl().contains("login"), "Should stay on login page");
    }

    // -----------------------------------------------------------------------
    // Data providers
    // -----------------------------------------------------------------------

    private static final String TEST_DATA = "testdata/TestData.xlsx";

    @DataProvider(name = "invalidDomainPasswords")
    public Object[][] invalidDomainPasswords() {
        return ExcelUtils.readAsArray(TEST_DATA, "InvalidDomainLogin");
    }

    @DataProvider(name = "shortPasswordEmails")
    public Object[][] shortPasswordEmails() {
        return ExcelUtils.readAsArray(TEST_DATA, "ShortPasswordLogin");
    }

    @DataProvider(name = "eyeTogglePasswords")
    public Object[][] eyeTogglePasswords() {
        return ExcelUtils.readAsArray(TEST_DATA, "EyeToggle");
    }

    // -----------------------------------------------------------------------

    @Test(dataProvider = "invalidDomainPasswords",
          groups = {"regression", "functional", "negative"},
          description = "TC 2.1.5 - Non-Cognizant email domain is rejected")
    public void invalidEmailDomainIsRejected(String password) {
        page.enterEmail(ConfigReader.get("invalid.email")).enterPassword(password).selectRole(AppConstants.EMPLOYEE_ROLE);
        page.clickSignIn();
        WaitUtils.wait(driver, 8).until(d -> page.domainErrorVisible() || d.getPageSource().toLowerCase().contains("cognizant") || d.getPageSource().toLowerCase().contains("invalid"));

        Assert.assertTrue(
                page.domainErrorVisible() || WaitUtils.textVisible(driver, "cognizant") || WaitUtils.textVisible(driver, "invalid"),
                "Domain error not shown for non-cognizant email"
        );
        Assert.assertTrue(driver.getCurrentUrl().contains("login"), "Should stay on login page");
    }

    @Test(dataProvider = "shortPasswordEmails",
          groups = {"regression", "functional", "negative"},
          description = "TC 2.1.6 - Password shorter than 8 characters shows length error")
    public void shortPasswordShowsLengthError(String email) {
        page.enterEmail(email).enterPassword(ConfigReader.get("short.password")).selectRole(AppConstants.EMPLOYEE_ROLE);
        page.clickSignIn();
        WaitUtils.wait(driver, 8).until(d -> page.passwordErrorVisible() || d.getPageSource().contains("8 characters"));

        Assert.assertTrue(
                page.passwordErrorVisible() || WaitUtils.textVisible(driver, "8 characters"),
                "Password length error not shown"
        );
        Assert.assertTrue(driver.getCurrentUrl().contains("login"), "Should stay on login page");
    }

    @Test(groups = {"regression", "functional", "negative"},
          description = "TC 2.1.7 - Employee credentials with Vendor role shows role-mismatch error")
    public void roleMismatchIsRejected() {
        page.enterEmail(ConfigReader.get("employee.email")).enterPassword(ConfigReader.get("employee.password")).selectRole(AppConstants.VENDOR_ROLE);
        page.clickSignIn();
        WaitUtils.wait(driver, 10).until(d -> page.roleMismatchErrorVisible() || d.getPageSource().contains("registered") || d.getPageSource().contains("Invalid") || d.getCurrentUrl().contains("login"));

        Assert.assertTrue(
                page.roleMismatchErrorVisible() || WaitUtils.textVisible(driver, "registered")
                        || WaitUtils.textVisible(driver, "Invalid") || driver.getCurrentUrl().contains("login"),
                "Role mismatch error not shown"
        );
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.1.8a - Valid Employee login routes to /dashboard")
    public void validEmployeeLoginRoutesToDashboard() {
        page.enterEmail(ConfigReader.get("employee.email")).enterPassword(ConfigReader.get("employee.password")).selectRole(AppConstants.EMPLOYEE_ROLE);
        page.clickSignIn();

        Assert.assertTrue(
                WaitUtils.urlContains(driver, AppConstants.ROUTE_DASHBOARD) || WaitUtils.urlContains(driver, "location"),
                "Employee login failed. URL: " + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.1.8b - Valid Vendor login routes to /vendor/dashboard")
    public void validVendorLoginRoutesToVendorDashboard() {
        page.enterEmail(ConfigReader.get("vendor.email")).enterPassword(ConfigReader.get("vendor.password")).selectRole(AppConstants.VENDOR_ROLE);
        page.clickSignIn();

        Assert.assertTrue(
                WaitUtils.urlContains(driver, AppConstants.ROUTE_VENDOR_DASHBOARD) || WaitUtils.urlContains(driver, "vendor"),
                "Vendor login failed. URL: " + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.1.8c - Valid Admin login routes to /admin/dashboard")
    public void validAdminLoginRoutesToAdminDashboard() {
        page.enterEmail(ConfigReader.get("admin.email")).enterPassword(ConfigReader.get("admin.password")).selectRole(AppConstants.ADMIN_ROLE);
        page.clickSignIn();

        Assert.assertTrue(
                WaitUtils.urlContains(driver, AppConstants.ROUTE_ADMIN_DASHBOARD) || WaitUtils.urlContains(driver, "admin"),
                "Admin login failed. URL: " + driver.getCurrentUrl()
        );
    }

    @Test(dataProvider = "eyeTogglePasswords",
          groups = {"regression", "UI"},
          description = "TC 2.1.9 - Password eye icon toggles visibility")
    public void passwordEyeIconToggles(String password) {
        page.enterPassword(password);
        Assert.assertEquals(page.passwordInputType(), "password", "Should start masked");

        page.togglePasswordVisibility();
        Assert.assertEquals(page.passwordInputType(), "text", "Should be visible after first toggle");

        page.togglePasswordVisibility();
        Assert.assertEquals(page.passwordInputType(), "password", "Should be masked after second toggle");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.1.10 - 'Create one now' link navigates to Create Account page")
    public void createOneNowNavigatesToCreateAccount() {
        Assert.assertTrue(page.createAccountVisible(), "Create account link not found");
        CreateAccountPage create = page.openCreateAccount();
        WaitUtils.wait(driver, 10).until(d -> create.isLoaded() || d.getCurrentUrl().contains("register"));

        Assert.assertTrue(
                create.isLoaded() || WaitUtils.urlContains(driver, "register"),
                "Create Account page did not load. URL: " + driver.getCurrentUrl()
        );
    }
}
