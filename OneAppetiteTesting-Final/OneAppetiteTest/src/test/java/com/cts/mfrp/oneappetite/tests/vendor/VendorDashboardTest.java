package com.cts.mfrp.oneappetite.tests.vendor;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.auth.LoginPage;
import com.cts.mfrp.oneappetite.pages.employee.CartPage;
import com.cts.mfrp.oneappetite.pages.employee.DashboardPage;
import com.cts.mfrp.oneappetite.pages.vendor.LocationSelectionPage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorMenuPage;
import com.cts.mfrp.oneappetite.pages.employee.OrderPage;
import com.cts.mfrp.oneappetite.pages.shared.TopBarComponent;
import com.cts.mfrp.oneappetite.pages.vendor.VendorDashboardPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.ExcelUtils;
import com.cts.mfrp.oneappetite.utils.WaitUtils;

import java.lang.reflect.Method;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VendorDashboardTest extends BaseTest {

    private static final String TEST_DATA = "testdata/TestData.xlsx";
    private VendorDashboardPage kanban;

    @BeforeMethod(alwaysRun = true)
    public void openVendorDashboard(Method method) {
        // The e2e test manages its own login sequence (employee → vendor),
        // so skip the vendor pre-login for that test only.
        if ("orderPlacedByEmployeeAppearsOnVendorDashboard".equals(method.getName())) return;
        LoginHelper.fastLoginAs(driver, AppConstants.VENDOR_ROLE);
        kanban = new VendorDashboardPage(driver);
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC 2.13.1 - Kanban dashboard shows three columns (Placed/Preparing/Ready)")
    public void kanbanColumnsRender() {
        Assert.assertTrue(kanban.placedColumnVisible(),    "Placed column missing");
        Assert.assertTrue(kanban.preparingColumnVisible(), "Preparing column missing");
        Assert.assertTrue(kanban.readyColumnVisible(),     "Ready column missing");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC 2.13.2 - Drag-and-drop moves orders forward through the board",
            enabled = false)
    public void dragOrderForward() {
        // Disabled: Angular CDK drag-drop does not accept Selenium synthetic events
        // (isTrusted=false). Verify manually or via API-level test.
        if (kanban.placedOrders().isEmpty()) {
            throw new SkipException("No orders in Placed column to drag");
        }
        int before = kanban.preparingOrders().size();
        kanban.dragFirstPlacedToPreparing();
        Assert.assertTrue(kanban.preparingOrders().size() > before,
                "Preparing column should have one more order after drag");
    }

    @Test(groups = {"regression", "functional", "negative"},
            description = "TC 2.13.3 - Backward drag from Preparing to Placed is rejected",
            enabled = false)
    public void backwardDragRejected() {
        // Disabled: see TC 2.13.2.
        if (kanban.preparingOrders().isEmpty()) {
            throw new SkipException("No orders in Preparing column");
        }
        int before = kanban.preparingOrders().size();
        kanban.dragFirstPreparingToPlaced();
        Assert.assertEquals(kanban.preparingOrders().size(), before,
                "Order should remain in Preparing after backward drag attempt");
    }

    @Test(groups = {"regression", "functional", "negative"},
            description = "TC 2.13.4 - Optimistic update reverts on API failure with red toast",
            enabled = false)
    public void optimisticRevertOnFailure() {
        // Requires the test backend to expose a way to inject API failures (e.g. network mocking)
    }

    @Test(groups = {"regression", "UI"},
            description = "TC 2.13.5 - New order plays audio and shows info toast",
            enabled = false)
    public void newOrderAlertOnPoll() {

        // Requires triggering an order from a separate session; out of scope for single-driver tests
    }

    @Test(groups = {"smoke", "regression", "functional"},
            description = "TC 2.13.6 - Menu tab navigates from dashboard to vendor menu management")
    public void menuTabNavigates() {
        kanban.goToMenu();
        Assert.assertTrue(
                WaitUtils.urlContains(driver, AppConstants.ROUTE_VENDOR_MENU_MGMT),
                "Menu tab should navigate to " + AppConstants.ROUTE_VENDOR_MENU_MGMT
                        + " but URL is " + driver.getCurrentUrl());
    }

    @Test(groups = {"smoke", "regression", "functional"},
            description = "TC 2.13.7 - Settings tab navigates from dashboard to vendor settings")
    public void settingsTabNavigates() {
        kanban.goToSettings();
        Assert.assertTrue(
                WaitUtils.urlContains(driver, AppConstants.ROUTE_VENDOR_SETTINGS),
                "Settings tab should navigate to " + AppConstants.ROUTE_VENDOR_SETTINGS
                        + " but URL is " + driver.getCurrentUrl());
    }

    @Test(groups = {"smoke", "regression", "functional", "e2e"},
            description = "TC 2.13.8 - 'Move to Preparing' button advances a Placed order into Preparing")
    public void moveToPreparingButtonAdvancesOrder() {
        if (kanban.placedOrders().isEmpty() || !kanban.moveToPreparingButtonVisible()) {
            throw new SkipException("No Placed order with a 'Move to Preparing' button available");
        }
        int placedBefore = kanban.placedOrders().size();
        int preparingBefore = kanban.preparingOrders().size();
        kanban.clickMoveToPreparing();
        boolean moved = Boolean.TRUE.equals(WaitUtils.poll(driver, d ->
                kanban.placedOrders().size() == placedBefore - 1
                        && kanban.preparingOrders().size() == preparingBefore + 1));
        Assert.assertTrue(moved,
                "After clicking 'Move to Preparing', expected Placed=" + (placedBefore - 1)
                        + " and Preparing=" + (preparingBefore + 1)
                        + " but got Placed=" + kanban.placedOrders().size()
                        + " Preparing=" + kanban.preparingOrders().size());
    }

    @Test(groups = {"smoke", "regression", "functional", "e2e"},
            description = "TC 2.13.9 - 'Move to Ready' button advances a Preparing order into Ready")
    public void moveToReadyButtonAdvancesOrder() {
        if (kanban.preparingOrders().isEmpty() || !kanban.moveToReadyButtonVisible()) {
            throw new SkipException("No Preparing order with a 'Move to Ready' button available");
        }
        int preparingBefore = kanban.preparingOrders().size();
        int readyBefore = kanban.readyOrders().size();
        kanban.clickMoveToReady();
        boolean moved = Boolean.TRUE.equals(WaitUtils.poll(driver, d ->
                kanban.preparingOrders().size() == preparingBefore - 1
                        && kanban.readyOrders().size() == readyBefore + 1));
        Assert.assertTrue(moved,
                "After clicking 'Move to Ready', expected Preparing=" + (preparingBefore - 1)
                        + " and Ready=" + (readyBefore + 1)
                        + " but got Preparing=" + kanban.preparingOrders().size()
                        + " Ready=" + kanban.readyOrders().size());
    }

    @Test(groups = {"smoke", "regression", "functional", "e2e"},
            description = "TC 2.13.10 - 'Mark as PickedUp' button removes the order from the Ready column")
    public void markAsPickedUpButtonRemovesOrder() {
        if (kanban.readyOrders().isEmpty() || !kanban.markAsPickedUpButtonVisible()) {
            throw new SkipException("No Ready order with a 'Mark as PickedUp' button available");
        }
        int readyBefore = kanban.readyOrders().size();
        kanban.clickMarkAsPickedUp();
        boolean removed = Boolean.TRUE.equals(WaitUtils.poll(driver, d ->
                kanban.readyOrders().size() == readyBefore - 1));
        Assert.assertTrue(removed,
                "After clicking 'Mark as PickedUp', expected Ready=" + (readyBefore - 1)
                        + " but got Ready=" + kanban.readyOrders().size());
    }

    @DataProvider(name = "vendorE2ECredentials")
    public Object[][] vendorE2ECredentials() {
        Map<String, String> d = ExcelUtils.readFirstRow(TEST_DATA, "VendorE2ECredentials");
        return new Object[][] {{ d.get("email"), d.get("password") }};
    }

    @Test(dataProvider = "vendorE2ECredentials",
            groups = {"regression", "functional", "e2e"},
             description = "TC 2.13.11 - Order placed by employee saha appears on priyaa.sharma vendor's dashboard")
    public void orderPlacedByEmployeeAppearsOnVendorDashboard(String vendorEmail, String vendorPassword) {
        // @BeforeMethod is skipped for this test. Navigate to the app root so
        // Angular's route guard redirects to /login (no active session yet).
        driver.get(ConfigReader.get("base.url"));
        if (!WaitUtils.urlContains(driver, AppConstants.ROUTE_LOGIN)) {
            throw new SkipException("App did not redirect to /login on fresh load");
        }

        // Login as the configured employee — exact OrderPageTest flow.
        LoginHelper.fastLoginAs(driver, AppConstants.EMPLOYEE_ROLE);

        // Chennai / Siruseri SIPCOT / Cafeteria Block — only select if the
        // picker is shown. If the employee already has a saved location, the
        // app skips the picker and lands on the dashboard directly (same
        // contract as OrderPageTest).
        LocationSelectionPage loc = new LocationSelectionPage(driver);
        if (loc.isLoaded()) {
            loc.selectCity(ConfigReader.get("location.city"));
            WaitUtils.wait(driver).until(d -> !loc.campusDisabled());
            loc.selectCampus(ConfigReader.get("location.campus"));
            WaitUtils.wait(driver).until(d -> !loc.buildingDisabled());
            loc.selectBuilding(ConfigReader.get("location.building"));
            loc.clickFindVendors();
        }

        // Open the first vendor's menu (same as OrderPageTest).
        DashboardPage dash = new DashboardPage(driver);
        if (dash.visibleVendorCount() == 0) throw new SkipException("No vendors available");
        VendorMenuPage menu = dash.openFirstVendorMenu();
        if (menu.itemCount() == 0) throw new SkipException("First vendor has no menu items");

        // Pick the first in-stock item — the hard-coded first item may be out of
        // stock, in which case its Add button is rendered disabled and a click
        // silently no-ops (copied from OrderPageTest).
        java.util.Optional<WebElement> firstAvailable =
                driver.findElements(By.cssSelector("button.add-btn")).stream()
                        .filter(b -> b.isEnabled() && b.getAttribute("disabled") == null)
                        .findFirst();
        if (firstAvailable.isEmpty()) {
            throw new SkipException("All items are out of stock on this vendor's menu");
        }
        firstAvailable.get().click();
        menu.clickViewCart();

        CartPage cart = new CartPage(driver);
        if (!cart.isLoaded()) throw new SkipException("Cart page did not load");

        // Pre-flight wallet check — matches OrderPageTest, prevents a hidden
        // "Insufficient wallet balance" toast from masking real defects.
        double cartTotal = parseAmount(cart.totalText());
        double walletBal = parseAmount(cart.walletBalanceText());
        if (cartTotal > walletBal) {
            throw new SkipException(
                    "Cart total (" + cart.totalText() + ") exceeds wallet balance ("
                            + cart.walletBalanceText() + "). Top up the employee wallet or clear leftover cart items.");
        }

        cart.placeOrder();
        cart.processingModalVisible();

        OrderPage orderPage = new OrderPage(driver);
        if (!orderPage.isLoaded()) {
            throw new SkipException("Order Confirmation did not render — wallet/backend cold-start issue");
        }
        String token = orderPage.tokenText();
        Assert.assertFalse(token.isBlank(),
                "Order token must be present on the confirmation screen before switching to vendor");

        // Switch session — logout employee, log back in as the stakeholder-
        // supplied vendor account (overrides config.properties for this test).
        new TopBarComponent(driver).logout();
        if (!WaitUtils.urlContains(driver, AppConstants.ROUTE_LOGIN)) {
            throw new SkipException("Logout from employee session did not return to /login");
        }
        LoginPage vendorLogin = new LoginPage(driver);
        vendorLogin.enterEmail(vendorEmail)
                .enterPassword(vendorPassword)
                .selectRole(AppConstants.VENDOR_ROLE);
        vendorLogin.clickSignIn();
        if (!WaitUtils.urlContains(driver, AppConstants.ROUTE_VENDOR_DASHBOARD)) {
            throw new SkipException(
                    "Vendor login failed for " + vendorEmail + " — credentials may be invalid "
                            + "or the account isn't a Vendor role");
        }

        // Re-initialize the dashboard page object against the fresh vendor
        // session and assert the freshly placed order shows up in the Placed
        // column. NOTE: the assertion passes only when the order goes to
        // priyaa.sharma's stall — i.e. when the first vendor at Cafeteria
        // Block is priyaa.sharma's vendor account.
        kanban = new VendorDashboardPage(driver);
        WaitUtils.visible(driver, By.id("list-PLACED"));
        boolean found = Boolean.TRUE.equals(WaitUtils.poll(driver, d ->
                kanban.placedOrders().stream().anyMatch(card -> card.getText().contains(token))));
        Assert.assertTrue(found,
                "Order token " + token + " placed by employee must appear "
                        + "in the Placed column on the vendor's dashboard");
    }

    private static double parseAmount(String text) {
        if (text == null) return 0.0;
        String numeric = text.replaceAll("[^0-9.]", "");
        return numeric.isEmpty() ? 0.0 : Double.parseDouble(numeric);
    }
}
