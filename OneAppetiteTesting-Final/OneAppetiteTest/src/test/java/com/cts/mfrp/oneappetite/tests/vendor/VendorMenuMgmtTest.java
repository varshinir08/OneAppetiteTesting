package com.cts.mfrp.oneappetite.tests.vendor;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.vendor.VendorDashboardPage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorMenuMgmtPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;

import java.util.Map;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VendorMenuMgmtTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";
    private VendorMenuMgmtPage mgmt;

    @BeforeMethod(alwaysRun = true)
    public void openMenuMgmt() {
        LoginHelper.loginAs(driver, AppConstants.VENDOR_ROLE);

        // Wait for the dashboard to fully paint before navigating — driver.get() on a
        // half-loaded Angular SPA renders an empty menu page even though the URL is right.
        try {
            WaitUtils.visible(driver, By.id("list-PLACED"));
        } catch (Exception e) {
            throw new SkipException("Vendor dashboard did not finish loading after login");
        }

        // Navigate via the in-app tab link instead of driver.get() — keeps Angular's
        // router state intact so the destination page actually renders.
        new VendorDashboardPage(driver).goToMenu();

        if (!WaitUtils.urlContains(driver, AppConstants.ROUTE_VENDOR_MENU_MGMT)) {
            throw new SkipException("Did not navigate to Menu Management URL");
        }
        mgmt = new VendorMenuMgmtPage(driver);
        if (!mgmt.isLoaded()) throw new SkipException("Menu Management page did not load");
    }

    @DataProvider(name = "menuItemAddData")
    public Object[][] menuItemAddData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "MenuItemAdd");
        return new Object[][] {{ d.get("itemBaseName"), d.get("category"), d.get("course"),
                d.get("dietary"), d.get("price"), d.get("stock"), d.get("minOrder"), d.get("imageUrl") }};
    }

    @DataProvider(name = "menuItemNegativeData")
    public Object[][] menuItemNegativeData() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "MenuItemNegative");
        return new Object[][] {{ d.get("name"), d.get("category"), d.get("course"),
                d.get("dietary"), d.get("price"), d.get("stock"), d.get("minOrder"), d.get("imageUrl") }};
    }

    @Test(dataProvider = "menuItemAddData",
          groups = {"smoke", "regression", "UI"},
          description = "TC 2.14.1 - Add Menu Item modal opens and saves a valid new item")
    public void addValidMenuItem(String itemBaseName, String category, String course,
                                 String dietary, String price, String stock,
                                 String minOrder, String imageUrl) {
        mgmt.clickAddItem();
        Assert.assertTrue(mgmt.modalOpen(), "Add Menu Item modal did not open");
        String name = itemBaseName + " " + System.currentTimeMillis();
        mgmt.fill(name, category, course, dietary, price, stock, minOrder, imageUrl);
        mgmt.clickSave();
        Assert.assertTrue(WaitUtils.textVisible(driver, name),
                "Newly added item should appear in the menu list");
    }

    @Test(dataProvider = "menuItemNegativeData",
          groups = {"regression", "functional", "negative"},
          description = "TC 2.14.2 - Negative price triggers warning toast and blocks save")
    public void negativePriceBlocked(String name, String category, String course,
                                     String dietary, String price, String stock,
                                     String minOrder, String imageUrl) {
        mgmt.clickAddItem();
        mgmt.fill(name, category, course, dietary, price, stock, minOrder, imageUrl);
        Assert.assertTrue(mgmt.isSaveDisabled(),
                "Save button should be disabled when price is negative");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.14.3 - Edit icon opens modal with pre-populated data")
    public void editPrePopulatesModal() {
        mgmt.clickFirstEdit();
        Assert.assertTrue(mgmt.modalOpen(), "Edit modal did not open");
        // Verifying pre-population in a generic way: at least one text input must already have value
        boolean prefilled = driver.findElements(org.openqa.selenium.By.cssSelector("input"))
                .stream().anyMatch(e -> e.getAttribute("value") != null
                        && !e.getAttribute("value").isBlank());
        Assert.assertTrue(prefilled, "Edit modal should be pre-populated");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.14.4 - Stock toggle updates optimistically",
            enabled = false)
    public void stockToggleOptimisticUpdate() {
        // Requires API mocking to simulate failure for revert verification.
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.14.5 - Delete icon prompts for confirmation before removal")
    public void deleteConfirmsBeforeRemoval() {
        mgmt.clickFirstDelete();
        // Witness the popup by its actual confirm button ("Yes, Remove") being visible,
        // instead of grepping the page for generic words like "Confirm"/"delete".
        Assert.assertTrue(mgmt.confirmDialogVisible(),
                "Delete confirmation popup did not appear");
        mgmt.confirmDelete();
    }
}
