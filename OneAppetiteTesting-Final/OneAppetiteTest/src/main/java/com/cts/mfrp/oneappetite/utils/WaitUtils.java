package com.cts.mfrp.oneappetite.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public final class WaitUtils {

    private WaitUtils() {}

    public static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInt("explicit.wait.seconds", 20)));
    }

    public static WebDriverWait wait(WebDriver driver, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    public static WebElement visible(WebDriver driver, By by) {
        return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static WebElement clickable(WebDriver driver, By by) {
        return wait(driver).until(ExpectedConditions.elementToBeClickable(by));
    }

    public static boolean urlContains(WebDriver driver, String fragment) {
        try {
            return wait(driver).until(ExpectedConditions.urlContains(fragment));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static boolean textVisible(WebDriver driver, String text) {
        try {
            return wait(driver).until(d -> d.getPageSource() != null
                    && d.getPageSource().toLowerCase().contains(text.toLowerCase()));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static boolean isPresent(WebDriver driver, By by) {
        try {
            List<WebElement> els = driver.findElements(by);
            return !els.isEmpty() && els.get(0).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static <T> T poll(WebDriver driver, Function<WebDriver, T> condition) {
        return wait(driver).until(condition);
    }
}
