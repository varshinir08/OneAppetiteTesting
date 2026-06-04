package com.cts.mfrp.oneappetite.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ExtentManager {

    private static final String STAMP =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    private static final ConcurrentMap<String, ExtentReports> REPORTS = new ConcurrentHashMap<>();
    private static final String REPORT_DIR = "test-output/extent";
    private static final String SUITE_KEY = "__suite__";

    private ExtentManager() {}

    /**
     * Single suite-wide ExtentReports instance — one consolidated HTML report for the whole
     * run. The ExtentReportListener calls this on every test event, so all tests across all
     * classes end up in the same report. Each test is tagged with its class name as a
     * category so you can still filter by class inside the report.
     */
    public static ExtentReports forSuite() {
        return REPORTS.computeIfAbsent(SUITE_KEY, k -> buildSuiteReporter());
    }

    /** Kept for back-compat — now an alias for forSuite() so legacy callers still work. */
    public static ExtentReports forPage(String pageName) {
        return forSuite();
    }

    /** All reports created so far — call .flush() on each at end of suite. */
    public static Collection<ExtentReports> all() {
        return REPORTS.values();
    }

    private static synchronized ExtentReports buildSuiteReporter() {
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) dir.mkdirs();
        String path = dir.getAbsolutePath() + "/OneAppetite-Suite-" + STAMP + ".html";
        ExtentSparkReporter spark = new ExtentSparkReporter(path);
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("OneAppetite - Full Test Report");
        spark.config().setReportName("OneAppetite Test Suite");
        ExtentReports r = new ExtentReports();
        r.attachReporter(spark);
        r.setSystemInfo("Suite", "OneAppetite Automation");
        r.setSystemInfo("Base URL", ConfigReader.get("base.url"));
        r.setSystemInfo("Browser", ConfigReader.get("browser", "chrome"));
        r.setSystemInfo("Headless", String.valueOf(ConfigReader.getBoolean("headless", false)));
        return r;
    }
}
