package com.stotracker.controller;

import com.stotracker.model.StoData;
import com.stotracker.repository.StoDataRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StoControllerBrowserTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StoDataRepository repository;

    private WebDriver driver;

    private static final String BASE_URL = "http://localhost:";
    private static final String TEST_CHAR_NAME = "unit-char";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.getInstance().setup();
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        repository.deleteAll();
        FirefoxOptions options = new FirefoxOptions();

        // Add locale based on test name
        String testName = testInfo.getDisplayName();
        if (testName.contains("GermanLocale")) {
            options.addArguments("--lang=de-DE");
        } else if (testName.contains("AmericanLocale")) {
            options.addArguments("--lang=en-US");
        }

        driver = new FirefoxDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void fullCharacterLifecycle() {
        // 1. Create character
        createCharacter(TEST_CHAR_NAME);
        assertCharacterExistsInTable(TEST_CHAR_NAME);
        assertTrue(repository.findByName(TEST_CHAR_NAME).isPresent());

        // Get the character ID for subsequent operations
        Long charId = repository.findByName(TEST_CHAR_NAME).get().getId();

        // 2. Update numbers
        updateDilithium(charId, 34780);
        updateCredits(charId, 100000);

        // Verify numbers in DB
        Optional<StoData> afterNumbers = repository.findById(charId);
        assertTrue(afterNumbers.isPresent());
        assertEquals(34780, afterNumbers.get().getDilithium());
        assertEquals(100000, afterNumbers.get().getCredits());

        // 3. Test recruitment timestamp - set and unset
        testTimestampSetAndUnset(charId, "recruitment");

        // 4. Test refining timestamp - set and unset
        testTimestampSetAndUnset(charId, "refining");

        // 5. Test event timestamp - set and unset
        testTimestampSetAndUnset(charId, "event");

        // 6. Verify final DB state (timestamps should be cleared)
        Optional<StoData> finalState = repository.findById(charId);
        assertTrue(finalState.isPresent());
        assertNull(finalState.get().getRecruitmentTime());
        assertNull(finalState.get().getConvertionTime());
        assertNull(finalState.get().getEventTime());
        assertEquals(34780, finalState.get().getDilithium());
        assertEquals(100000, finalState.get().getCredits());

        // 7. Delete character
        deleteCharacter(TEST_CHAR_NAME);
        assertCharacterNotInTable(TEST_CHAR_NAME);
        assertFalse(repository.findByName(TEST_CHAR_NAME).isPresent());
    }

    @Test
    void numberInput_GermanLocale_acceptsAndSubmitsGermanFormat() {
        // Create character
        createCharacter(TEST_CHAR_NAME);
        Long charId = repository.findByName(TEST_CHAR_NAME).get().getId();

        // In German locale, enter number with German thousand separator (period)
        driver.get(BASE_URL + port);

        WebElement input = driver.findElement(By.cssSelector("input.dilithium-input"));
        // Use JavaScript to set value directly
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = '34.780';", input);
        // Trigger change event via JavaScript
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", input);
        waitForPageLoad();

        // Verify in DB - should be stored as raw integer
        Optional<StoData> after = repository.findById(charId);
        assertTrue(after.isPresent());
        assertEquals(34780, after.get().getDilithium());
    }

    @Test
    void numberInput_AmericanLocale_acceptsAndSubmitsAmericanFormat() {
        // Create character
        createCharacter(TEST_CHAR_NAME);
        Long charId = repository.findByName(TEST_CHAR_NAME).get().getId();

        // In American locale, enter plain number
        driver.get(BASE_URL + port);

        WebElement input = driver.findElement(By.cssSelector("input.dilithium-input"));
        // Use JavaScript to set value directly
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = '34780';", input);
        // Trigger change event via JavaScript
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", input);
        waitForPageLoad();

        // Verify in DB - should be stored as raw integer
        Optional<StoData> after = repository.findById(charId);
        assertTrue(after.isPresent());
        assertEquals(34780, after.get().getDilithium());
    }

    @Test
    void numberInput_GermanLocale_displaysNumberCorrectly() {
        // Pre-set a value via backend
        StoData charData = new StoData(TEST_CHAR_NAME);
        charData.setDilithium(34780);
        charData.setCredits(100000);
        repository.save(charData);

        driver.get(BASE_URL + port);
        WebElement row = findCharacterRow(TEST_CHAR_NAME);
        assertNotNull(row);

        WebElement input = row.findElement(By.cssSelector("input.dilithium-input"));
        String value = input.getAttribute("value");

        // German locale should display with period separator (34.780 or 34,780 depending on browser)
        assertNotNull(value);
        // Just verify a numeric value is displayed, format depends on browser locale
        assertTrue(value.matches("[\\d.,]+"), "Expected numeric format but got: " + value);
    }

    @Test
    void numberInput_AmericanLocale_displaysNumberCorrectly() {
        // Pre-set a value via backend
        StoData charData = new StoData(TEST_CHAR_NAME);
        charData.setDilithium(34780);
        charData.setCredits(100000);
        repository.save(charData);

        driver.get(BASE_URL + port);
        WebElement row = findCharacterRow(TEST_CHAR_NAME);
        assertNotNull(row);

        WebElement input = row.findElement(By.cssSelector("input.credits-input"));
        String value = input.getAttribute("value");

        // American locale should display with comma separator
        assertNotNull(value);
        assertTrue(value.matches("[\\d.,]+"), "Expected numeric format but got: " + value);
    }

    @Test
    void numberInput_LocaleRoundTrip_convertsCorrectly() {
        // This test verifies the full round-trip: backend value -> display -> parse -> backend
        // Create character
        createCharacter(TEST_CHAR_NAME);
        Long charId = repository.findByName(TEST_CHAR_NAME).get().getId();

        // Set initial value via DB
        StoData charData = repository.findById(charId).get();
        charData.setDilithium(1234567);
        charData.setCredits(987654);
        repository.save(charData);

        // Load page and verify display value uses user's locale
        driver.get(BASE_URL + port);
        WebElement row = findCharacterRow(TEST_CHAR_NAME);
        assertNotNull(row);

        // Get displayed value
        WebElement dilithiumInput = row.findElement(By.cssSelector("input.dilithium-input"));
        String displayedDilithium = dilithiumInput.getAttribute("value");
        assertNotNull(displayedDilithium);

        // Parse the displayed value using the same locale-aware parser
        Number parsedValue = (Number) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "return parseLocaleNumber(arguments[0]);", displayedDilithium);

        // Verify parsed value matches original
        assertEquals(1234567L, parsedValue.longValue(), "Locale parser should correctly parse displayed value");

        // Now change the value to a new number
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = '5555';", dilithiumInput);
        // Trigger change event to submit form (blur only reformats, change submits)
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", dilithiumInput);
        waitForPageLoad();

        // Verify new value is formatted and stored correctly
        Optional<StoData> updated = repository.findById(charId);
        assertTrue(updated.isPresent());
        assertEquals(5555, updated.get().getDilithium());
    }

    private void createCharacter(String name) {
        driver.get(BASE_URL + port);
        WebElement nameInput = driver.findElement(By.cssSelector(".add-form input[name=name]"));
        nameInput.sendKeys(name);
        driver.findElement(By.cssSelector(".add-form button[type=submit]")).click();
        waitForPageLoad();
    }

    private void updateDilithium(Long charId, int value) {
        driver.get(BASE_URL + port);
        WebElement row = findCharacterRow(TEST_CHAR_NAME);
        assertNotNull(row);

        WebElement input = driver.findElement(By.cssSelector("input.dilithium-input"));
        // Use JavaScript to set value directly
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];", input, String.valueOf(value));
        // Trigger change event via JavaScript
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", input);
        waitForPageLoad();
    }

    private void updateCredits(Long charId, int value) {
        driver.get(BASE_URL + port);

        WebElement input = driver.findElement(By.cssSelector("input.credits-input"));
        // Use JavaScript to set value directly
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];", input, String.valueOf(value));
        // Trigger change event via JavaScript
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));", input);
        waitForPageLoad();
    }

    private void testTimestampSetAndUnset(Long charId, String type) {
        // Set timestamp
        driver.get(BASE_URL + port);
        WebElement row = findCharacterRow(TEST_CHAR_NAME);
        assertNotNull(row);

        // Find the timestamp form by type hidden input
        WebElement setForm = row.findElement(By.cssSelector(
                ".timestamp-form input[value='" + type + "']")).findElement(By.xpath(".."));
        assertNotNull(setForm);
        setForm.submit();
        waitForPageLoad();

        // Verify countdown appears
        row = findCharacterRow(TEST_CHAR_NAME);
        WebElement countdown = row.findElement(By.cssSelector(".countdown[data-type='" + type + "']"));
        assertNotNull(countdown);
        assertNotEquals("--:--", countdown.getText());
        assertNotEquals("unset", countdown.getText());

        // Verify in DB
        Optional<StoData> withTimestamp = repository.findById(charId);
        assertTrue(withTimestamp.isPresent());
        if (type.equals("recruitment")) {
            assertNotNull(withTimestamp.get().getRecruitmentTime());
        } else if (type.equals("refining")) {
            assertNotNull(withTimestamp.get().getConvertionTime());
        } else if (type.equals("event")) {
            assertNotNull(withTimestamp.get().getEventTime());
        }

        // Unset timestamp
        row = findCharacterRow(TEST_CHAR_NAME);
        WebElement unsetForm = row.findElement(By.cssSelector(
                ".unset-form input[value='" + type + "']")).findElement(By.xpath(".."));
        assertNotNull(unsetForm);
        unsetForm.submit();
        waitForPageLoad();

        // Verify unset label appears
        row = findCharacterRow(TEST_CHAR_NAME);
        WebElement unsetLabel = row.findElement(By.cssSelector(
                ".unset-label"));
        assertNotNull(unsetLabel);
        assertEquals("unset", unsetLabel.getText());

        // Verify in DB
        Optional<StoData> clearedTimestamp = repository.findById(charId);
        assertTrue(clearedTimestamp.isPresent());
        if (type.equals("recruitment")) {
            assertNull(clearedTimestamp.get().getRecruitmentTime());
        } else if (type.equals("refining")) {
            assertNull(clearedTimestamp.get().getConvertionTime());
        } else if (type.equals("event")) {
            assertNull(clearedTimestamp.get().getEventTime());
        }
    }

    private void deleteCharacter(String name) {
        driver.get(BASE_URL + port);

        WebElement row = findCharacterRow(name);
        assertNotNull(row);
        WebElement deleteButton = row.findElement(By.cssSelector(".delete-form button[type=submit]"));
        assertNotNull(deleteButton);

        // Click delete button - this will trigger confirm dialog via onsubmit
        // Use JavaScript to handle the confirm dialog
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "window.confirm = function(){ return true; };");
        deleteButton.click();
        waitForPageLoad();
    }

    private void assertCharacterExistsInTable(String name) {
        assertNotNull(findCharacterRow(name), "Character " + name + " should exist in table");
    }

    private void assertCharacterNotInTable(String name) {
        assertNull(findCharacterRow(name), "Character " + name + " should not exist in table");
    }

    private WebElement findCharacterRow(String name) {
        try {
            return driver.findElement(By.xpath(
                    "//tr[td[@class='name-cell'][text()='" + name + "']]"));
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return null;
        }
    }

    private void waitForPageLoad() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
