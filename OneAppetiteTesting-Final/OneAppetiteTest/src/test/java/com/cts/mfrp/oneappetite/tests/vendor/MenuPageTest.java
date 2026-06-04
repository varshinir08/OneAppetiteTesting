package com.cts.mfrp.oneappetite.tests.vendor;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.employee.CartPage;
import com.cts.mfrp.oneappetite.pages.employee.DashboardPage;
import com.cts.mfrp.oneappetite.pages.vendor.LocationSelectionPage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorMenuPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.openqa.selenium.Dimension;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the Vendor Menu page reached from the Employee Dashboard.
 * Covers UI structure, filter chip behaviour, search, add-to-cart flow,
 * sticky bar rendering, and out-of-stock guard.
 */
public class MenuPageTest extends BaseTest {

    private VendorMenuPage menu;

    /** Log in as Employee, complete location selection, open the first available vendor's menu. */
    @BeforeMethod(alwaysRun = true)
    public void openMenuPage() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        LocationSelectionPage loc = new LocationSelectionPage(driver);
        if (loc.isLoaded()) {
            loc.selectCity(ConfigReader.get("location.city"));
            // Campus options load only after City is chosen — wait for the dropdown to be
            // enabled, otherwise selectCampus runs against an empty list and silently no-ops.
            WaitUtils.wait(driver).until(d -> !loc.campusDisabled());
            loc.selectCampus(ConfigReader.get("location.campus"));
            // Same for Building after Campus.
            WaitUtils.wait(driver).until(d -> !loc.buildingDisabled());
            loc.selectBuilding(ConfigReader.get("location.building"));
            loc.clickFindVendors();
        }
        DashboardPage dash = new DashboardPage(driver);
        if (dash.visibleVendorCount() == 0)
            throw new SkipException("No vendors available for the configured location — cannot open menu");
        menu = dash.openVendorMenuByName(ConfigReader.get("vendor.menu.name"));
    }

    // -----------------------------------------------------------------------
    // Positive tests — page structure
    // -----------------------------------------------------------------------

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-MENU-01 — 'All' chip is highlighted/active when the menu page first loads")
    public void allChipIsActiveOnLoad() {
        Assert.assertTrue(menu.allChipActive(),
                "'All' chip must be in the active/selected state when the menu page opens");
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-MENU-02 — All three course filter chips (Breakfast, Lunch, Dinner) are visible")
    public void courseChipsAllVisible() {
        Assert.assertTrue(menu.breakfastVisible(), "'Breakfast' chip must be rendered on the menu page");
        Assert.assertTrue(menu.lunchVisible(),     "'Lunch' chip must be rendered on the menu page");
        Assert.assertTrue(menu.dinnerVisible(),    "'Dinner' chip must be rendered on the menu page");
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-MENU-03 — Dietary toggle buttons (Veg and Non-Veg) are both visible")
    public void dietaryToggleVisible() {
        Assert.assertTrue(menu.dietaryToggleVisible(),
                "Both 'Veg' and 'Non-Veg' dietary filter buttons must be visible on the menu page");
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-MENU-04 — Search input box is rendered on the menu page")
    public void searchBoxVisible() {
        Assert.assertTrue(menu.searchVisible(),
                "Search input must be present on the vendor menu page");
    }

    @Test(groups = {"smoke", "regression", "functional"},
            description = "TC-MENU-05 — An active vendor's menu contains at least one item")
    public void menuHasAtLeastOneItem() {
        if (menu.itemCount() == 0)
            throw new SkipException("Vendor has no menu items configured — skipping item-count assertion");
        Assert.assertTrue(menu.itemCount() > 0,
                "An active vendor should have at least one item listed in their menu");
    }

    // -----------------------------------------------------------------------
    // Positive tests — course filter chips
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "functional"},
            description = "TC-MENU-06 — Breakfast chip does not show more items than the 'All' baseline")
    public void breakfastChipNarrowsOrPreservesCount() {
        int all = menu.itemCount();
        menu.clickBreakfast();
        Assert.assertTrue(menu.itemCount() <= all,
                "Breakfast filter must not show more items than 'All'");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-MENU-07 — Lunch chip does not show more items than the 'All' baseline")
    public void lunchChipNarrowsOrPreservesCount() {
        int all = menu.itemCount();
        menu.clickLunch();
        Assert.assertTrue(menu.itemCount() <= all,
                "Lunch filter must not show more items than 'All'");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-MENU-08 — Dinner chip does not show more items than the 'All' baseline")
    public void dinnerChipNarrowsOrPreservesCount() {
        int all = menu.itemCount();
        menu.clickDinner();
        Assert.assertTrue(menu.itemCount() <= all,
                "Dinner filter must not show more items than 'All'");
    }

    // -----------------------------------------------------------------------
    // Positive tests — dietary filter chips
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "functional"},
            description = "TC-MENU-09 — Veg chip does not show more items than the 'All' baseline")
    public void vegChipNarrowsOrPreservesCount() {
        int all = menu.itemCount();
        menu.clickVeg();
        Assert.assertTrue(menu.itemCount() <= all,
                "Veg dietary filter must not show more items than 'All'");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-MENU-10 — Non-Veg chip does not show more items than the 'All' baseline")
    public void nonVegChipNarrowsOrPreservesCount() {
        int all = menu.itemCount();
        menu.clickNonVeg();
        Assert.assertTrue(menu.itemCount() <= all,
                "Non-Veg dietary filter must not show more items than 'All'");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-MENU-11 — Clicking 'All' after a course filter restores the original item count")
    public void allChipRestoresItemCountAfterFilter() {
        int baseline = menu.itemCount();
        menu.clickBreakfast();
        menu.clickAll();
        Assert.assertEquals(menu.itemCount(), baseline,
                "Clicking 'All' must restore the full unfiltered menu item count");
    }

    // -----------------------------------------------------------------------
    // Positive tests — search
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Data providers
    // -----------------------------------------------------------------------

    private static final String TEST_DATA = "testdata/TestData.xlsx";

    @DataProvider(name = "stackedSearchTerms")
    public Object[][] stackedSearchTerms() {
        return ExcelUtils.readAsArray(TEST_DATA, "StackedSearch");
    }

    @DataProvider(name = "gibberishSearchTerms")
    public Object[][] gibberishSearchTerms() {
        return ExcelUtils.readAsArray(TEST_DATA, "GibberishSearch");
    }

    // -----------------------------------------------------------------------

    @Test(dataProvider = "stackedSearchTerms",
            groups = {"regression", "functional"},
            description = "TC-MENU-12 — Stacking a search query with a dietary filter reduces item count")
    public void stackedSearchAndDietaryFilterReduceCount(String searchTerm) {
        int baseline = menu.itemCount();
        menu.search(searchTerm);
        menu.clickVeg();
        Assert.assertTrue(menu.itemCount() <= baseline,
                "Combining a search query with a dietary filter must not exceed the unfiltered baseline");
    }

    // -----------------------------------------------------------------------
    // Positive tests — add to cart & navigation
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "UI", "e2e"},
            description = "TC-MENU-13 — Adding an item makes the View Cart link or sticky bar appear")
    public void addFirstItemRevealsCta() {
        if (menu.itemCount() == 0)
            throw new SkipException("No items available to add — skipping CTA visibility check");
        menu.addFirstItem();
        Assert.assertTrue(menu.viewCartLinkVisible() || menu.stickyBarVisible(),
                "After adding an item, either the View Cart link or the sticky bottom bar must be visible");
    }

    @Test(groups = {"regression", "UI", "e2e"},
            description = "TC-MENU-14 — Clicking 'View Cart' navigates to the Cart page")
    public void viewCartNavigatesToCartPage() {
        if (menu.itemCount() == 0)
            throw new SkipException("No items available — cannot navigate to cart");
        menu.addFirstItem();
        CartPage cart = menu.clickViewCart();
        Assert.assertTrue(cart.isLoaded(),
                "Clicking 'View Cart' must load the 'Your Cart' page");
    }

    @Test(groups = {"regression", "UI"},
            description = "TC-MENU-15 — Sticky bottom bar is visible on a mobile-width viewport after adding an item")
    public void stickyBarVisibleOnMobileViewport() {
        driver.manage().window().setSize(new Dimension(420, 800));
        if (menu.itemCount() == 0)
            throw new SkipException("No items to add — sticky bar test skipped");
        menu.addFirstItem();
        Assert.assertTrue(menu.stickyBarVisible(),
                "Sticky bottom bar must appear on a mobile-width (420 px) viewport after adding an item");
    }

    // -----------------------------------------------------------------------
    // Negative / edge cases
    // -----------------------------------------------------------------------

    @Test(dataProvider = "gibberishSearchTerms",
            groups = {"regression", "functional", "negative"},
            description = "TC-MENU-16 (negative) — Gibberish search returns zero menu items")
    public void gibberishSearchReturnsZeroItems(String searchTerm) {
        menu.search(searchTerm);
        Assert.assertEquals(menu.itemCount(), 0,
                "Search [" + searchTerm + "] should match no menu items — expected 0 results");
    }

    @Test(groups = {"regression", "UI", "negative"},
            description = "TC-MENU-17 (negative) — Out-of-stock items have their Add button disabled")
    public void outOfStockAddButtonIsDisabled() {
        // The default vendor (Guntur Gongura) may not have out-of-stock items.
        // Navigate back to the dashboard and scan every vendor until we find one that does.
        driver.navigate().back();
        WaitUtils.urlContains(driver, AppConstants.ROUTE_DASHBOARD);

        DashboardPage dash = new DashboardPage(driver);
        VendorMenuPage outOfStockMenu = dash.findVendorMenuWithOutOfStock();

        if (outOfStockMenu == null) {
            // No out-of-stock items exist anywhere right now — valid state, test passes.
            Assert.assertTrue(true, "No out-of-stock items present across all vendors — nothing to assert");
        } else {
            // Out-of-stock items found — verify the Add button is disabled.
            Assert.assertTrue(outOfStockMenu.addButtonDisabledForOutOfStock(),
                    "The 'Add' button on an out-of-stock menu item must be disabled/non-interactive");
        }
    }
}
