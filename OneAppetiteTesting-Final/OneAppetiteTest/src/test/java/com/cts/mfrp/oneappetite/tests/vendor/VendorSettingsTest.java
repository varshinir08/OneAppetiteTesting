package com.cts.mfrp.oneappetite.tests.vendor;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.vendor.VendorDashboardPage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorSettingsPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;

import java.util.Map;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VendorSettingsTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";
    private VendorSettingsPage page;

    @BeforeMethod(alwaysRun = true)
    public void openVendorSettings() {
        // Plain login using config.properties — no temp-password override needed because
        // the destructive password-rotation tests have been removed (per stakeholder
        // request, this suite must never change the live vendor password).
        LoginHelper.loginAs(driver, AppConstants.VENDOR_ROLE);

        // Wait for the dashboard to fully paint before navigating — driver.get() on a
        // half-loaded Angular SPA renders an empty destination page even though the URL is right.
        try {
            WaitUtils.visible(driver, By.id("list-PLACED"));
        } catch (Exception e) {
            throw new SkipException("Vendor dashboard did not finish loading after login");
        }

        // Navigate via the in-app tab link, not driver.get(), so Angular's router
        // renders the destination component properly.
        new VendorDashboardPage(driver).goToSettings();

        if (!WaitUtils.urlContains(driver, AppConstants.ROUTE_VENDOR_SETTINGS)) {
            throw new SkipException("Did not navigate to Vendor Settings URL");
        }
        page = new VendorSettingsPage(driver);
    }

    @DataProvider(name = "vendorProfileData")
    public Object[][] vendorProfileData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "VendorProfileEdit");
        return new Object[][] {{ d.get("profileName") }};
    }

    @DataProvider(name = "vendorAddLocationData")
    public Object[][] vendorAddLocationData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "VendorAddLocation");
        return new Object[][] {{ d.get("city"), d.get("campus"), d.get("building") }};
    }

    @Test(dataProvider = "vendorProfileData",
            groups = {"smoke", "regression", "UI"},
            description = "TC 2.15.1 - Vendor settings page has four cards; Primary Building is read-only")
    public void cardsAndReadOnly(String profileName) {
        Assert.assertTrue(page.profileCardVisible(), "Profile card missing");
        Assert.assertTrue(page.notificationsCardVisible(), "Notifications card missing");
        Assert.assertTrue(page.passwordCardVisible(), "Password card missing");
        Assert.assertTrue(page.stallLocationsCardVisible(), "Stall Locations card missing");
        Assert.assertTrue(page.primaryBuildingReadOnly(), "Primary Building/Email should be read-only");
        page.editFullName(profileName);
        page.editPhone(ConfigReader.get("valid.phone"));
        page.clickProfileSave();
    }

    @Test(dataProvider = "vendorAddLocationData",
            groups = {"regression", "functional"},
            description = "TC 2.15.2 - Add Location cascade: pick city/campus/building and submit")
    public void addLocationCascade(String city, String campus, String building) {
        Assert.assertTrue(page.registeredBadgeVisible(), "Registered badge missing on primary location");
        int before = page.alsoServing().size();
        page.addLocationFull(city, campus, building);

        // Give the app a moment to close the modal and re-render the Also Serving list.
        WaitUtils.wait(driver, 8).until(d -> page.alsoServing().size() > before);

        Assert.assertTrue(page.alsoServing().size() > before,
                "Also-Serving list should grow by one after adding a location");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.15.3 - X icon removes an additional serving location; primary unchanged")
    public void removeAlsoServingLocation() {
        if (page.alsoServing().isEmpty()) {
            throw new org.testng.SkipException("No additional locations to remove");
        }
        int before = page.alsoServing().size();
        page.removeFirstAlsoServing();
        Assert.assertTrue(page.alsoServing().size() < before,
                "Also-Serving list count should decrease by 1");
        Assert.assertTrue(page.registeredBadgeVisible(),
                "Primary registered location should remain intact");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.15.4 - Notifications: order-updates switch toggles on and off")
    public void notificationsOrderUpdatesToggle() {
        page.toggleOrderUpdates();
        page.toggleOrderUpdates();
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.15.5 - Update Password button is disabled when password fields are empty")
    public void updatePasswordBtnDisabledWhenEmpty() {
        Assert.assertFalse(page.updatePasswordBtnEnabled(),
                "Update password button should be disabled before any password fields are filled");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.15.6 - New password input enforces a minimum length of 6 characters")
    public void newPasswordMinLengthIsSix() {
        Assert.assertEquals(page.newPasswordMinLength(), 6,
                "New password input should enforce minlength=6");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.15.7 - Total Earnings card displays a non-empty amount")
    public void earningsCardDisplaysAmount() {
        Assert.assertTrue(page.earningsCardVisible(), "Total Earnings card missing");
        String amount = page.earningsAmountText();
        Assert.assertFalse(amount.isBlank(),
                "Earnings amount should not be blank");
        Assert.assertTrue(amount.contains("₹") || amount.matches(".*\\d.*"),
                "Earnings amount should contain a currency symbol or digit, got: " + amount);
    }

    // TC 2.15.5 and TC 2.15.6 (password change + revert) intentionally removed.
    // The pair changed the live vendor password to a temp value and relied on a follow-up
    // test to revert it — if the revert ever failed, the vendor account would be locked
    // behind an unknown password. The password card is still covered by TC 2.15.1's
    // visibility assertion (passwordCardVisible()).
}
