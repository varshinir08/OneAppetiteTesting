package com.cts.mfrp.oneappetite.pages.vendor;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class LocationSelectionPage extends BasePage {

    public LocationSelectionPage(WebDriver driver) { super(driver); }

    // -----------------------------------------------------------------------
    // Confirmed from browser: plain <select> elements, no id/name/data-testid
    // Identified by placeholder option text
    // -----------------------------------------------------------------------
    private WebElement city()     { return driver.findElement(By.xpath("//select[option[.='Select city']]")); }
    private WebElement campus()   { return driver.findElement(By.xpath("//select[option[.='Select campus']]")); }
    private WebElement building() { return driver.findElement(By.xpath("//select[option[.='Select building']]")); }
    private WebElement findBtn()  { return driver.findElement(By.xpath("//button[contains(.,'Find Vendors')]")); }

    public boolean isLoaded() {
        try { return city().isDisplayed(); } catch (Exception e) { return false; }
    }

    public boolean cityVisible()     { try { return city().isDisplayed(); }     catch (Exception e) { return false; } }
    public boolean campusVisible()   { try { return campus().isDisplayed(); }   catch (Exception e) { return false; } }
    public boolean buildingVisible() { try { return building().isDisplayed(); } catch (Exception e) { return false; } }

    // Disabled = only has placeholder option (no real options loaded)
    public boolean campusDisabled() {
        try { return campus().findElements(By.tagName("option")).size() <= 1; }
        catch (Exception e) { return true; }
    }

    public boolean buildingDisabled() {
        try { return building().findElements(By.tagName("option")).size() <= 1; }
        catch (Exception e) { return true; }
    }

    public boolean findVendorsActive() {
        try {
            WebElement btn = findBtn();
            return btn.isEnabled() && btn.getAttribute("disabled") == null;
        } catch (Exception e) { return false; }
    }

    private void selectOption(WebElement select, String text) {
        try {
            new Select(select).selectByVisibleText(text.trim());
        } catch (Exception e) {
            // Fallback: click matching option directly
            select.findElements(By.tagName("option")).stream()
                    .filter(o -> o.getText().trim().equalsIgnoreCase(text.trim()))
                    .findFirst()
                    .ifPresent(WebElement::click);
        }
    }

    public void selectCity(String v)     { selectOption(city(), v); }
    public void selectCampus(String v)   { selectOption(campus(), v); }
    public void selectBuilding(String v) { selectOption(building(), v); }
    public void clickFindVendors()       { try { findBtn().click(); } catch (Exception e) { System.out.println("[findVendors] " + e.getMessage()); } }
}
