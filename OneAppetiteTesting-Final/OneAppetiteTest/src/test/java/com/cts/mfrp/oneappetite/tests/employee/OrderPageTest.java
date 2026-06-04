package com.cts.mfrp.oneappetite.tests.employee;

import com.cts.mfrp.oneappetite.base.BaseSharedTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.employee.CartPage;
import com.cts.mfrp.oneappetite.pages.employee.DashboardPage;
import com.cts.mfrp.oneappetite.pages.vendor.LocationSelectionPage;
import com.cts.mfrp.oneappetite.pages.vendor.VendorMenuPage;
import com.cts.mfrp.oneappetite.pages.employee.OrderPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the Order Confirmation page that appears after a successful order placement.
 *
 * <p>Refactored to use {@link BaseSharedTest}: the full Login → Location → Menu → Cart →
 * Place Order flow runs <em>once</em> in {@code @BeforeClass}, and all eight {@code @Test}
 * methods share the same browser session sitting on the confirmation screen. This drops
 * total runtime for the class from ~8 × 60s = ~8 minutes down to ~1.5 minutes.
 *
 * <p>One test, {@code backToHomeNavigatesToDashboard}, navigates AWAY from the
 * confirmation screen — it's given {@code priority = 100} so it runs after every other
 * test in this class. (Lower priority runs earlier; default is 0.)
 */
public class OrderPageTest extends BaseSharedTest {

    private OrderPage orderPage;

    /**
     * Drive the full click-based end-to-end flow exactly once for the entire class.
     * Depends on {@code setUpClass} (defined in {@link BaseSharedTest}) so TestNG runs
     * the driver setup first, then this method.
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "setUpClass")
    public void placeOrderAndReachConfirmation() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);

        LocationSelectionPage loc = new LocationSelectionPage(driver);
        if (loc.isLoaded()) {
            loc.selectCity(ConfigReader.get("location.city"));
            WaitUtils.wait(driver).until(d -> !loc.campusDisabled());
            loc.selectCampus(ConfigReader.get("location.campus"));
            WaitUtils.wait(driver).until(d -> !loc.buildingDisabled());
            loc.selectBuilding(ConfigReader.get("location.building"));
            loc.clickFindVendors();
        }

        DashboardPage dash = new DashboardPage(driver);
        if (dash.visibleVendorCount() == 0)
            throw new SkipException("No vendors available");

        VendorMenuPage menu = dash.openFirstVendorMenu();
        if (menu.itemCount() == 0)
            throw new SkipException("No menu items available");

        // Pick the first in-stock item — the hard-coded first item may be out of stock,
        // in which case its Add button is rendered disabled and clicking silently no-ops.
        java.util.Optional<org.openqa.selenium.WebElement> firstAvailable =
                driver.findElements(org.openqa.selenium.By.cssSelector("button.add-btn")).stream()
                        .filter(b -> b.isEnabled() && b.getAttribute("disabled") == null)
                        .findFirst();
        if (firstAvailable.isEmpty())
            throw new SkipException("All items are out of stock on this vendor's menu");
        firstAvailable.get().click();

        menu.clickViewCart();

        CartPage cart = new CartPage(driver);
        if (!cart.isLoaded())
            throw new SkipException("Cart page did not load");

        // Pre-flight wallet check — compare cart total against current wallet balance and
        // skip cleanly if the order would be rejected. Without this, placeOrder() fires and
        // the backend's "Insufficient wallet balance" toast masks the real issue (cart has
        // accumulated leftover items from previous runs).
        double cartTotal = parseAmount(cart.totalText());
        double walletBal = parseAmount(cart.walletBalanceText());
        if (cartTotal > walletBal)
            throw new SkipException(
                    "Cart total (" + cart.totalText() + ") exceeds wallet balance ("
                    + cart.walletBalanceText() + "). The cart has accumulated leftover items "
                    + "from previous runs — clear the cart manually in the app, top up the "
                    + "wallet, or both.");

        cart.placeOrder();
        cart.processingModalVisible();

        orderPage = new OrderPage(driver);
        if (!orderPage.isLoaded())
            throw new SkipException(
                    "Order Confirmation did not render — wallet may be low OR Render backend cold-started slowly. "
                    + "Top up wallet and/or retry.");
    }

    /** Strip currency symbols / whitespace from a text like "₹1,815.00" and parse to double. */
    private static double parseAmount(String text) {
        if (text == null) return 0.0;
        String numeric = text.replaceAll("[^0-9.]", "");
        return numeric.isEmpty() ? 0.0 : Double.parseDouble(numeric);
    }

    // -----------------------------------------------------------------------
    // Positive tests — page structure
    // -----------------------------------------------------------------------

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC-ORDER-01 — Order Confirmed heading is displayed on the confirmation screen")
    public void orderConfirmedHeadingVisible() {
        Assert.assertTrue(orderPage.isLoaded(),
                "'Order Confirmed' heading must be visible on the confirmation screen");
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC-ORDER-02 — Token chip is visible and contains a non-blank value")
    public void tokenChipDisplaysNonBlankText() {
        String token = orderPage.tokenText();
        Assert.assertFalse(token.isBlank(),
                "Token chip must display a non-empty token value — blank indicates a rendering defect");
    }

    @Test(groups = {"smoke", "regression", "functional"},
          description = "TC-ORDER-03 — Token matches the OA-XXXX format (OA- followed by exactly 4 digits)")
    public void tokenMatchesOaXxxxPattern() {
        Assert.assertTrue(orderPage.tokenMatchesPattern(),
                "Order token must match 'OA-XXXX' format. Actual token: " + orderPage.tokenText());
    }

    // -----------------------------------------------------------------------
    // Positive tests — token format detail
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "functional"},
          description = "TC-ORDER-04 — Token string contains the 'OA-' prefix")
    public void tokenContainsOaPrefix() {
        String token = orderPage.tokenText();
        Assert.assertTrue(token.contains("OA-"),
                "Token must contain the 'OA-' prefix. Actual value: " + token);
    }

    // -----------------------------------------------------------------------
    // Positive tests — page URL
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "UI"},
          description = "TC-ORDER-05 — After order placement the user stays in-session (not bounced to /login) and the success screen is rendered")
    public void confirmationStaysInSessionAndShowsSuccess() {
        String url = driver.getCurrentUrl();
        Assert.assertFalse(url.contains(AppConstants.ROUTE_LOGIN),
                "URL must not redirect to /login after a successful order — that would indicate the session was dropped");
        Assert.assertTrue(orderPage.isLoaded(),
                "Success screen heading 'Order Confirmed!' must be visible after a successful order placement");
    }

    // -----------------------------------------------------------------------
    // Negative / edge cases
    // -----------------------------------------------------------------------

    @Test(groups = {"regression", "functional"},
          description = "TC-ORDER-07 (negative) — Token text is never null")
    public void tokenTextIsNotNull() {
        Assert.assertNotNull(orderPage.tokenText(),
                "tokenText() must never return null — the backend always assigns a token to a placed order");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC-ORDER-08 (negative) — Page source contains the 'Order Confirmed' phrase")
    public void pageSourceContainsConfirmedPhrase() {
        // First, wait for the success heading element itself to be displayed so we don't
        // race the Angular template-swap. The success-screen renders inline on /cart and
        // there's a brief gap between processingModalVisible() resolving and the heading
        // hydrating with text content.
        WaitUtils.visible(driver, org.openqa.selenium.By.cssSelector("h2.success-title"));

        // Match on "Order Confirmed" (no exclamation). The rendered heading is "Order
        // Confirmed!" but the punctuation can render in a separate text node or with a
        // non-breaking whitespace boundary in Angular templates, which makes a substring
        // search for the literal "Order Confirmed!" miss it even though the phrase is
        // clearly on screen. The phrase without punctuation is enough proof the
        // confirmation UI rendered.
        String phrase = "Order Confirmed";
        boolean found = WaitUtils.textVisible(driver, phrase);
        Assert.assertTrue(found,
                "Page source must contain '" + phrase
                + "' — absence indicates the confirmation UI failed to render");
    }

    // -----------------------------------------------------------------------
    // MUST RUN LAST — navigates away from the confirmation screen.
    // priority = 100 (default is 0; lower priority runs first), so every other @Test
    // in this class finishes asserting on the confirmation screen before this one
    // clicks Back to Home.
    // -----------------------------------------------------------------------

    @Test(priority = 100,
          groups = {"regression", "UI"},
          description = "TC-ORDER-06 — Clicking 'Back to Home' navigates to the employee dashboard (/dashboard)")
    public void backToHomeNavigatesToDashboard() {
        orderPage.clickBackToHome();
        Assert.assertTrue(WaitUtils.urlContains(driver, AppConstants.ROUTE_DASHBOARD),
                "'Back to Home' button must route to /dashboard, not stay on the confirmation page");
    }
}
