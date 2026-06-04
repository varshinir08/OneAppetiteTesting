package com.cts.mfrp.oneappetite.pages.vendor;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class VendorMenuMgmtPage extends BasePage {

    @FindBy(xpath = "//button[normalize-space()='Add Menu Item']")
    private WebElement addItemBtn;

    @FindBy(xpath = "//div[@role='dialog']")
    private List<WebElement> modalEls;

    @FindBy(xpath = "//input[@name='itemName']")
    private WebElement itemName;

    @FindBy(xpath = "//input[@name='category']")
    private WebElement category;

    @FindBy(xpath = "//select[@name='mealCourse']")
    private WebElement mealCourse;

    @FindBy(xpath = "//select[@name='dietaryType']")
    private WebElement dietaryType;

    @FindBy(xpath = "//input[@name='price']")
    private WebElement price;

    @FindBy(xpath = "//input[@name='quantityAvailable']")
    private WebElement quantity;

    @FindBy(xpath = "//input[@name='minPrepTime']")
    private WebElement prepTime;

    @FindBy(xpath = "//input[@name='imageUrl']")
    private WebElement imageUrl;

    @FindBy(xpath = "//button[normalize-space()='Add Item']")
    private WebElement saveBtn;

    @FindBy(xpath = "//button[@title='Edit']")
    private WebElement editIcon;

    @FindBy(xpath = "//button[@title='Delete']")
    private WebElement deleteIcon;

    @FindBy(xpath = "(//span[@class='knob'])[1]")
    private WebElement stockToggle;

    @FindBy(xpath = "//button[normalize-space()='Yes, remove']")
    private WebElement confirmBtn;

    public VendorMenuMgmtPage(WebDriver driver) { super(driver); }

    public boolean isLoaded() {
        try {
            waitVisible(addItemBtn);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void clickAddItem() { click(addItemBtn); }
    public boolean modalOpen() { return anyDisplayed(modalEls); }

    public void fill(String name, String cat, String course, String diet,
                     String pr, String qty, String prep, String img) {
        if (name != null)   type(itemName, name);
        if (cat != null)    type(category, cat);
        if (course != null) new Select(waitVisible(mealCourse)).selectByVisibleText(course);
        if (diet != null)   new Select(waitVisible(dietaryType)).selectByVisibleText(diet);
        if (pr != null)   type(price, pr);
        if (qty != null)  type(quantity, qty);
        if (prep != null) type(prepTime, prep);
        if (img != null)  type(imageUrl, img);
    }

    public void clickSave()       { click(saveBtn); }
    public void clickFirstEdit()  { click(editIcon); }
    public void clickFirstDelete(){ click(deleteIcon); }
    public void confirmDelete()   { click(confirmBtn); }

    public boolean confirmDialogVisible() {
        try { waitVisible(confirmBtn); return true; }
        catch (Exception e) { return false; }
    }
    public boolean saveDialogVisible() {
        try { waitVisible(saveBtn); return true; }
        catch (Exception e) { return false; }
    }

    public boolean isSaveDisabled() {
        try {
            WebElement el = waitVisible(saveBtn);
            if (!el.isEnabled()) return true;
            String disabled = el.getAttribute("disabled");
            String aria     = el.getAttribute("aria-disabled");
            return disabled != null
                    || "true".equalsIgnoreCase(aria);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean priceFieldInvalid() {
        try {
            WebElement el = waitVisible(price);
            String aria = el.getAttribute("aria-invalid");
            String cls  = el.getAttribute("class");
            return "true".equalsIgnoreCase(aria)
                    || (cls != null && (cls.contains("ng-invalid") || cls.contains("invalid")));
        } catch (Exception e) {
            return false;
        }
    }

    public void toggleFirstStock() { click(stockToggle); }
    public WebElement firstStockSwitch() { return waitVisible(stockToggle); }
}
