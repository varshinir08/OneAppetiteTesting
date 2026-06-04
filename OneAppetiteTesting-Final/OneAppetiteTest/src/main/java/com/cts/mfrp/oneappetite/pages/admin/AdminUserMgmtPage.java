package com.cts.mfrp.oneappetite.pages.admin;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class AdminUserMgmtPage extends BasePage {

    @FindBy(css = "h1.hero-heading")
    private WebElement heading;

    @FindBy(xpath = "//div[contains(@class,'stat-card')][.//div[contains(@class,'stat-label')][normalize-space()='Total Users']]")
    private List<WebElement> totalUsersCard;
    @FindBy(xpath = "//div[contains(@class,'stat-card')][.//div[contains(@class,'stat-label')][normalize-space()='Employees']]")
    private List<WebElement> employeesCard;
    @FindBy(xpath = "//div[contains(@class,'stat-card')][.//div[contains(@class,'stat-label')][normalize-space()='Vendors']]")
    private List<WebElement> vendorsCard;
    @FindBy(xpath = "//div[contains(@class,'stat-card')][.//div[contains(@class,'stat-label')][normalize-space()='Inactive']]")
    private List<WebElement> inactiveCard;

    @FindBy(css = ".search-box input[type='text']")
    private WebElement searchBox;

    @FindBy(xpath = "//table[contains(@class,'users-table')]//tbody//tr[td[contains(@class,'user-cell')]]")
    private List<WebElement> userRows;

    @FindBy(css = "table.users-table tbody td.empty-row")
    private List<WebElement> emptyStateEls;

    @FindBy(css = "table.users-table tbody td.th-actions input.form-check-input[type='checkbox']")
    private List<WebElement> statusToggles;

    public AdminUserMgmtPage(WebDriver driver) { super(driver); }

    public boolean isLoaded() { return isDisplayed(heading); }
    public boolean statCardsVisible() {
        return anyDisplayed(totalUsersCard) && anyDisplayed(employeesCard)
                && anyDisplayed(vendorsCard) && anyDisplayed(inactiveCard);
    }

    public void clickPill(String label) { clickByText(label); }

    public void search(String q) { type(searchBox, q); }
    public void clearSearch()     { waitVisible(searchBox).clear(); }
    public List<WebElement> rows() { return userRows; }
    public boolean emptyStateVisible() { return anyDisplayed(emptyStateEls); }

    public String firstRowEmail() {
        if (userRows.isEmpty()) return null;
        return userRows.get(0).findElement(By.cssSelector("td.email-cell"))
                .getText().trim();
    }

    public void toggleStatusForRow(int index) {
        if (index < statusToggles.size()) statusToggles.get(index).click();
    }
}
