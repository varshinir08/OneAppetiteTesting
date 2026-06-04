package com.cts.mfrp.oneappetite.pages.vendor;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class VendorDashboardPage extends BasePage {

    @FindBy(id= "list-PLACED")
    private WebElement placedColumn;

    @FindBy(id= "list-PREPARING")
    private WebElement preparingColumn;

    @FindBy(id= "list-READY")
    private WebElement readyColumn;

    @FindBy(css = "article.order-card")
    private List<WebElement> allOrderCards;

    @FindBy(xpath = "//a[@routerlink='/vendor/menu']")
    private WebElement menuTab;

    @FindBy(xpath = "//a[@routerlink='/vendor/settings']")
    private WebElement settingsTab;

    // Per-card status-advance buttons. Each only renders on cards in the
    // matching column (Placed → Preparing → Ready → Picked Up).
    @FindBy(xpath = "//button[text()=' Move to Preparing ' ]")
    private List<WebElement> moveToPreparingBtns;

    @FindBy(xpath = "//button[normalize-space()='Move to Ready']")
    private List<WebElement> moveToReadyBtns;

    @FindBy(xpath = "//button[normalize-space()='Mark as Picked Up']")
    private List<WebElement> markAsPickedUpBtns;

    public VendorDashboardPage(WebDriver driver) { super(driver); }

    public void goToMenu()     { click(menuTab); }
    public void goToSettings() { click(settingsTab); }

    public boolean placedColumnVisible()    { return isDisplayed(placedColumn); }
    public boolean preparingColumnVisible() { return isDisplayed(preparingColumn); }
    public boolean readyColumnVisible()     { return isDisplayed(readyColumn); }

    public List<WebElement> ordersIn(WebElement column) {
        WebElement col = waitVisible(column);
        return col.findElements(By.cssSelector("article.order-card"));
    }

    public List<WebElement> placedOrders()    { return ordersIn(placedColumn); }
    public List<WebElement> preparingOrders() { return ordersIn(preparingColumn); }
    public List<WebElement> readyOrders()     { return ordersIn(readyColumn); }

    // Note: Angular CDK drag-drop ignores synthetic JS events (isTrusted=false),
    // so the tests calling this are @Test(enabled=false). Kept for manual exploration.
    public void dragOrder(WebElement fromColumn, WebElement toColumn) {
        List<WebElement> orders = ordersIn(fromColumn);
        if (orders.isEmpty()) return;
        WebElement source = orders.get(0);
        WebElement target = waitVisible(toColumn);

        JavascriptExecutor js = (JavascriptExecutor) driver;

        String setupScript =
                "const src = arguments[0];" +
                        "const handle = src.querySelector('[cdkDragHandle], .cdk-drag-handle, .drag-handle, .order-handle') || src;" +
                        "const hr = handle.getBoundingClientRect();" +
                        "window.__dragX = hr.left + hr.width/2;" +
                        "window.__dragY = hr.top  + hr.height/2;" +
                        "window.__fire = (el, type, x, y, btn) => {" +
                        "  el.dispatchEvent(new PointerEvent(type, {" +
                        "    bubbles:true, cancelable:true, composed:true," +
                        "    clientX:x, clientY:y, screenX:x, screenY:y," +
                        "    button:0, buttons:btn, pointerId:1, pointerType:'mouse', isPrimary:true" +
                        "  }));" +
                        "  el.dispatchEvent(new MouseEvent(type.replace('pointer','mouse'), {" +
                        "    bubbles:true, cancelable:true, composed:true, view:window," +
                        "    clientX:x, clientY:y, screenX:x, screenY:y, button:0, buttons:btn" +
                        "  }));" +
                        "};" +
                        "window.__fire(handle, 'pointerdown', window.__dragX, window.__dragY, 1);";
        js.executeScript(setupScript, source);

        String moveScript =
                "const tgt = arguments[0];" +
                        "const tr = tgt.getBoundingClientRect();" +
                        "const tx = tr.left + tr.width/2, ty = tr.top + tr.height/2;" +
                        "const f = arguments[1];" +
                        "const x = window.__dragX + (tx - window.__dragX) * f;" +
                        "const y = window.__dragY + (ty - window.__dragY) * f;" +
                        "window.__fire(document, 'pointermove', x, y, 1);";

        int steps = 10;
        for (int i = 1; i <= steps; i++) {
            js.executeScript(moveScript, target, (double) i / steps);
        }

        js.executeScript(
                "const tgt = arguments[0];" +
                        "const tr = tgt.getBoundingClientRect();" +
                        "const tx = tr.left + tr.width/2, ty = tr.top + tr.height/2;" +
                        "window.__fire(document, 'pointerup', tx, ty, 0);",
                target);
    }

    public void dragFirstPlacedToPreparing() { dragOrder(placedColumn, preparingColumn); }
    public void dragFirstPreparingToPlaced() { dragOrder(preparingColumn, placedColumn); }
    public void dragFirstPreparingToReady()  { dragOrder(preparingColumn, readyColumn); }

    public boolean moveToPreparingButtonVisible() { return !moveToPreparingBtns.isEmpty() && moveToPreparingBtns.get(0).isDisplayed(); }
    public boolean moveToReadyButtonVisible()     { return !moveToReadyBtns.isEmpty()     && moveToReadyBtns.get(0).isDisplayed(); }
    public boolean markAsPickedUpButtonVisible()  { return !markAsPickedUpBtns.isEmpty()  && markAsPickedUpBtns.get(0).isDisplayed(); }

    public void clickMoveToPreparing() { click(moveToPreparingBtns.get(0)); }
    public void clickMoveToReady()     { click(moveToReadyBtns.get(0)); }
    public void clickMarkAsPickedUp()  { click(markAsPickedUpBtns.get(0)); }
}
