package com.cts.mfrp.oneappetite.pages.employee;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Cart page served at /cart by CartViewComponent. Items are grouped under
 * &lt;section class="vendor-section"&gt; (one per vendor). Wallet and grand
 * total live in dedicated cards; per-vendor subtotal is in the section footer.
 */
public class CartPage extends BasePage {

    @FindBy(css = "h1.cart-title")
    private WebElement heading;

    @FindAll({
            @FindBy(css = "[data-testid='summary-card']"),
            @FindBy(css = ".summary-card")
    })
    private List<WebElement> summaryCard;

    @FindAll({
            @FindBy(css = "[data-testid='wallet-card']"),
            @FindBy(css = ".summary-card-wallet"),
            @FindBy(css = ".wallet-card")
    })
    private List<WebElement> walletCard;

    @FindAll({
            @FindBy(css = "[data-testid='items-card']"),
            @FindBy(css = ".vendor-section"),
            @FindBy(css = ".items-card")
    })
    private List<WebElement> itemsCard;

    @FindAll({
            @FindBy(css = "[data-testid='subtotal']"),
            @FindBy(css = ".vendor-section-foot strong"),
            @FindBy(css = ".subtotal")
    })
    private WebElement subtotalEl;

    @FindAll({
            @FindBy(css = "[data-testid='total']"),
            @FindBy(css = ".total-row.total-bold span:last-child"),
            @FindBy(css = ".total")
    })
    private WebElement totalEl;

    @FindBy(xpath = "//button[@class='place-order-btn']")
    private WebElement placeOrderBtn;

    @FindBy(css = ".processing-overlay")
    private List<WebElement> processingModalEls;

    @FindBy(xpath = "//*[contains(.,'Insufficient wallet balance')]")
    private List<WebElement> insufficientErrEls;

    @FindAll({
            @FindBy(css = "[data-testid='wallet-balance']"),
            @FindBy(css = ".wallet-balance")
    })
    private WebElement walletBalanceEl;

    public CartPage(WebDriver driver) { super(driver); }

    public boolean isLoaded()         { return isDisplayed(heading); }
    public boolean summaryVisible()   { return anyDisplayed(summaryCard); }
    public boolean walletVisible()    { return anyDisplayed(walletCard); }
    public boolean itemsVisible()     { return anyDisplayed(itemsCard); }
    public boolean placeOrderVisible(){ return isDisplayed(placeOrderBtn); }

    public String subtotalText()      { return waitVisible(subtotalEl).getText().trim(); }
    public String totalText()         { return waitVisible(totalEl).getText().trim(); }
    public String walletBalanceText() { return waitVisible(walletBalanceEl).getText().trim(); }

    public void increment() {
        WebElement btn = driver.findElement(By.cssSelector(".qty-stepper .stepper-btn.stepper-plus"));
        scrollToCenter(btn);
        btn.click();
    }
    public void decrement() {
        WebElement btn = driver.findElement(By.cssSelector(".qty-stepper .stepper-btn:not(.stepper-plus)"));
        scrollToCenter(btn);
        btn.click();
    }

    /** Visible name of the first cart-item row — use as a stable anchor when
     *  asserting incr→decr round-trips so both clicks hit the same row even
     *  when Angular re-orders items after a quantity change. */
    public String firstItemName() {
        return driver.findElement(By.cssSelector(".cart-item .ci-name")).getText().trim();
    }

    public void incrementItem(String itemName) {
        WebElement btn = driver.findElement(By.xpath(
                "//div[contains(@class,'ci-name') and normalize-space()=" + xpathLiteral(itemName) + "]"
                        + "/ancestor::div[contains(@class,'cart-item')][1]"
                        + "//button[contains(@class,'stepper-plus')]"));
        scrollToCenter(btn);
        btn.click();
    }

    public void decrementItem(String itemName) {
        WebElement btn = driver.findElement(By.xpath(
                "//div[contains(@class,'ci-name') and normalize-space()=" + xpathLiteral(itemName) + "]"
                        + "/ancestor::div[contains(@class,'cart-item')][1]"
                        + "//button[contains(@class,'stepper-btn') and not(contains(@class,'stepper-plus'))]"));
        scrollToCenter(btn);
        btn.click();
    }

    /** Escape a string for safe use as an XPath string literal — handles names
     *  containing apostrophes (e.g. "Maharaja's Special") by switching quote
     *  styles or falling back to concat(). */
    private static String xpathLiteral(String s) {
        if (!s.contains("'"))  return "'"  + s + "'";
        if (!s.contains("\"")) return "\"" + s + "\"";
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = s.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(",\"'\",");
            sb.append("'").append(parts[i]).append("'");
        }
        return sb.append(")").toString();
    }

    public void placeOrder() {
        WebElement btn = waitVisible(placeOrderBtn);
        scrollToCenter(btn);
        waitClickable(btn).click();
    }

    private void scrollToCenter(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});", el);
    }

    /**
     * Place Order has three possible immediate outcomes in the Angular component:
     *   (1) .processing-overlay renders while the API call is in-flight (most common)
     *   (2) .success-screen renders directly if the API responds in &lt;1 frame
     *   (3) .order-error renders synchronously if the wallet balance check fails
     *       (cart-view.component.ts:124 returns without ever setting isProcessing).
     * All three are valid proof that the click was registered. Returns false
     * only if NONE of these appears within 15 s.
     */
    public boolean processingModalVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                    !d.findElements(By.cssSelector(".processing-overlay")).isEmpty()
                            || !d.findElements(By.cssSelector(".success-screen")).isEmpty()
                            || !d.findElements(By.cssSelector(".success-title")).isEmpty()
                            || !d.findElements(By.cssSelector(".order-error")).isEmpty());
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Remove every item from the cart by repeatedly clicking the minus (decrement)
     * button until no qty-steppers remain.  When an item's quantity reaches zero
     * Angular removes its row, so this loop empties the cart completely.
     * Safe to call on an already-empty cart — returns immediately if no steppers exist.
     */
    public void clearAllItems() {
        // Guard of 200 covers carts with up to 200 total item-units.
        for (int guard = 0; guard < 200; guard++) {
            List<WebElement> minusBtns = driver.findElements(
                    By.cssSelector(".qty-stepper .stepper-btn:not(.stepper-plus)"));
            if (minusBtns.isEmpty()) return;
            try {
                scrollToCenter(minusBtns.get(0));
                minusBtns.get(0).click();
            } catch (Exception ignored) {
                // Button went stale while Angular re-rendered the row — re-query on next iteration
            }
        }
    }

    /** True when the wallet-balance error message is currently shown on the cart page. */
    public boolean insufficientWalletErrorVisible() {
        return !driver.findElements(By.cssSelector(".order-error")).isEmpty()
                || anyDisplayed(insufficientErrEls);
    }
}
