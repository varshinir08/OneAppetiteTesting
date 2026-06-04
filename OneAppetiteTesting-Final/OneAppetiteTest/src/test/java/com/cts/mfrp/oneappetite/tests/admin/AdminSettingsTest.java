package com.cts.mfrp.oneappetite.tests.admin;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.admin.AdminSettingsPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;

import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AdminSettingsTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";

    @DataProvider(name = "adminProfileData")
    public Object[][] adminProfileData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "AdminProfileEdit");
        return new Object[][] {{ d.get("profileName"), d.get("wrongCurrentPassword"), d.get("tempPassword") }};
    }

    @Test(dataProvider = "adminProfileData",
          groups = {"smoke", "regression", "UI"},
          description = "TC 2.17.1 - Admin settings has 3 cards, no Wallet card; profile editable")
    public void adminSettingsStructure(String profileName, String wrongCurrentPassword, String tempPassword) {
        LoginHelper.loginAs(driver, AppConstants.ADMIN_ROLE);
        LoginHelper.gotoViaSidebar(driver, AppConstants.ROUTE_ADMIN_SETTINGS);
        AdminSettingsPage page = new AdminSettingsPage(driver);
        Assert.assertTrue(page.profileCardVisible(),       "Profile card missing");
        Assert.assertTrue(page.notificationsCardVisible(), "Notifications card missing");
        Assert.assertTrue(page.passwordCardVisible(),      "Password card missing");
        Assert.assertFalse(page.walletCardVisible(),
                "Admin settings must not display the Wallet card (employee-only feature)");
        page.editFullName(profileName);
        page.editPhone(ConfigReader.get("valid.phone"));
        page.clickProfileSave();
        page.toggleAdminAlerts();
    }

    @Test(dataProvider = "adminProfileData",
          groups = {"regression", "functional", "negative"},
          description = "TC 2.17.2 (negative) - Password card rejects wrong current password (non-destructive: never submits a real rotation)")
    public void passwordCardRejectsWrongCurrent(String profileName, String wrongCurrentPassword, String tempPassword) {
        // Deliberately non-destructive: we only verify the negative path.
        // The earlier version of this test also rotated the password to a temp value and
        // reverted it in a finally block — if the revert ever failed, the admin account
        // would be locked behind an unknown password. Per stakeholder request, the real
        // rotation step is removed and only the wrong-current-password rejection is asserted.
        LoginHelper.loginAs(driver, AppConstants.ADMIN_ROLE);
        LoginHelper.gotoViaSidebar(driver, AppConstants.ROUTE_ADMIN_SETTINGS);
        AdminSettingsPage page = new AdminSettingsPage(driver);

        page.enterCurrentPassword(wrongCurrentPassword);
        page.enterNewPassword(tempPassword);
        page.enterConfirmPassword(tempPassword);
        page.clickPasswordSave();
        Assert.assertTrue(WaitUtils.textVisible(driver, "incorrect")
                        || WaitUtils.textVisible(driver, "Current password"),
                "Password card must show an error when the current-password field is wrong");
    }

    // TODO: re-enable once the frontend deploys SPA fallback on Render (rewrite /* -> /index.html).
    // Direct driver.get('/admin/settings') currently serves a blank page because Render doesn't
    // route unknown paths to index.html, so Angular never bootstraps and the route guard cannot run.
    @Test(groups = {"regression", "UI", "negative"},
          enabled = false,
          description = "TC 2.17.3 - Admin settings is role-gated for non-admin users")
    public void roleGuardOnAdminSettings() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        driver.get(ConfigReader.get("base.url") + AppConstants.ROUTE_ADMIN_SETTINGS);
        boolean redirected = WaitUtils.urlContains(driver, AppConstants.ROUTE_DASHBOARD)
                || WaitUtils.urlContains(driver, "403")
                || WaitUtils.urlContains(driver, "unauthorized");
        Assert.assertTrue(redirected,
                "Non-admin users should be redirected away from /admin/settings");
    }
}
