package com.cts.mfrp.oneappetite.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExcelUtils {

    private ExcelUtils() {}

    /**
     * Reads a classpath resource (e.g. "testdata/TestData.xlsx") and sheet into
     * an Object[][] suitable for TestNG @DataProvider.
     * The header row is skipped; each subsequent row becomes one Object[] of Strings.
     * DataFormatter is used so numeric/date/boolean cells come out as clean strings
     * (e.g. "123" not "123.0", "true" not "TRUE").
     */
    public static Object[][] readAsArray(String classpathResource, String sheetName) {
        URL resource = ExcelUtils.class.getClassLoader().getResource(classpathResource);
        if (resource == null)
            throw new IllegalStateException("Classpath resource not found: " + classpathResource);
        try (FileInputStream fis = new FileInputStream(Paths.get(resource.toURI()).toFile());
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null || sheet.getLastRowNum() < 1) return new Object[0][0];
            DataFormatter fmt = new DataFormatter();
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow == null ? 0 : headerRow.getLastCellNum();
            if (colCount == 0) return new Object[0][0];
            int dataRowCount = sheet.getLastRowNum(); // rows after header
            Object[][] data = new Object[dataRowCount][colCount];
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                for (int j = 0; j < colCount; j++) {
                    Cell c = (r == null) ? null : r.getCell(j);
                    data[i - 1][j] = (c == null) ? "" : fmt.formatCellValue(c).trim();
                }
            }
            return data;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read classpath resource: " + classpathResource, e);
        } catch (Exception e) {
            throw new IllegalStateException("Error loading " + classpathResource + " / " + sheetName, e);
        }
    }

    /**
     * Reads a classpath resource sheet into a list of row maps keyed by the first-row headers.
     * Uses DataFormatter so all cell types come out as clean strings.
     */
    public static List<Map<String, String>> readAsMapList(String classpathResource, String sheetName) {
        URL resource = ExcelUtils.class.getClassLoader().getResource(classpathResource);
        if (resource == null)
            throw new IllegalStateException("Classpath resource not found: " + classpathResource);
        try (FileInputStream fis = new FileInputStream(Paths.get(resource.toURI()).toFile());
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null || sheet.getLastRowNum() < 1) return Collections.emptyList();
            DataFormatter fmt = new DataFormatter();
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return Collections.emptyList();
            int colCount = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < colCount; i++) {
                Cell c = headerRow.getCell(i);
                headers.add(c == null ? "" : fmt.formatCellValue(c).trim());
            }
            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                Map<String, String> map = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell c = r.getCell(j);
                    map.put(headers.get(j), c == null ? "" : fmt.formatCellValue(c).trim());
                }
                rows.add(map);
            }
            return rows;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read classpath resource: " + classpathResource, e);
        } catch (Exception e) {
            throw new IllegalStateException("Error loading " + classpathResource + " / " + sheetName, e);
        }
    }

    /** Reads the first data row of a classpath sheet as a named-column map. */
    public static Map<String, String> readFirstRow(String classpathResource, String sheetName) {
        List<Map<String, String>> rows = readAsMapList(classpathResource, sheetName);
        if (rows.isEmpty())
            throw new IllegalStateException("No data rows in sheet '" + sheetName + "' of " + classpathResource);
        return rows.get(0);
    }

    /** Reads a sheet into a list of row maps keyed by the first-row headers. */
    public static List<Map<String, String>> read(String path, String sheetName) {
        List<Map<String, String>> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(path);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) return rows;
            Row header = sheet.getRow(0);
            if (header == null) return rows;
            List<String> headers = new ArrayList<>();
            for (Cell c : header) headers.add(c.getStringCellValue());
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                Map<String, String> map = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell c = r.getCell(j);
                    map.put(headers.get(j), c == null ? "" : asString(c));
                }
                rows.add(map);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
        return rows;
    }

    private static String asString(Cell c) {
        return switch (c.getCellType()) {
            case STRING  -> c.getStringCellValue();
            case NUMERIC -> String.valueOf((long) c.getNumericCellValue());
            case BOOLEAN -> String.valueOf(c.getBooleanCellValue());
            case FORMULA -> c.getCellFormula();
            default      -> "";
        };
    }
}
