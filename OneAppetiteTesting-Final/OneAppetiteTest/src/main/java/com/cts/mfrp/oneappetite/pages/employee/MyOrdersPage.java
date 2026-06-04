package com.cts.mfrp.oneappetite.pages.employee;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

/**
 * Page object for the Employee "My Orders" page (route /my-orders, custom element &lt;app-my-orders&gt;).
 * Surface:
 *   - Header (title + subtitle)
 *   - Filter tabs: All, Placed, Preparing, Ready, Picked Up
 *   - State containers: loading spinner, empty state, error state
 *   - Order cards: token (#OA-...), vendor, time, status badge, items (name/qty/price), footer (qty summary + total)
 */
public class MyOrdersPage extends BasePage {

    /* ---------- Header ---------- */

    @FindBy(css = "app-my-orders .orders-header h1")
    private WebElement heading;

    @FindBy(css = "app-my-orders .orders-header .subtitle")
    private WebElement subtitle;

    /* ---------- Filter tabs ---------- */

    @FindBy(css = "app-my-orders .filter-tabs .filter-tab")
    private List<WebElement> filterTabs;

    @FindBy(css = "app-my-orders .filter-tabs .filter-tab.active")
    private WebElement activeFilterTab;

    /* ---------- State blocks (conditionally rendered) ---------- */

    @FindBy(css = "app-my-orders .orders-state .spinner")
    private List<WebElement> loadingEls;

    @FindBy(css = "app-my-orders .orders-state.empty")
    private List<WebElement> emptyEls;

    @FindBy(css = "app-my-orders .orders-state.error")
    private List<WebElement> errorEls;

    /* ---------- Header CTA ("Order Again" — routes to /dashboard, per FRD) ---------- */

    @FindAll({
            @FindBy(css = "app-my-orders .orders-header .btn-primary"),
            @FindBy(css = "app-my-orders .orders-header button"),
            @FindBy(xpath = "//app-my-orders//*[contains(normalize-space(),'Order Again')]")
    })
    private List<WebElement> orderAgainEls;

    /* ---------- Order list ---------- */

    @FindBy(css = "app-my-orders ul.order-list > li.order-card")
    private List<WebElement> orderCards;

    /* ---------- Sidebar nav link (entry point) ---------- */

    @FindAll({
            @FindBy(css = "app-sidebar a[href$='/my-orders']"),
            @FindBy(css = "a[href$='/my-orders']"),
            @FindBy(css = "a[routerlink='/my-orders']"),
            @FindBy(xpath = "//app-sidebar//a[normalize-space()='My Orders']"),
            @FindBy(xpath = "//nav//a[normalize-space()='My Orders']")
    })
    private WebElement sidebarMyOrdersLink;

    public MyOrdersPage(WebDriver driver) { super(driver); }

    /**
     * Click the sidebar "My Orders" link from the dashboard and wait for the
     * page to render. Avoids direct URL nav (which 404/white-pages on Render).
     */
    public MyOrdersPage openFromSidebar() {
        WebElement link = waitVisible(sidebarMyOrdersLink);
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", link);
        try {
            waitClickable(link).click();
        } catch (Exception e) {
            jsClick(link);
        }
        wait.until(d -> d.getCurrentUrl().contains("/my-orders"));
        waitVisible(heading);
        return this;
    }

    /* ====================================================================== */
    /* Verifications                                                          */
    /* ====================================================================== */

    public boolean isLoaded() {
        return isDisplayed(heading) && "My Orders".equalsIgnoreCase(heading.getText().trim());
    }

    public String getHeading()  { return safeText(heading); }
    public String getSubtitle() { return safeText(subtitle); }

    public List<String> getFilterTabLabels() {
        List<String> labels = new ArrayList<>();
        for (WebElement tab : filterTabs) labels.add(tab.getText().trim());
        return labels;
    }

    public String getActiveFilterLabel() { return safeText(activeFilterTab); }

    public boolean filterTabVisible(String label) {
        for (WebElement tab : filterTabs) {
            if (label.equalsIgnoreCase(tab.getText().trim()) && tab.isDisplayed()) return true;
        }
        return false;
    }

    public boolean isLoadingVisible() { return anyDisplayed(loadingEls); }
    public boolean isEmptyStateVisible() { return anyDisplayed(emptyEls); }
    public boolean isErrorStateVisible() { return anyDisplayed(errorEls); }

    /** Per FRD: top-right "Order Again" button is visible. */
    public boolean isOrderAgainVisible() { return anyDisplayed(orderAgainEls); }

    /** Per FRD: clicking "Order Again" routes to /dashboard. */
    public void clickOrderAgain() {
        for (WebElement el : orderAgainEls) {
            try { if (el.isDisplayed()) { waitClickable(el).click(); return; } }
            catch (Exception ignored) {}
        }
        throw new NoSuchElementException("Order Again button not found on My Orders header");
    }

    public int getOrderCount() { return orderCards.size(); }

    public List<WebElement> visibleOrders() { return orderCards; }

    /* ====================================================================== */
    /* Actions                                                                */
    /* ====================================================================== */

    public MyOrdersPage clickFilter(String label) {
        for (WebElement tab : filterTabs) {
            if (label.equalsIgnoreCase(tab.getText().trim())) {
                waitClickable(tab).click();
                wait.until(d -> {
                    try { return label.equalsIgnoreCase(getActiveFilterLabel()); }
                    catch (Exception e) { return false; }
                });
                return this;
            }
        }
        throw new NoSuchElementException("Filter tab not found: " + label);
    }

    /* ====================================================================== */
    /* Per-card accessors — return a structured snapshot of one order card.   */
    /* ====================================================================== */

    public OrderCard getOrder(int index) {
        if (index < 0 || index >= orderCards.size()) {
            throw new IndexOutOfBoundsException("Order index " + index + " out of " + orderCards.size());
        }
        return new OrderCard(orderCards.get(index));
    }

    public List<OrderCard> getAllOrders() {
        List<OrderCard> out = new ArrayList<>();
        for (WebElement el : orderCards) out.add(new OrderCard(el));
        return out;
    }

    private static final By BY_TOKEN        = By.cssSelector(".order-vendor .token");
    private static final By BY_VENDOR       = By.cssSelector(".order-vendor h3");
    private static final By BY_TIME         = By.cssSelector(".order-vendor .time");
    private static final By BY_STATUS_BADGE = By.cssSelector(".status-badge");
    private static final By BY_ITEM_ROW     = By.cssSelector("ul.order-items > li");
    private static final By BY_ITEM_NAME    = By.cssSelector(".item-name");
    private static final By BY_ITEM_QTY     = By.cssSelector(".item-qty");
    private static final By BY_ITEM_PRICE   = By.cssSelector(".item-price");
    private static final By BY_QTY_SUMMARY  = By.cssSelector(".order-card-footer .qty-summary");
    private static final By BY_TOTAL        = By.cssSelector(".order-card-footer .total-amt");

    public static final class OrderCard {
        private final WebElement root;

        OrderCard(WebElement root) { this.root = root; }

        public String token()         { return childText(root, BY_TOKEN); }
        public String vendor()        { return childText(root, BY_VENDOR); }
        public String time()          { return childText(root, BY_TIME); }
        public String status()        { return childText(root, BY_STATUS_BADGE); }
        public String qtySummary()    { return childText(root, BY_QTY_SUMMARY); }
        public String total()         { return childText(root, BY_TOTAL); }

        /** CSS classes on the status badge, e.g. ["status-badge","badge-placed"]. */
        public String statusBadgeClass() {
            try { return root.findElement(BY_STATUS_BADGE).getAttribute("class"); }
            catch (NoSuchElementException | StaleElementReferenceException e) { return ""; }
        }

        public List<Item> items() {
            List<Item> out = new ArrayList<>();
            for (WebElement row : root.findElements(BY_ITEM_ROW)) out.add(new Item(row));
            return out;
        }

        public int itemRowCount() { return root.findElements(BY_ITEM_ROW).size(); }
    }

    public static final class Item {
        private final WebElement row;
        Item(WebElement row) { this.row = row; }

        public String name()  { return childText(row, BY_ITEM_NAME); }
        public String qty()   { return childText(row, BY_ITEM_QTY); }
        public String price() { return childText(row, BY_ITEM_PRICE); }
    }

    /* ---------- internal helpers ---------- */

    private static String childText(WebElement parent, By by) {
        try { return parent.findElement(by).getText().trim(); }
        catch (NoSuchElementException | StaleElementReferenceException e) { return ""; }
    }

    private static String safeText(WebElement el) {
        try { return el.getText().trim(); }
        catch (NoSuchElementException | StaleElementReferenceException e) { return ""; }
    }
}
