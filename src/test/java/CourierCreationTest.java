import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

public class CourierCreationTest {
    private Courier testCourier;
    private String courierId;

    @Before
    @Step("Настройка базового URI")
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    // Успешное создание курьера
    @Test
    @Step("Тест успешного создания курьера")
    public void testCreateCourierSuccess() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testCourier = createTestCourier("courier_" + timestamp, "password123", "Test Courier");

        createCourier(testCourier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    // Создание двух одинаковых курьеров
    @Test
    @Step("Тест создания двух одинаковых курьеров")
    public void testCreateDuplicateCouriers() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        testCourier = createTestCourier("duplicate_" + timestamp, "password123", "Duplicate Courier");

        // Первое создание курьера
        createCourier(testCourier)
                .then()
                .statusCode(201);

        // Попытка создать такого же курьера
        createCourier(testCourier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    // Создание курьера без логина
    @Test
    @Step("Тест создания курьера без логина")
    public void testCreateCourierWithoutLogin() {
        Map<String, String> courierData = createCourierDataWithoutLogin("password123", "No Login Courier");

        createCourierWithMap(courierData)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    // Создание курьера без пароля
    @Test
    @Step("Тест создания курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        Map<String, String> courierData = createCourierDataWithoutPassword("no_password_" + System.currentTimeMillis(), "No Password Courier");

        createCourierWithMap(courierData)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    // Создание курьера без имени
    @Test
    @Step("Тест создания курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        Map<String, String> courierData = createCourierDataWithoutFirstName("no_name_" + System.currentTimeMillis(), "password123");

        createCourierWithMap(courierData)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    // Создание курьера с существующим логином
    @Test
    @Step("Тест создания курьера с существующим логином")
    public void testCreateCourierWithExistingLogin() {
        // Сначала создаем курьера
        String timestamp = String.valueOf(System.currentTimeMillis());
        testCourier = createTestCourier("existing_" + timestamp, "password123", "Existing Courier");

        createCourier(testCourier)
                .then()
                .statusCode(201);

        Courier duplicateCourier = createTestCourier(
                testCourier.getLogin(),
                "different_password",
                "Different Name"
        );

        createCourier(duplicateCourier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Step("Создание тестового курьера: login={login}, password={password}, firstName={firstName}")
    private Courier createTestCourier(String login, String password, String firstName) {
        return new Courier(login, password, firstName);

    }

    @Step("Создание курьера")
    private Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Создание курьера с данными в формате Map")
    private Response createCourierWithMap(Map<String, String> courierData) {
        return given()
                .header("Content-type", "application/json")
                .body(courierData)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Подготовка данных курьера без логина")
    private Map<String, String> createCourierDataWithoutLogin(String password, String firstName) {
        Map<String, String> courierData = new HashMap<>();
        courierData.put("password", password);
        courierData.put("firstName", firstName);
        return courierData;
    }

    @Step("Подготовка данных курьера без пароля: login={login}, firstName={firstName}")
    private Map<String, String> createCourierDataWithoutPassword(String login, String firstName) {
        Map<String, String> courierData = new HashMap<>();
        courierData.put("login", login);
        courierData.put("firstName", firstName);
        return courierData;
    }

    @Step("Подготовка данных курьера без имени: login={login}, password={password}")
    private Map<String, String> createCourierDataWithoutFirstName(String login, String password) {
        Map<String, String> courierData = new HashMap<>();
        courierData.put("login", login);
        courierData.put("password", password);
        return courierData;
    }

    @Step("Логин курьера: login={login}, password={password}")
    private Response loginCourier(String login, String password) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("login", login);
        loginData.put("password", password);

        return given()
                .header("Content-type", "application/json")
                .body(loginData)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Удаление курьера")
    private Response deleteCourier(String courierId) {
        return given()
                .when()
                .delete("/api/v1/courier/" + courierId);
    }

    @After
    @Step("Очистка тестовых данных")
    public void tearDown() {
        // Удаляем тестового курьера после каждого теста, если он был создан
        if (testCourier != null && testCourier.getLogin() != null) {
            // Получаем ID курьера для удаления
            Response loginResponse = loginCourier(testCourier.getLogin(), testCourier.getPassword());

            if (loginResponse.statusCode() == 200) {
                courierId = loginResponse.path("id").toString();

                // Удаляем курьера
                deleteCourier(courierId)
                        .then()
                        .statusCode(200);
            }
        }
    }
}