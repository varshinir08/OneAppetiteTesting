import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class UpdateTestData {

    public static void main(String[] args) throws Exception {
        String path = args[0];
        String outPath = args.length > 1 ? args[1] : path;
        Workbook wb;
        try (FileInputStream fis = new FileInputStream(path)) {
            wb = new XSSFWorkbook(fis);
        }

        addSheet(wb, "CreateAccountNegative",
            new String[]{"fullName", "validPassword", "cognizantEmail"},
            new String[]{"Test User", "ValidPass@1", "testuser@cognizant.com"});

        addSheet(wb, "ForgotPasswordOtp",
            new String[]{"invalidOtp"},
            new String[]{"000000"});

        addSheet(wb, "VendorE2ECredentials",
            new String[]{"email", "password"},
            new String[]{"priyaa.sharma@cognizant.com", "Vendor@123"});

        addSheet(wb, "VendorProfileEdit",
            new String[]{"profileName"},
            new String[]{"Vendor Updated"});

        addSheet(wb, "VendorAddLocation",
            new String[]{"city", "campus", "building"},
            new String[]{"Chennai", "MEPZ SEZ", "SDB-2"});

        addSheet(wb, "MenuItemAdd",
            new String[]{"itemBaseName", "category", "course", "dietary", "price", "stock", "minOrder", "imageUrl"},
            new String[]{"Grilled Paneer", "Starters", "Lunch", "Vegetarian", "150", "50", "10", "https://example.com/paneer.jpg"});

        addSheet(wb, "MenuItemNegative",
            new String[]{"name", "category", "course", "dietary", "price", "stock", "minOrder", "imageUrl"},
            new String[]{"Invalid Item", "Starters", "Lunch", "Vegetarian", "-50", "10", "5", "https://example.com/img.jpg"});

        addSheet(wb, "AdminProfileEdit",
            new String[]{"profileName", "wrongCurrentPassword", "tempPassword"},
            new String[]{"Admin Updated", "WrongPass@1", "NewAdmin@1"});

        addSheet(wb, "EmployeeSettingsData",
            new String[]{"shortCurrentPwd", "shortNewPwd", "validCurrentPwd", "validNewPwd",
                         "mismatchConfirmPwd", "wrongCurrentPwd", "wrongNewPwd",
                         "nonNumericPhone", "invalidAmount", "invalidUpi", "activeChipLabel"},
            new String[]{"AnyValueHere@1", "abc", "AnyValueHere@1", "LongEnough@1",
                         "Different@2", "DefinitelyWrong@9999", "DummyNew@1234",
                         "abcdefghij", "0", "notanupi", "₹500"});

        addSheet(wb, "TopBarData",
            new String[]{"building", "searchTerm"},
            new String[]{"Academic Block", "pizza"});

        addSheet(wb, "DashboardSearch",
            new String[]{"vendorSearchTerm", "gibberishVendorSearch1", "gibberishVendorSearch2"},
            new String[]{"a", "zzzxxx_no_vendor_match_99999", "zzzxxx_no_vendor_match"});

        try (FileOutputStream fos = new FileOutputStream(outPath)) {
            wb.write(fos);
        }
        wb.close();
        System.out.println("TestData.xlsx updated successfully. Written to: " + outPath);
    }

    private static void addSheet(Workbook wb, String sheetName, String[] headers, String[] row) {
        Sheet existing = wb.getSheet(sheetName);
        if (existing != null) {
            wb.removeSheetAt(wb.getSheetIndex(existing));
        }
        Sheet sheet = wb.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        Row dataRow = sheet.createRow(1);
        for (int i = 0; i < row.length; i++) {
            dataRow.createCell(i).setCellValue(row[i]);
        }
    }
}
