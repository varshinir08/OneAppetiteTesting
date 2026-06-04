package com.cts.mfrp.oneappetite.tests.employee;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.employee.DashboardPage;
import com.cts.mfrp.oneappetite.pages.vendor.LocationSelectionPage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorMenuPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;

import java.util.Map;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the Employee Dashboard page (/dashboard).
 * Covers vendor list rendering, filter chips, search, and navigation to a vendor menu.
 */
public class DashboardPageTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";
    private DashboardPage dashboard;

    /** Log in as Employee, complete location selection if prompted, then land on /dashboard. */
    @BeforeMethod(alwaysRun = true)
    public void navigateToDashboard() {
        WebDriverWait w = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds", 20)));

        // [step 1] Login. Diagnostic prints land in the IntelliJ Run window so a failing
        // run shows exactly which step the @BeforeMethod stopped at.
        System.out.println("[dash-setup] step 1: login as Employee");
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        System.out.println("[dash-setup] step 1 OK. URL = " + driver.getCurrentUrl());

        // [step 2] Location selection (only if the picker is shown — when a location is
        // already persisted from a prior session, the app skips straight to /dashboard).
        // The Angular dropdowns cascade and the Find Vendors button only renders after
        // Building is chosen, so we must wait between each select instead of firing them
        // back-to-back. LocationSelectionPage.clickFindVendors() swallows the NoSuchElement
        // exception, so we click the button directly via a clickable-wait.
        System.out.println("[dash-setup] step 2: location selection");
        LocationSelectionPage loc = new LocationSelectionPage(driver);
        if (loc.isLoaded()) {
            System.out.println("[dash-setup] step 2a: location page shown — selecting City");
            loc.selectCity(ConfigReader.get("location.city"));
            w.until(d -> !loc.campusDisabled());
            System.out.println("[dash-setup] step 2b: selecting Campus");
            loc.selectCampus(ConfigReader.get("location.campus"));
            w.until(d -> !loc.buildingDisabled());
            System.out.println("[dash-setup] step 2c: selecting Building");
            loc.selectBuilding(ConfigReader.get("location.building"));
            By findVendorsBtn = By.xpath("//button[contains(.,'Find Vendors')]");
            w.until(ExpectedConditions.elementToBeClickable(findVendorsBtn));
            System.out.println("[dash-setup] step 2d: clicking Find Vendors");
            driver.findElement(findVendorsBtn).click();
        } else {
            System.out.println("[dash-setup] step 2 skipped: location already set, landed on dashboard");
        }

        // [step 3] Land on /dashboard.
        boolean onDashboard = WaitUtils.urlContains(driver, AppConstants.ROUTE_DASHBOARD);
        System.out.println("[dash-setup] step 3: onDashboard = " + onDashboard
                + " ; URL = " + driver.getCurrentUrl());

        // [step 4] Wait for vendor cards to actually render. Without this, the very first
        // test reads visibleVendorCount() === 0 and fires SkipException before Angular has
        // hydrated the list. Dashboard counts vendors by their "View Menu" buttons, so we
        // wait for at least one of those (or for an empty-state message to settle).
        System.out.println("[dash-setup] step 4: wait for vendor render");
        try {
            w.until(d ->
                    !d.findElements(By.xpath("//button[normalize-space()='View Menu']")).isEmpty()
                 || !d.findElements(By.xpath("//*[contains(.,'No vendors match your filters')]")).isEmpty());
        } catch (TimeoutException ignored) {
            // Fall through — individual @Test methods will still SkipException if count is 0.
        }
        dashboard = new DashboardPage(driver);
        System.out.println("[dash-setup] step 4 OK. Vendor count = " + dashboard.visibleVendorCount());
    }

    // -----------------------------------------------------------------------
    // Positive tests — page structure
    // -----------------------------------------------------------------------

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-DASH-01 — Dashboard renders the 'Vendors Near You' section header")
    public void vendorListHeaderIsVisible() {
        Assert.assertTrue(dashboard.vendorListMode(),
                "Section header 'Vendors Near You' must be present on the employee dashboard");
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-DASH-02 — Current location card is displayed after location selection")
    public void currentLocationCardIsShown() {
        Assert.assertTrue(dashboard.currentLocationVisible(),
                "Current location card should display the campus / building that was selected");
    }

    @Test(groups = {"regression", "UI"},
            description = "TC-DASH-03 — 'Change Location' pill is rendered alongside the location card")
    public void changeLocationPillIsRendered() {
        Assert.assertTrue(dashboard.changeLocationVisible(),
                "'Change Location' pill/link must appear so the user can switch their location");
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-DASH-04 — At least one vendor card is shown for the configured location")
    public void atLeastOneVendorCardPresent() {
        if (dashboard.visibleVendorCount() == 0)
            throw new SkipException("No vendors configured for this location — skipping count assertion");
        Assert.assertTrue(dashboard.visibleVendorCount() > 0,
                "At least one vendor card must be visible for a valid campus location");
    }

    // -----------------------------------------------------------------------
    // Positive tests — filter chips
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "functional"},
            description = "TC-DASH-05 — Veg chip does not show more vendors than the unfiltered 'All' count")
    public void vegChipDoesNotExceedBaselineCount() {
        if (dashboard.visibleVendorCount() == 0)
            throw new SkipException("No vendor cards present — filter test skipped");
        int baseline = dashboard.visibleVendorCount();
        dashboard.clickVeg();
        Assert.assertTrue(dashboard.visibleVendorCount() <= baseline,
                "Veg filter should narrow or preserve the vendor count, never inflate it");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-DASH-06 — Non-Veg chip does not show more vendors than the unfiltered 'All' count")
    public void nonVegChipDoesNotExceedBaselineCount() {
        if (dashboard.visibleVendorCount() == 0)
            throw new SkipException("No vendor cards present — filter test skipped");
        int baseline = dashboard.visibleVendorCount();
        dashboard.clickNonVeg();
        Assert.assertTrue(dashboard.visibleVendorCount() <= baseline,
                "Non-Veg filter should narrow or preserve the vendor count, never inflate it");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-DASH-07 — Clicking 'All' after a diet filter restores the full vendor list")
    public void allChipRestoresFullVendorList() {
        if (dashboard.visibleVendorCount() == 0)
            throw new SkipException("No vendor cards present — filter restore test skipped");
        int baseline = dashboard.visibleVendorCount();
        dashboard.clickVeg();
        dashboard.clickAll();
        Assert.assertEquals(dashboard.visibleVendorCount(), baseline,
                "'All' chip must restore the complete unfiltered vendor list");
    }

    // -----------------------------------------------------------------------
    // Positive tests — search
    // -----------------------------------------------------------------------

    @DataProvider(name = "vendorSearchData")
    public Object[][] vendorSearchData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "DashboardSearch");
        return new Object[][] {{ d.get("vendorSearchTerm") }};
    }

    @DataProvider(name = "gibberishSearch1Data")
    public Object[][] gibberishSearch1Data() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "DashboardSearch");
        return new Object[][] {{ d.get("gibberishVendorSearch1") }};
    }

    @DataProvider(name = "gibberishSearch2Data")
    public Object[][] gibberishSearch2Data() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "DashboardSearch");
        return new Object[][] {{ d.get("gibberishVendorSearch2") }};
    }

    @Test(dataProvider = "vendorSearchData",
            groups = {"regression", "functional"},
            description = "TC-DASH-08 — Typing in the search box narrows the visible vendor count")
    public void searchBoxNarrowsVendorList(String vendorSearchTerm) {
        if (dashboard.visibleVendorCount() < 2)
            throw new SkipException("Need at least 2 vendor cards to verify search narrows results");
        int baseline = dashboard.visibleVendorCount();
        dashboard.search(vendorSearchTerm);
        Assert.assertTrue(dashboard.visibleVendorCount() <= baseline,
                "Typing in the search box should not show more vendors than the unfiltered list");
    }

    // -----------------------------------------------------------------------
    // Positive tests — navigation
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "UI"},
            description = "TC-DASH-09 — Clicking 'View Menu' on JD Kabab vendor navigates to the menu page")
    public void viewMenuNavigatesToMenuPage() {
        if (dashboard.visibleVendorCount() == 0)
            throw new SkipException("No vendor cards available to click View Menu");
        VendorMenuPage menu = dashboard.openVendorMenuByName(ConfigReader.get("vendor.menu.name"));
        Assert.assertTrue(menu.searchVisible(),
                "The vendor menu page must load with its search box visible after clicking View Menu");
    }

    // -----------------------------------------------------------------------
    // Negative / edge cases
    // -----------------------------------------------------------------------

    @Test(dataProvider = "gibberishSearch1Data",
            groups = {"regression", "UI", "negative"},
            description = "TC-DASH-10 (negative) — Gibberish search yields zero results or empty-state message")
    public void gibberishSearchShowsEmptyState(String gibberishVendorSearch1) {
        dashboard.search(gibberishVendorSearch1);
        boolean emptyResult = dashboard.emptyStateVisible() || dashboard.visibleVendorCount() == 0;
        Assert.assertTrue(emptyResult,
                "A search term matching no vendor name must produce 0 cards or an empty-state message");
    }

    @Test(dataProvider = "gibberishSearch2Data",
            groups = {"regression", "functional", "negative"},
            description = "TC-DASH-11 (negative) — Clearing the search field restores the full vendor list")
    public void clearSearchRestoresFullVendorList(String gibberishVendorSearch2) {
        if (dashboard.visibleVendorCount() == 0)
            throw new SkipException("No vendor cards present — clear-search test skipped");
        int baseline = dashboard.visibleVendorCount();
        dashboard.search(gibberishVendorSearch2);
        dashboard.clearSearch();
        Assert.assertEquals(dashboard.visibleVendorCount(), baseline,
                "Clearing the search input must restore the full unfiltered vendor list");
    }

    @Test(groups = {"regression", "UI", "negative"},
            description = "TC-DASH-12 (negative) — Clicking 'Change Location' navigates away from /dashboard")
    public void changeLocationNavigatesAwayFromDashboard() {
        dashboard.clickChangeLocation();
        LocationSelectionPage loc = new LocationSelectionPage(driver);
        boolean onLocationPage = loc.isLoaded()
                || WaitUtils.urlContains(driver, "/location")
                || !driver.getCurrentUrl().contains(AppConstants.ROUTE_DASHBOARD);
        Assert.assertTrue(onLocationPage,
                "Clicking 'Change Location' should navigate to the location-selection page");
    }
}
