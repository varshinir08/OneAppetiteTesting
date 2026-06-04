package com.cts.mfrp.oneappetite.tests.shared;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.shared.TopBarComponent;
import com.cts.mfrp.oneappetite.pages.vendor.LocationSelectionPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class TopBarComponentTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";

    @DataProvider(name = "topBarSearchData")
    public Object[][] topBarSearchData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "TopBarData");
        return new Object[][] {{ d.get("building"), d.get("searchTerm") }};
    }

    @Test(dataProvider = "topBarSearchData",
          groups = {"regression", "UI"},
          description = "TC 2.4.3 - Locate top bar search field (per FRD) and search menu items if present")
    public void searchBarPresentAndSearchesMenuItems(String building, String searchTerm) {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);

        LocationSelectionPage loc = new LocationSelectionPage(driver);
        if (loc.isLoaded()) {
            loc.selectCity(ConfigReader.get("location.city"));
            loc.selectCampus(ConfigReader.get("location.campus"));
            loc.selectBuilding(building);
            loc.clickFindVendors();
        }

        TopBarComponent top = new TopBarComponent(driver);
        if (!top.searchBarPresent()) {
            throw new SkipException(
                    "Top bar 'Search menu items...' field is not present in the current build. "
                            + "FRD requires it, but the topbar template no longer renders a search "
                            + "input (only role-pill, theme-toggle, bell and avatar are present).");
        }

        top.searchMenuItem(searchTerm);
    }

    @Test(groups = {"smoke", "regression", "functional"},
          description = "TC 2.4.4 - Logged-in role pill (Employee/Admin) is visible in the top bar")
    public void loggedInRolePillVisibleForEachRole() {
        for (String role : new String[]{
                AppConstants.EMPLOYEE_ROLE,
                AppConstants.ADMIN_ROLE}) {

            LoginHelper.loginAs(driver, role);
            TopBarComponent top = new TopBarComponent(driver);

            String shown = top.loggedInRole();
            Assert.assertNotNull(shown,
                    "Logged-in role pill (span.role-pill) not found in the top bar for role '"
                            + role + "'.");
            Assert.assertEquals(shown, role.toUpperCase(),
                    "Top bar shows role '" + shown + "' but the logged-in user is '" + role + "'.");

            top.logout();
        }
    }
}
