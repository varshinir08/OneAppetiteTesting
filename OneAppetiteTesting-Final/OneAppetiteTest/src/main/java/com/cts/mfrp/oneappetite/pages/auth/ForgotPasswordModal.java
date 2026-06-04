package com.cts.mfrp.oneappetite.pages.auth;

import com.cts.mfrp.oneappetite.pages.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ForgotPasswordModal extends BasePage {

    @FindAll({
            @FindBy(css = "[role='dialog']"),
            @FindBy(css = ".modal"),
            @FindBy(css = "[class*='modal']")
    })
    private List<WebElement> modalEls;

    @FindAll({
            @FindBy(css = "input[type='tel']"),
            @FindBy(css = "input[name='phone']"),
            @FindBy(css = "input[placeholder*='phone' i]")
    })
    private WebElement phoneInput;

    @FindBy(xpath = "//button[normalize-space()='Send OTP']")
    private WebElement sendOtpButton;

    @FindBy(xpath = "//*[contains(.,'10 digits') or contains(.,'phone number must be')]")
    private List<WebElement> phoneErrors;

    @FindAll({
            @FindBy(css = "input[name='otp']"),
            @FindBy(css = "input[placeholder*='OTP' i]"),
            @FindBy(css = "input[maxlength='6']")
    })
    private WebElement otpInput;

    @FindBy(xpath = "//button[normalize-space()='Verify']")
    private WebElement verifyButton;

    @FindBy(xpath = "//*[contains(.,'Wrong number')]")
    private WebElement wrongNumberLink;

    @FindBy(xpath = "//*[contains(.,'Resend OTP')]")
    private WebElement resendOtpLink;

    @FindBy(xpath = "//*[contains(.,'Invalid or expired OTP')]")
    private List<WebElement> otpErrors;

    @FindAll({
            @FindBy(css = "input[name='newPassword']"),
            @FindBy(css = "input[placeholder*='New' i][type='password']")
    })
    private WebElement newPasswordInput;

    @FindAll({
            @FindBy(css = "input[name='confirmPassword']"),
            @FindBy(css = "input[placeholder*='Confirm' i][type='password']")
    })
    private WebElement confirmPasswordInput;

    @FindBy(xpath = "//button[normalize-space()='Reset' or normalize-space()='Reset Password']")
    private WebElement resetButton;

    public ForgotPasswordModal(WebDriver driver) { super(driver); }

    public boolean isModalOpen()       { return anyDisplayed(modalEls); }
    public boolean isStep1Visible()    { return isDisplayed(phoneInput) && isDisplayed(sendOtpButton); }
    public boolean isStep2Visible()    { return isDisplayed(otpInput) && isDisplayed(verifyButton); }
    public boolean isStep3Visible()    { return isDisplayed(newPasswordInput) && isDisplayed(confirmPasswordInput); }
    public boolean phoneErrorVisible() { return anyDisplayed(phoneErrors); }
    public boolean otpErrorVisible()   { return anyDisplayed(otpErrors); }

    public void enterPhone(String phone)        { type(phoneInput, phone); }
    public void clickSendOtp()                  { click(sendOtpButton); }
    public void enterOtp(String otp)            { type(otpInput, otp); }
    public void clickVerify()                   { click(verifyButton); }
    public void clickWrongNumber()              { click(wrongNumberLink); }
    public void clickResendOtp()                { click(resendOtpLink); }
    public void enterNewPassword(String pw)     { type(newPasswordInput, pw); }
    public void enterConfirmPassword(String pw) { type(confirmPasswordInput, pw); }
    public void clickReset()                    { click(resetButton); }
}
