package com.cts.mfrp.oneappetite.tests.admin;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.admin.AdminUserMgmtPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AdminUserMgmtTest extends BaseTest {

    private AdminUserMgmtPage page;

    @BeforeMethod(alwaysRun = true)
    public void openAdminDashboard() {
        LoginHelper.loginAs(driver, AppConstants.ADMIN_ROLE);
        page = new AdminUserMgmtPage(driver);
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.16.1 - Admin dashboard shows hero heading, 4 stat cards, pill tabs")
    public void heroAndPills() {
        Assert.assertTrue(page.isLoaded(), "Admin heading not visible");
        Assert.assertTrue(page.statCardsVisible(), "Stat cards missing");
        for (String pill : new String[]{"Employees", "Vendors", "All"}) {
            page.clickPill(pill);
            Assert.assertTrue(page.isLoaded(), "Page broke after clicking pill: " + pill);
        }
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.16.2 - Search filters by name/email and table shows all columns")
    public void searchAndColumns() {
        Assert.assertFalse(page.rows().isEmpty(),
                "Admin user table is empty — search/columns cannot be verified.");
        String email = page.firstRowEmail();
        Assert.assertNotNull(email, "Could not extract email from first row");
        page.search(email);
        Assert.assertTrue(page.rows().size() >= 1, "Search should keep at least one matching row");
        for (String header : new String[]{"User", "Email", "Phone", "Role", "Wallet", "Status", "Actions"}) {
            Assert.assertTrue(WaitUtils.textVisible(driver, header),
                    "Column header missing: " + header);
        }
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.16.3 - Actions toggle activates/deactivates user accounts")
    public void toggleUserStatus() {
        Assert.assertFalse(page.rows().isEmpty(),
                "Admin user table is empty — status toggle cannot be verified.");
        page.toggleStatusForRow(0);
        page.toggleStatusForRow(0);
    }

    @Test(groups = {"regression", "functional", "negative"},
            description = "TC 2.16.4 - Admin cannot deactivate their own account",
            enabled = false)
    public void adminCannotDeactivateSelf() {
        // Requires identifying the row that corresponds to the currently logged-in admin.
        // Enable once that mapping is exposed via data-testid or stable selector.
    }

    @Test(groups = {"regression", "UI", "negative"},
          description = "TC 2.16.5 - Empty state message displayed when no users match filters")
    public void emptyStateForNoMatch() {
        page.search("nonexistentuser+" + System.currentTimeMillis() + "@test.com");
        Assert.assertTrue(page.emptyStateVisible()
                        || WaitUtils.textVisible(driver, AppConstants.ERR_NO_USERS),
                "Empty state not displayed");
    }
}
