package com.cts.mfrp.oneappetite.tests.shared;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.shared.TopBarComponent;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NotificationsTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "functional"},
          description = "TC 2.5.1 - Bell icon shows pulsing unread badge capped at 9+ and dropdown of 5")
    public void bellOpensDropdownWithFiveItems() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        TopBarComponent top = new TopBarComponent(driver);
        Assert.assertTrue(top.bellVisible(), "Bell icon not visible");
        String badge = top.unreadBadgeText();
        if (!badge.isBlank()) {
            try {
                int count = Integer.parseInt(badge.replace("+", ""));
                Assert.assertTrue(badge.equals("9+") || count <= 9,
                        "Badge should cap at 9+ when more than 9 unread");
            } catch (NumberFormatException ignored) { /* text-only badge ok */ }
        }
        top.openNotificationPanel();
        Assert.assertTrue(top.notificationPanelOpen(), "Notification panel did not open");
        Assert.assertTrue(top.notifications().size() <= 5,
                "Panel should show at most 5 most-recent notifications");
    }


    @Test(groups = {"regression", "UI"},
          description = "TC 2.5.2 - Mark all as read clears badge; panel closes on outside click and ESC")
    public void markAllReadAndPanelClose() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        TopBarComponent top = new TopBarComponent(driver);
        top.openNotificationPanel();
        top.markAllRead();
        Assert.assertTrue(top.unreadBadgeText().isBlank() || "0".equals(top.unreadBadgeText()),
                "Unread badge should be cleared");
        top.openNotificationPanel();
        new Actions(driver).moveByOffset(10, 10).click().perform();
        Assert.assertFalse(top.notificationPanelOpen(), "Panel should close on outside click");
        top.openNotificationPanel();
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        Assert.assertFalse(top.notificationPanelOpen(), "Panel should close on ESC");
    }
}
