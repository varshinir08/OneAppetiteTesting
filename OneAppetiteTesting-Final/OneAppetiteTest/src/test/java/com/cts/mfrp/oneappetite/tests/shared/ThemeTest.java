package com.cts.mfrp.oneappetite.tests.shared;

import com.cts.mfrp.oneappetite.base.BaseTest;
import com.cts.mfrp.oneappetite.constants.AppConstants;
import com.cts.mfrp.oneappetite.pages.shared.TopBarComponent;
import com.cts.mfrp.oneappetite.tests.support.LoginHelper;
import com.cts.mfrp.oneappetite.utils.WaitUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ThemeTest extends BaseTest {

    @Test(groups = {"smoke", "regression", "UI"},
          description = "TC 2.4.1 - Theme toggle switches modes and persists across logout/login")
    public void themeToggleAndPersistence() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        TopBarComponent top = new TopBarComponent(driver);
        Assert.assertEquals(top.currentTheme().toLowerCase(), "light",
                "App should start in Light mode");
        top.toggleTheme();
        Assert.assertTrue(WaitUtils.textVisible(driver, AppConstants.TOAST_THEME_DARK),
                "Dark mode toast not shown");
        Assert.assertEquals(top.currentTheme().toLowerCase(), "dark",
                "html should reflect dark theme");
        top.logout();
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        Assert.assertEquals(new TopBarComponent(driver).currentTheme().toLowerCase(), "dark",
                "Theme preference should persist across sessions");
    }

    @Test(groups = {"regression", "UI"},
          description = "TC 2.4.2 - Glassmorphic backgrounds render in both Light and Dark modes")
    public void glassmorphicElementsRenderBothThemes() {
        LoginHelper.loginAs(driver, AppConstants.EMPLOYEE_ROLE);
        TopBarComponent top = new TopBarComponent(driver);

        for (String themeTarget : new String[]{"dark", "light"}) {
            if (!top.currentTheme().equalsIgnoreCase(themeTarget)) top.toggleTheme();
            boolean glassPresent = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "return [...document.querySelectorAll('nav,aside,[role=dialog],.dropdown,.modal')]"
                            + ".some(el => { const s=getComputedStyle(el); "
                            + "return s.backdropFilter && s.backdropFilter !== 'none'; });");
            Assert.assertTrue(glassPresent,
                    "No element with backdrop-filter found in " + themeTarget + " mode");
        }
    }
}
