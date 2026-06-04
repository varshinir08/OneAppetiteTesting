package com.cts.mfrp.oneappetite.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRIES = 1;
    private int count = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (count < MAX_RETRIES) {
            count++;
            return true;
        }
        return false;
    }
}
