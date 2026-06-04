package com.cts.mfrp.oneappetite.tests.auth;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.auth.ForgotPasswordModal;
import com.cts.mfrp.oneappetite.pages.auth.LoginPage;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

public class ForgotPasswordTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";
    private LoginPage loginPage;

    @BeforeMethod(alwaysRun = true)
    public void openLoginPage() {
        // Wait for Angular to render
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(60))
                .until(d -> {
                    String src = ((JavascriptExecutor) d)
                            .executeScript("return document.documentElement.outerHTML").toString();
                    return src.length() > 500;
                });
        loginPage = new LoginPage(driver);
    }

    // Helper — opens forgot password modal
    // Returns null and fails with BLOCKED message if link is missing
    private ForgotPasswordModal openModal(String tcId) {
        if (!loginPage.forgotPasswordVisible()) {
            Assert.fail(tcId + " BLOCKED: 'Forgot Password' link missing from Login page — defect from TC 2.1.3");
            return null;
        }
        return loginPage.openForgotPassword();
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.2.1 - Forgot Password link opens modal; URL stays /login")
    public void forgotPasswordOpensModal() {
        ForgotPasswordModal modal = openModal("TC 2.2.1");

        Assert.assertTrue(modal.isModalOpen(), "Modal did not open");
        Assert.assertTrue(driver.getCurrentUrl().contains("login"), "URL should remain /login");
        Assert.assertTrue(modal.isStep1Visible(), "Step 1 (phone + Send OTP) should be visible");
    }

    @Test(groups = {"regression", "functional", "negative"},
          description = "TC 2.2.2 - Phone fewer than 10 digits shows validation error")
    public void shortPhoneShowsValidationError() {
        ForgotPasswordModal modal = openModal("TC 2.2.2");

        modal.enterPhone(ConfigReader.get("short.phone"));
        modal.clickSendOtp();
        WaitUtils.wait(driver, 5).until(d -> modal.phoneErrorVisible() || d.getPageSource().contains("10 digits"));

        Assert.assertTrue(
                modal.phoneErrorVisible() || WaitUtils.textVisible(driver, "10 digits"),
                "Phone error not shown for short number"
        );
        Assert.assertFalse(modal.isStep2Visible(), "Should NOT advance to Step 2 with short phone");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.2.3 - Valid 10-digit phone advances to Step 2 with OTP toast")
    public void validPhoneAdvancesToStep2() {
        ForgotPasswordModal modal = openModal("TC 2.2.3");

        modal.enterPhone(ConfigReader.get("valid.phone"));
        modal.clickSendOtp();
        try { WaitUtils.wait(driver, 10).until(d -> modal.isStep2Visible() || d.getPageSource().contains("OTP sent")); } catch (org.openqa.selenium.TimeoutException ignored) {}

        Assert.assertTrue(
                modal.isStep2Visible()
                        || WaitUtils.textVisible(driver, AppConstants.TOAST_OTP_SENT)
                        || WaitUtils.textVisible(driver, "OTP sent"),
                "Step 2 or OTP sent toast not shown after valid phone"
        );
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.2.4 - Wrong number returns to Step 1; Resend OTP works")
    public void wrongNumberAndResend() {
        ForgotPasswordModal modal = openModal("TC 2.2.4");

        modal.enterPhone(ConfigReader.get("valid.phone"));
        modal.clickSendOtp();
        try { WaitUtils.wait(driver, 10).until(d -> modal.isStep2Visible()); } catch (org.openqa.selenium.TimeoutException ignored) {}

        if (!modal.isStep2Visible())
            Assert.fail("TC 2.2.4 PRE-CONDITION: Could not reach Step 2");

        modal.clickWrongNumber();
        WaitUtils.wait(driver, 5).until(d -> modal.isStep1Visible());
        Assert.assertTrue(modal.isStep1Visible(),   "'Wrong number' should return to Step 1");
        Assert.assertFalse(modal.isStep2Visible(),  "Step 2 should be hidden after Wrong number");

        modal.enterPhone(ConfigReader.get("valid.phone"));
        modal.clickSendOtp();
        try { WaitUtils.wait(driver, 10).until(d -> modal.isStep2Visible()); } catch (org.openqa.selenium.TimeoutException ignored) {}
        Assert.assertTrue(modal.isStep2Visible(), "Should return to Step 2 after re-sending OTP");

        modal.clickResendOtp();
        WaitUtils.wait(driver, 8).until(d -> d.getPageSource().contains("OTP sent") || d.getPageSource().contains(AppConstants.TOAST_OTP_SENT));
        Assert.assertTrue(
                WaitUtils.textVisible(driver, "OTP sent") || WaitUtils.textVisible(driver, AppConstants.TOAST_OTP_SENT),
                "OTP sent toast should reappear after Resend"
        );
    }

    @DataProvider(name = "forgotPasswordOtpData")
    public Object[][] forgotPasswordOtpData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "ForgotPasswordOtp");
        return new Object[][] {{ d.get("invalidOtp") }};
    }

    @Test(dataProvider = "forgotPasswordOtpData",
          groups = {"regression", "functional", "negative"},
          description = "TC 2.2.5 - Incorrect OTP shows error; modal stays on Step 2")
    public void invalidOtpShowsError(String invalidOtp) {
        ForgotPasswordModal modal = openModal("TC 2.2.5");

        modal.enterPhone(ConfigReader.get("valid.phone"));
        modal.clickSendOtp();
        try { WaitUtils.wait(driver, 10).until(d -> modal.isStep2Visible()); } catch (org.openqa.selenium.TimeoutException ignored) {}

        if (!modal.isStep2Visible())
            Assert.fail("TC 2.2.5 PRE-CONDITION: Could not reach Step 2");

        modal.enterOtp(invalidOtp);
        modal.clickVerify();
        WaitUtils.wait(driver, 8).until(d -> modal.otpErrorVisible() || d.getPageSource().contains("Invalid or expired OTP"));

        Assert.assertTrue(
                modal.otpErrorVisible() || WaitUtils.textVisible(driver, "Invalid or expired OTP"),
                "'Invalid or expired OTP' error not shown for wrong OTP"
        );
        Assert.assertFalse(modal.isStep3Visible(), "Should NOT advance to Step 3 with wrong OTP");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.2.6 - Valid OTP + new password resets successfully",
            enabled = false) // Requires real OTP delivery
    public void resetPasswordHappyPath() {
        // Enable when OTP can be intercepted or mocked in test environment
    }
}
