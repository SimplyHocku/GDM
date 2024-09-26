package org.example;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CreateExcelFile {
    static String osName = System.getProperty("os.name");

    public static void main(String[] args) throws IOException {
        JiraPage page = new JiraPage();
        ElementsCollection tasks = page.start().getRowContent();
        if (tasks.isEmpty()) {
            return;
        }

        List<SelenideElement> copiedElements = tasks.stream()
                .toList();


        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("TestCases");

        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {
                "№", " ", "ТК", "Название", "Количество тестов", "Количество шагов",
                "Ревью ТК", "Приоритет", "Статус задачи", "Статус проверки", "Тестировщик"
        };

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        int rowNumber = 1;
        int taskNumber = 0;

        for (SelenideElement copiedElement : copiedElements) {
            taskNumber += 1;
            String issueType = copiedElement.$x(".//td[@class = 'issuetype']").$x(".//img").getAttribute("alt");
            String issueKey = copiedElement.$x(".//td[@class = 'issuekey']").$x(".//a").getText();
            String issueKeyLink = "https://jira.mos.social/browse/" + issueKey;
            String summary = copiedElement.$x(".//td[@class = 'summary']").$x(".//a[@class = 'issue-link']").getText();
            String priority = copiedElement.$x(".//td[@class = 'priority']").$x(".//img").getAttribute("alt");
            String status = copiedElement.$x(".//td[@class = 'status']").$x(".//span").getText();

            String[] rowData = {Integer.toString(taskNumber), issueType, issueKeyLink, summary, priority, status};
            Integer[] rowNumbers = {0, 1, 2, 3, 7, 8};
            XSSFRow row = sheet.createRow(rowNumber);
            for (int cellNumber = 0; cellNumber < 6; cellNumber++) {
                XSSFCell cell = row.createCell(rowNumbers[cellNumber]);
                cell.setCellValue(rowData[cellNumber]);
            }

            ElementsCollection taskCases = page.getTestCases(issueKey);


            if (!taskCases.isEmpty()) {
                for (int tk = 0; tk < taskCases.size(); tk++) {
                    rowNumber += 1;
                    row = sheet.createRow(rowNumber);
                    issueType = taskCases.get(tk).$x(".//td[@class = 'issuetype']").$x(".//img").getAttribute("alt");
                    issueKey = "https://jira.mos.social/browse/" + taskCases.get(tk).$x(".//td[@class = 'issuekey']").$x(".//a").getText();
                    summary = taskCases.get(tk).$x(".//td[@class = 'summary']").$x(".//a[@class = 'issue-link']").getText();
                    priority = taskCases.get(tk).$x(".//td[@class = 'priority']").$x(".//img").getAttribute("alt");
                    status = taskCases.get(tk).$x(".//td[@class = 'status']").$x(".//span").getText();

                    rowData = new String[]{issueType, issueKey, summary, status, priority};
                    rowNumbers = new Integer[]{1, 2, 3, 6, 7};

                    for (int cellNumber = 0; cellNumber < 5; cellNumber++) {
                        XSSFCell cell = row.createCell(rowNumbers[cellNumber]);
                        cell.setCellValue(rowData[cellNumber]);
                    }
                }
            }
            page.getDriver().switchTo().window(page.getTab(0));
            rowNumber += 1;
        }
        File path = null;
        if (osName.equalsIgnoreCase("linux")) {
            path = new File("/home/" + System.getProperty("user.name") + File.separator + "гдщка.xlsx");
        } else if (osName.equalsIgnoreCase("win")) {
            path = new File(System.getProperty("user.home") + File.separator + "Desktop");
            if (!path.exists()) {
                path = new File(System.getProperty("user.home") + File.separator + "Рабочий стол");
            }
            path = new File(path + File.separator + "гдшка.xlsx");
        }


        assert path != null;
        if (!path.exists()) {
            path.createNewFile();
        }
        try (FileOutputStream fileOut = new FileOutputStream(path)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
