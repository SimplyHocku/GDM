package org.example;

import com.codeborne.selenide.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;

public class JiraPage {


    private final SelenideElement loginInput = $x("//input[@id = 'login-form-username']");
    private final SelenideElement passwordInput = $x("//input[@id = 'login-form-password']");
    private final SelenideElement btnSubmitLogin = $x("//button[@id = 'login-form-submit']");

    private String project = "";
    private String fixVersion = "";
    private String login = "";
    private String password = "";
    private String status = "";
    String urlSearch;

    private WebDriver driver;
    static String firstTab, secondTab;

    {
        try {
            this.readData();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String loginUrl = "https://jira.mos.social/login.jsp?os_destination=%2Fsecure%2FMyJiraHome.jspa";
        Selenide.open(loginUrl);
        this.driver = WebDriverRunner.getWebDriver();

        $(".aui-page-panel").shouldBe(Condition.visible);


        this.loginInput.setValue(login);
        this.passwordInput.setValue(password);
        this.btnSubmitLogin.click();

        firstTab = driver.getWindowHandle();
        ((JavascriptExecutor) driver).executeScript("window.open()");
        Set<String> windowHandles = driver.getWindowHandles();
        secondTab = windowHandles.stream()
                .filter(handle -> !handle.equals(firstTab))
                .findFirst()
                .orElse(null);

        if (secondTab != null) {
            driver.switchTo().window(secondTab);
            Selenide.open("https://jira.mos.social/issues");
        }
        driver.switchTo().window(firstTab);

    }

    private void readData() throws FileNotFoundException {
        String filePath;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            filePath = System.getProperty("user.home") + File.separator + "Desktop";
            if (!new File(filePath).exists()) {
                filePath = System.getProperty("user.home") + File.separator + "Рабочий стол";
            }
            filePath = filePath + File.separator + "PFLPS.txt";
        } else {
            filePath = System.getProperty("user.home") + File.separator + "PFLPS.txt";
        }
        System.out.println(filePath);
        if (!new File(filePath).exists()){
            throw new FileNotFoundException("Укажите файл с данными для выборки!");
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            for (int row = 0; row < lines.size(); row++) {
                if (row == 0) {
                    project = lines.get(row);
                } else if (row == 1) {
                    fixVersion = lines.get(row);
                } else if (row == 2) {
                    login = lines.get(row);
                } else if (row == 3) {
                    password = lines.get(row);
                } else if (row == 4) {
                    status = lines.get(row);
                    if (lines.get(row).split(",").length > 1) {
                        String[] statuses = lines.get(row).split(",");
                        status = String.join("%2C%20", statuses);
                    }
                    urlSearch = "https://jira.mos.social/issues/?jql=project%%20%%3D%%20%s%%20AND%%20fixVersion%%20%%3D%%20%s%%20and%%20status%%20in%%20(%s)%%20%%20%%20ORDER%%20BY%%20priority%%20DESC%%2C%%20key%%20ASC".formatted(project, fixVersion, status);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JiraPage start() {
        Selenide.open(urlSearch);
        return this;
    }

    ElementsCollection getRowContent() {
        return $$x("//tr[contains(@id, 'issue')]");
    }

    ElementsCollection getTestCases(String taskName) {
        this.driver.switchTo().window(secondTab);
        Selenide.open("https://jira.mos.social/issues/?jql=issueFunction%%20in%%20linkedIssuesOf(%%22key%%20%%3D%%20%s%%22%%2C%%20%%22is%%20tested%%20by%%22)".formatted(taskName));
        ElementsCollection testCases = $$x("//tr[contains(@id, 'issue')]");
        if (!testCases.isEmpty()) {
            return testCases;
        }
        Selenide.open("https://jira.mos.social/issues/?jql=issueFunction%%20in%%20linkedIssuesOf(%%22key%%20%%3D%%20%s%%22%%2C%%20%%22tested%%20by%%22)".formatted(taskName));
        if (!testCases.isEmpty()) {
            return testCases;
        }
        return testCases;
    }

    public WebDriver getDriver() {
        return this.driver;
    }

    public String getTab(int num) {
        if (num == 0) {
            return firstTab;
        } else if (num == 1) {
            return secondTab;
        }
        return "";
    }
}
