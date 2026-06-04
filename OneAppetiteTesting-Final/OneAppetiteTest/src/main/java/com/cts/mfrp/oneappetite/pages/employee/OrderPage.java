package com.cts.mfrp.oneappetite.pages.employee;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Order confirmation view rendered by CartViewComponent after a successful order.
 * Heading is &lt;h2 class="success-title"&gt; ("Order Confirmed!"), token lives in
 * &lt;span class="token-number"&gt;, and the back-link is &lt;a class="go-menu-btn"&gt;.
 */
public class OrderPage extends BasePage {

    private static final Pattern TOKEN = Pattern.compile("OA-\\d{4}");

    @FindAll({
            @FindBy(css = "h2.success-title"),
            @FindBy(xpath = "//*[contains(.,'Order Confirmed')]")
    })
    private WebElement heading;

    @FindAll({
            @FindBy(css = "span.token-number"),
            @FindBy(xpath = "//*[contains(.,'YOUR TOKEN')]/following::*[contains(text(),'OA-')][1]")
    })
    private WebElement tokenChip;

    @FindAll({
            @FindBy(css = "a.go-menu-btn"),
            @FindBy(xpath = "//button[contains(.,'Back to Home')] | //a[contains(.,'Back to Home')]")
    })
    private WebElement backHomeBtn;

    public OrderPage(WebDriver driver) { super(driver); }

    public boolean isLoaded() { return isDisplayed(heading); }

    public String tokenText() { return waitVisible(tokenChip).getText().trim(); }

    public boolean tokenMatchesPattern() {
        if (!isDisplayed(tokenChip)) return false;
        Matcher m = TOKEN.matcher(tokenText());
        return m.find();
    }

    public void clickBackToHome() { click(backHomeBtn); }
}
