package com.cts.mfrp.oneappetite.tests.employee;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.employee.MyOrdersPage;
import com.cts.mfrp.oneappetite.pages.employee.MyOrdersPage.Item;
import com.cts.mfrp.oneappetite.pages.employee.MyOrdersPage.OrderCard;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class MyOrdersTest extends BaseTest {

    private static final List<String> EXPECTED_FILTERS =
            Arrays.asList("All", "Placed", "Preparing", "Ready", "Picked Up");

    private MyOrdersPage page;

    @BeforeMethod(alwaysRun = true)
    public void openMyOrders() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        page = new MyOrdersPage(driver).openFromSidebar();
    }

    /* ====================================================================== */
    /* Header & route                                                         */
    /* ====================================================================== */

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.11.1 - My Orders page loads with correct route and heading")
    public void pageLoadsWithCorrectRouteAndHeading() {
        Assert.assertTrue(page.currentUrl().contains(AppConstants.ROUTE_MY_ORDERS),
                "URL should contain " + AppConstants.ROUTE_MY_ORDERS + " but was " + page.currentUrl());
        Assert.assertTrue(page.isLoaded(), "My Orders page heading not displayed");
        Assert.assertEquals(page.getHeading(), AppConstants.MY_ORDERS_HEADING);
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.2 - Subtitle text is present and non-empty")
    public void subtitleIsPresent() {
        String subtitle = page.getSubtitle();
        Assert.assertFalse(subtitle.isBlank(), "Subtitle is blank");
        Assert.assertTrue(subtitle.toLowerCase().contains("track"),
                "Subtitle should describe tracking orders, got: " + subtitle);
    }

    /* ====================================================================== */
    /* Filter tabs                                                            */
    /* ====================================================================== */

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.11.3 - All five filter tabs render in the correct order")
    public void allFilterTabsRender() {
        List<String> actual = page.getFilterTabLabels();
        Assert.assertEquals(actual, EXPECTED_FILTERS,
                "Filter tabs mismatch. Expected " + EXPECTED_FILTERS + " got " + actual);
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.11.4 - 'All' is the default active filter on first load")
    public void defaultActiveFilterIsAll() {
        Assert.assertEquals(page.getActiveFilterLabel(), "All",
                "Default active filter should be 'All'");
    }

    @DataProvider(name = "filters")
    public Object[][] filters() {
        return new Object[][] { {"Placed"}, {"Preparing"}, {"Ready"}, {"Picked Up"}, {"All"} };
    }

    @Test(groups = {"regression", "UI"},
            description = "TC 2.11.5 - Each filter tab becomes active when clicked",
            dataProvider = "filters")
    public void clickingFilterMakesItActive(String label) {
        page.clickFilter(label);
        Assert.assertEquals(page.getActiveFilterLabel(), label,
                "Active tab should be '" + label + "' after click");
        Assert.assertTrue(page.isLoaded(), "Page broke after selecting filter: " + label);
    }

    @Test(groups = {"regression", "functional", "negative"},
          description = "TC 2.11.6 - Clicking a non-existent filter throws (negative)")
    public void clickingUnknownFilterThrows() {
        try {
            page.clickFilter("Cancelled");
            Assert.fail("Expected NoSuchElementException for unknown filter 'Cancelled'");
        } catch (org.openqa.selenium.NoSuchElementException expected) { /* good */ }
    }

    /* ====================================================================== */
    /* Order list & state                                                     */
    /* ====================================================================== */

    @Test(groups = {"regression", "UI"},
          description = "TC 2.11.7 - Page renders exactly one of: orders / empty / loading / error")
    public void exactlyOneStateRendered() {
        boolean hasOrders  = page.getOrderCount() > 0;
        boolean empty      = page.isEmptyStateVisible();
        boolean loading    = page.isLoadingVisible();
        boolean error      = page.isErrorStateVisible();

        int trueCount = (hasOrders ? 1 : 0) + (empty ? 1 : 0) + (loading ? 1 : 0) + (error ? 1 : 0);
        Assert.assertEquals(trueCount, 1,
                "Exactly one of orders/empty/loading/error should be visible. "
                        + "orders=" + hasOrders + " empty=" + empty
                        + " loading=" + loading + " error=" + error);
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.8 - Each order card exposes token, vendor, time, status, total")
    public void orderCardCoreFieldsPresent() {
        Assert.assertTrue(page.getOrderCount() > 0,
                "Expected at least one order card to validate fields against");
        for (OrderCard card : page.getAllOrders()) {
            Assert.assertTrue(card.token().matches("(?i)#OA-\\d+"),
                    "Token should match #OA-<digits>, was: " + card.token());
            Assert.assertFalse(card.vendor().isBlank(), "Vendor name is blank");
            Assert.assertFalse(card.time().isBlank(),   "Time is blank");
            Assert.assertFalse(card.status().isBlank(), "Status is blank");
            Assert.assertTrue(card.total().matches("₹\\d+(\\.\\d{2})?"),
                    "Total should be in ₹0.00 format, was: " + card.total());
        }
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.9 - Every item row has name, qty (×N), and price (₹...)")
    public void itemRowsHaveNameQtyPrice() {
        Assert.assertTrue(page.getOrderCount() > 0,
                "Expected at least one order card to validate item rows against");
        for (OrderCard card : page.getAllOrders()) {
            Assert.assertTrue(card.itemRowCount() > 0, "Order has no item rows: " + card.token());
            for (Item item : card.items()) {
                Assert.assertFalse(item.name().isBlank(),
                        "Item name blank in " + card.token());
                Assert.assertTrue(item.qty().matches("×\\d+"),
                        "Qty should be ×N, was: " + item.qty());
                Assert.assertTrue(item.price().matches("₹\\d+(\\.\\d{2})?"),
                        "Price should be ₹N, was: " + item.price());
            }
        }
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.10 - Footer qty summary matches item row count")
    public void footerQtySummaryMatchesRows() {
        Assert.assertTrue(page.getOrderCount() > 0,
                "Expected at least one order card to validate footer summary against");
        for (OrderCard card : page.getAllOrders()) {
            String summary = card.qtySummary();   // e.g. "1 items"
            int summaryNum = Integer.parseInt(summary.replaceAll("\\D+", ""));
            Assert.assertEquals(summaryNum, card.itemRowCount(),
                    "Footer summary '" + summary + "' should equal item-row count for "
                            + card.token());
        }
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.11.11 - Empty state shows expected copy when filter yields no orders")
    public void emptyStateCopyForNoMatch() {
        // Iterate filters until we hit one that empties the list (or run out).
        for (String f : new String[]{"Picked Up", "Ready", "Preparing", "Placed"}) {
            page.clickFilter(f);
            if (page.getOrderCount() == 0) {
                Assert.assertTrue(
                        page.isEmptyStateVisible()
                                || WaitUtils.textVisible(driver, AppConstants.ERR_NO_ORDERS),
                        "Filter '" + f + "' produced no orders but empty state not shown");
                return;
            }
        }
        Assert.fail("All filters returned orders; empty-state copy could not be validated");
    }

    /* ====================================================================== */
    /* FRD additions                                                          */
    /* ====================================================================== */

    @Test(groups = {"regression", "UI"},
          description = "TC 2.11.12 - 'Order Again' button is visible in the top-right header")
    public void orderAgainButtonVisible() {
        Assert.assertTrue(page.isOrderAgainVisible(),
                "FRD requires an 'Order Again' button at the top right of the My Orders header");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.11.13 - Clicking 'Order Again' routes to /dashboard")
    public void orderAgainRoutesToDashboard() {
        Assert.assertTrue(page.isOrderAgainVisible(),
                "FRD requires an 'Order Again' button; cannot click what isn't rendered");
        page.clickOrderAgain();
        Assert.assertTrue(WaitUtils.urlContains(driver, AppConstants.ROUTE_DASHBOARD),
                "Clicking Order Again should navigate to " + AppConstants.ROUTE_DASHBOARD
                        + " but URL was " + driver.getCurrentUrl());
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.14 - Selecting a non-All filter narrows the list to matching statuses")
    public void filterNarrowsListToMatchingStatuses() {
        Assert.assertTrue(page.getOrderCount() > 0,
                "Expected at least one order to validate filter narrowing");
        java.util.Map<String, String> labelToStatus = new java.util.LinkedHashMap<>();
        labelToStatus.put("Placed",    "PLACED");
        labelToStatus.put("Preparing", "PREPARING");
        labelToStatus.put("Ready",     "READY");
        labelToStatus.put("Picked Up", "PICKED_UP");

        for (java.util.Map.Entry<String, String> e : labelToStatus.entrySet()) {
            String label = e.getKey();
            String expectedStatus = e.getValue();
            page.clickFilter(label);
            for (OrderCard card : page.getAllOrders()) {
                String actual = card.status().trim().toUpperCase().replace(' ', '_');
                Assert.assertEquals(actual, expectedStatus,
                        "Filter '" + label + "' should only show " + expectedStatus
                                + " orders, but " + card.token() + " shows " + actual);
            }
        }
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.15 - Each card's total equals Σ(item price × qty)")
    public void totalEqualsSumOfItems() {
        Assert.assertTrue(page.getOrderCount() > 0,
                "Expected at least one order to validate totals");
        for (OrderCard card : page.getAllOrders()) {
            java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
            for (Item item : card.items()) {
                int qty = Integer.parseInt(item.qty().replaceAll("\\D+", ""));
                java.math.BigDecimal price = new java.math.BigDecimal(
                        item.price().replaceAll("[^0-9.]", ""));
                sum = sum.add(price.multiply(java.math.BigDecimal.valueOf(qty)));
            }
            java.math.BigDecimal total = new java.math.BigDecimal(
                    card.total().replaceAll("[^0-9.]", ""));
            Assert.assertTrue(total.compareTo(sum) == 0,
                    "Order " + card.token() + " total (" + total
                            + ") != Σ(item price × qty) (" + sum + ")");
        }
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.16 - All visible order tokens are unique")
    public void orderTokensAreUnique() {
        Assert.assertTrue(page.getOrderCount() > 0,
                "Expected at least one order to check token uniqueness");
        java.util.List<String> tokens = new java.util.ArrayList<>();
        for (OrderCard card : page.getAllOrders()) tokens.add(card.token());
        java.util.Set<String> unique = new java.util.HashSet<>(tokens);
        Assert.assertEquals(unique.size(), tokens.size(),
                "Duplicate tokens detected: " + tokens);
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.11.17 - Orders are listed most-recent first (per FRD)")
    public void ordersSortedMostRecentFirst() {
        Assert.assertTrue(page.getOrderCount() >= 2,
                "Need at least 2 orders to verify sort order; got " + page.getOrderCount());
        // Rendered as "MMM d, hh:mm a" with no year (e.g. "May 15, 09:17 AM").
        // Prefix a fixed year so they're parseable and still comparable to each other.
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                .ofPattern("yyyy MMM d, hh:mm a", java.util.Locale.ENGLISH);
        java.time.LocalDateTime prev = null;
        for (OrderCard card : page.getAllOrders()) {
            String raw = card.time();
            Assert.assertFalse(raw == null || raw.isBlank() || "—".equals(raw),
                    "Order timestamp must be present and parseable, was: " + raw);
            java.time.LocalDateTime cur;
            try {
                cur = java.time.LocalDateTime.parse("2000 " + raw, fmt);
            } catch (Exception parseFail) {
                Assert.fail("Cannot parse order time '" + raw + "': " + parseFail.getMessage());
                return;
            }
            if (prev != null) {
                Assert.assertTrue(!cur.isAfter(prev),
                        "Orders should be most-recent first, but " + raw + " comes after " + prev);
            }
            prev = cur;
        }
    }
}
