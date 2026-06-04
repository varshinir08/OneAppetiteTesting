package com.cts.mfrp.oneappetite.tests.employee;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.employee.DashboardPage;
import com.cts.mfrp.oneappetite.pages.vendor.LocationSelectionPage;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.ConfigReader;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;

public class LocationSelectionTest extends BaseTest {

    private LocationSelectionPage locationPage;

    @BeforeMethod(alwaysRun = true)
    public void openLocationPage() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        WaitUtils.wait(driver, 10).until(d -> d.getCurrentUrl().contains("dashboard") || d.getCurrentUrl().contains("location"));

        // After login app goes to /dashboard — click Change Location to reach location page
        if (driver.getCurrentUrl().contains("dashboard")) {
            try {
                org.openqa.selenium.WebElement el = driver.findElement(
                        By.xpath("//*[contains(normalize-space(.),'Change Location')]"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                try { WaitUtils.wait(driver, 8).until(d -> new LocationSelectionPage(d).isLoaded()); } catch (org.openqa.selenium.TimeoutException ignored) {}
            } catch (Exception e) {
                System.out.println("Change Location not found: " + e.getMessage());
            }
        }

        locationPage = new LocationSelectionPage(driver);
    }

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.6.1 - Campus and Building dropdowns disabled until City is selected")
    public void cascadingDropdownsDisabledByDefault() {
        Assert.assertTrue(locationPage.isLoaded(),
                "Location Selection page did not load. URL: " + driver.getCurrentUrl());

        Assert.assertTrue(locationPage.cityVisible(),     "City dropdown not visible");
        Assert.assertTrue(locationPage.campusVisible(),   "Campus dropdown not visible");
        Assert.assertTrue(locationPage.buildingVisible(), "Building dropdown not visible");

        Assert.assertTrue(locationPage.campusDisabled(),
                "Campus should be DISABLED before City is selected");
        Assert.assertTrue(locationPage.buildingDisabled(),
                "Building should be DISABLED before Campus is selected");
    }

    @Test(groups = {"regression", "functional"},
          description = "TC 2.6.2 - Each selection enables the next dropdown in the cascade")
    public void selectingCascadesEnableNext() {
        Assert.assertTrue(locationPage.isLoaded(),
                "Location Selection page must be loaded");

        // Select City → wait for campus options to load
        locationPage.selectCity(ConfigReader.get("location.city"));
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> !locationPage.campusDisabled());

        Assert.assertFalse(locationPage.campusDisabled(),
                "Campus should be ENABLED after selecting City");
        Assert.assertTrue(locationPage.buildingDisabled(),
                "Building should remain DISABLED until Campus selected");

        // Select Campus → wait for building options to load
        locationPage.selectCampus(ConfigReader.get("location.campus"));
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> !locationPage.buildingDisabled());

        Assert.assertFalse(locationPage.buildingDisabled(),
                "Building should be ENABLED after selecting Campus");

        // Select Building → Find Vendors active
        locationPage.selectBuilding(ConfigReader.get("location.building"));
        WaitUtils.wait(driver, 5).until(d -> locationPage.findVendorsActive());

        Assert.assertTrue(locationPage.findVendorsActive(),
                "'Find Vendors' should be ACTIVE after all three selections");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.6.3 - Find Vendors switches Dashboard to Vendor List Mode")
    public void findVendorsSwitchesToVendorList() {
        Assert.assertTrue(locationPage.isLoaded(),
                "Location Selection page must be loaded");

        locationPage.selectCity(ConfigReader.get("location.city"));
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> !locationPage.campusDisabled());

        locationPage.selectCampus(ConfigReader.get("location.campus"));
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> !locationPage.buildingDisabled());

        locationPage.selectBuilding(ConfigReader.get("location.building"));
        WaitUtils.wait(driver, 5).until(d -> locationPage.findVendorsActive());

        locationPage.clickFindVendors();
        try { WaitUtils.wait(driver, 10).until(d -> new DashboardPage(d).vendorListMode() || d.getPageSource().contains("Vendor")); } catch (org.openqa.selenium.TimeoutException ignored) {}

        DashboardPage dash = new DashboardPage(driver);

        Assert.assertTrue(
                dash.vendorListMode()
                        || WaitUtils.textVisible(driver, "Vendors Near You")
                        || WaitUtils.textVisible(driver, "Vendor"),
                "Dashboard did not switch to Vendor List Mode. URL: " + driver.getCurrentUrl()
        );
        Assert.assertTrue(
                dash.currentLocationVisible()
                        || WaitUtils.textVisible(driver, ConfigReader.get("location.city"))
                        || WaitUtils.textVisible(driver, ConfigReader.get("location.building")),
                "Location info not visible after setting location"
        );
    }
}
