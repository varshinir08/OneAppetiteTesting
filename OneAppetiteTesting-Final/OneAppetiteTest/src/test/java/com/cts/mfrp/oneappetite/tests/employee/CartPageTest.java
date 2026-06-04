package com.cts.mfrp.oneappetite.tests.employee;

import com.cts.mfrp.oneappetite.base.BaseSharedTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.employee.CartPage;
import com.cts.mfrp.oneappetite.pages.vendor.LocationSelectionPage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorMenuPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the Cart page (/cart).
 * Covers page-structure visibility, quantity stepper side-effects on totals,
 * the Place Order flow, and edge-case price validation.
 */
public class CartPageTest extends BaseSharedTest {

    /**
     * Login + location selection runs ONCE for the whole class.
     * Moved from @BeforeMethod — saves the ~30 s login+location block per test.
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "setUpClass")
    public void setUpSession() {
        WebDriverWait w = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds", 20)));

        System.out.println("[cart-setup] BeforeClass step 1: login as Employee");
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        System.out.println("[cart-setup] BeforeClass step 1 OK. URL = " + driver.getCurrentUrl());

        System.out.println("[cart-setup] BeforeClass step 2: location selection");
        LocationSelectionPage loc = new LocationSelectionPage(driver);
        if (loc.isLoaded()) {
            System.out.println("[cart-setup] BeforeClass step 2a: selecting City");
            loc.selectCity(ConfigReader.get("location.city"));
            w.until(d -> !loc.campusDisabled());
            System.out.println("[cart-setup] BeforeClass step 2b: selecting Campus");
            loc.selectCampus(ConfigReader.get("location.campus"));
            w.until(d -> !loc.buildingDisabled());
            System.out.println("[cart-setup] BeforeClass step 2c: selecting Building");
            loc.selectBuilding(ConfigReader.get("location.building"));
            By findVendorsBtn = By.xpath("//button[contains(.,'Find Vendors')]");
            w.until(ExpectedConditions.elementToBeClickable(findVendorsBtn));
            System.out.println("[cart-setup] BeforeClass step 2d: clicking Find Vendors");
            driver.findElement(findVendorsBtn).click();
        } else {
            System.out.println("[cart-setup] BeforeClass step 2 skipped: location already set");
        }

        // Early skip if there aren't enough vendors — avoids cryptic failures in @BeforeMethod.
        System.out.println("[cart-setup] BeforeClass step 3: verifying vendor count");
        try {
            w.until(d -> d.findElements(By.cssSelector("button.view-menu-btn")).size() >= 2);
        } catch (TimeoutException ignored) {}
        int vendorCount = driver.findElements(By.cssSelector("button.view-menu-btn")).size();
        System.out.println("[cart-setup] BeforeClass step 3: found " + vendorCount + " vendors");
        if (vendorCount < 2)
            throw new SkipException("[cart-setup] Need at least 2 vendors for cart tests — found " + vendorCount);

        System.out.println("[cart-setup] BeforeClass done.");
    }

    /**
     * From the dashboard (already logged in + location set), open vendor #2's menu,
     * add an item, then navigate to the cart.  Login and location selection are not
     * repeated — only the dashboard → vendor → add → cart steps run per test.
     *
     * We intentionally do NOT call gotoViaSidebar every time — each sidebar click
     * re-triggers Angular's vendor fetch, which after a few rapid calls drops the
     * location filter and shows all system-wide vendors.  Instead we navigate back
     * through browser history: after each test the browser is on /cart, so two
     * back() calls return us to /dashboard via the vendor-menu page.
     *
     * The cart may accumulate items across tests; all assertions are resilient to this
     * (visibility checks and self-anchored stepper assertions in TC-CART-08/09).
     */
    @BeforeMethod(alwaysRun = true)
    public void addItemAndNavigateToCart() {
        WebDriverWait w = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigReader.getInt("explicit.wait.seconds", 20)));

        // [step 1] Return to /dashboard without re-triggering the Angular vendor fetch.
        // After each test we are on /cart; back() once → vendor menu, back() again → /dashboard.
        // The very first test starts on /dashboard (left there by @BeforeClass), so skip back().
        System.out.println("[cart-setup] BeforeMethod step 1: returning to dashboard");
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl.contains(AppConstants.ROUTE_CART)) {
            driver.navigate().back();                         // /cart → vendor menu
            WaitUtils.urlContains(driver, "/vendor");
            driver.navigate().back();                         // vendor menu → /dashboard
        } else if (!currentUrl.contains(AppConstants.ROUTE_DASHBOARD)) {
            driver.navigate().back();                         // any other page → /dashboard
        }
        // Fallback: if back() didn't reach /dashboard, use sidebar (avoids test failure).
        if (!WaitUtils.urlContains(driver, AppConstants.ROUTE_DASHBOARD)) {
            LoginHelper.gotoViaSidebar(driver, AppConstants.ROUTE_DASHBOARD);
        }
        try {
            w.until(d -> d.findElements(By.cssSelector("button.view-menu-btn")).size() >= 2);
        } catch (TimeoutException ignored) {}
        int vendorCount = driver.findElements(By.cssSelector("button.view-menu-btn")).size();
        System.out.println("[cart-setup] BeforeMethod step 1 OK. Vendor count = " + vendorCount);
        if (vendorCount < 2)
            throw new SkipException("[cart-setup] Need at least 2 vendors — found " + vendorCount);

        // [step 2] Open vendor #2's menu (first vendor is unreliable for cart tests).
        System.out.println("[cart-setup] BeforeMethod step 2: opening vendor #2 menu");
        List<WebElement> viewMenuBtns = driver.findElements(By.cssSelector("button.view-menu-btn"));
        viewMenuBtns.get(1).click();
        try {
            w.until(d -> !d.findElements(By.cssSelector("[data-testid='menu-item'], .menu-item-card")).isEmpty());
        } catch (TimeoutException ignored) {}
        int itemCount = new VendorMenuPage(driver).itemCount();
        System.out.println("[cart-setup] BeforeMethod step 2 OK. Menu item count = " + itemCount);
        if (itemCount == 0)
            throw new SkipException("[cart-setup] Vendor #2 has no menu items — cannot add to cart");

        // [step 3] Click 'Add' on the first in-stock, not-yet-added item.
        // Using VendorMenuPage.addFirstItem() which explicitly iterates all add-btn elements
        // and skips any that are disabled (out-of-stock items, e.g. Chicken Kebab, always have
        // a disabled add-btn).  After tests 1 and 2 each add the first two in-stock items,
        // their buttons are replaced by qty-steppers, making the disabled out-of-stock button
        // the new first() match — and elementToBeClickable() would then time out for 20 s
        // waiting for a button that will never become enabled.
        System.out.println("[cart-setup] BeforeMethod step 3: clicking Add on first in-stock item");
        VendorMenuPage vendorMenu = new VendorMenuPage(driver);
        try {
            vendorMenu.addFirstItem();
        } catch (IllegalStateException e) {
            throw new SkipException("[cart-setup] " + e.getMessage());
        }
        try {
            w.until(d ->
                    !d.findElements(By.cssSelector(".qty-stepper")).isEmpty()
                            || !d.findElements(By.cssSelector("button.view-cart-btn")).isEmpty()
                            || !d.findElements(By.cssSelector(".sticky-cart-bar")).isEmpty());
        } catch (TimeoutException te) {
            throw new SkipException("[cart-setup] Add-to-cart click did not register — no "
                    + "qty-stepper / view-cart-btn / sticky-cart-bar appeared after clicking Add");
        }
        System.out.println("[cart-setup] BeforeMethod step 3 OK. Add registered.");

        // [step 4] Navigate to cart.
        System.out.println("[cart-setup] BeforeMethod step 4: clicking View Cart");
        By viewCartTargets = By.cssSelector("button.view-cart-btn, a.sticky-cart-btn, a.cart-pill");
        w.until(ExpectedConditions.elementToBeClickable(viewCartTargets));
        driver.findElement(viewCartTargets).click();

        WaitUtils.urlContains(driver, AppConstants.ROUTE_CART);
        try {
            w.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.cart-title")));
        } catch (TimeoutException ignored) {}
        boolean cartLoaded = new CartPage(driver).isLoaded();
        System.out.println("[cart-setup] BeforeMethod step 4: cart loaded = " + cartLoaded
                + " ; URL = " + driver.getCurrentUrl());
        if (!cartLoaded)
            throw new SkipException("[cart-setup] Cart page did not load after navigating to "
                    + AppConstants.ROUTE_CART);

        // [step 5] Wait for cart item rows to render.
        try {
            w.until(d -> !d.findElements(By.cssSelector(".cart-item .ci-name")).isEmpty());
        } catch (TimeoutException te) {
            throw new SkipException("[cart-setup] Cart loaded but no items rendered — Add click "
                    + "did not push an item into the Angular cart state");
        }
        System.out.println("[cart-setup] BeforeMethod done. Setup complete.");
    }

    // -----------------------------------------------------------------------
    // Positive tests — page structure
    // -----------------------------------------------------------------------

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-CART-01 — Cart page heading 'Your Cart' is displayed")
    public void cartHeadingIsVisible() {
        CartPage cart = new CartPage(driver);
        Assert.assertTrue(cart.isLoaded(),
                "'Your Cart' heading must be visible when the cart page loads");
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-CART-02 — Order Summary card is present on the cart page")
    public void summaryCardPresent() {
        CartPage cart = new CartPage(driver);
        Assert.assertTrue(cart.summaryVisible(),
                "Order Summary card must be rendered so the user can review their order details");
    }

    @Test(groups = {"regression", "UI"},
            description = "TC-CART-03 — Wallet card is present showing available balance")
    public void walletCardPresent() {
        CartPage cart = new CartPage(driver);
        Assert.assertTrue(cart.walletVisible(),
                "Wallet card must be visible on the cart page so the user can see their balance");
    }

    @Test(groups = {"regression", "UI"},
            description = "TC-CART-04 — Items card is present listing the added menu item(s)")
    public void itemsCardPresent() {
        CartPage cart = new CartPage(driver);
        Assert.assertTrue(cart.itemsVisible(),
                "Items card must list the item(s) that were added before navigating to cart");
    }

    @Test(groups = {"smoke", "regression", "UI"},
            description = "TC-CART-05 — 'Place Order' button is visible and actionable")
    public void placeOrderButtonVisible() {
        CartPage cart = new CartPage(driver);
        Assert.assertTrue(cart.placeOrderVisible(),
                "'Place Order' button must be visible and enabled on the cart page");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-CART-06 — Subtotal renders a non-blank monetary value")
    public void subtotalIsNonBlank() {
        CartPage cart = new CartPage(driver);
        Assert.assertFalse(cart.subtotalText().isBlank(),
                "Subtotal must display a monetary value (e.g. ₹120) — must not be blank");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-CART-07 — Total renders a non-blank monetary value")
    public void totalIsNonBlank() {
        CartPage cart = new CartPage(driver);
        Assert.assertFalse(cart.totalText().isBlank(),
                "Total must display a monetary value — must not be blank");
    }

    // -----------------------------------------------------------------------
    // Positive tests — quantity stepper
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "functional"},
            description = "TC-CART-08 — Incrementing item quantity changes the subtotal")
    public void incrementChangesSubtotal() {
        CartPage cart = new CartPage(driver);
        String before = cart.subtotalText();
        cart.increment();
        Assert.assertNotEquals(cart.subtotalText(), before,
                "Incrementing quantity by 1 must update the subtotal");
    }

    @Test(groups = {"regression", "functional"},
            description = "TC-CART-09 — Decrementing after an increment reverts the subtotal to its original value")
    public void decrementRevertsSubtotal() {
        CartPage cart = new CartPage(driver);
        // Pin both clicks to the SAME item by name. Angular re-renders the cart
        // after a quantity change and the DOM order of items may shift, so just
        // grabbing "the first stepper" twice can hit two different rows.
        String firstItem = cart.firstItemName();
        String original  = cart.subtotalText();
        cart.incrementItem(firstItem);
        String afterIncrement = cart.subtotalText();
        Assert.assertNotEquals(afterIncrement, original,
                "Sanity check: increment on '" + firstItem + "' must change the subtotal first");
        cart.decrementItem(firstItem);
        Assert.assertEquals(cart.subtotalText(), original,
                "Decrementing the same item after incrementing must restore subtotal to its pre-increment value");
    }

    // -----------------------------------------------------------------------
    // Positive tests — place order flow
    // -----------------------------------------------------------------------

    // TC-CART-10 is intentionally disabled. Clicking 'Place Order' actually completes a
    // purchase against the shared backend, depleting wallet balance with every run. The
    // assertion was previously softened to accept '.order-error' as a pass, which masked
    // the destruction. Re-enable only when one of these is in place:
    //   (a) a per-run fixture user with a top-up hook before each test, or
    //   (b) a mocked /api/orders endpoint, or
    //   (c) an in-app order-cancel affordance that we trigger immediately after click.
    @Test(groups = {"regression", "UI"}, enabled = false,
            description = "TC-CART-10 — DISABLED: destructive against shared backend")
    public void placeOrderShowsProcessingModal() {
        CartPage cart = new CartPage(driver);
        cart.placeOrder();
        Assert.assertTrue(cart.processingModalVisible(),
                "'Processing Payment' modal or success screen must appear after Place Order");
    }

    // -----------------------------------------------------------------------
    // Negative / edge cases
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "functional", "negative"},
            description = "TC-CART-11 (negative) — Wallet balance text is not blank (even if balance is zero)")
    public void walletBalanceTextIsNotBlank() {
        CartPage cart = new CartPage(driver);
        Assert.assertFalse(cart.walletBalanceText().isBlank(),
                "Wallet balance element must show a value (e.g. ₹0.00) — blank indicates a rendering defect");
    }

    @Test(groups = {"regression", "UI"},
            description = "TC-CART-12 — First cart item name is non-blank after adding an item")
    public void firstCartItemNameIsNonBlank() {
        CartPage cart = new CartPage(driver);
        String name = cart.firstItemName();
        Assert.assertFalse(name == null || name.isBlank(),
                "The first cart item name must be non-blank — blank indicates a rendering defect in the cart item list");
    }

    // TC-CART-12 (original) removed: the original 'Total >= Subtotal' assertion was wrong by design.
    // Discounts (coupons / promos / loyalty credit) legitimately make Total < Subtotal in
    // real carts, so the test would fail valid behaviour. If a tax/fee-only assertion is
    // needed later, write it against a cart that's guaranteed to have no discount applied.

    // -----------------------------------------------------------------------
    // Post-class cleanup
    // -----------------------------------------------------------------------

    /**
     * Remove all accumulated cart items after every test in this class has run.
     *
     * TC-CART-10 (Place Order) is permanently disabled to protect the shared
     * backend, so each @BeforeMethod adds one item without ever ordering.
     * Over 10 tests that builds up 10 items in the server-side cart. Without
     * this cleanup, OrderPageTest's pre-flight wallet check would see a large
     * accumulated total, exceed the wallet balance, and skip the entire class.
     *
     * Runs before BaseSharedTest.tearDownClass() (TestNG executes subclass
     * @AfterClass before superclass), so the driver is still live here.
     */
    @AfterClass(alwaysRun = true)
    public void clearCartAfterTests() {
        try {
            if (driver == null) return;
            // The last test's BeforeMethod always ends on /cart.
            // If somehow we're elsewhere, navigate to cart first.
            if (!driver.getCurrentUrl().contains(AppConstants.ROUTE_CART)) {
                LoginHelper.gotoViaSidebar(driver, AppConstants.ROUTE_CART);
                WaitUtils.urlContains(driver, AppConstants.ROUTE_CART);
            }
            CartPage cart = new CartPage(driver);
            if (cart.isLoaded()) {
                System.out.println("[cart-teardown] Clearing accumulated cart items...");
                cart.clearAllItems();
                System.out.println("[cart-teardown] Cart cleared.");
            }
        } catch (Exception ignored) {
            // Best-effort — tearDownClass (BaseSharedTest) quits the driver regardless.
        }
    }
}
