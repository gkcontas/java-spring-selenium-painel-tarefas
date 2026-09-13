package com.gkcontas.taskboard.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * End-to-end test driving a real browser (running in a Testcontainers
 * container) against the actual running application, covering the golden
 * path: create a task, mark it as completed, delete it.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskBoardE2ETest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final BrowserWebDriverContainer<?> BROWSER = new BrowserWebDriverContainer<>("selenium/standalone-chrome:127.0")
            .withCapabilities(new ChromeOptions());

    @LocalServerPort
    private int port;

    private RemoteWebDriver driver;

    @BeforeEach
    void setUp() {
        org.testcontainers.Testcontainers.exposeHostPorts(port);
        driver = new RemoteWebDriver(BROWSER.getSeleniumAddress(), new ChromeOptions());
        driver.get(baseUrl() + "/tasks");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldCreateCompleteAndDeleteATaskThroughTheBrowser() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement titleInput = driver.findElement(By.id("task-title-input"));
        titleInput.sendKeys("Buy milk");
        driver.findElement(By.id("add-task-button")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".task-item")));

        List<WebElement> taskItems = driver.findElements(By.cssSelector(".task-item"));
        assertThat(taskItems).hasSize(1);
        assertThat(taskItems.get(0).findElement(By.className("task-title")).getText()).isEqualTo("Buy milk");
        assertThat(taskItems.get(0).getDomAttribute("class")).doesNotContain("completed");

        taskItems.get(0).findElement(By.className("complete-button")).click();

        wait.until(driver1 -> driver1.findElement(By.cssSelector(".task-item")).getDomAttribute("class").contains("completed"));
        WebElement completedItem = driver.findElement(By.cssSelector(".task-item"));
        assertThat(completedItem.getDomAttribute("class")).contains("completed");
        assertThat(driver.findElements(By.className("complete-button"))).isEmpty();

        completedItem.findElement(By.className("delete-button")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("empty-state")));
        assertThat(driver.findElements(By.cssSelector(".task-item"))).isEmpty();
    }

    private String baseUrl() {
        return "http://host.testcontainers.internal:" + port;
    }
}
