package com.cts.mfrp.oneappetite.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener implements ITestListener {

    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    private static String classNameFor(ITestResult result) {
        return result.getMethod().getRealClass().getSimpleName();
    }

    @Override
    public void onTestStart(ITestResult result) {
        // Single suite-wide report — every test from every class lands in the same HTML file.
        ExtentTest t = ExtentManager.forSuite().createTest(
                result.getMethod().getMethodName(),
                result.getMethod().getDescription());
        // Tag the test with its class name so you can still filter by class in the report UI.
        t.assignCategory(classNameFor(result));
        String[] groups = result.getMethod().getGroups();
        if (groups != null && groups.length > 0) t.assignCategory(groups);
        TEST.set(t);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        TEST.get().log(Status.PASS, "Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        TEST.get().log(Status.FAIL, result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        TEST.get().log(Status.SKIP, "Skipped: " +
                (result.getThrowable() != null ? result.getThrowable().getMessage() : ""));
    }

    @Override
    public void onFinish(ITestContext context) {
        for (ExtentReports r : ExtentManager.all()) {
            r.flush();
        }
    }

    public static ExtentTest current() {
        return TEST.get();
    }
}
